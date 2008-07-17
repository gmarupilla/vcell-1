package cbit.vcell.microscopy;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.media.jai.BorderExtender;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.AddConstDescriptor;
import javax.media.jai.operator.AndDescriptor;
import javax.media.jai.operator.BorderDescriptor;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.DilateDescriptor;
import javax.media.jai.operator.ErodeDescriptor;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.LookupDescriptor;

import loci.formats.ImageTools;
import cbit.image.ImageException;
import cbit.image.VCImage;
import cbit.image.VCImageUncompressed;
import cbit.sql.KeyValue;
import cbit.sql.SimulationVersion;
import cbit.sql.VersionFlag;
import cbit.util.Extent;
import cbit.util.FileUtils;
import cbit.util.ISize;
import cbit.util.Issue;
import cbit.util.Matchable;
import cbit.util.NumberUtils;
import cbit.util.Origin;
import cbit.util.TokenMangler;
import cbit.vcell.VirtualMicroscopy.ImageDataset;
import cbit.vcell.VirtualMicroscopy.UShortImage;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.desktop.controls.DataManager;
import cbit.vcell.field.FieldDataFileOperationSpec;
import cbit.vcell.field.FieldDataIdentifierSpec;
import cbit.vcell.field.FieldFunctionArguments;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.ImageSubVolume;
import cbit.vcell.geometry.RegionImage;
import cbit.vcell.mapping.FeatureMapping;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.SpeciesContextSpec;
import cbit.vcell.math.Constant;
import cbit.vcell.math.Equation;
import cbit.vcell.math.Function;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.PdeEquation;
import cbit.vcell.math.SubDomain;
import cbit.vcell.microscopy.ROI.RoiType;
import cbit.vcell.model.Feature;
import cbit.vcell.model.MassActionKinetics;
import cbit.vcell.model.Model;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.Species;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.modelopt.gui.DataSource;
import cbit.vcell.opt.ReferenceData;
import cbit.vcell.opt.SimpleReferenceData;
import cbit.vcell.parser.DivideByZeroException;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.server.GroupAccessNone;
import cbit.vcell.server.PropertyLoader;
import cbit.vcell.server.SessionLog;
import cbit.vcell.server.User;
import cbit.vcell.simdata.DataSetControllerImpl;
import cbit.vcell.simdata.ExternalDataIdentifier;
import cbit.vcell.simdata.SimDataBlock;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.simdata.VariableType;
import cbit.vcell.simdata.DataSetControllerImpl.ProgressListener;
import cbit.vcell.solver.DefaultOutputTimeSpec;
import cbit.vcell.solver.OutputTimeSpec;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.SolverStatus;
import cbit.vcell.solver.TimeBounds;
import cbit.vcell.solver.TimeStep;
import cbit.vcell.solver.ode.ODESolverResultSet;
import cbit.vcell.solver.ode.ODESolverResultSetColumnDescription;
import cbit.vcell.solver.test.MathTestingUtilities;
import cbit.vcell.solvers.CartesianMesh;
import cbit.vcell.solvers.FVSolverStandalone;

public class FRAPStudy implements Matchable{
	private transient String xmlFilename = null;
	private transient String directory = null;
	public static final String SPECIES_NAME_PREFIX_MOBILE = "fluor_mobile"; 
	public static final String SPECIES_NAME_PREFIX_IMMOBILE = "fluor_immobile"; 
	public static final String SPECIES_NAME_PREFIX_COMBINED = "fluor_combined"; 
	private String name = null;
	private String description = null;
	private String originalImageFilePath = null;
	private FRAPData frapData = null;
	private BioModel bioModel = null;
	private ExternalDataInfo frapDataExternalDataInfo = null;
	private ExternalDataInfo roiExternalDataInfo = null;
	
	PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	public static class FRAPModelParameters {
		public final String startIndexForRecovery;
		public final String diffusionRate;
		public final String monitorBleachRate;
		public final String mobileFraction;
		public final String slowerRate;
		
		public FRAPModelParameters(
				String startingIndexForRecovery,
				String diffusionRate,
				String monitorBleachRate,
				String mobileFraction,
				String slowerRate){
			this.startIndexForRecovery = startingIndexForRecovery;
			this.diffusionRate = diffusionRate;
			this.monitorBleachRate = monitorBleachRate;
			this.mobileFraction = mobileFraction;
			this.slowerRate = slowerRate;
		}
	}
	
	private FRAPModelParameters frapModelParameters;
	
	public static final String EXTRACELLULAR_NAME = "extracellular";
	public static final String CYTOSOL_NAME = "cytosol";
	public static final short Epsilon = 1; // to aoid divided by zero error.

	public static class AnalysisParameters{
		public final Double diffusionRate;
		public final Double mobileFraction;
		public final Double bleachWhileMonitorRate;
		
		public AnalysisParameters(Double diffusionRate,Double mobileFraction,Double bleachWhileMonitorRate){
			if(diffusionRate == null || mobileFraction == null || bleachWhileMonitorRate == null){
				throw new IllegalArgumentException("AnalysisParameters cannot be null");
			}
			this.diffusionRate = diffusionRate;
			this.mobileFraction = mobileFraction;
			this.bleachWhileMonitorRate = bleachWhileMonitorRate;
		}
		public boolean equals(Object obj){
			if(!(obj instanceof AnalysisParameters)){
				return false;
			}
			AnalysisParameters analysisParameters = (AnalysisParameters)obj;
			return
				diffusionRate.equals(analysisParameters.diffusionRate) &&
				mobileFraction.equals(analysisParameters.mobileFraction) &&
				bleachWhileMonitorRate.equals(analysisParameters.bleachWhileMonitorRate);
		}
		public int hashCode(){
			return ((diffusionRate!=null)?(diffusionRate.hashCode()):(0)) +
			((mobileFraction!=null)?(mobileFraction.hashCode()):(0)) +
			((bleachWhileMonitorRate!=null)?(bleachWhileMonitorRate.hashCode()):(0));
		}
		public String toString(){
			return "diffusion="+diffusionRate+", mobileFraction="+mobileFraction+", monitorBleachRate="+bleachWhileMonitorRate;
		}
	};

	public static class CurveInfo {
		AnalysisParameters analysisParameters;
		RoiType roiType = null;

		public CurveInfo(AnalysisParameters analysisParameters,RoiType roi){
			this.analysisParameters = analysisParameters;
			this.roiType = roi;
		}

		public boolean equals(Object obj){
			if (!(obj instanceof CurveInfo)){
				return false;
			}
			CurveInfo ci = (CurveInfo)obj;
			if(analysisParameters == null && ci.analysisParameters == null){
				return ci.roiType==roiType;
			}
			if(ci.analysisParameters != null && analysisParameters != null){
				return
				ci.analysisParameters.equals(analysisParameters) &&
				(ci.roiType==roiType);			
			}
			return false;
		}
		
		public int hashCode(){
			return (analysisParameters != null?analysisParameters.hashCode():0)+roiType.toString().hashCode();
		}
		public boolean isExperimentInfo(){
			return analysisParameters == null;
		}
		public AnalysisParameters getAnalysisParameters(){
			return analysisParameters;
		}
		public RoiType getROIType(){
			return roiType;
		}
	}

	
	public static class SpatialAnalysisResults{
		public final AnalysisParameters[] analysisParameters;
		public final double[] shiftedSimTimes;
		public final Hashtable<CurveInfo, double[]> curveHash;
		public static RoiType[] ORDERED_ROITYPES =
			new RoiType[] {
				RoiType.ROI_BLEACHED,	
				RoiType.ROI_BLEACHED_RING1,	
				RoiType.ROI_BLEACHED_RING2,	
				RoiType.ROI_BLEACHED_RING3,	
				RoiType.ROI_BLEACHED_RING4,	
				RoiType.ROI_BLEACHED_RING5,
				RoiType.ROI_BLEACHED_RING6,
				RoiType.ROI_BLEACHED_RING7,
				RoiType.ROI_BLEACHED_RING8
			};
		
		public static final int ANALYSISPARAMETERS_COLUMNS_COUNT = 3;
		public static final int COLUMN_INDEX_DIFFUSION_RATE = 0;
		public static final int COLUMN_INDEX_MOBILE_FRACTION = 1;
		public static final int COLUMN_INDEX_MONITOR_BLEACH_RATE = 2;
		
		public static final int ARRAY_INDEX_EXPDATASOURCE = 0;
		public static final int ARRAY_INDEX_SIMDATASOURCE = 1;
		
		private static final String ENDOFLINE = "\r\n";
		private static final String SEPCHAR = "\t";


		public SpatialAnalysisResults(
				AnalysisParameters[] analysisParameters,double[] shiftedSimTimes,
				Hashtable<CurveInfo, double[]> curveHash){
			this.analysisParameters = analysisParameters;
			this.shiftedSimTimes = shiftedSimTimes;
			this.curveHash = curveHash;
		}
	
