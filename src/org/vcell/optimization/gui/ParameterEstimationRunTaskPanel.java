/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.optimization.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.vcell.optimization.CopasiOptSolverCallbacks;
import org.vcell.optimization.CopasiOptimizationSolver;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptProgressType;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationMethod;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationMethodType;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationParameter;
import org.vcell.util.BeanUtils;
import org.vcell.util.Issue;
import org.vcell.util.ProgressDialogListener;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.GuiUtils;
import org.vcell.util.gui.HyperLinkLabel;
import org.vcell.util.gui.ProgressDialog;
import org.vcell.util.gui.ScrollTable;

import cbit.vcell.client.ClientRequestManager;
import cbit.vcell.client.GuiConstants;
import cbit.vcell.client.VCellLookAndFeel;
import cbit.vcell.client.desktop.biomodel.VCellSortTableModel;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.math.InconsistentDomainException;
import cbit.vcell.math.Variable;
import cbit.vcell.model.Parameter;
import cbit.vcell.modelopt.ModelOptimizationSpec;
import cbit.vcell.modelopt.ParameterEstimationTask;
import cbit.vcell.modelopt.ReferenceDataMappingSpec;
import cbit.vcell.modelopt.gui.DataSource;
import cbit.vcell.modelopt.gui.MultisourcePlotPane;
import cbit.vcell.opt.OdeObjectiveFunction;
import cbit.vcell.opt.OptimizationException;
import cbit.vcell.opt.OptimizationResultSet;
import cbit.vcell.opt.OptimizationSolverSpec;
import cbit.vcell.opt.OptimizationSpec;
import cbit.vcell.opt.ReferenceData;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.ode.ODESolverResultSet;

@SuppressWarnings("serial")
public class ParameterEstimationRunTaskPanel extends JPanel {

	private JTextArea optimizeResultsTextArea = null;
	private JComboBox optimizationMethodComboBox = null;
	private JButton plotButton = null;
	private JButton saveSolutionAsNewSimButton = null;
	private JPanel solutionPanel = null;
	private JButton solveButton = null;
	private JButton helpButton = null;
	private JPanel solverPanel = null;
	private ParameterEstimationTask parameterEstimationTask = null;
	private JCheckBox computeProfileDistributionsCheckBox = null;
	private JButton evaluateConfidenceIntervalButton = null;
	
	private ScrollTable optimizationMethodParameterTable = null;
	private OptimizationMethodParameterTableModel optimizationMethodParameterTableModel;
	private InternalEventHandler eventHandler = new InternalEventHandler();
	private JComboBox numberOfRunComboBox = null;
	private JLabel numberOfRunLabel = new JLabel("Number of Runs: ");
	
	private RunStatusProgressDialog runStatusDialog;
	private ScrollTable optimizationSolutionParameterTable = null;
	private OptimizationSolutionParameterTableModel optimizationSolutionParameterTableModel;

	private ScrollTable optimizationTaskSummaryTable = null;
	private OptimizationTaskSummaryTableModel optimizationTaskSummaryTableModel;

	private CopasiOptimizationMethodsHelpPanel copasiHelpPanel = null;
	private HyperLinkLabel copasiLinkLabel = new HyperLinkLabel("See COPASI for additional parameter estimation options and model analysis features", eventHandler, 0);
	
	private class RunStatusProgressDialog extends ProgressDialog {
		private JTextField numEvaluationsTextField = null;
		private JTextField objectiveFunctionValueTextField = null;
		private JTextField currentValueTextField = null;
		private JLabel progressLabel;
		private JLabel numRunsLabel;
		RunStatusProgressDialog(Frame owner) {
			super(owner);
			initialize();
		}	
		
