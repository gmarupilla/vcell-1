/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.modelopt;
import org.vcell.optimization.CopasiOptSolverCallbacks;
import org.vcell.util.Compare;
import org.vcell.util.Issue;

import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.MathSymbolMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.math.InconsistentDomainException;
import cbit.vcell.math.MathException;
import cbit.vcell.opt.OptimizationResultSet;
import cbit.vcell.opt.OptimizationSolverSpec;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.ode.ODESolverResultSet;
/**
 * Insert the type's description here.
 * Creation date: (5/2/2006 4:35:50 PM)
 * @author: Jim Schaff
 */
@SuppressWarnings("serial")
public class ParameterEstimationTask extends AnalysisTask {
	private ModelOptimizationSpec fieldModelOptimizationSpec = null;
	private OptimizationSolverSpec fieldOptimizationSolverSpec = null;
	
	private transient ModelOptimizationMapping fieldModelOptimizationMapping = null;
	private transient MathSymbolMapping fieldMathSymbolMapping = null;
	private transient OptimizationResultSet fieldOptimizationResultSet = null;
	private transient CopasiOptSolverCallbacks fieldOptSolverCallbacks = new CopasiOptSolverCallbacks();
	private transient java.lang.String fieldSolverMessageText = new String();
	private SimulationContext simulationContext = null;

/**
 * ParameterEstimationTask constructor comment.
 */
public ParameterEstimationTask(SimulationContext simContext) throws ExpressionException {
	super();
	simulationContext = simContext;
	fieldModelOptimizationSpec = new ModelOptimizationSpec(this);
	fieldModelOptimizationMapping = new ModelOptimizationMapping(fieldModelOptimizationSpec);
	fieldOptimizationSolverSpec = new OptimizationSolverSpec(OptimizationSolverSpec.SOLVERTYPE_CFSQP);
}


/**
 * ParameterEstimationTask constructor comment.
 */
public ParameterEstimationTask(ParameterEstimationTask taskToCopy) throws ExpressionException {
	super();
	fieldModelOptimizationSpec = new ModelOptimizationSpec(taskToCopy.getModelOptimizationSpec());
	fieldModelOptimizationMapping = new ModelOptimizationMapping(fieldModelOptimizationSpec);
	fieldOptimizationSolverSpec = new OptimizationSolverSpec(taskToCopy.getOptimizationSolverSpec());
}


/**
 * Insert the method's description here.
 * Creation date: (5/3/2006 5:12:57 PM)
 * @param message java.lang.String
 */
public void appendSolverMessageText(String message) {
	setSolverMessageText(getSolverMessageText()+message);
}


/**
 * Checks for internal representation of objects, not keys from database
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(org.vcell.util.Matchable obj) {
	if (obj instanceof ParameterEstimationTask){
		ParameterEstimationTask task = (ParameterEstimationTask)obj;

		//
		// only compare non-transient fields.
		//

		if (!getModelOptimizationSpec().compareEqual(task.getModelOptimizationSpec())){
			return false;
		}

		if (!Compare.isEqual(getOptimizationSolverSpec(),task.getOptimizationSolverSpec())){
			return false;
		}
		
		return true;
	}
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (5/2/2006 11:04:39 PM)
 * @param issueList java.util.Vector
 */
public void gatherIssues(java.util.List<Issue> issueList) {
	getModelOptimizationSpec().gatherIssues(issueList);
	if (getModelOptimizationMapping().getOptimizationSpec()!=null){
		getModelOptimizationMapping().getOptimizationSpec().gatherIssues(issueList);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/8/2006 6:44:13 PM)
 * @return java.lang.String
 */
public java.lang.String getAnalysisTypeDisplayName() {
	return "Parameter Estimation";
}


/**
 * Insert the method's description here.
 * Creation date: (5/12/2006 11:20:12 AM)
 * @return cbit.vcell.mapping.MathSymbolMapping
 */
public cbit.vcell.mapping.MathSymbolMapping getMathSymbolMapping() {
	return fieldMathSymbolMapping;
}


/**
 * Gets the modelOptimizationMapping property (cbit.vcell.modelopt.ModelOptimizationMapping) value.
 * @return The modelOptimizationMapping property value.
 * @see #setModelOptimizationMapping
 */
public ModelOptimizationMapping getModelOptimizationMapping() {
	return fieldModelOptimizationMapping;
}


/**
 * Gets the modelOptimizationSpec property (cbit.vcell.modelopt.ModelOptimizationSpec) value.
 * @return The modelOptimizationSpec property value.
 * @see #setModelOptimizationSpec
 */
public ModelOptimizationSpec getModelOptimizationSpec() {
	return fieldModelOptimizationSpec;
}


/**
 * Insert the method's description here.
 * Creation date: (5/2/2006 11:10:40 PM)
 * @return cbit.vcell.solver.ode.ODESolverResultSet
 * @throws InconsistentDomainException 
 */
public ODESolverResultSet getOdeSolverResultSet() throws ExpressionException, InconsistentDomainException {
	return ModelOptimizationMapping.getOdeSolverResultSet(getModelOptimizationMapping().getOptimizationSpec(),getOptimizationResultSet());
}


/**
 * Gets the optimizationResultSet property (cbit.vcell.opt.OptimizationResultSet) value.
 * @return The optimizationResultSet property value.
 * @see #setOptimizationResultSet
 */
public cbit.vcell.opt.OptimizationResultSet getOptimizationResultSet() {
	return fieldOptimizationResultSet;
}


/**
 * Gets the optimizationSolverSpec property (cbit.vcell.opt.OptimizationSolverSpec) value.
 * @return The optimizationSolverSpec property value.
 * @see #setOptimizationSolverSpec
 */
public cbit.vcell.opt.OptimizationSolverSpec getOptimizationSolverSpec() {
	return fieldOptimizationSolverSpec;
}


/**
 * Gets the optSolverCallbacks property (cbit.vcell.opt.solvers.OptSolverCallbacks) value.
 * @return The optSolverCallbacks property value.
 * @see #setOptSolverCallbacks
 */
public CopasiOptSolverCallbacks getOptSolverCallbacks() {
	return fieldOptSolverCallbacks;
}


/**
 * Gets the solverMessageText property (java.lang.String) value.
 * @return The solverMessageText property value.
 * @see #setSolverMessageText
 */
public java.lang.String getSolverMessageText() {
	return fieldSolverMessageText;
}


/**
 * Insert the method's description here.
 * Creation date: (5/2/2006 5:00:50 PM)
 */
public void refreshDependencies() {
	getModelOptimizationSpec().refreshDependencies();
	if (getModelOptimizationMapping() != null) {
		getModelOptimizationMapping().refreshDependencies();
	}
}


/**
 * Insert the method's description here.
 * Creation date: (5/2/2006 11:01:21 PM)
 */
public void refreshMappings() throws MappingException, MathException {
	if (fieldModelOptimizationSpec.getReferenceData()==null){
		throw new RuntimeException("reference data not specified");
	}
	ReferenceDataMappingSpec[] refDataMappingSpecs = fieldModelOptimizationSpec.getReferenceDataMappingSpecs();
	for (int i = 0; i < refDataMappingSpecs.length; i++){
		if (refDataMappingSpecs[i].getModelObject()==null){
			throw new RuntimeException("In your reference data, '"+refDataMappingSpecs[i].getReferenceDataColumnName()+"' has not been assigned a Model Association. " +
					"Go to \"Reference Data\" to assign model associations to all reference data.");
		}
	}
	fieldMathSymbolMapping = getModelOptimizationMapping().computeOptimizationSpec();
}


/**
 * Sets the optimizationResultSet property (cbit.vcell.opt.OptimizationResultSet) value.
 * @param optimizationResultSet The new value for the property.
 * @see #getOptimizationResultSet
 */
public void setOptimizationResultSet(OptimizationResultSet optimizationResultSet) {
	OptimizationResultSet oldValue = fieldOptimizationResultSet;
	fieldOptimizationResultSet = optimizationResultSet;
	firePropertyChange("optimizationResultSet", oldValue, optimizationResultSet);
}


/**
 * Sets the optimizationSolverSpec property (cbit.vcell.opt.OptimizationSolverSpec) value.
 * @param optimizationSolverSpec The new value for the property.
 * @see #getOptimizationSolverSpec
 */
public void setOptimizationSolverSpec(OptimizationSolverSpec optimizationSolverSpec) {
	OptimizationSolverSpec oldValue = fieldOptimizationSolverSpec;
	fieldOptimizationSolverSpec = optimizationSolverSpec;
	firePropertyChange("optimizationSolverSpec", oldValue, optimizationSolverSpec);
}

/**
 * Sets the solverMessageText property (java.lang.String) value.
 * @param solverMessageText The new value for the property.
 * @see #getSolverMessageText
 */
public void setSolverMessageText(java.lang.String solverMessageText) {
	String oldValue = fieldSolverMessageText;
	fieldSolverMessageText = solverMessageText;
	firePropertyChange("solverMessageText", oldValue, solverMessageText);
}

public Double getCurrentSolution(ParameterMappingSpec parameterMappingSpec) {
	if (getOptimizationResultSet() != null){
		//
		// find mapping from this parameterMappingSpec to the optimization parameter.  Then lookup the opt parameter in the result set.
		//
		for (int i = 0; i < getModelOptimizationMapping().getParameterMappings().length; i++){
			if (getModelOptimizationMapping().getParameterMappings()[i].getModelParameter() == parameterMappingSpec.getModelParameter()){
				ParameterMapping parameterMapping = getModelOptimizationMapping().getParameterMappings()[i];
				String[] parameterNames = getOptimizationResultSet().getOptSolverResultSet().getParameterNames();
				if (parameterNames != null) {
					for (int j = 0; j < parameterNames.length; j++){
						if (parameterNames[j].equals(parameterMapping.getOptParameter().getName())){
							return new Double(getOptimizationResultSet().getOptSolverResultSet().getBestEstimates()[j]);
						}
					}
				}
			}
		}
	}
	return null;
}


public final SimulationContext getSimulationContext() {
	return simulationContext;
}
}
