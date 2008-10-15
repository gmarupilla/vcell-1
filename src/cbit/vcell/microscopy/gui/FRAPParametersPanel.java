package cbit.vcell.microscopy.gui;

import java.awt.Color;
import cbit.util.Compare;
import cbit.util.NumberUtils;
import cbit.vcell.microscopy.FRAPData;
import cbit.vcell.microscopy.FRAPStudy;
import cbit.vcell.microscopy.gui.FRAPStudyPanel.FrapChangeInfo;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import cbit.vcell.microscopy.gui.FRAPEstimationPanel;
import cbit.vcell.opt.Parameter;

public class FRAPParametersPanel extends JPanel {
	private FRAPEstimationPanel estimationPanel;
	private JComboBox frapDataTimesComboBox;
	private JLabel immobileFractionValueJLabel;
	private JTextField monitorBleachRateTextField;
	private JTextField mobileFractionTextField;
	private JTextField diffusionRateTextField;
	private JTextField secondDiffRateTextField;
	private JTextField secondMobileFracTextField;
//	private static final String PARAM_EST_EQUATION_STRING = "FRAP Model Parameter Estimation Equation";
//	private static final String PLOT_TITLE_STRING = "Plot of average data intensity at each timepoint within the 'bleach' ROI -and- Plot of estimation fit";
	
	private static String MONITOR_BLEACH_RATE_DESCRIPTION = "Monitor Bleach Rate";
	private static String MOBILE_FRACTION_DESCRIPTION = "Mobile Fraction";
	private static String DIFFUSION_RATE_DESCRIPTION = "Diffusion Rate";
	private static String SEC_DIFFUSION_RATE_DESCRIPTION = "Secondary Diffusion Rate";
	private static String SEC_MOBILE_FRACTION_DESCRIPTION = "Secondary Mobile Fraction";

	
	public FRAPParametersPanel() {
		super();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0};
		gridBagLayout.columnWidths = new int[] {7,7};
		setLayout(gridBagLayout);

		final JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new EtchedBorder(Color.gray, Color.lightGray), "Initial FRAP Model Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("", Font.BOLD, 12), null));
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] {7,0,0};
		gridBagLayout_1.rowHeights = new int[] {0,7,7,7,7,0,7,7};
		panel.setLayout(gridBagLayout_1);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints.weightx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 1;

		estimationPanel = new FRAPEstimationPanel();
		estimationPanel.setBorder(new TitledBorder(new EtchedBorder(Color.gray, Color.lightGray), "FRAP Model Parameter Assistant (Select 'Estimation Method')", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("", Font.BOLD, 12), null));
		estimationPanel.addPropertyChangeListener(
			new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					if(evt.getPropertyName().equals(FRAPEstimationPanel.FRAP_PARAMETER_ESTIMATE_VALUES_PROPERTY)){
						FRAPEstimationPanel.FRAPParameterEstimateValues frapParamEstVals =
							(FRAPEstimationPanel.FRAPParameterEstimateValues)evt.getNewValue();
						
						if(frapParamEstVals.startTimeRecovery != null){
							boolean bFound = false;
							for (int i = 0; i < frapDataTimesComboBox.getItemCount(); i++) {
								if(frapDataTimesComboBox.getItemAt(i).equals(frapParamEstVals.startTimeRecovery.toString())){
									bFound = true;
									frapDataTimesComboBox.setSelectedIndex(i);
									break;
								}
							}
							if(!bFound){
								throw new IllegalArgumentException("couldn't find time "+frapParamEstVals.startTimeRecovery.toString()+" in FRAP data while setting");
							}
						}
						if(frapParamEstVals.diffusionRate != null){
							diffusionRateTextField.setText(""+NumberUtils.formatNumber(frapParamEstVals.diffusionRate, 5));
						}
						if(frapParamEstVals.mobileFraction != null){
							mobileFractionTextField.setText(""+NumberUtils.formatNumber(frapParamEstVals.mobileFraction, 5));
//							immobileFractionValueJLabel.setText(""+NumberUtils.formatNumber((1.0-frapParamEstVals.mobileFraction),5));
						}
						if(frapParamEstVals.bleachWhileMonitorRate != null){
							monitorBleachRateTextField.setText(""+NumberUtils.formatNumber(frapParamEstVals.bleachWhileMonitorRate, 5));
						}
						updateImmobileFractionModelText();
					}
				}
			}
		);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_1.weighty = 1;
		gridBagConstraints_1.fill = GridBagConstraints.BOTH;
		gridBagConstraints_1.gridy = 0;
		gridBagConstraints_1.gridx = 0;
		add(estimationPanel, gridBagConstraints_1);
		add(panel, gridBagConstraints);

		final JLabel parameterLabel = new JLabel();