		private void initialize() {
			setTitle("Running Parameter Estimation");
			setResizable(true);
			setModal(true);
			
			JPanel runStatusPanel = new JPanel();
			runStatusPanel.setLayout(new GridBagLayout());
			
			objectiveFunctionValueTextField = new javax.swing.JTextField(20);
			objectiveFunctionValueTextField.setEditable(false);
			
			numEvaluationsTextField = new javax.swing.JTextField();
			numEvaluationsTextField.setEditable(false);

			currentValueTextField = new javax.swing.JTextField();
			currentValueTextField.setEditable(false);
			
			int gridy = 0;	//number of runs label
			numRunsLabel = new JLabel("");
			java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; gbc.gridy = gridy;
			gbc.insets = new java.awt.Insets(10, 4, 4, 4);
			gbc.anchor = GridBagConstraints.LINE_END;
			runStatusPanel.add(numRunsLabel, gbc);
			
			gridy++;
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; gbc.gridy = gridy;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			gbc.anchor = GridBagConstraints.LINE_END;
			runStatusPanel.add(new javax.swing.JLabel("Best Value: "), gbc);
	
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 1; 
			gbc.gridy = gridy;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			runStatusPanel.add(objectiveFunctionValueTextField, gbc);
	
			gridy ++;
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			gbc.anchor = GridBagConstraints.LINE_END;
			runStatusPanel.add(new javax.swing.JLabel("No. of Evaluations: "), gbc);
	
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 1; 
			gbc.gridy = gridy;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			runStatusPanel.add(numEvaluationsTextField, gbc);
			
			gridy ++;
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			gbc.anchor = GridBagConstraints.LINE_END;
			progressLabel = new javax.swing.JLabel("Progress: ");
			runStatusPanel.add(progressLabel, gbc);
			
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 1; 
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			runStatusPanel.add(getProgressBar(), gbc);
				
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 1; 
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			runStatusPanel.add(currentValueTextField, gbc);
			
			gridy ++;
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy;
			gbc.gridwidth = 2;
			gbc.insets = new java.awt.Insets(4, 4, 10, 4);
			gbc.weightx = 1.0;
			getCancelButton().setText("Stop");
			runStatusPanel.add(getCancelButton(), gbc);

			add(runStatusPanel, BorderLayout.CENTER);
			pack();
			BeanUtils.centerOnComponent(this, getParent());
			
			addProgressDialogListener(new ProgressDialogListener() {
				public void cancelButton_actionPerformed(EventObject newEvent) {
					stop();
				}
			});
		}

		@Override
		public void setCancelButtonVisible(boolean bVisible) {
		}

		@Override
		public void setMessage(String message) {
		}
		@Override
		public void setProgress(int progress) {
			super.setProgress(progress);
		}
		public void setCurrentValue(double value) {
			currentValueTextField.setText("" + value);
		}
		public void setNumEvaluations(int num) {
			numEvaluationsTextField.setText("" + num);
		}
		public void setObjectFunctionValue(double d) {
			objectiveFunctionValueTextField.setText("" + d);
		}	
		public void setNumRunMessage(int currentRun, int totalRun)
		{
			if(totalRun == 1)
			{
				numRunsLabel.setVisible(false);
			}
			else
			{
				numRunsLabel.setVisible(true);
				numRunsLabel.setText("Running No." + (currentRun+1)  + " of total " + totalRun + " runs.");
			}
		}
		public void showProgressBar(CopasiOptimizationMethod com) {
			progressBar.setValue(0);
			progressLabel.setText(com.getType().getProgressLabel() + ": ");
			currentValueTextField.setText(null);
			switch(com.getType().getProgressType()) {
			case NO_Progress:
				progressLabel.setVisible(false);
				progressBar.setVisible(false);
				currentValueTextField.setVisible(false);
				break;
			case Progress:
				progressLabel.setVisible(true);
				progressBar.setVisible(true);
				currentValueTextField.setVisible(false);
				break;
			case Current_Value:
				progressLabel.setVisible(true);
				progressBar.setVisible(false);
				currentValueTextField.setVisible(true);
				break;
			}
		}
	}

	public static class OptimizationSolutionParameter {
		private String name;
		private Double modelValue;
		private double value;
		private OptimizationSolutionParameter(String name, Double modelValue, double value) {
			super();
			this.name = name;
			this.modelValue = modelValue;
			this.value = value;
		}
		public final String getName() {
			return name;
		}
		public final Double getModelValue() {
			return (modelValue == null)? null:modelValue;
		}
		public final double getValue() {
			return value;
		}
	}
	
	public static class OptimizationTaskSummary {
		private String name;
		private double value;
		private OptimizationTaskSummary(String name, double value) {
			super();
			this.name = name;
			this.value = value;
		}
		public final String getName() {
			return name;
		}
		public final double getValue() {
			return value;
		}
	}
	
	private static class OptimizationTaskSummaryTableModel extends VCellSortTableModel<OptimizationTaskSummary> {
		static final int COLUMN_Result = 0;
		static final int COLUMN_Value = 1;
		
		public OptimizationTaskSummaryTableModel(ScrollTable table) {
			super(table, new String[] {"Result", "Value"});
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			OptimizationTaskSummary ots = getValueAt(rowIndex);
			return columnIndex == COLUMN_Result ? ots.getName() : ots.getValue();
		}

		@Override
		protected Comparator<OptimizationTaskSummary> getComparator(int col, boolean ascending) {
			return null;
		}
		
		public void refresh(OptimizationResultSet optimizationResultSet) {
			ArrayList<OptimizationTaskSummary> list = new ArrayList<OptimizationTaskSummary>();
			double objVal = optimizationResultSet.getOptSolverResultSet().getLeastObjectiveFunctionValue();
			long numEval = optimizationResultSet.getOptSolverResultSet().getObjFunctionEvaluations();
			list.add(new OptimizationTaskSummary("Objective Value", objVal));
			list.add(new OptimizationTaskSummary("No. of Evaluations", numEval));
			setData(list);
		}

		@Override
		public boolean isSortable(int col) {
			return false;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == COLUMN_Value ? Double.class : String.class;
		}
	}
	