		public Hashtable<Double, DataSource[]> createSummaryReportSourceData(
				final double[] frapDataTimeStamps,int startIndexForRecovery,final Double modelDiffusionRate) throws Exception{
			
			Hashtable<Double, DataSource[]> allDataHash = new Hashtable<Double, DataSource[]>();
			
			Hashtable<CurveInfo, double[]> ROIInfoHash = curveHash;
			Set<CurveInfo> roiInfoSet = ROIInfoHash.keySet();
			Iterator<CurveInfo> roiInfoIter = roiInfoSet.iterator();
			Hashtable<RoiType, double[]> expROIData = new Hashtable<RoiType, double[]>();
			Hashtable<AnalysisParameters, Hashtable<RoiType, double[]>> simROIData = new Hashtable<AnalysisParameters, Hashtable<RoiType,double[]>>();
			int roiCount = 0;
			int analysisParametersCount = 0;
			while(roiInfoIter.hasNext()){
				CurveInfo roiCurveInfo = roiInfoIter.next();
				if(roiCurveInfo.isExperimentInfo()){
					expROIData.put(roiCurveInfo.getROIType(), ROIInfoHash.get(roiCurveInfo));
					roiCount++;
				}else{
					Hashtable<RoiType,double[]> simROIDataHash = simROIData.get(roiCurveInfo.getAnalysisParameters());
					if(simROIDataHash == null){
						simROIDataHash  = new Hashtable<RoiType, double[]>();
						simROIData.put(roiCurveInfo.getAnalysisParameters(), simROIDataHash);
						analysisParametersCount++;
					}
					simROIDataHash.put(roiCurveInfo.getROIType(), ROIInfoHash.get(roiCurveInfo));
				}
			}

			ReferenceData referenceData = 
				createReferenceData(frapDataTimeStamps,null,startIndexForRecovery,"");
			
			for (int analysisParametersRow = 0; analysisParametersRow < analysisParametersCount; analysisParametersRow++) {
				AnalysisParameters currentAnalysisParameters = analysisParameters[analysisParametersRow];
				
				ODESolverResultSet odeSolverResultSet =
					createODESolverResultSet(currentAnalysisParameters,null,"D="+NumberUtils.formatNumber(currentAnalysisParameters.diffusionRate, 3)+"_");
				final DataSource expDataSource = new DataSource(referenceData,"exp");
				final DataSource simDataSource = new DataSource(odeSolverResultSet, "sim");
				DataSource[] newDataSourceArr = new DataSource[2];
				newDataSourceArr[ARRAY_INDEX_EXPDATASOURCE] = expDataSource;
				newDataSourceArr[ARRAY_INDEX_SIMDATASOURCE] = simDataSource;
				allDataHash.put(currentAnalysisParameters.diffusionRate,newDataSourceArr);
			}
			return allDataHash;
		}


		public Object[][] createSummaryReportTableData(double[] frapDataTimeStamps,int startTimeIndex){
			String[] summaryReportColumnNames = SpatialAnalysisResults.getSummaryReportColumnNames();
			final Object[][] tableData = new Object[analysisParameters.length][summaryReportColumnNames.length];
			for (int analysisParametersRow = 0; analysisParametersRow < analysisParameters.length; analysisParametersRow++) {
				AnalysisParameters currentAnalysisParameters = analysisParameters[analysisParametersRow];
				tableData[analysisParametersRow][COLUMN_INDEX_DIFFUSION_RATE] = currentAnalysisParameters.diffusionRate;
				tableData[analysisParametersRow][COLUMN_INDEX_MOBILE_FRACTION] = currentAnalysisParameters.mobileFraction;
				tableData[analysisParametersRow][COLUMN_INDEX_MONITOR_BLEACH_RATE] = currentAnalysisParameters.bleachWhileMonitorRate;
				
				for (int roiColumn = 0; roiColumn < SpatialAnalysisResults.ORDERED_ROITYPES.length; roiColumn++) {
					ODESolverResultSet simDataSource =
						createODESolverResultSet(currentAnalysisParameters,SpatialAnalysisResults.ORDERED_ROITYPES[roiColumn],"");
					ReferenceData expDataSource =
						createReferenceData(frapDataTimeStamps,SpatialAnalysisResults.ORDERED_ROITYPES[roiColumn],startTimeIndex,"");

					int numSamples = expDataSource.getNumRows();
					double sumSquaredError = MathTestingUtilities.calcWeightedSquaredError(simDataSource, expDataSource);
					tableData[analysisParametersRow][roiColumn+ANALYSISPARAMETERS_COLUMNS_COUNT] =
						Math.sqrt(sumSquaredError)/(numSamples-1);//unbiased estimator is numsamples-1
				}
			}
			return tableData;
		}
		
		public static String createCSVSummaryReport(String[] summaryReportColumnNames,Object[][] summaryData){
			StringBuffer reportSB = new StringBuffer();
			for (int i = 0; i < summaryReportColumnNames.length; i++) {
				reportSB.append((i != 0?SEPCHAR:"")+summaryReportColumnNames[i]);
			}
			reportSB.append(ENDOFLINE);
			for (int j = 0; j < summaryData.length; j++) {
				for (int k = 0; k < summaryReportColumnNames.length; k++) {
					reportSB.append((k != 0?SEPCHAR:"")+summaryData[j][k].toString());
				}
				reportSB.append(ENDOFLINE);
			}
			return reportSB.toString();
		}

		public static String createCSVTimeData(
				String[] summaryReportColumnNames,int selectedColumn,
				double[] expTimes,double[] expColumnData,
				double[] simTimes,double[] simColumnData){
			StringBuffer dataSB = new StringBuffer();
			dataSB.append("Exp Time"+SEPCHAR+" Exp Data (norm):"+summaryReportColumnNames[selectedColumn]+SEPCHAR+
					"Sim Time"+SEPCHAR+" Sim Data (norm):"+summaryReportColumnNames[selectedColumn]);
			dataSB.append(ENDOFLINE);
			for (int j = 0; j < Math.max(expTimes.length, simTimes.length); j++) {
				if(j <expTimes.length){
					dataSB.append(expTimes[j]+SEPCHAR+expColumnData[j]);
				}else{
					dataSB.append("NULL"+SEPCHAR+"NULL");
				}
				dataSB.append(SEPCHAR);
				if(j <simTimes.length){
					dataSB.append(simTimes[j]+SEPCHAR+simColumnData[j]);
				}else{
					dataSB.append("NULL"+SEPCHAR+"NULL");
				}
				dataSB.append(ENDOFLINE);
			}
			return dataSB.toString();
		}
		public static String[] getSummaryReportColumnNames(){
			String[] columnNames = new String[SpatialAnalysisResults.ORDERED_ROITYPES.length+ANALYSISPARAMETERS_COLUMNS_COUNT];
			for (int i = 0; i < columnNames.length; i++) {
				if(i==COLUMN_INDEX_DIFFUSION_RATE){
					columnNames[i] = "Diffusion Rate";
				}else if(i==COLUMN_INDEX_MOBILE_FRACTION){
					columnNames[i] = "Mobile Fraction";
				}else if(i==COLUMN_INDEX_MONITOR_BLEACH_RATE){
					columnNames[i] = "Monitor Bleach Rate";
				}else{
					columnNames[i] = "SE "+SpatialAnalysisResults.ORDERED_ROITYPES[i-ANALYSISPARAMETERS_COLUMNS_COUNT].name();
				}
			}
			return columnNames;
		}


