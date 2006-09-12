package cbit.vcell.solvers;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.util.SessionLog;
import cbit.vcell.server.LocalVCellConnection;
import cbit.vcell.server.VCellConnection;
import cbit.vcell.solver.ode.ODESolver;
import cbit.vcell.solver.SolverListener;
import java.io.*;
import java.util.*;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.SimulationJob;
import cbit.vcell.math.MathDescription;
import cbit.vcell.solver.SolverStatus;
import cbit.vcell.solver.ode.ODESimData;
import cbit.vcell.solver.SolverEvent;
import cbit.vcell.solver.ode.ODESolverResultSet;
import cbit.vcell.solver.Solver;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public class SolverControllerImpl {
	//
	// for inner class only
	//
	private LocalVCellConnection vcConn = null;
	private SolverListener solverListener = null;
	
	private SimulationJob simulationJob = null;
	private SessionLog sessionLog = null;
	private Solver solver = null;
	private File directory = null;
	private Date startDate = null;
	private Date endDate = null;

/**
 * This method was created by a SmartGuide.
 * @param simContext cbit.vcell.math.MathDescription
 * @param platform cbit.vcell.solvers.Platform
 * @param directory java.lang.String
 * @param simIdentifier java.lang.String
 */
public SolverControllerImpl (LocalVCellConnection argVCellConnection, SessionLog sessionLog, SimulationJob argSimulationJob, java.io.File directory) throws cbit.vcell.solver.SolverException {
	this.simulationJob = argSimulationJob;
	this.directory = directory;
	this.sessionLog = sessionLog;
	this.vcConn = argVCellConnection;
	//
	// instantiate an appropriate solver
	//
	this.solver = cbit.vcell.solvers.SolverFactory.createSolver(sessionLog,directory,argSimulationJob);
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Date
 */
public Date getEndDate() {
	return endDate;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.MathDescription
 */
public SimulationJob getSimulationJob() {
	return simulationJob;
}


/**
 * Insert the method's description here.
 * Creation date: (6/26/2001 6:25:18 PM)
 * @return cbit.vcell.solver.Solver
 */
public cbit.vcell.solver.Solver getSolver() {
	return solver;
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Date
 */
public Date getStartDate() {
	return startDate;
}


/**
 * This method was created by a SmartGuide.
 */
public void startSimulationJob() throws SimExecutionException {
	sessionLog.print("SolverControllerImpl.startSimulationJob() ... starting...");
	if (getSolver() != null){
		startDate = new Date();
		getSolver().startSolver();
		endDate = new Date();
	}
	sessionLog.print("SolverControllerImpl.startSimulationJob() ... started");
}


/**
 * This method was created by a SmartGuide.
 */
public void stopSimulationJob() {
	if (getSolver() != null) {
		getSolver().stopSolver();
		endDate = new Date();
	}
}
}