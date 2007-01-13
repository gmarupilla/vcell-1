package cbit.vcell.opt.solvers;
import cbit.vcell.opt.OptimizationSpec;
import cbit.vcell.opt.OptimizationResultSet;
import cbit.vcell.opt.OptimizationException;
import cbit.vcell.opt.OptimizationSolverSpec;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.io.*;

import org.vcell.expression.ExpressionException;

/**
 * Insert the type's description here.
 * Creation date: (3/16/00 3:07:48 PM)
 * @author: 
 */
public class LocalOptimizationService implements OptimizationService {
	OptimizationSolver optSolver = null;	

/**
 * OptimizationServiceImpl constructor comment.
 */
public LocalOptimizationService() {
	super();
}


/**
 * solve method comment.
 */
public OptimizationResultSet solve(OptimizationSpec optSpec, OptimizationSolverSpec optSolverSpec, OptSolverCallbacks optSolverCallbacks) throws OptimizationException {
	if (optSolverSpec.getSolverType().equals(OptimizationSolverSpec.SOLVERTYPE_POWELL)){
		optSolver = new PowellOptimizationSolver();
//	}else if (optSolverSpec.getSolverType().equals(OptimizationSolverSpec.SOLVERTYPE_CONJUGATE_GRADIENT)){
//		optSolver = new ConjugateGradientOptimizationSolver();
	}else if (optSolverSpec.getSolverType().equals(OptimizationSolverSpec.SOLVERTYPE_CFSQP) 
//				|| optSolverSpec.getSolverType().equals(OptimizationSolverSpec.SOLVERTYPE_SIMULTANEOUS)
	) {
		optSolver = new NativeOptSolver();
	}else {
		throw new RuntimeException("unsupported solver type '"+optSolverSpec.getSolverType()+"'");
	}
	cbit.vcell.opt.OptimizationResultSet optResultSet = null;
	try {
		optResultSet = optSolver.solve(optSpec,optSolverSpec,optSolverCallbacks);
	}catch (IOException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}catch (ExpressionException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}catch(OptimizationException e) {
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}finally{
		optSolver = null;
	}
	return optResultSet;
}
}