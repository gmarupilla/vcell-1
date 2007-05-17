package cbit.vcell.solvers;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.util.*;
import java.util.*;
import java.io.*;

import cbit.vcell.solver.*;
import cbit.vcell.field.FieldDataIdentifierSpec;
import cbit.vcell.field.FieldFunctionArguments;
import cbit.vcell.math.*;
import cbit.vcell.messaging.JmsUtils;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class CppClassCoderSimulation extends CppClassCoder {
	private SimulationJob simulationJob = null;
	private String baseDataName = null;

/**
 * VarContextCppCoder constructor comment.
 * @param name java.lang.String
 */
protected CppClassCoderSimulation(CppCoderVCell cppCoderVCell, SimulationJob argSimulationJob, String baseDataName) 
{
	super(cppCoderVCell,"UserSimulation", "Simulation");
	this.simulationJob = argSimulationJob;
	this.baseDataName = baseDataName;
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeConstructor(java.io.PrintWriter out) throws Exception {
	out.println(getClassName()+"::"+getClassName()+"(CartesianMesh *mesh)");
	out.println(": Simulation(mesh)");
	out.println("{");
	out.println("VolumeRegionVariable		*volumeRegionVar;");
	out.println("MembraneRegionVariable		*membraneRegionVar;");
	out.println("VolumeVariable    *volumeVar;");
	out.println("MembraneVariable  *membraneVar;");
	out.println("ContourVariable   *contourVar;");
	out.println("// ImplicitPDESolver *pdeSolver;");
	out.println("PdeSolverDiana    *pdeSolver;");
	out.println("ODESolver         *odeSolver;");
	out.println("SparseLinearSolver    *slSolver;");
	out.println("EqnBuilder        *builder;");
	out.println("SparseMatrixEqnBuilder        *smbuilder;");
	out.println("long sizeX = mesh->getNumVolumeX();");
	out.println("long sizeY = mesh->getNumVolumeY();");
	out.println("long sizeZ = mesh->getNumVolumeZ();");
	out.println("int numSolveRegions;");
	out.println("int *solveRegions;");
	out.println("int numVolumeRegions = mesh->getNumVolumeRegions();");
	out.println("int i;");
	out.println("int regionCount;");
	out.println("");	

	out.println("\tint symmflg = 1;    // define symmflg = 0 (general) or 1 (symmetric)");

	Simulation simulation = simulationJob.getWorkingSim();
	Variable variables[] = simulation.getVariables();
	for (int i=0;i<variables.length;i++){
	  	Variable var = (Variable)variables[i];
	  	String units;
	  	if (var instanceof VolVariable){
	  		units = "uM";
	  		VolVariable volVar = (VolVariable)var;
	  		out.println("   volumeVar = new VolumeVariable(sizeX,sizeY,sizeZ,\""+volVar.getName()+"\",\""+units+"\");");
	  		
	  		//
	  		// need to specify which SubDomains should be solved for
	  		//
	  		Vector<SubDomain> listOfSubDomains = new Vector<SubDomain>();
	  		int totalNumCompartments = 0;
	  		StringBuffer compartmentNames = new StringBuffer();
	  		Enumeration subDomainEnum = simulation.getMathDescription().getSubDomains();
	  		while (subDomainEnum.hasMoreElements()){
		  		SubDomain subDomain = (SubDomain)subDomainEnum.nextElement();
		  		if (subDomain instanceof CompartmentSubDomain){
			  		CompartmentSubDomain compartmentSubDomain = (CompartmentSubDomain)subDomain;
			  		totalNumCompartments++;
			  		if (subDomain.getEquation(var) != null){
				  		listOfSubDomains.add(compartmentSubDomain);
				  		int handle = simulation.getMathDescription().getHandle(compartmentSubDomain);
				  		compartmentNames.append(compartmentSubDomain.getName()+"("+handle+") ");
			  		}
		  		}
	  		}
	  		if (totalNumCompartments == listOfSubDomains.size()){
		  		//
		  		// every compartments has an equation, set numSolveRegions accordingly
		  		//
		  		out.println("    // solving for all regions");
		  		out.println("    numSolveRegions = 0;  // flag specifying to solve for all regions");
		  		out.println("    solveRegions = NULL;");
	  		}else{
		  		//
		  		// only solve for some compartments
		  		//
			  	out.println("   // solving for only regions belonging to ("+compartmentNames.toString()+"), first 'numSolveRegions' elements used");
			  	out.println("   solveRegions = new int[numVolumeRegions];");
		  		
		  		//
		  		//  build list of regions belonging to the required SubDomains
		  		//
				out.println("   regionCount = 0;");
		  		out.println("   for (i = 0; i < numVolumeRegions; i++){");
		  		out.println("      VolumeRegion *volRegion = mesh->getVolumeRegion(i);");
			  	for (int j = 0; j < listOfSubDomains.size(); j++){
					CompartmentSubDomain compartmentSubDomain = (CompartmentSubDomain)listOfSubDomains.elementAt(j);
				  	int handle = simulation.getMathDescription().getHandle(compartmentSubDomain);
					out.println("      if (volRegion->getFeature()->getHandle() == (FeatureHandle)(0xff & "+handle+")){  // test if this region is same as '"+compartmentSubDomain.getName()+"'");
					out.println("          solveRegions[regionCount++] = volRegion->getId();");
					out.println("      }");
				}
				out.println("   }");
		  		out.println("   numSolveRegions = regionCount;");
	  		}
	  		
	  		if (simulation.getMathDescription().isPDE(volVar)){
		  		if (simulation.getMathDescription().hasVelocity(volVar)) { // Convection
					out.println("#ifdef USE_PDESOLVERDIANA");		  			
		  			out.println("\tsymmflg = 0;    // define symmflg = 0 (general) or 1 (symmetric)");
		  			out.println("\tpdeSolver = new PdeSolverDiana(volumeVar,mesh,symmflg,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tbuilder = new EqnBuilderReactionDiffusionConvection(volumeVar,mesh,pdeSolver);");
		  			out.println("\tpdeSolver->setEqnBuilder(builder);");
		  			out.println("\taddSolver(pdeSolver);");
	  			} else {
					out.println("#ifdef USE_PDESOLVERDIANA");		  			
		  			out.println("\tsymmflg = 1;    // define symmflg = 0 (general) or 1 (symmetric)");
		  			out.println("\tpdeSolver = new PdeSolverDiana(volumeVar,mesh,symmflg,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tbuilder = new EqnBuilderReactionDiffusion(volumeVar,mesh,pdeSolver);");	  			
		  			out.println("\tpdeSolver->setEqnBuilder(builder);");
		  			out.println("\taddSolver(pdeSolver);");
	  			}
		  		out.println("#else");
	  			out.println("\tsmbuilder = new SparseVolumeEqnBuilder(volumeVar,mesh," + (simulation.getMathDescription().hasVelocity(volVar) ? "false" : "true") + ", numSolveRegions, solveRegions);");
	  			out.println("\tslSolver = new SparseLinearSolver(volumeVar,smbuilder,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
	  			out.println("\taddSolver(slSolver);");
	  			out.println("#endif");
	  		}else{
	  			out.println("   //odeSolver = new ODESolver(volumeVar,mesh);");
	  			out.println("   odeSolver = new ODESolver(volumeVar,mesh,numSolveRegions,solveRegions);");
	  			out.println("   builder = new EqnBuilderReactionForward(volumeVar,mesh,odeSolver);");
	  			out.println("   odeSolver->setEqnBuilder(builder);");
	  			out.println("   addSolver(odeSolver);");
	  		}		
	  		out.println("   addVariable(volumeVar);");
	  		out.println("");
	  	}else if (var instanceof MemVariable) { // membraneVariable
		  	units = "molecules/squm";
	  		MemVariable memVar = (MemVariable)var;
		  	if (simulation.getMathDescription().isPDE(memVar)) {
		  		out.println("\tmembraneVar = new MembraneVariable(mesh->getNumMembraneElements(),\""+memVar.getName()+"\",\""+units+"\");");
		  		out.println("\tsmbuilder = new MembraneEqnBuilderDiffusion(membraneVar,mesh);");
	  			out.println("\tslSolver = new SparseLinearSolver(membraneVar,smbuilder,"+simulation.hasTimeVaryingDiffusionOrAdvection(memVar)+");");	  			
	  			out.println("\taddSolver(slSolver);");
		  		out.println("\taddVariable(membraneVar);");
		  	} else {		  		
		  		out.println("   // solving for all regions");
		  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
		  		out.println("   solveRegions = NULL;");
		  		out.println("   membraneVar = new MembraneVariable(mesh->getNumMembraneElements(),\""+memVar.getName()+"\",\""+units+"\");");
	  			out.println("   odeSolver = new ODESolver(membraneVar,mesh,numSolveRegions,solveRegions);");
	  			out.println("   builder = new MembraneEqnBuilderForward(membraneVar,mesh,odeSolver);");
	  			out.println("   odeSolver->setEqnBuilder(builder);");
	  			out.println("   addSolver(odeSolver);");
		  		out.println("   addVariable(membraneVar);");
		  	}
	  	}else if (var instanceof FilamentVariable) { // contourVariable
	  		units = "molecules/um";
	  		FilamentVariable filamentVar = (FilamentVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   contourVar = new ContourVariable(mesh->getNumMembraneElements(),\""+filamentVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(contourVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new ContourEqnBuilderForward(contourVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(contourVar);");
	  	}else if (var instanceof VolumeRegionVariable) { // volumeRegionVariable
	  		units = "uM";
	  		VolumeRegionVariable volumeRegionVar = (VolumeRegionVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   volumeRegionVar = new VolumeRegionVariable(mesh->getNumVolumeRegions(),\""+volumeRegionVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(volumeRegionVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new VolumeRegionEqnBuilder(volumeRegionVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(volumeRegionVar);");
	  	}else if (var instanceof MembraneRegionVariable) { // membraneRegionVariable
	  		units = "molecules/um^2";
	  		MembraneRegionVariable membraneRegionVar = (MembraneRegionVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   membraneRegionVar = new MembraneRegionVariable(mesh->getNumMembraneRegions(),\""+membraneRegionVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(membraneRegionVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new MembraneRegionEqnBuilder(membraneRegionVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(membraneRegionVar);");
	  	}	
	}		  	
	out.println("}");
}


/**
 * This method was created by a SmartGuide.
 * @param printWriter java.io.PrintWriter
 */
public void writeDeclaration(java.io.PrintWriter out) {
	out.println("//---------------------------------------------");
	out.println("//  class " + getClassName());
	out.println("//---------------------------------------------");

	out.println("class " + getClassName() + " : public " + getParentClassName());
	out.println("{");
	out.println(" public:");
	out.println("   "+getClassName() + "(CartesianMesh *mesh);");
	out.println("};");
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeGetSimTool(java.io.PrintWriter out) throws Exception {

	Simulation simulation = simulationJob.getWorkingSim();
	SolverTaskDescription taskDesc = simulation.getSolverTaskDescription();
	if (taskDesc==null){
		throw new Exception("task description not defined");
	}	

	out.println("");
	out.println("SimTool *getSimTool()");
	out.println("{");
	out.println("");
	ISize meshSampling = simulation.getMeshSpecification().getSamplingSize();
//	char fs = File.separatorChar;
//	String baseDataName = "SIMULATION" + fs + mathDesc.getName() + fs + "UserData" ;
	StringBuffer newBaseDataName = new StringBuffer();
	for (int i=0;i<baseDataName.length();i++){
		if (baseDataName.charAt(i) == '\\'){
			newBaseDataName.append(baseDataName.charAt(i));
			newBaseDataName.append(baseDataName.charAt(i));
		}else{
			newBaseDataName.append(baseDataName.charAt(i));
		}
	}
	out.println("\tSimulation *sim = NULL;");
	out.println("\tVCellModel *model = NULL;");
	out.println("\tCartesianMesh *mesh = NULL;");
	out.println("\tSimTool *pSimTool = new SimTool(\"Simulate\");");
	out.println("\tint numX = "+meshSampling.getX()+";");
	out.println("\tint numY = "+meshSampling.getY()+";");
	out.println("\tint numZ = "+meshSampling.getZ()+";");
	out.println("\ttheApplication = new App();");
	out.println("\tmodel = new UserVCellModel();");
	out.println("\tassert(model);");
	out.println("\ttheApplication->setModel(model);");
	out.println("\tSimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_STARTING, \"initializing mesh...\"));");
	out.println("\tchar tempString[1024];");
	out.println("\tsprintf(tempString, \"%s%c" + simulationJob.getSimulationJobID() + ".vcg\\0\", outputPath, DIRECTORY_SEPARATOR);");
	out.println("\tmesh = new CartesianMesh(tempString);");
	out.println("\tSimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_STARTING, \"mesh initialized\"));");
	out.println("\tassert(mesh);");
	out.println("\tsim = new UserSimulation(mesh);");
	out.println("\tassert(sim);");
	out.println("\ttheApplication->setSimulation(sim);");
	out.println();
	out.println("\tsim->initSimulation();");
	out.println("\tpSimTool->setSimulation(sim);");
	out.println("\tsprintf(tempString, \"%s%c" + simulationJob.getSimulationJobID() + "\\0\", outputPath, DIRECTORY_SEPARATOR);");
	out.println("\tpSimTool->setBaseFilename(tempString);");
	out.println("\tpSimTool->loadFinal();   // initializes to the latest file if it exists");
	out.println("");
	out.println("\tpSimTool->setTimeStep("+taskDesc.getTimeStep().getDefaultTimeStep()+");");
	out.println("\tpSimTool->setEndTimeSec("+taskDesc.getTimeBounds().getEndingTime()+");");

	if (taskDesc.getOutputTimeSpec().isDefault()){
		out.println("\tpSimTool->setKeepEvery("+((DefaultOutputTimeSpec)taskDesc.getOutputTimeSpec()).getKeepEvery()+");");
	}else{
		throw new RuntimeException("unexpected OutputTime specification type :"+taskDesc.getOutputTimeSpec().getClass().getName());
	}
	//out.println("\tpSimTool->setStoreEnable(TRUE);");
	//out.println("\tpSimTool->setFileCompress(FALSE);");
	
	out.println();
	out.println("\treturn pSimTool;");
	out.println("}");
}


/**
 * This method was created by a SmartGuide.
 * @param printWriter java.io.PrintWriter
 */
public void writeImplementation(java.io.PrintWriter out) throws Exception {
	out.println("//---------------------------------------------");
	out.println("//  main routine");
	out.println("//---------------------------------------------");
	writeMain(out);
	out.println("");
	writeGetSimTool(out);
	out.println("");
	out.println("//---------------------------------------------");
	out.println("//  class " + getClassName());
	out.println("//---------------------------------------------");
	writeConstructor(out);
	out.println("");
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeMain(java.io.PrintWriter out) throws Exception {

	Simulation simulation = simulationJob.getWorkingSim();
	FieldFunctionArguments[] fieldFuncArgs = simulation.getMathDescription().getFieldFunctionArguments();
	//FieldDataIdentifierSpec[] fieldDataIDSs = simulationJob.getFieldDataIdentifierSpecs();
	SolverTaskDescription taskDesc = simulation.getSolverTaskDescription();
	if (taskDesc==null){
		throw new Exception("task description not defined");
	}	
	
	out.println("#include <sys/stat.h>");
	
	out.println("#ifdef WIN32");
	out.println("#define DIRECTORY_SEPARATOR '\\\\'");
	out.println("#else");
	out.println("#define DIRECTORY_SEPARATOR '/'");
	out.println("#endif");
	String parentPath = new File(baseDataName).getParent();
	StringBuffer newParentPath = new StringBuffer();
	for (int i = 0; i < parentPath.length(); i ++){
		if (baseDataName.charAt(i) == '\\'){
			newParentPath.append(baseDataName.charAt(i));
			newParentPath.append(baseDataName.charAt(i));
		}else{
			newParentPath.append(baseDataName.charAt(i));
		}
	}	
	out.println("static char* outputPath = \"" + newParentPath +"\";");
	out.println();

	out.println("#ifndef VCELL_CORBA");
	out.println("//-------------------------------------------");
	out.println("//   BATCH (NON-CORBA) IMPLEMENTATION");
	out.println("//-------------------------------------------");
	out.println("");
	out.println("#ifdef VCELL_MPI");
	out.println("#include <mpi.h>");
	out.println("#endif");
	out.println("");

	if (fieldFuncArgs != null && fieldFuncArgs.length > 0) {
		out.println();
		for (int i = 0; i < fieldFuncArgs.length; i ++) {		
			out.println("double* " + FieldDataIdentifierSpec.getGlobalVariableName_C(fieldFuncArgs[i]) + " = 0;");
		}
		out.println();
		out.println("double* getFieldData(char*, char*, char*);");
		out.println();
	}

	
	out.println("int vcellExit(int returnCode, char* returnMsg) {");
	out.println("\tif (!SimTool::bStopSimulation) {");
	out.println("\t\tif (returnCode != 0) {");
	out.println("\t\t\tSimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_FAILURE, returnMsg));");
	out.println("\t\t}");
	out.println("\t\tSimulationMessaging::getInstVar()->waitUntilFinished();");
	out.println("\t}");

	if (fieldFuncArgs != null && fieldFuncArgs.length > 0) {
		out.println();
		out.println("\tdelete SimulationMessaging::getInstVar();");	
		for (int i = 0; i < fieldFuncArgs.length; i ++) {
			out.println("\tdelete[] " + FieldDataIdentifierSpec.getGlobalVariableName_C(fieldFuncArgs[i]) + ";");
		}
		out.println();
	}

	out.println("\treturn returnCode;");
	out.println("}");
	
	out.println("int main(int argc, char *argv[])");
	out.println("{");
		
	out.println("");
	out.println("#ifdef VCELL_MPI");
	out.println("\tint ierr = MPI_Init(&argc,&argv);");
	out.println("\tassert(ierr == MPI_SUCCESS);");
	out.println("#endif");
	out.println("");

	out.println("\tint returnCode = 0;");
	out.println("\tstring returnMsg;");
	// Fei Changes Begin
	out.println("\ttry {");
	out.println("\t\tjint taskID = -1;");
	out.println("\t\tfor (int i = 1; i < argc; i ++) {");
	out.println("\t\t\tif (!strcmp(argv[i], \"-nz\")) {");
	out.println("\t\t\t\tSimTool::bSimZip = false;");
	out.println("\t\t\t} else if (!strcmp(argv[i], \"-d\")) {");
	out.println("\t\t\t\ti ++;");
	out.println("\t\t\t\toutputPath = argv[i];");
	out.println("\t\t\t} else {");
	out.println("\t\t\t\tfor (int j = 0; j < strlen(argv[i]); j ++) {");
	out.println("\t\t\t\t\tif (argv[i][j] < '0' || argv[i][j] > '9') {");
	out.println("\t\t\t\t\t\tcout << \"Wrong argument : \" << argv[i] << endl;");
	out.println("\t\t\t\t\t\tcout << \"Arguments : [-d output] [-nz] [taskID]\" <<  endl;");
	out.println("\t\t\t\t\t\texit(1);");
	out.println("\t\t\t\t\t}");
	out.println("\t\t\t\t}");	
	out.println("\t\t\t\ttaskID = atoi(argv[i]);");
	out.println("\t\t\t}");
	out.println("\t\t}");
	
	out.println("\t\tstruct stat buf;");	
	out.println("\t\tif (stat(outputPath, &buf)) {");
	out.println("\t\t\tcerr << \"Output directory [\" << outputPath <<\"] doesn't exist\" << endl;");
	out.println("\t\t\texit(1);");
	out.println("\t\t}");
	
	out.println("\t\tif (taskID == -1) { // no messaging");
	out.println("\t\t\tSimulationMessaging::create();");
	out.println("\t\t} else {");
	out.println("\t\t\tchar* broker = \"" + JmsUtils.getJmsUrl() + "\";");
    out.println("\t\t\tchar *smqusername = \"" + JmsUtils.getJmsUserID() + "\";");
    out.println("\t\t\tchar *password = \"" + JmsUtils.getJmsPassword() + "\";");
    out.println("\t\t\tchar *qname = \"" + JmsUtils.getQueueWorkerEvent() + "\";");  
	out.println("\t\t\tchar* tname = \"" + JmsUtils.getTopicServiceControl() + "\";");
	out.println("\t\t\tchar* vcusername = \"" + simulation.getVersion().getOwner().getName() + "\";");
	out.println("\t\t\tjint simKey = " + simulation.getVersion().getVersionKey() + ";");
	out.println("\t\t\tjint jobIndex = " + simulationJob.getJobIndex() + ";");
	out.println("\t\t\tSimulationMessaging::create(broker, smqusername, password, qname, tname, vcusername, simKey, jobIndex, taskID);");
	out.println("\t\t}");
	out.println("\t\tSimulationMessaging::getInstVar()->start(); // start the thread");

	if (fieldFuncArgs != null && fieldFuncArgs.length > 0) {
		out.println();
		for (int i = 0; i < fieldFuncArgs.length; i ++) {
			String fieldName = fieldFuncArgs[i].getFieldName();
			String varName = fieldFuncArgs[i].getVariableName();
			File fieldFile = new File(baseDataName + FieldDataIdentifierSpec.getDefaultFieldDataFileNameForSimulation(fieldFuncArgs[i]));
			out.println("\t\t" + FieldDataIdentifierSpec.getGlobalVariableName_C(fieldFuncArgs[i]) + " = getFieldData(\"" + fieldName + "\",\"" + TokenMangler.getEscapedString_C(fieldFile.toString()) + "\", \"" + varName + "\");");
		}
		out.println();
	}
		
	out.println("\t\tSimTool *pSimTool = getSimTool();");
	if (taskDesc.getTaskType() == SolverTaskDescription.TASK_UNSTEADY){
		out.println("\t\tpSimTool->start();");
	}else{
		out.println("\t\tpSimTool->startSteady("+taskDesc.getErrorTolerance().getAbsoluteErrorTolerance()+","+taskDesc.getTimeBounds().getEndingTime()+");");
	}		

	out.println("\t}catch (const char *exStr){");
	out.println("\t\treturnMsg = \"Exception while running : \";");
	out.println("\t\treturnMsg += exStr;");
	out.println("\t\treturnCode = 1;");
   	out.println("\t}catch (...){");
	out.println("\t\treturnMsg = \"Unknown exception while running ... \";");
	out.println("\t\treturnCode = 1;");
	out.println("\t}");

	out.println("#ifdef VCELL_MPI");
	out.println("\tMPI_Finalize();");
	out.println("#endif");

	out.println("\treturn vcellExit(returnCode, (char*)returnMsg.c_str());");
	out.println("}");
   	
	//out.println("   try {");
	//out.println("          SimTool *pSimTool = getSimTool();");
	//if (taskDesc.getTaskType() == SolverTaskDescription.TASK_UNSTEADY){
		//out.println("      pSimTool->start();");
	//}else{
		//out.println("      pSimTool->startSteady("+taskDesc.getErrorTolerance().getAbsoluteErrorTolerance()+","+taskDesc.getTimeBounds().getEndingTime()+");");
	//}		
	//out.println("      cerr << \"Simulation Complete in Main() ... \" << endl;");
	//out.println("   }catch (char *exStr){");
	//out.println("      cerr << \"Exception while running ... \" << exStr << endl;");
	//out.println("      exit(-1);");
	//out.println("   }catch (...){");
	//out.println("      cerr << \"Unknown Exception while running ... \" << endl;");
	//out.println("      exit(-1);");
	//out.println("   }");
	//out.println("");
	
	//out.println("#ifdef VCELL_MPI");
	//out.println("   MPI_Finalize();");
	//out.println("#endif");
	
	//out.println("   exit(0);");
	//out.println("}");
	
	// Fei Changes End
	
	out.println("#else  // end not VCELL_CORBA");
	out.println("//-------------------------------------------");
	out.println("//   CORBA IMPLEMENTATION");
	out.println("//-------------------------------------------");
	out.println("#include <OB/CORBA.h>");
	out.println("#include <OB/Util.h>");
	out.println("");
	out.println("#include <Simulation_impl.h>");
	out.println("");
	out.println("#include <stdlib.h>");
	out.println("#include <errno.h>");
	out.println("");
	out.println("#ifdef HAVE_FSTREAM");
	out.println("#   include <fstream>");
	out.println("#else");
	out.println("#   include <fstream.h>");
	out.println("#endif");
	out.println("");
	out.println("int main(int argc, char* argv[], char*[])");
	out.println("{");
	out.println("    try");
	out.println("    {");
	out.println("	//");
	out.println("	// Create ORB and BOA");
	out.println("	//");
	out.println("	CORBA_ORB_var orb = CORBA_ORB_init(argc, argv);");
	out.println("	CORBA_BOA_var boa = orb -> BOA_init(argc, argv);");
	out.println("	");
	out.println("	orb->conc_model(CORBA_ORB::ConcModelThreaded);");
	out.println("	boa->conc_model(CORBA_BOA::ConcModelThreadPool);");
	out.println("	boa->conc_model_thread_pool(4);");
	out.println("	");
	out.println("	//");
	out.println("	// Create implementation object");
	out.println("	//");
	out.println("	mathService_Simulation_var p = new Simulation_impl(getSimTool());");
	out.println("	");
	out.println("	//");
	out.println("	// Save reference");
	out.println("	//");
	out.println("	CORBA_String_var s = orb -> object_to_string(p);");
	out.println("	");
	out.println("	const char* refFile = \"Simulation.ref\";");
	out.println("	ofstream out(refFile);");
	out.println("	if(out.fail())");
	out.println("	{");
	out.println("	    cerr << argv[0] << \": can't open `\" << refFile << \"': \"");
	out.println("		 << strerror(errno) << endl;");
	out.println("	    return 1;");
	out.println("	}");
	out.println("	");
	out.println("	out << s << endl;");
	out.println("	out.close();");
	out.println("	");
	out.println("	//");
	out.println("	// Run implementation");
	out.println("	//");
	out.println("	boa -> impl_is_ready(CORBA_ImplementationDef::_nil());");
	out.println("    }");
	out.println("    catch(CORBA_SystemException& ex)");
	out.println("    {");
	out.println("	OBPrintException(ex);");
	out.println("	return 1;");
	out.println("    }");
	out.println("");
	out.println("    return 0;");
	out.println("}");
	out.println("");
	out.println("#endif // end VCELL_CORBA");
	out.println("");
}
}