		public ReferenceData[] createReferenceDataForAllDiffusionRates(double[] frapDataTimeStamps){
			ReferenceData[] referenceDataArr = new ReferenceData[analysisParameters.length];
			for (int i = 0; i < analysisParameters.length; i++) {
				referenceDataArr[i] = createReferenceData(frapDataTimeStamps,null,0,"");//new SimpleReferenceData(expRefDataLabels, expRefDataWeights, expRefDataColumns);
			}
			return referenceDataArr;
		}
		private boolean isROITypeOK(RoiType argROIType){
			boolean bFound = false;
			for (int i = 0; i < ORDERED_ROITYPES.length; i++) {
				if(ORDERED_ROITYPES[i].equals(argROIType)){
					bFound = true;
					break;
				}
			}
			return bFound;
		}
		public ReferenceData createReferenceData(double[] frapDataTimeStamps,RoiType argROIType,int startTimeIndex,String description){
			if(argROIType != null){
				if(!isROITypeOK(argROIType)){
					throw new IllegalArgumentException("couldn't find ROIType "+argROIType);
				}
			}
			final int numROITypes = (argROIType == null?FRAPStudy.SpatialAnalysisResults.ORDERED_ROITYPES.length:1);
			String[] expRefDataLabels = new String[numROITypes+1];
			double[] expRefDataWeights = new double[numROITypes+1];
			double[][] expRefDataColumns = new double[numROITypes+1][];
			expRefDataLabels[0] = "t";
			expRefDataWeights[0] = 1.0;
			double[] truncatedTimes = new double[frapDataTimeStamps.length-startTimeIndex];
			System.arraycopy(frapDataTimeStamps, startTimeIndex, truncatedTimes, 0, /*shiftedSimTimes.length*/truncatedTimes.length);
			expRefDataColumns[0] = truncatedTimes;
			for (int j2 = 0; j2 < numROITypes; j2++) {
				RoiType currentROIType = (argROIType == null?FRAPStudy.SpatialAnalysisResults.ORDERED_ROITYPES[j2]:argROIType);
				expRefDataLabels[j2+1] = (description == null?"":description)+currentROIType.toString();
				expRefDataWeights[j2+1] = 1.0;
				double[] allTimesData = curveHash.get(new FRAPStudy.CurveInfo(null,currentROIType));
				double[] truncatedTimesData = new double[allTimesData.length-startTimeIndex];
				System.arraycopy(allTimesData, startTimeIndex, truncatedTimesData, 0, truncatedTimesData.length);
				expRefDataColumns[j2+1] = truncatedTimesData;
			}
			return new SimpleReferenceData(expRefDataLabels, expRefDataWeights, expRefDataColumns);
		}
		public ODESolverResultSet[] createODESolverResultSetForAllDiffusionRates(){
			ODESolverResultSet[] odeSolverResultSetArr = new ODESolverResultSet[analysisParameters.length];
			for (int i = 0; i < analysisParameters.length; i++) {
				odeSolverResultSetArr[i] = createODESolverResultSet(analysisParameters[i],null,"D="+analysisParameters[i].diffusionRate);//fitOdeSolverResultSet;
			}
			return odeSolverResultSetArr;
		}
		public ODESolverResultSet createODESolverResultSet(AnalysisParameters argAnalysisParameters,RoiType argROIType,String description){
			if(argROIType != null){
				if(!isROITypeOK(argROIType)){
					throw new IllegalArgumentException("couldn't find ROIType "+argROIType);
				}
			}
			int analysisParametersIndex = -1;
			for (int i = 0; i < analysisParameters.length; i++) {
				if(analysisParameters[i].equals(argAnalysisParameters)){
					analysisParametersIndex = i;
					break;
				}
			}
			if(analysisParametersIndex == -1){
				throw new IllegalArgumentException("couldn't find AnalysisParameteers "+analysisParametersIndex);
			}
			int numROITypes = (argROIType == null?FRAPStudy.SpatialAnalysisResults.ORDERED_ROITYPES.length:1);
			ODESolverResultSet fitOdeSolverResultSet = new ODESolverResultSet();
			fitOdeSolverResultSet.addDataColumn(new ODESolverResultSetColumnDescription("t"));
			for (int j = 0; j < numROITypes; j++) {
				RoiType currentROIType = (argROIType == null?FRAPStudy.SpatialAnalysisResults.ORDERED_ROITYPES[j]:argROIType);
				String name = (description == null?/*"sim D="+diffusionRates[diffusionRateIndex]+"::"*/"":description)+currentROIType.toString();
				fitOdeSolverResultSet.addDataColumn(new ODESolverResultSetColumnDescription(name));
			}
			//
			// populate time
			//
			for (int j = 0; j < shiftedSimTimes.length; j++) {
				double[] row = new double[numROITypes+1];
				row[0] = shiftedSimTimes[j];
				fitOdeSolverResultSet.addRow(row);
			}
			//
			// populate values
			//
			for (int j = 0; j < numROITypes; j++) {
				RoiType currentROIType = (argROIType == null?FRAPStudy.SpatialAnalysisResults.ORDERED_ROITYPES[j]:argROIType);
				double[] values = curveHash.get(new FRAPStudy.CurveInfo(analysisParameters[analysisParametersIndex],currentROIType)); // get simulated data for this ROI
				for (int k = 0; k < values.length; k++) {
					fitOdeSolverResultSet.setValue(k, j+1, values[k]);
				}
			}
			return fitOdeSolverResultSet;
		}
	}
	
	//
	// store the external data identifiers as annotation within a MathModel.
	//
	
	public ExternalDataInfo getFrapDataExternalDataInfo() {
		return frapDataExternalDataInfo;
	}
	
	
	public static FRAPStudy.SpatialAnalysisResults spatialAnalysis(
		DataManager simulationDataManager,
		int startingIndexForRecovery,
		double startingIndexForRecoveryExperimentalTimePoint,
		String argMobileDiffusionRate,
		String argMobileFraction,
		String argBleachWhileMonitorRate,
		/*SubDomain subDomain,*/
		FRAPData frapData,
		double[] preBleachAverageXYZ,
		DataSetControllerImpl.ProgressListener progressListener) throws Exception{
		
		
		Double mobileDiffusionRate = Double.parseDouble(argMobileDiffusionRate);
		Double mobileFraction = (argMobileFraction != null?Double.parseDouble(argMobileFraction):1.0);
		Double bleachWhileMonitorRate = (argBleachWhileMonitorRate != null?Double.parseDouble(argBleachWhileMonitorRate):0.0);

//		//
//		// get List of variable names
//		//
//		ArrayList<Double> diffList = new ArrayList<Double>();
//		ArrayList<String> pdeVarNameList = new ArrayList<String>();
//		Enumeration<Equation> equEnum = subDomain.getEquations();
//		while (equEnum.hasMoreElements()){
//			Equation equ = equEnum.nextElement();
//			if (equ instanceof PdeEquation){
//				PdeEquation pde = (PdeEquation)equ;
//				pdeVarNameList.add(pde.getVariable().getName());
//				try {
//					diffList.add(pde.getDiffusionExpression().evaluateConstant());
//				} catch (DivideByZeroException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ExpressionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		String[] varNames = pdeVarNameList.toArray(new String[pdeVarNameList.size()]);
//		Double[] diffusionRates = diffList.toArray(new Double[diffList.size()]);
		
		
//		Double mobileDiffusionRate = null;
//		Enumeration<Equation> equEnum = subDomain.getEquations();
//		while (equEnum.hasMoreElements()){
//			Equation equ = equEnum.nextElement();
//			if (equ instanceof PdeEquation){
//				PdeEquation pde = (PdeEquation)equ;
////				pdeVarNameList.add(pde.getVariable().getName());
//				if(pde.getVariable().getName().equals(SPECIES_NAME_PREFIX_MOBILE)){
//					try {
////						diffList.add(pde.getDiffusionExpression().evaluateConstant());
//						mobileDiffusionRate = new Double(pde.getDiffusionExpression().evaluateConstant());
//					} catch (DivideByZeroException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (ExpressionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		
		String[] varNames = new String[] {SPECIES_NAME_PREFIX_COMBINED};
//		Double[] diffusionRates = new Double[] {mobileDiffusionRate};
		AnalysisParameters analysisParameters =
			new AnalysisParameters(mobileDiffusionRate,mobileFraction,bleachWhileMonitorRate);
		
		
		
		//
		// get timing to compare experimental data with simulated results.
		//
		double[] simTimes = simulationDataManager.getDataSetTimes();
		double[] shiftedSimTimes = simTimes.clone();
		for (int j = 0; j < simTimes.length; j++) {
			shiftedSimTimes[j] = simTimes[j] + startingIndexForRecoveryExperimentalTimePoint;//timeStamps[startingIndexOfRecovery];
		}
		//
		// for each simulation time step, get the data under each ROI <indexed by ROI, then diffusion
		//
		// preallocate arrays for all data first
		Hashtable<CurveInfo, double[]> curveHash = new Hashtable<CurveInfo, double[]>();
		ArrayList<int[]> nonZeroIndicesForROI = new ArrayList<int[]>();
		for (int i = 0; i < SpatialAnalysisResults.ORDERED_ROITYPES.length; i++) {
//			for (int j = 0; j < diffusionRates.length; j++) {
				curveHash.put(new CurveInfo(analysisParameters,SpatialAnalysisResults.ORDERED_ROITYPES[i]), new double[simTimes.length]);
//			}
			ROI roi_2D = frapData.getRoi(SpatialAnalysisResults.ORDERED_ROITYPES[i]);
			nonZeroIndicesForROI.add(roi_2D.getRoiImages()[0].getNonzeroIndices());
		}
		//
		// collect data for experiment (over all ROIs), normalize with pre-bleach average
		//
		for (int i = 0; i < SpatialAnalysisResults.ORDERED_ROITYPES.length; i++) {
			double[] normalizedAverageFluorAtEachTimeUnderROI = new double[frapData.getImageDataset().getImageTimeStamps().length];
			for (int j = 0; j < normalizedAverageFluorAtEachTimeUnderROI.length; j++) {
				normalizedAverageFluorAtEachTimeUnderROI[j] =
					frapData.getAverageUnderROI(0, j,
						frapData.getRoi(SpatialAnalysisResults.ORDERED_ROITYPES[i]), preBleachAverageXYZ);				
			}
			curveHash.put(new CurveInfo(null,SpatialAnalysisResults.ORDERED_ROITYPES[i]), normalizedAverageFluorAtEachTimeUnderROI);
		}
		
		//
		// get Simulation Data
		//
		int totalLen = simTimes.length*varNames.length*SpatialAnalysisResults.ORDERED_ROITYPES.length;
		if(progressListener != null){progressListener.updateProgress(0);}
		for (int i = 0; i < simTimes.length; i++) {
			for (int j = 0; j < varNames.length; j++) {
				SimDataBlock simDataBlock = simulationDataManager.getSimDataBlock(varNames[j], simTimes[i]);
				double[] data = simDataBlock.getData();
				for (int k = 0; k < SpatialAnalysisResults.ORDERED_ROITYPES.length; k++) {
					int[] roiIndices = nonZeroIndicesForROI.get(k);
					if(roiIndices != null && roiIndices.length > 0){
						double accum = 0.0;
						for (int index = 0; index < roiIndices.length; index++) {
							accum += data[roiIndices[index]];
						}
						double[] values = curveHash.get(new CurveInfo(analysisParameters,SpatialAnalysisResults.ORDERED_ROITYPES[k]));
						values[i] = accum/roiIndices.length;
					}
					double currentLen = i*varNames.length*SpatialAnalysisResults.ORDERED_ROITYPES.length + j*SpatialAnalysisResults.ORDERED_ROITYPES.length + k;
					if(progressListener != null){progressListener.updateProgress(currentLen/totalLen);}
				}
			}
		}

		SpatialAnalysisResults spatialAnalysisResults = 
			new SpatialAnalysisResults(new AnalysisParameters[] {analysisParameters},/*varNames,*/shiftedSimTimes,curveHash);
		return spatialAnalysisResults;
	}
	