	private static class OptimizationSolutionParameterTableModel extends VCellSortTableModel<OptimizationSolutionParameter> {
		static final int COLUMN_Parameter = 0;
		static final int COLUMN_ModelValue = 1;
		static final int COLUMN_Value = 2;
				
		public OptimizationSolutionParameterTableModel(ScrollTable table) {
			super(table, new String[] {"Parameter", "Model Value", "Best Estimate"});
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			OptimizationSolutionParameter cop = getValueAt(rowIndex);
			if(columnIndex == COLUMN_Parameter)
			{
				return cop.getName();
			}
			else if(columnIndex == COLUMN_ModelValue)
			{
				return (cop.getModelValue() == null)?null:cop.getModelValue();
			}
			else if(columnIndex == COLUMN_Value)
			{
				return cop.getValue();
			}
			return null;
		}

		@Override
		protected Comparator<OptimizationSolutionParameter> getComparator(int col, boolean ascending) {
			return null;
		}
		
		@Override
		public boolean isSortable(int col) {
			return false;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == COLUMN_Parameter ? String.class : Double.class;
		}
		
		public void refresh(OptimizationResultSet optimizationResultSet, ParameterEstimationTask paramEstTask) {
			ArrayList<OptimizationSolutionParameter> list = new ArrayList<OptimizationSolutionParameter>();
			int len = optimizationResultSet.getOptSolverResultSet().getParameterNames().length;
			for (int i = 0; i < len; i ++) {
				Double modelValue = null;
				if(paramEstTask != null && paramEstTask.getModelOptimizationSpec() != null && 
				   paramEstTask.getModelOptimizationSpec().getParameterMappingSpecs() != null && paramEstTask.getModelOptimizationSpec().getParameterMappingSpecs().length > 0 &&
				   paramEstTask.getModelParameterByMathName(optimizationResultSet.getOptSolverResultSet().getParameterNames()[i]) != null)
				{
					try {
						
						Parameter modelParameter = paramEstTask.getModelParameterByMathName(optimizationResultSet.getOptSolverResultSet().getParameterNames()[i]);
						modelValue = new Double(modelParameter.getConstantValue());
						
					} catch (ExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				list.add(new OptimizationSolutionParameter(optimizationResultSet.getOptSolverResultSet().getParameterNames()[i], modelValue, optimizationResultSet.getOptSolverResultSet().getBestEstimates()[i]));
			}
			setData(list);
		}
	}
		
	private static class OptimizationMethodParameterTableModel extends VCellSortTableModel<CopasiOptimizationParameter> {
		CopasiOptimizationMethod copasiOptimizationMethod;
		static final int COLUMN_Parameter = 0;
		static final int COLUMN_Value = 1;
		
		OptimizationMethodParameterTableModel(ScrollTable table) {
			super(table, new String[] {"Parameter", "Value"});
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			CopasiOptimizationParameter cop = getValueAt(rowIndex);
			return columnIndex == COLUMN_Parameter ? cop.getType().getDisplayName() : cop.getValue();
		}

		@Override
		protected Comparator<CopasiOptimizationParameter> getComparator(int col, boolean ascending) {
			return null;
		}
		
		private void refreshData() {
			List<CopasiOptimizationParameter> list = null;
			
			if (copasiOptimizationMethod.getParameters() != null) {
				list = Arrays.asList(copasiOptimizationMethod.getParameters());
			}
			setData(list);
		}
		
		public final void setCopasiOptimizationMethod(CopasiOptimizationMethod copasiOptimizationMethod) {
			this.copasiOptimizationMethod = copasiOptimizationMethod;
			refreshData();
			GuiUtils.flexResizeTableColumns(ownerTable);
		}

		@Override
		public boolean isSortable(int col) {
			return false;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == COLUMN_Value;
		}
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == COLUMN_Value) {
				CopasiOptimizationParameter cop = getValueAt(rowIndex);
				cop.setValue((Double) aValue);
			}
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == COLUMN_Value ? Double.class : String.class;
		}
	}
	