//		parameterLabel.setBorder(new LineBorder(Color.black, 1, false));
		parameterLabel.setBorder(new EtchedBorder());
		parameterLabel.setHorizontalAlignment(SwingConstants.CENTER);
		parameterLabel.setText("Parameter Type");
		final GridBagConstraints gridBagConstraints_19 = new GridBagConstraints();
		gridBagConstraints_19.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_19.fill = GridBagConstraints.BOTH;
		gridBagConstraints_19.gridy = 0;
		gridBagConstraints_19.gridx = 0;
		panel.add(parameterLabel, gridBagConstraints_19);

		final JLabel valueLabel = new JLabel();
//		valueLabel.setBorder(new LineBorder(Color.black, 1, false));
		valueLabel.setBorder(new EtchedBorder());
		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		valueLabel.setText("Value");
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.weightx = 1;
		gridBagConstraints_20.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_20.fill = GridBagConstraints.BOTH;
		gridBagConstraints_20.gridy = 0;
		gridBagConstraints_20.gridx = 1;
		panel.add(valueLabel, gridBagConstraints_20);

		final JLabel unitsLabel = new JLabel();
		unitsLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		unitsLabel.setBorder(new LineBorder(Color.black, 1, false));
		unitsLabel.setBorder(new EtchedBorder());
		unitsLabel.setText("Units");
		final GridBagConstraints gridBagConstraints_31 = new GridBagConstraints();
		gridBagConstraints_31.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_31.fill = GridBagConstraints.BOTH;
		gridBagConstraints_31.gridy = 0;
		gridBagConstraints_31.gridx = 2;
		panel.add(unitsLabel, gridBagConstraints_31);

		final JLabel diffusionRateLabel = new JLabel();
		diffusionRateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		diffusionRateLabel.setText("Prim. Diff. Rate");
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_18.fill = GridBagConstraints.BOTH;
		gridBagConstraints_18.gridy = 1;
		gridBagConstraints_18.gridx = 0;
		panel.add(diffusionRateLabel, gridBagConstraints_18);

		diffusionRateTextField = new JTextField();
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_12.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_12.weightx = 0;
		gridBagConstraints_12.gridy = 1;
		gridBagConstraints_12.gridx = 1;
		panel.add(diffusionRateTextField, gridBagConstraints_12);

		final JLabel umsecLabel = new JLabel();
		umsecLabel.setText("um2/s");
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_14.gridy = 1;
		gridBagConstraints_14.gridx = 2;
		panel.add(umsecLabel, gridBagConstraints_14);

		final JLabel mobileFractionLabel = new JLabel();
		mobileFractionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mobileFractionLabel.setText("Prim. Mobile Frac.");
		final GridBagConstraints gridBagConstraints_21 = new GridBagConstraints();
		gridBagConstraints_21.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_21.fill = GridBagConstraints.BOTH;
		gridBagConstraints_21.gridy = 2;
		gridBagConstraints_21.gridx = 0;
		panel.add(mobileFractionLabel, gridBagConstraints_21);

		mobileFractionTextField = new JTextField();
		mobileFractionTextField.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(final UndoableEditEvent e) {
				updateImmobileFractionModelText();
			}
		});
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_13.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_13.gridy = 2;
		gridBagConstraints_13.gridx = 1;
		panel.add(mobileFractionTextField, gridBagConstraints_13);

		final JLabel label = new JLabel();
		final GridBagConstraints gridBagConstraints_32 = new GridBagConstraints();
		gridBagConstraints_32.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_32.gridy = 2;
		gridBagConstraints_32.gridx = 2;
		panel.add(label, gridBagConstraints_32);

		JLabel secDiffRateLabel = new JLabel();
		secDiffRateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		secDiffRateLabel.setText("Sec.Diff.Rate(slow)");
		final GridBagConstraints gridBagConstraints_secdiff = new GridBagConstraints();
		gridBagConstraints_secdiff.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_secdiff.fill = GridBagConstraints.BOTH;
		gridBagConstraints_secdiff.gridy = 3;
		gridBagConstraints_secdiff.gridx = 0;
		panel.add(secDiffRateLabel, gridBagConstraints_secdiff);

		secondDiffRateTextField = new JTextField();
		final GridBagConstraints gridBagConstraints_secDiffTF = new GridBagConstraints();
		gridBagConstraints_secDiffTF.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_secDiffTF.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_secDiffTF.weightx = 0;
		gridBagConstraints_secDiffTF.gridy = 3;
		gridBagConstraints_secDiffTF.gridx = 1;
		panel.add(secondDiffRateTextField, gridBagConstraints_secDiffTF);

		final JLabel umsecLabel2 = new JLabel();
		umsecLabel2.setText("um2/s");
		final GridBagConstraints gridBagConstraints_secDFunit = new GridBagConstraints();
		gridBagConstraints_secDFunit.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_secDFunit.gridy = 3;
		gridBagConstraints_secDFunit.gridx = 2;
		panel.add(umsecLabel2, gridBagConstraints_secDFunit);
		
		final JLabel secMobileFracLabel = new JLabel();
		secMobileFracLabel.setHorizontalAlignment(SwingConstants.CENTER);
		secMobileFracLabel.setText("Sec. Mobile Frac.");
		final GridBagConstraints gridBagConstraints_secMFl = new GridBagConstraints();
		gridBagConstraints_secMFl.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_secMFl.fill = GridBagConstraints.BOTH;
		gridBagConstraints_secMFl.gridy = 4;
		gridBagConstraints_secMFl.gridx = 0;
		panel.add(secMobileFracLabel, gridBagConstraints_secMFl);

		secondMobileFracTextField = new JTextField();
		secondMobileFracTextField.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(final UndoableEditEvent e) {
				updateImmobileFractionModelText();
			}
		});
		final GridBagConstraints gridBagConstraints_secMFt = new GridBagConstraints();
		gridBagConstraints_secMFt.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_secMFt.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_secMFt.gridy = 4;
		gridBagConstraints_secMFt.gridx = 1;
		panel.add(secondMobileFracTextField, gridBagConstraints_secMFt);

		final JLabel label2 = new JLabel();
		final GridBagConstraints gridBagConstraints_label = new GridBagConstraints();
		gridBagConstraints_label.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_label.gridy = 4;
		gridBagConstraints_label.gridx = 2;
		panel.add(label2, gridBagConstraints_label);
		
		final JLabel immobileFractionLabel = new JLabel();
		immobileFractionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		immobileFractionLabel.setText("Immobile Fraction");
		final GridBagConstraints gridBagConstraints_22 = new GridBagConstraints();
		gridBagConstraints_22.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_22.fill = GridBagConstraints.BOTH;
		gridBagConstraints_22.gridy = 5;
		gridBagConstraints_22.gridx = 0;
		panel.add(immobileFractionLabel, gridBagConstraints_22);

		immobileFractionValueJLabel = new JLabel();