	public static void runFVSolverStandalone(
		File simulationDataDir,
		SessionLog sessionLog,
		Simulation sim,
		ExternalDataIdentifier imageDataExtDataID,
		ExternalDataIdentifier roiExtDataID,
		ProgressListener progressListener) throws Exception{

		FieldFunctionArguments[] fieldFunctionArgs = sim.getMathDescription().getFieldFunctionArguments();
		FieldDataIdentifierSpec[] fieldDataIdentifierSpecs = new FieldDataIdentifierSpec[fieldFunctionArgs.length];
		for (int i = 0; i < fieldDataIdentifierSpecs.length; i++) {
			if (fieldFunctionArgs[i].getFieldName().equals(imageDataExtDataID.getName())){
				fieldDataIdentifierSpecs[i] = new FieldDataIdentifierSpec(fieldFunctionArgs[i],imageDataExtDataID);
			}else if (fieldFunctionArgs[i].getFieldName().equals(roiExtDataID.getName())){
				fieldDataIdentifierSpecs[i] = new FieldDataIdentifierSpec(fieldFunctionArgs[i],roiExtDataID);
			}else{
				throw new RuntimeException("failed to resolve field named "+fieldFunctionArgs[i].getFieldName());
			}
		}
		
		int jobIndex = 0;
		SimulationJob simJob = new SimulationJob(sim,fieldDataIdentifierSpecs,jobIndex);
		
		System.setProperty(PropertyLoader.jmsURL, "abc");
		System.setProperty(PropertyLoader.jmsUser, "abc");
		System.setProperty(PropertyLoader.jmsPassword, "abc");
		System.setProperty(PropertyLoader.jmsSimJobQueue, "abc");
		System.setProperty(PropertyLoader.jmsWorkerEventQueue, "abc");
		System.setProperty(PropertyLoader.jmsServiceControlTopic, "abc");
		//
		//FVSolverStandalone class expects the PropertyLoader.finiteVolumeExecutableProperty to exist
		System.setProperty(PropertyLoader.finiteVolumeExecutableProperty, LocalWorkspace.getFinitVolumeExecutableFullPathname());
		//
		FVSolverStandalone fvSolver = new FVSolverStandalone(simJob,simulationDataDir,sessionLog,false);
		fvSolver.startSolver();
		
		SolverStatus status = fvSolver.getSolverStatus();
		while (status.getStatus() != SolverStatus.SOLVER_FINISHED && status.getStatus() != SolverStatus.SOLVER_ABORTED )
		{
			if(progressListener != null){
				progressListener.updateProgress(fvSolver.getProgress());
			}
			Thread.sleep(100);
			status = fvSolver.getSolverStatus();
		}

		if(status.getStatus() == SolverStatus.SOLVER_FINISHED){
			String roiMeshFileName =
				ExternalDataIdentifier.createCanonicalMeshFileName(
					roiExtDataID.getKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT, false);
			String imageDataMeshFileName =
				ExternalDataIdentifier.createCanonicalMeshFileName(
					imageDataExtDataID.getKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT, false);
			String simulationMeshFileName =
				ExternalDataIdentifier.createCanonicalMeshFileName(
					sim.getVersion().getVersionKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT, false);
			// delete old external data mesh files and copy simulation mesh file to them
			File roiMeshFile = new File(simulationDataDir,roiMeshFileName);
			File imgMeshFile = new File(simulationDataDir,imageDataMeshFileName);
			File simMeshFile = new File(simulationDataDir,simulationMeshFileName);
			if(!roiMeshFile.delete()){throw new Exception("Couldn't delete ROI Mesh file "+roiMeshFile.getAbsolutePath());}
			if(!imgMeshFile.delete()){throw new Exception("Couldn't delete ImageData Mesh file "+imgMeshFile.getAbsolutePath());}
			FileUtils.copyFile(simMeshFile, roiMeshFile);
			FileUtils.copyFile(simMeshFile, imgMeshFile);
		}
		else{
			throw new Exception("Sover did not finish normally." + status.toString());
		}
	}

	public static ExternalDataInfo createNewExternalDataInfo(LocalWorkspace localWorkspace,String extDataIDName){
		File targetDir = new File(localWorkspace.getDefaultSimDataDirectory());
		ExternalDataIdentifier newImageDataExtDataID =
			new ExternalDataIdentifier(LocalWorkspace.createNewKeyValue(),
					LocalWorkspace.getDefaultOwner(),extDataIDName);
		ExternalDataInfo newImageDataExtDataInfo =
			new ExternalDataInfo(newImageDataExtDataID,
				new File(targetDir,newImageDataExtDataID.getID()+SimDataConstants.LOGFILE_EXTENSION).getAbsolutePath());
		return newImageDataExtDataInfo;
	}
	public void setFrapDataExternalDataInfo(ExternalDataInfo imageDatasetExternalDataInfo) {
		ExternalDataInfo oldValue = this.frapDataExternalDataInfo;
		this.frapDataExternalDataInfo = imageDatasetExternalDataInfo;
		propertyChangeSupport.firePropertyChange("imageDatasetExternalDataInfo", oldValue, imageDatasetExternalDataInfo);
	}

	public ExternalDataInfo getRoiExternalDataInfo() {
		return roiExternalDataInfo;
	}

	public void setRoiExternalDataInfo(ExternalDataInfo roiExternalDataInfo) {
		ExternalDataInfo oldValue = this.roiExternalDataInfo;
		this.roiExternalDataInfo = roiExternalDataInfo;
		propertyChangeSupport.firePropertyChange("roiExternalDataInfo", oldValue, roiExternalDataInfo);
	}
		
	public BioModel getBioModel() {
		return bioModel;
	}
	
