package cbit.vcell.solvers;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;

import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.solver.SimulationMessage;
import cbit.vcell.solver.Solver;
import cbit.vcell.solver.SolverException;
import cbit.vcell.solver.SolverStatus;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/

/**
 * This interface was generated by a SmartGuide.
 * 
 */
public class FVSolverStandalone extends FVSolver implements Solver {
	private boolean bMessaging = true; 
/**
 * This method was created by a SmartGuide.
 * @param mathDesc cbit.vcell.math.MathDescription
 * @param platform cbit.vcell.solvers.Platform
 * @param directory java.lang.String
 * @param simID java.lang.String
 * @param clientProxy cbit.vcell.solvers.ClientProxy
 */
public FVSolverStandalone (SimulationJob argSimulationJob, File dir, SessionLog sessionLog) throws SolverException {
	this(argSimulationJob, dir, sessionLog, true);
}
	
public FVSolverStandalone (SimulationJob argSimulationJob, File dir, SessionLog sessionLog, boolean arg_bMessaging) throws SolverException {
	super(argSimulationJob, dir, sessionLog);
	bMessaging = arg_bMessaging;
}

/**
 * This method was created by a SmartGuide.
 */
protected void initialize() throws SolverException {
	try {
		Simulation sim = simulationJob.getSimulation();
		if (sim.isSerialParameterScan()) {
			//write functions file for all the simulations in the scan
			for (int scan = 0; scan < sim.getScanCount(); scan ++) {
				SimulationJob simJob = new SimulationJob(sim, scan, simulationJob.getFieldDataIdentifierSpecs());
				// ** Dumping the functions of a simulation into a '.functions' file.
				String basename = new File(getSaveDirectory(), simJob.getSimulationJobID()).getPath();
				String functionFileName = basename + FUNCTIONFILE_EXTENSION;
				
				Vector<AnnotatedFunction> funcList = simJob.getSimulationSymbolTable().createAnnotatedFunctionsList();				
				//Try to save existing user defined functions	
				try{
					File existingFunctionFile = new File(functionFileName);
					if (existingFunctionFile.exists()){
						Vector<AnnotatedFunction> oldFuncList = FunctionFileGenerator.readFunctionsFile(existingFunctionFile, simulationJob.getSimulationJobID());
						for(AnnotatedFunction func : oldFuncList){
							if(func.isOldUserDefined()){
								funcList.add(func);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					//ignore
				}
				
				//Try to save existing user defined functions
				FunctionFileGenerator functionFileGenerator = new FunctionFileGenerator(functionFileName, funcList);

				try {
					functionFileGenerator.generateFunctionFile();
				}catch (Exception e){
					e.printStackTrace(System.out);
					throw new RuntimeException("Error creating .function file for "+functionFileGenerator.getBasefileName()+e.getMessage());
				}
			}
			
		} else {
			writeFunctionsFile();
		}
		writeVCGAndResampleFieldData();
	
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_INIT));
		fireSolverStarting(SimulationMessage.MESSAGE_SOLVEREVENT_STARTING_INIT);
	
		setSolverStatus(new SolverStatus(SolverStatus.SOLVER_RUNNING, SimulationMessage.MESSAGE_SOLVER_RUNNING_INPUT_FILE));
			
		File fvinputFile = new File(getSaveDirectory(), cppCoderVCell.getBaseFilename()+".fvinput");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(fvinputFile));
			new FiniteVolumeFileWriter(pw, simulationJob, getResampledGeometry(), getSaveDirectory(), bMessaging).write();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	
		String executableName = PropertyLoader.getRequiredProperty(PropertyLoader.finiteVolumeExecutableProperty);
		setMathExecutable(new MathExecutable(new String[] {executableName, fvinputFile.getAbsolutePath()}));
	} catch (Exception ex) {
		ex.printStackTrace(System.out);
		throw new SolverException(ex.getMessage());
	}
}
}