//		immobileFractionValueJLabel.setText("ImmobileFrac");
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_10.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_10.gridy = 5;
		gridBagConstraints_10.gridx = 1;
		panel.add(immobileFractionValueJLabel, gridBagConstraints_10);

		final JLabel label_1 = new JLabel();
		final GridBagConstraints gridBagConstraints_33 = new GridBagConstraints();
		gridBagConstraints_33.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_33.gridy = 5;
		gridBagConstraints_33.gridx = 2;
		panel.add(label_1, gridBagConstraints_33);

		final JLabel monitorBleachRateLabel = new JLabel();
		monitorBleachRateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		monitorBleachRateLabel.setText("Monitor Bleach Rate");
		final GridBagConstraints gridBagConstraints_23 = new GridBagConstraints();
		gridBagConstraints_23.ipadx = 8;
		gridBagConstraints_23.fill = GridBagConstraints.BOTH;
		gridBagConstraints_23.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_23.gridy = 6;
		gridBagConstraints_23.gridx = 0;
		panel.add(monitorBleachRateLabel, gridBagConstraints_23);

		monitorBleachRateTextField = new JTextField();
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_15.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_15.gridy = 6;
		gridBagConstraints_15.gridx = 1;
		panel.add(monitorBleachRateTextField, gridBagConstraints_15);

		final JLabel perSecLabel = new JLabel();
		perSecLabel.setText("1/s");
		final GridBagConstraints gridBagConstraints_34 = new GridBagConstraints();
		gridBagConstraints_34.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_34.gridy = 6;
		gridBagConstraints_34.gridx = 2;
		panel.add(perSecLabel, gridBagConstraints_34);


		final JLabel startIndexRecoveryLabel = new JLabel();
		startIndexRecoveryLabel.setHorizontalAlignment(SwingConstants.CENTER);
		startIndexRecoveryLabel.setText("Start Time Recovery");
		final GridBagConstraints gridBagConstraints_41 = new GridBagConstraints();
		gridBagConstraints_41.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_41.fill = GridBagConstraints.BOTH;
		gridBagConstraints_41.gridy = 7;
		gridBagConstraints_41.gridx = 0;
		panel.add(startIndexRecoveryLabel, gridBagConstraints_41);

		frapDataTimesComboBox = new JComboBox();
		final GridBagConstraints gridBagConstraints_42 = new GridBagConstraints();
		gridBagConstraints_42.fill = GridBagConstraints.BOTH;
		gridBagConstraints_42.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints_42.gridy = 7;
		gridBagConstraints_42.gridx = 1;
		panel.add(frapDataTimesComboBox, gridBagConstraints_42);

		final JLabel sLabel = new JLabel();
		sLabel.setText("s");
		final GridBagConstraints gridBagConstraints_43 = new GridBagConstraints();
		gridBagConstraints_43.gridy = 7;
		gridBagConstraints_43.gridx = 2;
		panel.add(sLabel, gridBagConstraints_43);
		
	}

	public void changeCoreFRAPModelParameters(String diffusionRateString,String MobileFractionString,String monitorBeachRateString, String secondRateString, String secondFractionString)
	{
		diffusionRateTextField.setText(NumberUtils.formatNumber(Double.parseDouble(diffusionRateString), 5));
		monitorBleachRateTextField.setText(NumberUtils.formatNumber(Double.parseDouble(monitorBeachRateString), 5));
		mobileFractionTextField.setText(NumberUtils.formatNumber(Double.parseDouble(MobileFractionString), 5));
		if(secondRateString != null)
		{
			secondDiffRateTextField.setText(NumberUtils.formatNumber(Double.parseDouble(secondRateString), 5));
		}
		else
		{
			secondDiffRateTextField.setText("");
		}
		if(secondFractionString != null)
		{
			secondMobileFracTextField.setText(NumberUtils.formatNumber(Double.parseDouble(secondFractionString), 5));
		}
		else
		{
			secondMobileFracTextField.setText("");
		}
	}
	public FrapChangeInfo createCompleteFRAPChangeInfo(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo,
			boolean bCellROISame,boolean bBleachROISame,boolean bBackgroundROISame,boolean bROISameSize){
		return new FrapChangeInfo(
				!bCellROISame || !bBleachROISame || !bBackgroundROISame,
				!bROISameSize,
				isUserDiffusionRateChanged(savedFrapModelInfo),
				getUserDiffusionRateString(),
				isUserMonitorBleachRateChanged(savedFrapModelInfo),
				getUserMonitorBleachRateString(),
				isUserMobileFractionChanged(savedFrapModelInfo),
				getUserMobileFractionString(),
				isUserSecondRateChanged(savedFrapModelInfo),
				getUserSecondRateString(),
				isUserSecondFractionChanged(savedFrapModelInfo),
				getUserSecondFractionString(),
				isUserStartIndexForRecoveryChanged(savedFrapModelInfo),
				getUserStartIndexForRecoveryString());

	}
	public void initializeSavedFrapModelInfo(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo,double[] frapDataTimeStamps){

		diffusionRateTextField.setText((savedFrapModelInfo == null
				?""
				:(savedFrapModelInfo.lastBaseDiffusionrate == null
					?""
					:savedFrapModelInfo.lastBaseDiffusionrate.toString())));
		monitorBleachRateTextField.setText((savedFrapModelInfo == null
			?""
			:(savedFrapModelInfo.lastBleachWhileMonitoringRate == null
				?""
				:savedFrapModelInfo.lastBleachWhileMonitoringRate.toString())));
		mobileFractionTextField.setText((savedFrapModelInfo == null
				?""
				:(savedFrapModelInfo.lastMobileFraction == null
					?""
					:savedFrapModelInfo.lastMobileFraction.toString())));
		updateImmobileFractionModelText();
		secondDiffRateTextField.setText((savedFrapModelInfo == null
				?""
				:(savedFrapModelInfo.lastSecondRate == null
					?""
					:savedFrapModelInfo.lastSecondRate.toString())));
		secondMobileFracTextField.setText((savedFrapModelInfo == null
				?""
				:(savedFrapModelInfo.lastSecondFraction == null
					?""
					:savedFrapModelInfo.lastSecondFraction.toString())));
		
		frapDataTimesComboBox.removeAllItems();
		for (int i = 0; i < frapDataTimeStamps.length; i++) {
			frapDataTimesComboBox.insertItemAt(FRAPStudyPanel.convertDoubletoString(frapDataTimeStamps[i]), i);
		}
		frapDataTimesComboBox.setSelectedIndex(0);
		if(savedFrapModelInfo != null && savedFrapModelInfo.startingIndexForRecovery != null){
			frapDataTimesComboBox.setSelectedIndex(new Integer(savedFrapModelInfo.startingIndexForRecovery));
		}
		repaint();

	}
	private void updateImmobileFractionModelText(){
		double fastMobileFraction = -1; //if slow mobile fraction is nothing, we'll update immobile according to primary mobile fraction 
		try{
			fastMobileFraction = Double.parseDouble(mobileFractionTextField.getText());
			double slowMobileFraction = Double.parseDouble(secondMobileFracTextField.getText());
			if((fastMobileFraction + slowMobileFraction )<= 1.0){
				double immobileFractionIntermediate = 1.0 - (fastMobileFraction + slowMobileFraction );
				immobileFractionValueJLabel.setText(""+Double.parseDouble(NumberUtils.formatNumber(immobileFractionIntermediate, 5)));
			}else{
				immobileFractionValueJLabel.setText("");
			}
		}catch(Exception e2){
			if((fastMobileFraction != -1) && (fastMobileFraction <= 1.0))
			{
				double immobileFractionIntermediate = 1.0 - fastMobileFraction;
				immobileFractionValueJLabel.setText(""+Double.parseDouble(NumberUtils.formatNumber(immobileFractionIntermediate, 5)));
			}
			else
			{
				immobileFractionValueJLabel.setText("");
			}
		}
	}
	
	public void refreshFRAPModelParameterEstimates(FRAPData frapData) throws Exception {
		estimationPanel.refreshFRAPModelParameterEstimates(frapData);
	}

	public void insertFRAPModelParametersIntoFRAPStudy(FRAPStudy frapStudy) throws Exception{
		if(frapStudy != null){
			frapStudy.setFrapModelParameters(null);
			try{
				String diffusionRateText = getUserDiffusionRateString();
				if(diffusionRateText != null && diffusionRateText.length()>0){
					//check validity
					double diffusionRateDouble = Double.parseDouble(diffusionRateText);
					if(diffusionRateDouble < 0){
						throw new Exception("'"+DIFFUSION_RATE_DESCRIPTION+"' must be >= 0.0");
					}

				}
			}catch(NumberFormatException e){
				throw new Exception("Error parsing '"+DIFFUSION_RATE_DESCRIPTION+"', "+e.getMessage());
			}
			try{
				String mobileFractionText = getUserMobileFractionString();
				if(mobileFractionText != null && mobileFractionText.length()>0){
					//check validity
					double mobileFractionDouble = Double.parseDouble(mobileFractionText);
					if(mobileFractionDouble < 0 || mobileFractionDouble > 1.0){
						throw new Exception("'"+MOBILE_FRACTION_DESCRIPTION+"' must be between 0.0 and 1.0");
					}

				}
			}catch(NumberFormatException e){
				throw new Exception("Error parsing '"+MOBILE_FRACTION_DESCRIPTION+"', "+e.getMessage());
			}
			try{
				String secondRateText = getUserSecondRateString();
				if(secondRateText != null && secondRateText.length()>0){
					//check validity
					double secondRateDouble = Double.parseDouble(secondRateText);
					if(secondRateDouble < 0){
						throw new Exception("'"+SEC_DIFFUSION_RATE_DESCRIPTION+"' must be >= 0.0");
					}

				}
			}catch(NumberFormatException e){
				throw new Exception("Error parsing '"+SEC_DIFFUSION_RATE_DESCRIPTION+"', "+e.getMessage());
			}
			try{
				String secondFractionText = getUserSecondFractionString();
				if(secondFractionText != null && secondFractionText.length()>0){
					//check validity
					double secondFractionDouble = Double.parseDouble(secondFractionText);
					if(secondFractionDouble < 0){
						throw new Exception("'"+SEC_MOBILE_FRACTION_DESCRIPTION+"' must be between 0.0 and 1.0");
					}

				}
			}catch(NumberFormatException e){
				throw new Exception("Error parsing '"+SEC_MOBILE_FRACTION_DESCRIPTION+"', "+e.getMessage());
			}
			
			try{
				String monitorBleachRateText =getUserMonitorBleachRateString();
				if(monitorBleachRateText != null && monitorBleachRateText.length()>0){
					//check validity
					double monitorBleadchRateDouble = Double.parseDouble(monitorBleachRateText);
					if(monitorBleadchRateDouble < 0){
						throw new Exception("'"+MONITOR_BLEACH_RATE_DESCRIPTION+"' must be >= 0.0");
					}
				}
			}catch(NumberFormatException e){
				throw new Exception("Error parsing '"+MONITOR_BLEACH_RATE_DESCRIPTION+"', "+e.getMessage());
			}
			
			//secondary rate and mobile fraction can be both null or both not null, otherwise we don't allow
			if(( secondDiffRateTextField.getText().equals("") && !secondMobileFracTextField.getText().equals("")) ||
			   (!secondDiffRateTextField.getText().equals("") && secondMobileFracTextField.getText().equals("")))
			{
				throw new Exception("Secondary diffution rate is required, since secondary mobile fraction has been set.\n If secondary diffusion is not desired, please leave the secondary diffusion rate and mobile fraction blank.");
			}
			else if(( !secondDiffRateTextField.getText().equals("") && !secondMobileFracTextField.getText().equals("") && !mobileFractionTextField.getText().equals(""))&&
					(immobileFractionValueJLabel.getText()== null || immobileFractionValueJLabel.getText().equals("")))
			{
				throw new Exception("Immobile fraction is invalid. primay mobile fracton + secondary mobile fraction + immobile fraction should not exceed 1.");
			}
			FRAPStudy.FRAPModelParameters frapModelParameters =
					new FRAPStudy.FRAPModelParameters(
							getUserStartIndexForRecoveryString(),
							getUserDiffusionRateString(),
							getUserMonitorBleachRateString(),
							getUserMobileFractionString(),
							getUserSecondRateString(),
							getUserSecondFractionString()
					);
			frapStudy.setFrapModelParameters(frapModelParameters);
		}
	}
	private String getUserDiffusionRateString(){
		return
			(diffusionRateTextField.getText() == null || diffusionRateTextField.getText().length() == 0
				?null
				:diffusionRateTextField.getText());
	}
	private boolean isUserDiffusionRateChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.lastBaseDiffusionrate), getUserDiffusionRateString());
	}
	
	private String getUserMonitorBleachRateString(){
		return
			(monitorBleachRateTextField.getText() == null || monitorBleachRateTextField.getText().length() == 0
				?null
				:monitorBleachRateTextField.getText());
	}
	private boolean isUserMonitorBleachRateChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.lastBleachWhileMonitoringRate), getUserMonitorBleachRateString());
	}

	private String getUserMobileFractionString(){
		return
			(mobileFractionTextField.getText() == null || mobileFractionTextField.getText().length() == 0
				?null
				:mobileFractionTextField.getText());
	}
	private boolean isUserMobileFractionChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.lastMobileFraction), getUserMobileFractionString());
	}

	private String getUserSecondRateString(){
		return
			(secondDiffRateTextField.getText() == null || secondDiffRateTextField.getText().length() == 0
				?null
				:secondDiffRateTextField.getText());
	}
	private boolean isUserSecondRateChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.lastSecondRate), getUserSecondRateString());
	}

	private String getUserSecondFractionString()
	{
		if(secondMobileFracTextField.getText() == null || secondMobileFracTextField.getText().length() == 0)
		{
			return null;
		}
		else
		{
			return secondMobileFracTextField.getText();
		}
		
	}
	private boolean isUserSecondFractionChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.lastSecondFraction), getUserSecondFractionString());
	}
	
	private String getUserStartIndexForRecoveryString(){
		if(frapDataTimesComboBox.getItemCount() == 0){
			return null;
		}
		return frapDataTimesComboBox.getSelectedIndex()+"";
	}
	private boolean isUserStartIndexForRecoveryChanged(FRAPStudyPanel.SavedFrapModelInfo savedFrapModelInfo){
		return !Compare.isEqualOrNull((savedFrapModelInfo==null?null:savedFrapModelInfo.startingIndexForRecovery),getUserStartIndexForRecoveryString());
	}
}