	public static BioModel createNewBioModel(
			FRAPStudy sourceFrapStudy,
			Double baseDiffusionRate,
			String bleachWhileMonitoringRateString,
			String mobileFractionString,
			KeyValue simKey,
			User owner,
			int startingIndexForRecovery) throws Exception {

		if (sourceFrapStudy==null){
			throw new Exception("No FrapStudy is defined");
		}
		if (sourceFrapStudy.getFrapData()==null){
			throw new Exception("No FrapData is defined");
		}
		if (sourceFrapStudy.getFrapData().getImageDataset()==null){
			throw new Exception("No ImageDataSet is defined");
		}
		ROI cellROI_2D = sourceFrapStudy.getFrapData().getRoi(RoiType.ROI_CELL);
		if (cellROI_2D==null){
			throw new Exception("No Cell ROI is defined");
		}
		if(baseDiffusionRate == null){
			throw new Exception("Diffusion Rate is not defined");
		}
		if(mobileFractionString == null){
			throw new Exception("Mobile fraction is not defined");
		}

		if(owner == null){
			throw new Exception("Owner is not defined");
		}

		Extent extent = sourceFrapStudy.getFrapData().getImageDataset().getExtent();

		double[] timeStamps = sourceFrapStudy.getFrapData().getImageDataset().getImageTimeStamps();
		TimeBounds timeBounds = new TimeBounds(0.0,timeStamps[timeStamps.length-1]-timeStamps[startingIndexForRecovery]);
		double timeStepVal = timeStamps[timeStamps.length-1] - timeStamps[timeStamps.length-2];
//		timeStepVal/= 10.0;
		TimeStep timeStep = new TimeStep(timeStepVal, timeStepVal, timeStepVal);

		int numX = cellROI_2D.getRoiImages()[0].getNumX();
		int numY = cellROI_2D.getRoiImages()[0].getNumY();
		int numZ = cellROI_2D.getRoiImages().length;
		short[] shortPixels = cellROI_2D.getRoiImages()[0].getPixels();
		byte[] bytePixels = new byte[numX*numY*numZ];
		final byte EXTRACELLULAR_PIXVAL = 0;
		final byte CYTOSOL_PIXVAL = 1;
		for (int i = 0; i < bytePixels.length; i++) {
			if (shortPixels[i]!=0){
				bytePixels[i] = CYTOSOL_PIXVAL;
			}
		}
		VCImage maskImage;
		try {
			maskImage = new VCImageUncompressed(null,bytePixels,extent,numX,numY,numZ);
		} catch (ImageException e) {
			e.printStackTrace();
			throw new RuntimeException("failed to create mask image for geometry");
		}
		Geometry geometry = new Geometry("geometry",maskImage);
		if(geometry.getGeometrySpec().getNumSubVolumes() != 2){
			throw new Exception("Cell ROI has no ExtraCellular.");
		}
		int subVolume0PixVal = ((ImageSubVolume)geometry.getGeometrySpec().getSubVolume(0)).getPixelValue();
		geometry.getGeometrySpec().getSubVolume(0).setName((subVolume0PixVal == EXTRACELLULAR_PIXVAL?EXTRACELLULAR_NAME:CYTOSOL_NAME));
		int subVolume1PixVal = ((ImageSubVolume)geometry.getGeometrySpec().getSubVolume(1)).getPixelValue();
		geometry.getGeometrySpec().getSubVolume(1).setName((subVolume1PixVal == CYTOSOL_PIXVAL?CYTOSOL_NAME:EXTRACELLULAR_NAME));
		geometry.getGeometrySurfaceDescription().updateAll();

		BioModel bioModel = new BioModel(null);
		bioModel.setName("unnamed");
		Model model = new Model("model");
		bioModel.setModel(model);
		model.addFeature(EXTRACELLULAR_NAME, null, null);
		Feature extracellular = (Feature)model.getStructure(EXTRACELLULAR_NAME);
		model.addFeature(CYTOSOL_NAME, extracellular, "plasmaMembrane");
		Feature cytosol = (Feature)model.getStructure(CYTOSOL_NAME);

		String roiDataName = "roiData";
		
		final int MOBILE_IMMOBILE_SPECIES_COUNT = 2;
		final int MOBILE_SPECIES_INDEX = 0;
		final int IMMOBILE_SPECIES_INDEX = 1;
		
		Expression[] diffusionConstants = new Expression[MOBILE_IMMOBILE_SPECIES_COUNT];
		Species[] species = new Species[MOBILE_IMMOBILE_SPECIES_COUNT];
		SpeciesContext[] speciesContexts = new SpeciesContext[MOBILE_IMMOBILE_SPECIES_COUNT];
		Expression[] initialConditions = new Expression[MOBILE_IMMOBILE_SPECIES_COUNT];
		
		double mobileFraction = (mobileFractionString != null?Double.parseDouble(mobileFractionString):new Double(1.0));
		//Mobile Species
		diffusionConstants[MOBILE_SPECIES_INDEX] = new Expression(baseDiffusionRate);
		species[MOBILE_SPECIES_INDEX] =
				new Species(SPECIES_NAME_PREFIX_MOBILE,
//						TokenMangler.fixTokenStrict(SPECIES_NAME_PREFIX_MOBILE+NumberUtils.formatNumber(diffusionConstants[MOBILE_SPECIES_INDEX].evaluateConstant(),3)),
						"Mobile bleachable species");
		speciesContexts[MOBILE_SPECIES_INDEX] = 
				new SpeciesContext(null,species[MOBILE_SPECIES_INDEX].getCommonName(),species[MOBILE_SPECIES_INDEX],cytosol,true);
		initialConditions[MOBILE_SPECIES_INDEX] =
				new Expression(mobileFraction+"*(field("+roiDataName+",postbleach_first,0) / field("+roiDataName+",prebleach_avg,0))");
		
		//Immobile Species (No diffusion)
		//Set very small diffusion rate on immobile to force evaluation as state variable (instead of FieldData function)
		//If left as a function errors occur because functions involving FieldData require a database connection
		final String IMMOBILE_DIFFUSION_KLUDGE = "1e-14";
		diffusionConstants[IMMOBILE_SPECIES_INDEX] = new Expression(IMMOBILE_DIFFUSION_KLUDGE);
		species[IMMOBILE_SPECIES_INDEX] =
				new Species(SPECIES_NAME_PREFIX_IMMOBILE,
//						TokenMangler.fixTokenStrict(SPECIES_NAME_PREFIX_IMMOBILE+NumberUtils.formatNumber(diffusionConstants[IMMOBILE_SPECIES_INDEX].evaluateConstant(),3)),
						"Immobile bleachable species");
		speciesContexts[IMMOBILE_SPECIES_INDEX] = 
				new SpeciesContext(null,species[IMMOBILE_SPECIES_INDEX].getCommonName(),species[IMMOBILE_SPECIES_INDEX],cytosol,true);
		initialConditions[IMMOBILE_SPECIES_INDEX] =
				new Expression((1.0-mobileFraction)+"*(field("+roiDataName+",postbleach_first,0) / field("+roiDataName+",prebleach_avg,0))");

		
		// for parameter scans, use cube root of 10 (3 per decade) = factor of 2.154434690030230132025595313452
		// add reactions to species if there is bleachWhileMonitoring rate.
		for (int i = 0; i < initialConditions.length; i++) {
			model.addSpecies(species[i]);
			model.addSpeciesContext(speciesContexts[i]);
			if (bleachWhileMonitoringRateString != null){
				SimpleReaction simpleReaction = new SimpleReaction(cytosol,speciesContexts[i].getName()+"_bleach");
				simpleReaction.addReactant(speciesContexts[i], 1);
				MassActionKinetics massActionKinetics = new MassActionKinetics(simpleReaction);
				simpleReaction.setKinetics(massActionKinetics);
				KineticsParameter kforward = massActionKinetics.getForwardRateParameter();
				simpleReaction.getKinetics().setParameterValue(kforward, new Expression(new Double(bleachWhileMonitoringRateString)));
				model.addReactionStep(simpleReaction);
				//we save bleachWhileMonitoringRate during generating the bio model, this was saved to nowhere previously.
			}
		}

		SimulationContext simContext = new SimulationContext(bioModel.getModel(),geometry);
		bioModel.addSimulationContext(simContext);
		FeatureMapping cytosolFeatureMapping = (FeatureMapping)simContext.getGeometryContext().getStructureMapping(cytosol);
		cytosolFeatureMapping.setSubVolume(geometry.getGeometrySpec().getSubVolume(CYTOSOL_NAME));
		cytosolFeatureMapping.setResolved(true);
		FeatureMapping extracellularFeatureMapping = (FeatureMapping)simContext.getGeometryContext().getStructureMapping(extracellular);
		extracellularFeatureMapping.setSubVolume(geometry.getGeometrySpec().getSubVolume(EXTRACELLULAR_NAME));
		extracellularFeatureMapping.setResolved(true);
		
		for (int i = 0; i < speciesContexts.length; i++) {
			SpeciesContextSpec scs = simContext.getReactionContext().getSpeciesContextSpec(speciesContexts[i]);
			scs.getInitialConditionParameter().setExpression(initialConditions[i]);
			scs.getDiffusionParameter().setExpression(diffusionConstants[i]);
		}

		MathMapping mathMapping = new MathMapping(simContext);
		MathDescription mathDesc = mathMapping.getMathDescription();
		//Add total fluorescence as function of mobile and immobile fractions
		mathDesc.addVariable(
			new Function(SPECIES_NAME_PREFIX_COMBINED,
				new Expression(species[MOBILE_SPECIES_INDEX].getCommonName()+"+"+species[IMMOBILE_SPECIES_INDEX].getCommonName())));
		simContext.setMathDescription(mathDesc);

		SimulationVersion simVersion = new SimulationVersion(simKey,"sim1",owner,new GroupAccessNone(),new KeyValue("0"),new BigDecimal(0),new Date(),VersionFlag.Current,"",null);
		Simulation newSimulation = new Simulation(simVersion,mathDesc);
		simContext.addSimulation(newSimulation);
		newSimulation.getSolverTaskDescription().setTimeBounds(timeBounds);
		newSimulation.getMeshSpecification().setSamplingSize(cellROI_2D.getISize());
		newSimulation.getSolverTaskDescription().setTimeStep(timeStep);
//		newSimulation.getSolverTaskDescription().setOutputTimeSpec(new DefaultOutputTimeSpec(10));
		
		return bioModel;
	}
	