	private class InternalEventHandler implements ActionListener, PropertyChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == getPlotButton()) 
				plot();
			if (e.getSource() == getSaveSolutionAsNewSimButton()) 
				saveSolutionAsNewSimulation();
			if (e.getSource() == getSolveButton()) 
				solve();
			else if (e.getSource() == getOptimizationMethodComboBox()) { 
				optimizationMethodComboBox_ActionPerformed();	
			}else if (e.getSource() == getNumberOfRunComboBox()) {
				saveNumberOfRuns();
			}else if (e.getSource() == helpButton) {
				showCopasiMethodHelp();
			} else if (e.getSource() == copasiLinkLabel){
				try {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + "http://www.copasi.org/tiki-view_articles.php");
				} catch (IOException e1) {
					e1.printStackTrace(System.out);
					DialogUtils.showErrorDialog(ParameterEstimationRunTaskPanel.this, "COPASI web site is not able to be connected.");
				}
			}
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == parameterEstimationTask && (evt.getPropertyName().equals("optimizationResultSet"))) 
				optimizationResultSet_This();
			if (evt.getSource() == parameterEstimationTask.getOptSolverCallbacks() 
					&& (evt.getPropertyName().equals(CopasiOptSolverCallbacks.COPASI_EVALUATION_HOLDER))) { 
				getRunStatusDialog().setNumEvaluations(parameterEstimationTask.getOptSolverCallbacks().getEvaluationCount());
				getRunStatusDialog().setObjectFunctionValue(parameterEstimationTask.getOptSolverCallbacks().getObjectiveFunctionValue());
				getRunStatusDialog().setNumRunMessage(parameterEstimationTask.getOptSolverCallbacks().getRunNumber(), parameterEstimationTask.getOptimizationSolverSpec().getNumOfRuns());
				if (optimizationMethodParameterTableModel.copasiOptimizationMethod.getType().getProgressType() == CopasiOptProgressType.Progress) {
					getRunStatusDialog().setProgress(parameterEstimationTask.getOptSolverCallbacks().getPercent());
				}
				else if (optimizationMethodParameterTableModel.copasiOptimizationMethod.getType().getProgressType() == CopasiOptProgressType.Current_Value) {
					getRunStatusDialog().setCurrentValue(parameterEstimationTask.getOptSolverCallbacks().getCurrentValue());
				}
			}
		}
	}
	
	public ParameterEstimationRunTaskPanel() {
		super();
		initialize();
	}
	
	public ParameterEstimationTask getParameterEstimationTask()
	{
		return parameterEstimationTask;
	}
	
	public void showCopasiMethodHelp() 
	{
		CopasiOptimizationMethodType methodType = ((CopasiOptimizationMethodType)optimizationMethodComboBox.getSelectedItem());
		getCopasiOptimizationHelpPanel().refreshSolverInfo(methodType);
		JOptionPane helpPane = new JOptionPane(copasiHelpPanel, JOptionPane.INFORMATION_MESSAGE);
		JDialog dialog = helpPane.createDialog(this, "Copasi Methods Help Information");
		dialog.setResizable(true);
		dialog.setVisible(true);
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getSolverPanel(), gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(4, 4, 4, 4);
		add(getSolutionPanel(), gbc);
		
		JPanel panel = new JPanel(new BorderLayout());
		copasiLinkLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(copasiLinkLabel, BorderLayout.CENTER);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(0, 8, 4, 0);
		add(panel, gbc);
		
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (CopasiOptimizationMethodType com : CopasiOptimizationMethodType.values()){
			model.addElement(com);
		}
		getOptimizationMethodComboBox().setModel(model);
		getOptimizationMethodComboBox().addActionListener(eventHandler);
		getOptimizationMethodComboBox().setSelectedItem(CopasiOptimizationMethodType.EvolutionaryProgram);
		getOptimizationMethodComboBox().setRenderer(new DefaultListCellRenderer() {
			
			@Override
			public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof CopasiOptimizationMethodType) {
					setText(((CopasiOptimizationMethodType) value).getDisplayName());
				}
				return this;
			}
		});
		getNumberOfRunComboBox().addActionListener(eventHandler);
		getSolveButton().addActionListener(eventHandler);
		helpButton.addActionListener(eventHandler);
		getPlotButton().addActionListener(eventHandler);
		getSaveSolutionAsNewSimButton().addActionListener(eventHandler);
	}
	
	private CopasiOptimizationMethodsHelpPanel getCopasiOptimizationHelpPanel()
	{
		if(copasiHelpPanel == null)
		{
			copasiHelpPanel = new CopasiOptimizationMethodsHelpPanel();
		}
		return copasiHelpPanel;
	}
	
	/**
	 * Comment
	 */
	private String displayResults(OptimizationResultSet optResultSet) {
		if (optResultSet==null){
			return "no results";
		}
		StringBuffer buffer = new StringBuffer();

		buffer.append("\n-------------Optimizer Output-----------------\n");
		buffer.append(optResultSet.getOptSolverResultSet().getOptimizationStatus() + "\n");
		buffer.append("best objective function :"+optResultSet.getOptSolverResultSet().getLeastObjectiveFunctionValue()+"\n");
		buffer.append("num function evaluations :"+optResultSet.getOptSolverResultSet().getObjFunctionEvaluations()+"\n");
		if (optResultSet.getOptSolverResultSet().getOptimizationStatus().isNormal()){
			buffer.append("status: complete\n");
		}else{
			buffer.append("status: aborted\n");
		}
		for (int i = 0; optResultSet.getOptSolverResultSet().getParameterNames()!=null && i < optResultSet.getOptSolverResultSet().getParameterNames().length; i++){
			buffer.append(optResultSet.getOptSolverResultSet().getParameterNames()[i]+" = "+optResultSet.getOptSolverResultSet().getBestEstimates()[i]+"\n");
		}

		return buffer.toString();
	}


	/**
	 * Return the JPanel10 property value.
	 * @return javax.swing.JPanel
	 */
	private JPanel getSolverPanel() {
		if (solverPanel == null) {
			try {
				solverPanel = new javax.swing.JPanel();
				solverPanel.setBorder(new TitledBorder(GuiConstants.TAB_PANEL_BORDER, "Supported COPASI Methods", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, VCellLookAndFeel.defaultFont.deriveFont(Font.BOLD)));
				solverPanel.setLayout(new java.awt.GridBagLayout());

				optimizationMethodParameterTable = new ScrollTable();
				optimizationMethodParameterTableModel = new OptimizationMethodParameterTableModel(optimizationMethodParameterTable);
				optimizationMethodParameterTable.setModel(optimizationMethodParameterTableModel);
				
				computeProfileDistributionsCheckBox = new JCheckBox("Compute Profile Distributions");
				computeProfileDistributionsCheckBox.setVisible(false);//TODO: need to implement it later
				
				helpButton = new JButton("Copasi Methods Help");
				
				java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; 
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.gridwidth = 2;
				gbc.insets = new java.awt.Insets(4, 4, 4, 0);
				gbc.anchor = GridBagConstraints.LINE_START;
				solverPanel.add(computeProfileDistributionsCheckBox, gbc);

				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; 
				gbc.gridy = 1;
				gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				gbc.gridwidth = 2;
				solverPanel.add(getOptimizationMethodComboBox(), gbc);

				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; 
				gbc.gridy = 2;
				gbc.fill = java.awt.GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				gbc.gridwidth = 2;
				solverPanel.add(new JScrollPane(optimizationMethodParameterTable), gbc);
				
				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; 
				gbc.gridy = 3;
				gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.insets = new java.awt.Insets(4, 4, 4, 0);
				gbc.anchor = GridBagConstraints.LINE_END;
				solverPanel.add(numberOfRunLabel, gbc);
				
				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 1; 
				gbc.gridy = 3;
				gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.insets = new java.awt.Insets(4, 0, 4, 4);
				solverPanel.add(getNumberOfRunComboBox(), gbc);
				
				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; 
				gbc.gridy = 4;
				gbc.insets = new java.awt.Insets(4, 0, 4, 0);
				gbc.weightx = 1.0;
//				gbc.anchor = GridBagConstraints.LINE_END;
				solverPanel.add(getSolveButton(), gbc);

				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 1; 
				gbc.gridy = 4;
				gbc.insets = new java.awt.Insets(4, 0, 4, 0);
				gbc.weightx = 1.0;
				gbc.anchor = GridBagConstraints.LINE_START;
				solverPanel.add(helpButton, gbc);
				
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return solverPanel;
	}
	
	private RunStatusProgressDialog getRunStatusDialog() {
		if (runStatusDialog == null) {
			runStatusDialog = new RunStatusProgressDialog(JOptionPane.getFrameForComponent(this));
		}	
		return runStatusDialog;
	}

	/**
	 * Return the JPanel7 property value.
	 * @return javax.swing.JPanel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private JPanel getSolutionPanel() {
		if (solutionPanel == null) {
			try {
				solutionPanel = new javax.swing.JPanel();
				solutionPanel.setBorder(new TitledBorder(GuiConstants.TAB_PANEL_BORDER, "Solution", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, VCellLookAndFeel.defaultFont.deriveFont(Font.BOLD)));
				solutionPanel.setLayout(new java.awt.GridBagLayout());
				
				optimizationSolutionParameterTable = new ScrollTable();
				optimizationSolutionParameterTableModel = new OptimizationSolutionParameterTableModel(optimizationSolutionParameterTable);
				optimizationSolutionParameterTable.setModel(optimizationSolutionParameterTableModel);
				JPanel solutionParametersPanel = new JPanel(new BorderLayout());
				solutionParametersPanel.add(optimizationSolutionParameterTable.getEnclosingScrollPane(), BorderLayout.CENTER);
				
				optimizationTaskSummaryTable = new ScrollTable();
//				optimizationTaskSummaryTable.setTableHeader(null);
				optimizationTaskSummaryTableModel = new OptimizationTaskSummaryTableModel(optimizationTaskSummaryTable);
				optimizationTaskSummaryTable.setModel(optimizationTaskSummaryTableModel);
				JPanel taskSummaryPanel = new JPanel(new BorderLayout());
				taskSummaryPanel.add(optimizationTaskSummaryTable.getEnclosingScrollPane(), BorderLayout.CENTER);
				
				JTabbedPane tabbedPane = new JTabbedPane();
				solutionParametersPanel.setBorder(GuiConstants.TAB_PANEL_BORDER);
				taskSummaryPanel.setBorder(GuiConstants.TAB_PANEL_BORDER);
				
				tabbedPane.addTab("Parameters", solutionParametersPanel);
				tabbedPane.addTab("Task Summary", taskSummaryPanel);
				
				int gridy = 0;
				GridBagConstraints  gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = gridy;
				gbc.gridwidth = 4;
				gbc.fill = java.awt.GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				solutionPanel.add(tabbedPane, gbc);
				
				JPanel panel = new javax.swing.JPanel();
				panel.setLayout(new java.awt.FlowLayout());
				panel.add(getPlotButton());
				panel.add(getSaveSolutionAsNewSimButton());
//				panel.add(getEvaluateConfidenceIntervalButton()); //TODO: put it back after implemented the confidence interval evaluations

				gridy ++;
				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = gridy;
				gbc.gridwidth = 4;
				gbc.fill = java.awt.GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				solutionPanel.add(panel, gbc);

			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return solutionPanel;
	}

	/**
	 * Return the JTextPane1 property value.
	 * @return javax.swing.JTextPane
	 */
	private JTextArea getOptimizeResultsTextPane() {
		if (optimizeResultsTextArea == null) {
			try {
				optimizeResultsTextArea = new javax.swing.JTextArea(5,20);
				optimizeResultsTextArea.setLineWrap(true);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return optimizeResultsTextArea;
	}

	/**
	 * Return the PlotButton property value.
	 * @return javax.swing.JButton
	 */
	private JButton getPlotButton() {
		if (plotButton == null) {
			try {
				plotButton = new JButton("Plot");
				plotButton.setEnabled(false);
			} catch (Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return plotButton;
	}

	private JButton getEvaluateConfidenceIntervalButton() {
		if ( evaluateConfidenceIntervalButton == null) {
			try {
				evaluateConfidenceIntervalButton = new javax.swing.JButton("Confidence Interval");
				evaluateConfidenceIntervalButton.setEnabled(false);
			} catch (Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return evaluateConfidenceIntervalButton;
	}

	/**
	 * Return the SaveAsNewSimulationButton property value.
	 * @return javax.swing.JButton
	 */
	private JButton getSaveSolutionAsNewSimButton() {
		if (saveSolutionAsNewSimButton == null) {
			try {
				saveSolutionAsNewSimButton = new javax.swing.JButton();
				saveSolutionAsNewSimButton.setName("SaveSolutionAsNewSimButton");
				saveSolutionAsNewSimButton.setText("Save Solution as New Simulation...");
				saveSolutionAsNewSimButton.setEnabled(false);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return saveSolutionAsNewSimButton;
	}
	
	/**
	 * Return the JButton2 property value.
	 * @return javax.swing.JButton
	 */
	private JButton getSolveButton() {
		if (solveButton == null) {
			try {
				solveButton = new javax.swing.JButton("Solve by Copasi");
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return solveButton;
	}

	/**
	 * Comment
	 */
	private String getSolverMessageText() {
		if (parameterEstimationTask!=null){
			return parameterEstimationTask.getSolverMessageText();
		}else{
			return "";
		}
	}


	/**
	 * Return the SolverTypeComboBox property value.
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getOptimizationMethodComboBox() {
		if (optimizationMethodComboBox == null) {
			try {
				optimizationMethodComboBox = new JComboBox();
				optimizationMethodComboBox.setName("SolverTypeComboBox");
			} catch (Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return optimizationMethodComboBox;
	}
	
	private JComboBox getNumberOfRunComboBox()
	{
		if(numberOfRunComboBox == null)
		{
			numberOfRunComboBox = new JComboBox();
			for(int i = 1; i <= 25; i++) //add 1..25
			{
				numberOfRunComboBox.addItem(i+"");
			}
			for(int i=2; i<=4; i++)//add 50,75,100
			{
				numberOfRunComboBox.addItem(i*25+"");
			}
			for(int i=2; i<=10; i++)//add 200..1000
			{
				numberOfRunComboBox.addItem(i*100+"");
			}
			numberOfRunComboBox.setEditable(true);
		}
		return numberOfRunComboBox;
	}

	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	private void handleException(Throwable exception) {
		System.out.println("--------- UNCAUGHT EXCEPTION ---------");
		exception.printStackTrace(System.out);
	}


	/**
	 * Comment
	 */
	private void optimizationResultSet_This() 
	{
		OptimizationResultSet optResultSet = parameterEstimationTask.getOptimizationResultSet();
		optimizationSolutionParameterTableModel.refresh(optResultSet, parameterEstimationTask);
		optimizationTaskSummaryTableModel.refresh(optResultSet);
		if(optResultSet.getOptSolverResultSet().getOptimizationStatus()!= null)
		{
			String message = displayResults(optResultSet);
			parameterEstimationTask.appendSolverMessageText("\n"+message);
		}
		if (optResultSet!=null){
			getSaveSolutionAsNewSimButton().setEnabled(true);
			getPlotButton().setEnabled(true);
		}else{
			getSaveSolutionAsNewSimButton().setEnabled(false);
			getPlotButton().setEnabled(false);
		}
	}


	/**
	 * Sets the parameterEstimationTask property (cbit.vcell.modelopt.ParameterEstimationTask) value.
	 * @param newValue The new value for the property.
	 * @see #getParameterEstimationTask
	 */
	public void setParameterEstimationTask(ParameterEstimationTask newValue) {
		ParameterEstimationTask oldValue = parameterEstimationTask;
		parameterEstimationTask = newValue;
		/* Stop listening for events from the current object */
		if (oldValue != null) {
			oldValue.removePropertyChangeListener(eventHandler);
			oldValue.getOptSolverCallbacks().removePropertyChangeListener(eventHandler);
		}

		/* Listen for events from the new object */
		if (newValue != null) {
			newValue.addPropertyChangeListener(eventHandler);
			newValue.getOptSolverCallbacks().addPropertyChangeListener(eventHandler);
		}
		getOptimizeResultsTextPane().setText(this.getSolverMessageText());
		// set table data
		if(parameterEstimationTask.getOptimizationSolverSpec() != null)
		{
			if(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod() != null)
			{
				optimizationMethodComboBox.setSelectedItem(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod().getType());
				optimizationMethodParameterTableModel.setCopasiOptimizationMethod(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod());
			}
			numberOfRunComboBox.setSelectedItem(parameterEstimationTask.getOptimizationSolverSpec().getNumOfRuns() + "");
		}
		//set result table data
		if(parameterEstimationTask.getOptimizationResultSet() != null && parameterEstimationTask.getOptimizationResultSet().getOptSolverResultSet() != null &&
		   parameterEstimationTask.getOptimizationResultSet().getOptSolverResultSet().getBestEstimates() != null && 
		   parameterEstimationTask.getOptimizationResultSet().getOptSolverResultSet().getBestEstimates().length > 0)
		{
			optimizationResultSet_This();
		}
	}
	
	/**
	 * Comment
	 */
	private void optimizationMethodComboBox_ActionPerformed() {
		CopasiOptimizationMethodType methodType = (CopasiOptimizationMethodType)getOptimizationMethodComboBox().getSelectedItem();
		CopasiOptimizationMethod com = new CopasiOptimizationMethod(methodType);
		if(parameterEstimationTask != null && parameterEstimationTask.getOptimizationSolverSpec() != null)
		{
			if(parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod() == null || 
			   !parameterEstimationTask.getOptimizationSolverSpec().getCopasiOptimizationMethod().getType().getDisplayName().equals(methodType.getDisplayName()))
			{
				parameterEstimationTask.getOptimizationSolverSpec().setCopasiOptimizationMethod(com);
			}
		}
		optimizationMethodParameterTableModel.setCopasiOptimizationMethod(com);
		if(methodType.isStochasticMethod())
		{
			numberOfRunComboBox.setVisible(true);
			numberOfRunLabel.setVisible(true);
		}	
		else
		{
			numberOfRunComboBox.setVisible(false);
			numberOfRunLabel.setVisible(false);
		}
	}

	private void saveNumberOfRuns() {
		int numOfRuns = Integer.parseInt((String)(getNumberOfRunComboBox().getSelectedItem()));
		if(parameterEstimationTask != null && parameterEstimationTask.getOptimizationSolverSpec() != null)
		{
			parameterEstimationTask.getOptimizationSolverSpec().setNumOfRuns(numOfRuns);
		}
	}
	
	private void solve() throws NumberFormatException{
		CopasiOptimizationMethod com = optimizationMethodParameterTableModel.copasiOptimizationMethod;
		OptimizationSolverSpec optSolverSpec = new OptimizationSolverSpec(com);
		//get num runs for stochstic opt mehtods before starting solving...
		if(com.getType().isStochasticMethod())
		{
			int numRuns = Integer.parseInt(((String)numberOfRunComboBox.getSelectedItem()));
			optSolverSpec.setNumOfRuns(numRuns);
		}
		parameterEstimationTask.setOptimizationSolverSpec(optSolverSpec);
		parameterEstimationTask.getModelOptimizationSpec().setComputeProfileDistributions(computeProfileDistributionsCheckBox.isSelected());
		parameterEstimationTask.getOptSolverCallbacks().reset();
		Double endValue = com.getEndValue();
		parameterEstimationTask.getOptSolverCallbacks().setEvaluation(0, Double.POSITIVE_INFINITY, 0, endValue, 0);
		getRunStatusDialog().showProgressBar(com);//(endValue != null);

		ArrayList<AsynchClientTask> taskList = new ArrayList<AsynchClientTask>();
		AsynchClientTask[] updateTasks = ClientRequestManager.updateMath(this, parameterEstimationTask.getSimulationContext(), false);
		for (AsynchClientTask task : updateTasks) {
			taskList.add(task);
		}
		
		AsynchClientTask task1 = new AsynchClientTask("checking issues", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				StringBuffer issueText = new StringBuffer();				
				java.util.Vector<Issue> issueList = new java.util.Vector<Issue>();
				parameterEstimationTask.gatherIssues(issueList);
				boolean bFailed = false;
				for (int i = 0; i < issueList.size(); i++){
					Issue issue = (Issue)issueList.elementAt(i);
					issueText.append(issue.getMessage()+"\n");
					if (issue.getSeverity() == Issue.SEVERITY_ERROR){
						bFailed = true;
						break;
					}
				}
				if (bFailed){
					throw new OptimizationException(issueText.toString());
				}
				parameterEstimationTask.refreshMappings();
			}
		};
		taskList.add(task1);
		
		AsynchClientTask task2 = new AsynchClientTask("solving", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {		
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				OptimizationResultSet optResultSet = CopasiOptimizationSolver.solve(parameterEstimationTask);
				hashTable.put("Optimiation Result Set", optResultSet);
			}

		};
		taskList.add(task2);
		
		AsynchClientTask setResultTask = new AsynchClientTask("set results", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {		
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				OptimizationResultSet optResultSet = (OptimizationResultSet) hashTable.get("Optimiation Result Set"); 
				parameterEstimationTask.setOptimizationResultSet(optResultSet);
			}

		};
		taskList.add(setResultTask);
		
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), taskList.toArray(new AsynchClientTask[taskList.size()]), getRunStatusDialog(), true, true, true, null, false);
	}
	
	private void plot() {
		try {
			java.util.Vector<DataSource> dataSourceList = new java.util.Vector<DataSource>();
			java.util.Vector<String> nameVector = new java.util.Vector<String>();
	
			ModelOptimizationSpec modelOptimizationSpec = parameterEstimationTask.getModelOptimizationSpec();
			ReferenceDataMappingSpec[] mappingSpecs = modelOptimizationSpec.getReferenceDataMappingSpecs();
			int timeIndex = modelOptimizationSpec.getReferenceDataTimeColumnIndex();
	
			ReferenceData referenceData = modelOptimizationSpec.getReferenceData();
			if (referenceData!=null) {
				dataSourceList.add(new DataSource.DataSourceReferenceData("EXPT", timeIndex, referenceData));
				String[] refColumnNames = referenceData.getColumnNames();
				for (int i = 0; i < refColumnNames.length; i ++) {
					if (i == timeIndex) {
						continue;
					}
					nameVector.add(refColumnNames[i]);
					break;
				}
			}
	
			ODESolverResultSet odeSolverResultSet = parameterEstimationTask.getOdeSolverResultSet();

			if (odeSolverResultSet!=null){
				dataSourceList.add(new DataSource.DataSourceOdeSolverResultSet("EST", odeSolverResultSet));
				if (mappingSpecs != null) {
					for (int i = 0; i < mappingSpecs.length; i ++) {
						if (i == timeIndex) {
							continue;
						}
						Variable var = parameterEstimationTask.getMathSymbolMapping().getVariable(mappingSpecs[i].getModelObject());
						nameVector.add(var.getName());
						break;
					}
				}
			}
			DataSource[] dataSources = (DataSource[])BeanUtils.getArray(dataSourceList,DataSource.class);
			MultisourcePlotPane multisourcePlotPane = new MultisourcePlotPane();
			multisourcePlotPane.setDataSources(dataSources);	
	
			String[] nameArray = new String[nameVector.size()];
			nameArray = (String[])BeanUtils.getArray(nameVector, String.class);
			multisourcePlotPane.select(nameArray);
	
			DialogUtils.showComponentCloseDialog(JOptionPane.getFrameForComponent(this), multisourcePlotPane, "Data Plot");
		}catch (Exception e){
			e.printStackTrace(System.out);
		} 
	}

	private void saveSolutionAsNewSimulation() {
		try {
			OptimizationSpec optSpec = parameterEstimationTask.getModelOptimizationMapping().getOptimizationSpec();
			if (optSpec == null){
				throw new RuntimeException("optimization not yet performed");
			}
			if (optSpec.getObjectiveFunction() instanceof OdeObjectiveFunction){
				//
				// add new simulation to the Application (other bookkeeping required?)
				//
				SimulationContext simContext = parameterEstimationTask.getModelOptimizationSpec().getSimulationContext();
				Simulation newSim = simContext.addNewSimulation();
				parameterEstimationTask.getModelOptimizationMapping().applySolutionToMathOverrides(newSim,parameterEstimationTask.getOptimizationResultSet());
				DialogUtils.showInfoDialog(this, "created simulation \""+newSim.getName()+"\"");
			}
		}catch (Exception e){
			e.printStackTrace(System.out);
			DialogUtils.showErrorDialog(this, e.getMessage(), e);
		}
	}

	private void stop() {
		if (parameterEstimationTask!=null && parameterEstimationTask.getOptSolverCallbacks()!=null){
			parameterEstimationTask.getOptSolverCallbacks().setStopRequested(true);
		}
	}

}