	private KernelJAI createCircularBinaryKernel(int radius){
		int enclosingBoxSideLength = radius*2+1;
		float[] kernalData = new float[enclosingBoxSideLength*enclosingBoxSideLength];
		Point2D kernalPoint = new Point2D.Float(0f,0f);
		int index = 0;
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				if(kernalPoint.distance(x, y) <= radius){
					kernalData[index] = 1.0f;
				}
				index++;
			}
		}
		return new KernelJAI(enclosingBoxSideLength,enclosingBoxSideLength,radius,radius,kernalData);
	}
	private PlanarImage binarize(UShortImage source){
		return binarize(ImageTools.makeImage(source.getPixels(), source.getNumX(), source.getNumY()));
	}
	private PlanarImage binarize(BufferedImage source){
		PlanarImage planarSource = PlanarImage.wrapRenderedImage(source);
		double[][] minmaxArr = (double[][])ExtremaDescriptor.create(planarSource, null, 1, 1, false, 1,null).getProperty("extrema");
		short[] lookupData = new short[(int)0x010000];
		lookupData[(int)minmaxArr[1][0]] = 1;
		LookupTableJAI lookupTable = new LookupTableJAI(lookupData,true);
		planarSource = LookupDescriptor.create(planarSource, lookupTable, null).createInstance();
		return planarSource;		
	}
	private UShortImage convertToUShortImage(PlanarImage source,Extent extent) throws ImageException{
    	short[] shortData = new short[source.getWidth() * source.getHeight()];
    	source.getData().getDataElements(0, 0, source.getWidth(),source.getHeight(), shortData);
    	return new UShortImage(shortData,extent,source.getWidth(),source.getHeight(),1);	
	}
	private UShortImage erodeDilate(UShortImage source,KernelJAI dilateErodeKernel,UShortImage mask,boolean bErode) throws ImageException{
		PlanarImage completedImage = null;
		PlanarImage operatedImage = null;
		PlanarImage planarSource = binarize(source);
		Integer borderPad = dilateErodeKernel.getWidth()/2;
		planarSource = 
			BorderDescriptor.create(planarSource,
				borderPad, borderPad, borderPad, borderPad,
				BorderExtender.createInstance(BorderExtender.BORDER_ZERO), null).createInstance();
		if(bErode){
			planarSource = AddConstDescriptor.create(planarSource, new double[] {1.0}, null).createInstance();
	    	RenderedOp erodeOP = ErodeDescriptor.create(planarSource, dilateErodeKernel, null);
	    	operatedImage = erodeOP.createInstance();
			
		}else{
	    	RenderedOp dilationOP = DilateDescriptor.create(planarSource, dilateErodeKernel, null);
	    	operatedImage = dilationOP.createInstance();
		}
		operatedImage =
    		CropDescriptor.create(operatedImage,
    			new Float(0), new Float(0),
    			new Float(source.getNumX()), new Float(source.getNumY()), null);
    	operatedImage = binarize(operatedImage.getAsBufferedImage());
		if (mask != null) {
			RenderedOp andDescriptor = AndDescriptor.create(operatedImage,binarize(mask), null);
			completedImage = andDescriptor.createInstance();
		}else{
			completedImage = operatedImage;
		}
		return convertToUShortImage(completedImage, source.getExtent());
    	
}
//	private void writeUShortFile(UShortImage uShortImage,File outFile){
//		writeBufferedImageFile(
//			ImageTools.makeImage(uShortImage.getPixels(),uShortImage.getNumX(), uShortImage.getNumY()),outFile);
//
//	}
//	private void writeBufferedImageFile(BufferedImage bufferedImage,File outFile){
//		try{
//		ImageIO.write(
//			FormatDescriptor.create(bufferedImage, DataBuffer.TYPE_BYTE,null).createInstance(),
//			"bmp", outFile);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//	}
	private UShortImage fastDilate(UShortImage dilateSource,int radius,UShortImage mask) throws ImageException{
		short[] sourcePixels = dilateSource.getPixels();
		short[] targetPixels = dilateSource.getPixels().clone();
		KernelJAI dilateKernel = createCircularBinaryKernel(radius);
		float[] kernelData = dilateKernel.getKernelData();
		BitSet kernelBitSet = new BitSet(kernelData.length);
		for (int i = 0; i < kernelData.length; i++) {
			if(kernelData[i] == 1.0f){
				kernelBitSet.set(i);
			}
		}
		boolean bNeedsFill = false;
		for (int y = 0; y < dilateSource.getNumY(); y++) {
			int yOffset = y*dilateSource.getNumX();
			int yMinus = yOffset-dilateSource.getNumX();
			int yPlus = yOffset+dilateSource.getNumX();
			for (int x = 0; x < dilateSource.getNumX(); x++) {
				bNeedsFill = false;
				if(sourcePixels[x+yOffset] != 0){
					if(x-1 >= 0 && sourcePixels[(x-1)+yOffset] == 0){
						bNeedsFill = true;
					}else
					if(y-1 >= 0 && sourcePixels[x+yMinus] == 0){
						bNeedsFill = true;
					}else
					if(x+1 < dilateSource.getNumX() && sourcePixels[(x+1)+yOffset] == 0){
						bNeedsFill = true;
					}else
					if(y+1 < dilateSource.getNumY() && sourcePixels[x+yPlus] == 0){
						bNeedsFill = true;
					}
					if(bNeedsFill){
						int masterKernelIndex = 0;
						for (int y2 = y-radius; y2 <= y+radius; y2++) {
							if(y2>= 0 && y2 <dilateSource.getNumY()){
								int kernelIndex = masterKernelIndex;
								int targetYIndex = y2*dilateSource.getNumX();
								for (int x2 = x-radius; x2 <= x+radius; x2++) {
									if(kernelBitSet.get(kernelIndex) &&
										x2>= 0 && x2 <dilateSource.getNumX()){
										targetPixels[targetYIndex+x2] = 1;
									}
									kernelIndex++;
								}
							}
							masterKernelIndex+= dilateKernel.getWidth();
						}
					}
				}
			}
		}
		UShortImage resultImage =
			new UShortImage(targetPixels,
					dilateSource.getExtent(),
					dilateSource.getNumX(),
					dilateSource.getNumY(),
					dilateSource.getNumZ());
		resultImage.and(mask);
		return resultImage;
	}
	public void refreshDependentROIs(){
		UShortImage cellROI_2D = null;
		UShortImage bleachedROI_2D = null;
		UShortImage dilatedROI_2D_1 = null;
    	UShortImage dilatedROI_2D_2 = null;
    	UShortImage dilatedROI_2D_3 = null;
    	UShortImage dilatedROI_2D_4 = null;
    	UShortImage dilatedROI_2D_5 = null;
    	UShortImage erodedROI_2D_0 = null;
    	UShortImage erodedROI_2D_1 = null;
    	UShortImage erodedROI_2D_2 = null;

    	try {
    		cellROI_2D =
    			convertToUShortImage(binarize(getFrapData().getRoi(RoiType.ROI_CELL).getRoiImages()[0]),
    				getFrapData().getRoi(RoiType.ROI_CELL).getRoiImages()[0].getExtent());
    		bleachedROI_2D =
    			convertToUShortImage(
    					AndDescriptor.create(binarize(getFrapData().getRoi(RoiType.ROI_BLEACHED).getRoiImages()[0]),
    						binarize(cellROI_2D), null).createInstance(),
    					getFrapData().getRoi(RoiType.ROI_BLEACHED).getRoiImages()[0].getExtent());
//			writeUShortFile(cellROI_2D, new File("D:\\developer\\eclipse\\workspace\\cellROI_2D.bmp"));
//			writeUShortFile(bleachedROI_2D, new File("D:\\developer\\eclipse\\workspace\\bleachedROI_2D.bmp"));

    		dilatedROI_2D_1 =
    			fastDilate(bleachedROI_2D, 8, cellROI_2D);
//    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(8), cellROI_2D,false);

			dilatedROI_2D_2 = 
				fastDilate(bleachedROI_2D, 16, cellROI_2D);
//    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(16), cellROI_2D,false);

	    	dilatedROI_2D_3 = 
	    		fastDilate(bleachedROI_2D, 24, cellROI_2D);
//    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(24), cellROI_2D,false);

	    	dilatedROI_2D_4 = 
	    		fastDilate(bleachedROI_2D, 32, cellROI_2D);
//    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(32), cellROI_2D,false);

	    	dilatedROI_2D_5 = 
	    		fastDilate(bleachedROI_2D, 40, cellROI_2D);
//    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(40), cellROI_2D,false);
			
			erodedROI_2D_0 = new UShortImage(bleachedROI_2D);
			
			erodedROI_2D_1 = 
    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(3), null,true);
			
			erodedROI_2D_2 = 
    			erodeDilate(bleachedROI_2D, createCircularBinaryKernel(6), null,true);
			
			UShortImage reverseErodeROI_2D_1 = new UShortImage(erodedROI_2D_1);
			reverseErodeROI_2D_1.reverse();
			erodedROI_2D_0.and(reverseErodeROI_2D_1);
			
			UShortImage reverseErodeROI_2D_2 = new UShortImage(erodedROI_2D_2);
			reverseErodeROI_2D_2.reverse();
			erodedROI_2D_1.and(reverseErodeROI_2D_2);
			
			UShortImage reverseDilateROI_2D_4 = new UShortImage(dilatedROI_2D_4);
			reverseDilateROI_2D_4.reverse();
			dilatedROI_2D_5.and(reverseDilateROI_2D_4);

			UShortImage reverseDilateROI_2D_3 = new UShortImage(dilatedROI_2D_3);
//			writeUShortFile(dilatedROI_2D_3, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_3.bmp"));
			reverseDilateROI_2D_3.reverse();
//			writeUShortFile(reverseDilateROI_2D_3, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_3_reverse.bmp"));
//			writeUShortFile(dilatedROI_2D_4, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_4.bmp"));
			dilatedROI_2D_4.and(reverseDilateROI_2D_3);
//			writeUShortFile(dilatedROI_2D_4, new File("D:\\developer\\eclipse\\workspace\\dilatedROI_2D_4_anded.bmp"));

			UShortImage reverseDilateROI_2D_2 = new UShortImage(dilatedROI_2D_2);
			reverseDilateROI_2D_2.reverse();
			dilatedROI_2D_3.and(reverseDilateROI_2D_2);

			UShortImage reverseDilateROI_2D_1 = new UShortImage(dilatedROI_2D_1);
			reverseDilateROI_2D_1.reverse();
			dilatedROI_2D_2.and(reverseDilateROI_2D_1);

			UShortImage reverseBleach_2D = new UShortImage(bleachedROI_2D);
			reverseBleach_2D.reverse();
			dilatedROI_2D_1.and(reverseBleach_2D);

    	}catch (ImageException e){
    		e.printStackTrace(System.out);
    	}
		ROI roiBleachedRing1_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING1);
		if (roiBleachedRing1_2D==null){
			roiBleachedRing1_2D = new ROI(erodedROI_2D_2,RoiType.ROI_BLEACHED_RING1);
			getFrapData().addReplaceRoi(roiBleachedRing1_2D);
		}else{
			System.arraycopy(erodedROI_2D_2.getPixels(), 0, roiBleachedRing1_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_2.getPixels().length);
		}
		ROI roiBleachedRing2_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING2);
		if (roiBleachedRing2_2D==null){
			roiBleachedRing2_2D = new ROI(erodedROI_2D_1,RoiType.ROI_BLEACHED_RING2);
			getFrapData().addReplaceRoi(roiBleachedRing2_2D);
		}else{
			System.arraycopy(erodedROI_2D_1.getPixels(), 0, roiBleachedRing2_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_1.getPixels().length);
		}
		ROI roiBleachedRing3_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING3);
		if (roiBleachedRing3_2D==null){
			roiBleachedRing3_2D = new ROI(erodedROI_2D_0,RoiType.ROI_BLEACHED_RING3);
			getFrapData().addReplaceRoi(roiBleachedRing3_2D);
		}else{
			System.arraycopy(erodedROI_2D_0.getPixels(), 0, roiBleachedRing3_2D.getRoiImages()[0].getPixels(), 0, erodedROI_2D_0.getPixels().length);
		}
		ROI roiBleachedRing4_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING4);
		if (roiBleachedRing4_2D==null){
			roiBleachedRing4_2D = new ROI(dilatedROI_2D_1,RoiType.ROI_BLEACHED_RING4);
			getFrapData().addReplaceRoi(roiBleachedRing4_2D);
		}else{
			System.arraycopy(dilatedROI_2D_1.getPixels(), 0, roiBleachedRing4_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_1.getPixels().length);

		}
		ROI roiBleachedRing5_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING5);
		if (roiBleachedRing5_2D==null){
			roiBleachedRing5_2D = new ROI(dilatedROI_2D_2,RoiType.ROI_BLEACHED_RING5);
			getFrapData().addReplaceRoi(roiBleachedRing5_2D);
		}else{
			System.arraycopy(dilatedROI_2D_2.getPixels(), 0, roiBleachedRing5_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_2.getPixels().length);
		}
		ROI roiBleachedRing6_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING6);
		if (roiBleachedRing6_2D==null){
			roiBleachedRing6_2D = new ROI(dilatedROI_2D_3,RoiType.ROI_BLEACHED_RING6);
			getFrapData().addReplaceRoi(roiBleachedRing6_2D);
		}else{
			System.arraycopy(dilatedROI_2D_3.getPixels(), 0, roiBleachedRing6_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_3.getPixels().length);
		}
		ROI roiBleachedRing7_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING7);
		if (roiBleachedRing7_2D==null){
			roiBleachedRing7_2D = new ROI(dilatedROI_2D_4,RoiType.ROI_BLEACHED_RING7);
			getFrapData().addReplaceRoi(roiBleachedRing7_2D);
		}else{
			System.arraycopy(dilatedROI_2D_4.getPixels(), 0, roiBleachedRing7_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_4.getPixels().length);
//			writeUShortFile(roiBleachedRing7_2D.getRoiImages()[0], new File("D:\\developer\\eclipse\\workspace\\ROI_BLEACHED_RING7.bmp"));
		}
		ROI roiBleachedRing8_2D = getFrapData().getRoi(RoiType.ROI_BLEACHED_RING8);
		if (roiBleachedRing8_2D==null){
			roiBleachedRing8_2D = new ROI(dilatedROI_2D_5,RoiType.ROI_BLEACHED_RING8);
			getFrapData().addReplaceRoi(roiBleachedRing8_2D);
		}else{
			System.arraycopy(dilatedROI_2D_5.getPixels(), 0, roiBleachedRing8_2D.getRoiImages()[0].getPixels(), 0, dilatedROI_2D_5.getPixels().length);
		}
	}
	public static final String IMAGE_EXTDATA_NAME = "timeData";
	public void  saveImageDatasetAsExternalData(LocalWorkspace localWorkspace,ExternalDataIdentifier newImageExtDataID,int startingIndexForRecovery) throws Exception{
			ImageDataset imageDataset = getFrapData().getImageDataset();
			if (imageDataset.getSizeC()>1){
				throw new RuntimeException("FRAPStudy.saveImageDatasetAsExternalData(): multiple channels not yet supported");
			}
			Extent extent = imageDataset.getExtent();
			ISize isize = imageDataset.getISize();
			int numImageToStore = imageDataset.getSizeT()-startingIndexForRecovery; //not include the prebleach 
	    	short[][][] pixData = new short[numImageToStore][1][]; //original fluor data only
	    	double[] timesArray = new double[numImageToStore];
	    	
	    	for (int tIndex = startingIndexForRecovery; tIndex < imageDataset.getSizeT(); tIndex++) {
	    		short[] originalData = imageDataset.getPixelsZ(0, tIndex);// images according to zIndex at time points(from recovery index to the end)
	    		pixData[tIndex-startingIndexForRecovery][0] = originalData;
	    		timesArray[tIndex-startingIndexForRecovery] = imageDataset.getImageTimeStamps()[tIndex]-imageDataset.getImageTimeStamps()[startingIndexForRecovery];
	    	}
	    	//changed in March 2008. Though biomodel is not created, we still let user save to xml file.
	    	Origin origin = new Origin(0,0,0);
	    	CartesianMesh cartesianMesh  = getCartesianMesh();
	    	FieldDataFileOperationSpec fdos = new FieldDataFileOperationSpec();
	    	fdos.opType = FieldDataFileOperationSpec.FDOS_ADD;
	    	fdos.cartesianMesh = cartesianMesh;
	    	fdos.shortSpecData =  pixData;
	    	fdos.specEDI = newImageExtDataID;
	    	fdos.varNames = new String[] {"fluor"};
	    	fdos.owner = LocalWorkspace.getDefaultOwner();
	    	fdos.times = timesArray;
	    	fdos.variableTypes = new VariableType[] {VariableType.VOLUME};
	    	fdos.origin = origin;
	    	fdos.extent = extent;
	    	fdos.isize = isize;
			localWorkspace.getDataSetControllerImpl().fieldDataFileOperation(fdos);
	}
	
	public static File[] getCanonicalExternalDataFiles(LocalWorkspace localWorkspace,ExternalDataIdentifier originalExtDataID){
		if(originalExtDataID != null){
			File userDir = new File(localWorkspace.getDefaultSimDataDirectory());
			File fdLogFile =
				new File(userDir,
						ExternalDataIdentifier.createCanonicalSimLogFileName(originalExtDataID.getKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT,false));
			File fdMeshFile =
				new File(userDir,
						ExternalDataIdentifier.createCanonicalMeshFileName(originalExtDataID.getKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT,false));
			File fdFunctionFile =
				new File(userDir,
						ExternalDataIdentifier.createCanonicalFunctionsFileName(originalExtDataID.getKey(),FieldDataFileOperationSpec.JOBINDEX_DEFAULT,false));
			File fdZipFile =
				new File(userDir,
						ExternalDataIdentifier.createCanonicalSimZipFileName(originalExtDataID.getKey(), 0,FieldDataFileOperationSpec.JOBINDEX_DEFAULT,false));
			return new File[] {fdLogFile,fdMeshFile,fdFunctionFile,fdZipFile};
		}
		return null;
	}
	public static void deleteCanonicalExternalData(LocalWorkspace localWorkspace,ExternalDataIdentifier originalExtDataID) throws Exception{
		File[] externalDataFiles = getCanonicalExternalDataFiles(localWorkspace, originalExtDataID);
		for (int i = 0;externalDataFiles != null && i < externalDataFiles.length; i++) {
			externalDataFiles[i].delete();
		}
	}
	private CartesianMesh getCartesianMesh() throws Exception{
		CartesianMesh cartesianMesh = null;
		ImageDataset imgDataSet = getFrapData().getImageDataset();
		Extent extent = imgDataSet.getExtent();
		ISize isize = imgDataSet.getISize();
		Origin origin = new Origin(0,0,0);
    	if (getBioModel()==null){
    		cartesianMesh = CartesianMesh.createSimpleCartesianMesh(origin, extent,isize, 
    						new RegionImage( new VCImageUncompressed(null, new byte[isize.getXYZ()], extent, isize.getX(),isize.getY(),isize.getZ()),
    						0,null,null,RegionImage.NO_SMOOTHING));
    	}
    	else
    	{
	    	RegionImage regionImage = getBioModel().getSimulationContexts()[0].getGeometry().getGeometrySurfaceDescription().getRegionImage();
	    	if(regionImage == null){
	    		getBioModel().getSimulationContexts()[0].getGeometry().getGeometrySurfaceDescription().updateAll();
	    		regionImage = getBioModel().getSimulationContexts()[0].getGeometry().getGeometrySurfaceDescription().getRegionImage();
	    	}
	    	cartesianMesh = CartesianMesh.createSimpleCartesianMesh(origin, extent, isize, regionImage);
    	}
    	return cartesianMesh;
	}
	public static final String ROI_EXTDATA_NAME = "roiData";
	public void saveROIsAsExternalData(LocalWorkspace localWorkspace,ExternalDataIdentifier newROIExtDataID,int startingIndexForRecovery) throws Exception{
			ImageDataset imageDataset = getFrapData().getImageDataset();
			Extent extent = imageDataset.getExtent();
			ISize isize = imageDataset.getISize();
			int NumTimePoints = 1; 
			int NumChannels = 13;//actually it is total number of ROIs(cell,bleached + 8 rings)+prebleach+firstPostBleach+lastPostBleach
	    	double[][][] pixData = new double[NumTimePoints][NumChannels][]; // dimensions: time points, channels, whole image ordered by z slices. 
	    	

	    	long[] accumPrebleachImage = new long[imageDataset.getISize().getXYZ()];//ISize: Image size including x, y, z. getXYZ()=x*y*z
	    	double[] avgPrebleachDouble = new double[accumPrebleachImage.length];
	    	// changed in June, 2008 average prebleach depends on if there is prebleach images. 
	    	// Since the initial condition is normalized by prebleach avg, we have to take care the divided by zero error.
			if(startingIndexForRecovery > 0){
				for (int timeIndex = 0; timeIndex < startingIndexForRecovery; timeIndex++) {
					short[] pixels = getFrapData().getImageDataset().getPixelsZ(0, timeIndex);//channel index is 0. it is not supported yet. get image size X*Y*Z stored by time index. image store by UShortImage[Z*T]
					for (int i = 0; i < accumPrebleachImage.length; i++) {
						accumPrebleachImage[i] += 0x0000ffff&pixels[i];
					}
				}
				for (int i = 0; i < avgPrebleachDouble.length; i++) {
					avgPrebleachDouble[i] = (double)accumPrebleachImage[i]/(double)startingIndexForRecovery;
				}
			}
			else{
				//if no prebleach image, use the last recovery image intensity as prebleach average.
				System.err.println("need to determine factor for prebleach average if no pre bleach images.");
				short[] pixels = getFrapData().getImageDataset().getPixelsZ(0, (imageDataset.getSizeT() - 1));
				for (int i = 0; i < pixels.length; i++) {
					avgPrebleachDouble[i] = 0x0000ffff&pixels[i];
				}
			}
			
			// since prebleach will be used to normalize image data, we have to eliminate the 0?
			//or maybe we should leave as 0 and check later for zero when its used and deal with it.
			for (int i = 0; i < avgPrebleachDouble.length; i++) {
				if(avgPrebleachDouble[i] == 0){
					avgPrebleachDouble[i] = FRAPStudy.Epsilon;
				}

			}
    		pixData[0][0] = avgPrebleachDouble; // average of prebleach
    		pixData[0][1] = createDoubleArray(getFrapData().getImageDataset().getPixelsZ(0, startingIndexForRecovery)); // first post-bleach
    		pixData[0][2] = createDoubleArray(getFrapData().getImageDataset().getPixelsZ(0, imageDataset.getSizeT()-1)); // last post-bleach image, actually at last time point
    		pixData[0][3] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED).getBinaryPixelsXYZ(1));
    		pixData[0][4] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_CELL).getBinaryPixelsXYZ(1));
    		if (getFrapData().getRoi(RoiType.ROI_BLEACHED_RING1) == null){
    			//throw new RuntimeException("must first generate \"derived masks\"");
    			pixData[0][5] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][6] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][7] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][8] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][9] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][10] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][11] = new double[imageDataset.getISize().getXYZ()];
	    		pixData[0][12] = new double[imageDataset.getISize().getXYZ()];
    		}
    		else{
	    		pixData[0][5] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING1).getBinaryPixelsXYZ(1));
	    		pixData[0][6] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING2).getBinaryPixelsXYZ(1));
	    		pixData[0][7] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING3).getBinaryPixelsXYZ(1));
	    		pixData[0][8] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING4).getBinaryPixelsXYZ(1));
	    		pixData[0][9] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING5).getBinaryPixelsXYZ(1));
	    		pixData[0][10] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING6).getBinaryPixelsXYZ(1));
	    		pixData[0][11] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING7).getBinaryPixelsXYZ(1));
	    		pixData[0][12] = createDoubleArray(getFrapData().getRoi(RoiType.ROI_BLEACHED_RING8).getBinaryPixelsXYZ(1));
    		}
    		CartesianMesh cartesianMesh = getCartesianMesh();
    		Origin origin = new Origin(0,0,0);
	    		    	
	    	FieldDataFileOperationSpec fdos = new FieldDataFileOperationSpec();
	    	fdos.opType = FieldDataFileOperationSpec.FDOS_ADD;
	    	fdos.cartesianMesh = cartesianMesh;
	    	fdos.doubleSpecData =  pixData;
	    	fdos.specEDI = newROIExtDataID;
	    	fdos.varNames = new String[] {
	    			"prebleach_avg",
	    			"postbleach_first",
	    			"postbleach_last",
	    			"bleached_mask", 
	    			"cell_mask", 
	    			"ring1_mask",
	    			"ring2_mask",
	    			"ring3_mask",
	    			"ring4_mask",
	    			"ring5_mask",
	    			"ring6_mask",
	    			"ring7_mask",
	    			"ring8_mask"};
	    	fdos.owner = LocalWorkspace.getDefaultOwner();
	    	fdos.times = new double[] { 0.0 };
	    	fdos.variableTypes = new VariableType[] {
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME,
	    			VariableType.VOLUME};
	    	fdos.origin = origin;
	    	fdos.extent = extent;
	    	fdos.isize = isize;
	    	localWorkspace.getDataSetControllerImpl().fieldDataFileOperation(fdos);
	}
	
	private double[] createDoubleArray(short[] shortData){
		double[] doubleData = new double[shortData.length];
		for (int i = 0; i < doubleData.length; i++) {
			doubleData[i] = 0x0000FFFF&shortData[i];
		}
		return doubleData;
	}
	//get external image dataset file name or ROI file name
	public File getExternalDataFile(ExternalDataInfo arg_extDataInfo)
	{
		final ExternalDataInfo extDataInfo = arg_extDataInfo;
		File extFile = new File(extDataInfo.getFilename());
		File f = new File(extFile.getParent(), extDataInfo.getExternalDataIdentifier().getOwner().getName());
		f= new  File(f,extFile.getName());
		return f;
	}
	
	public void clearBioModel(){
		setBioModel(null);
	}
	public void setBioModel(BioModel argBioModel) {
		BioModel oldValue = this.bioModel;
		this.bioModel = argBioModel;
		propertyChangeSupport.firePropertyChange("bioModel", oldValue, argBioModel);
	}
	public FRAPData getFrapData() {
		return frapData;
	}
	
	public void setFrapData(FRAPData frapData) {
		this.frapData = frapData;
	}
	
	public void gatherIssues(Vector<Issue> issueList){
		if (frapData!=null){
			frapData.gatherIssues(issueList);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		propertyChangeSupport.firePropertyChange("description", oldValue, description);
	}

	public String getOriginalImageFilePath() {
		return originalImageFilePath;
	}

	public void setOriginalImageFilePath(String originalImageFilePath) {
		String oldValue = this.originalImageFilePath;
		this.originalImageFilePath = originalImageFilePath;
		propertyChangeSupport.firePropertyChange("originalImageFilePath", oldValue, originalImageFilePath);
	}
	public String getXmlFilename() {
		return xmlFilename;
	}

	public void setXmlFilename(String xmlFilename) {
		String oldValue = this.xmlFilename;
		this.xmlFilename = xmlFilename;
		propertyChangeSupport.firePropertyChange("xmlFilename", oldValue, xmlFilename);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange("name", oldValue, name);
	}

	public String getDirectory() {
		return directory;
	}

	void setDirectory(String directory) {
		String oldValue = this.directory;
		this.directory = directory;
		propertyChangeSupport.firePropertyChange("directory", oldValue, directory);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public boolean compareEqual(Matchable obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof FRAPStudy) 
		{
			FRAPStudy fStudy = (FRAPStudy)obj;
			if(!cbit.util.Compare.isEqualOrNull(name, fStudy.name))
			{
				return false;
			}
			if(!cbit.util.Compare.isEqualOrNull(description, fStudy.description))
			{
				return false;
			}
			if(!cbit.util.Compare.isEqualOrNull(originalImageFilePath, fStudy.originalImageFilePath))
			{
				return false;
			}
			if(!this.getFrapData().compareEqual(fStudy.getFrapData()))
			{
				return false;
			}
			if(!cbit.util.Compare.isEqualOrNull(getBioModel(), fStudy.getBioModel()))
			{
				return false;
			}
			if(!cbit.util.Compare.isEqualOrNull(getFrapDataExternalDataInfo(),fStudy.getFrapDataExternalDataInfo()))
			{
				return false;
			}
			if (!cbit.util.Compare.isEqualOrNull(getRoiExternalDataInfo(),fStudy.getRoiExternalDataInfo()))
			{
				return false;
			}
			return true;
		}

		return false;
	}


	public FRAPModelParameters getFrapModelParameters() {
		return frapModelParameters;
	}


	public void setFrapModelParameters(FRAPModelParameters frapModelParameters) {
		this.frapModelParameters = frapModelParameters;
	}

}
