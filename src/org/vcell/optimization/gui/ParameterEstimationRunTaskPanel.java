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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.vcell.optimization.ConfidenceInterval;
import org.vcell.optimization.CopasiOptimizationSolver;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationMethod;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationMethodType;
import org.vcell.optimization.CopasiOptimizationSolver.CopasiOptimizationParameter;
import org.vcell.optimization.OptSolverResultSet;
import org.vcell.optimization.OptSolverResultSet.ProfileDistribution;
import org.vcell.optimization.ProfileSummaryData;
import org.vcell.util.BeanUtils;
import org.vcell.util.DescriptiveStatistics;
import org.vcell.util.Issue;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.GuiUtils;
import org.vcell.util.gui.ProgressDialog;
import org.vcell.util.gui.ProgressDialogListener;
import org.vcell.util.gui.ScrollTable;

import cbit.plot.Plot2D;
import cbit.plot.PlotData;
import cbit.vcell.client.GuiConstants;
import cbit.vcell.client.VCellLookAndFeel;
import cbit.vcell.client.desktop.biomodel.VCellSortTableModel;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.mapping.StructureMapping;
import cbit.vcell.math.InconsistentDomainException;
import cbit.vcell.math.MathDescription;
import cbit.vcell.math.MathException;
import cbit.vcell.math.Variable;
import cbit.vcell.matrix.MatrixException;
import cbit.vcell.model.ModelException;
import cbit.vcell.modelopt.ModelOptimizationSpec;
import cbit.vcell.modelopt.ParameterEstimationTask;
import cbit.vcell.modelopt.ParameterMappingSpec;
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

	private javax.swing.JTextArea optimizeResultsTextArea = null;
	private javax.swing.JComboBox optimizationMethodComboBox = null;
	private javax.swing.JButton plotButton = null;
	private javax.swing.JButton saveSolutionAsNewSimButton = null;
	private javax.swing.JPanel solutionPanel = null;
	private javax.swing.JButton solveButton = null;
	private javax.swing.JButton helpButton = null;
	private javax.swing.JPanel solverPanel = null;
	private ParameterEstimationTask parameterEstimationTask = null;
	private JCheckBox computeProfileDistributionsCheckBox = null;
	private JButton evaluateConfidenceIntervalButton = null;
	
	private ScrollTable optimizationMethodParameterTable = null;
	private OptimizationMethodParameterTableModel optimizationMethodParameterTableModel;
	private InternalEventHandler eventHandler = new InternalEventHandler();
	
	private ProgressDialog runStatusDialog;
	private ScrollTable optimizationSolutionParameterTable = null;
	private OptimizationSolutionParameterTableModel optimizationSolutionParameterTableModel;

	private ScrollTable optimizationTaskSummaryTable = null;
	private OptimizationTaskSummaryTableModel optimizationTaskSummaryTableModel;

	private class RunStatusProgressDialog extends ProgressDialog {
		private JTextField numEvaluationsTextField = null;
		private JTextField objectiveFunctionValueTextField = null;
		RunStatusProgressDialog(Frame owner) {
			super(owner);
		}	
		
		@Override
		protected void initialize() {
			setTitle("Running Parameter Estimation");
			setResizable(true);
			setModal(true);
			
			JPanel runStatusPanel = new JPanel();
			runStatusPanel.setLayout(new GridBagLayout());
			
			objectiveFunctionValueTextField = new javax.swing.JTextField();
			objectiveFunctionValueTextField.setEditable(false);
			
			numEvaluationsTextField = new javax.swing.JTextField();
			numEvaluationsTextField.setEditable(false);

			int gridy = 0;		
			java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
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
			runStatusPanel.add(new javax.swing.JLabel("Progress: "), gbc);
			
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 1; 
			gbc.gridy = gridy;
			gbc.weightx = 1.0;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
			runStatusPanel.add(getProgressBar(), gbc);
				
			gridy ++;
			gbc = new java.awt.GridBagConstraints();
			gbc.gridx = 0; 
			gbc.gridy = gridy;
			gbc.gridwidth = 2;
			gbc.insets = new java.awt.Insets(4, 4, 4, 4);
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
		public void setCancelButtonVisible(boolean arg1) {
		}

		@Override
		public void setMessage(String message) {
		}
		
		private void setNumEvaluations(String num) {
			numEvaluationsTextField.setText(num);
		}

		private void setObjectFunctionValue(String d) {
			objectiveFunctionValueTextField.setText(d);
		}
	}

	public static class OptimizationSolutionParameter {
		private String name;
		private double value;
		private OptimizationSolutionParameter(String name, double value) {
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
		
		private void refreshData() {
			List<OptimizationTaskSummary> list = null;			
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
		static final int COLUMN_Value = 1;
		
		public OptimizationSolutionParameterTableModel(ScrollTable table) {
			super(table, new String[] {"Parameter", "Value"});
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			OptimizationSolutionParameter cop = getValueAt(rowIndex);
			return columnIndex == COLUMN_Parameter ? cop.getName() : cop.getValue();
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
			return columnIndex == COLUMN_Value ? Double.class : String.class;
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
	
	private class InternalEventHandler implements java.awt.event.ActionListener, java.beans.PropertyChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == getPlotButton()) 
				plot();
			if (e.getSource() == getSaveSolutionAsNewSimButton()) 
				saveSolutionAsNewSimulation();
			if (e.getSource() == getSolveButton()) 
				solve();
			else if (e.getSource() == getEvaluateConfidenceIntervalButton()) { 
				evaluateConfidenceInterval(); 
			} else if (e.getSource() == getOptimizationMethodComboBox()) { 
				optimizationMethodComboBox_ActionPerformed();	
			} else if (e.getSource() == helpButton) {
				copasiMethodHelp();
			}
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == parameterEstimationTask && (evt.getPropertyName().equals("optimizationResultSet"))) 
				optimizationResultSet_This(parameterEstimationTask.getOptimizationResultSet());
			if (evt.getSource() == parameterEstimationTask && (evt.getPropertyName().equals("solverMessageText"))) 
				getOptimizeResultsTextPane().setText(String.valueOf(parameterEstimationTask.getSolverMessageText()));
		}
	}
	
	public ParameterEstimationRunTaskPanel() {
		super();
		initialize();
	}
	
	public void copasiMethodHelp() {
		
	}

	private void initialize() {
		setLayout(new java.awt.GridBagLayout());

		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = 0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getSolverPanel(), gbc);

		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = 0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getSolutionPanel(), gbc);
		
		javax.swing.DefaultComboBoxModel model = new DefaultComboBoxModel();
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
		
		getSolveButton().addActionListener(eventHandler);
		getPlotButton().addActionListener(eventHandler);
	}
	
	/**
	 * Comment
	 */
	private java.lang.String displayResults(OptimizationResultSet optResultSet) {
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
	private javax.swing.JPanel getSolverPanel() {
		if (solverPanel == null) {
			try {
				solverPanel = new javax.swing.JPanel();
				solverPanel.setBorder(new TitledBorder(GuiConstants.TAB_PANEL_BORDER, "COPASI Method", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, VCellLookAndFeel.defaultFont.deriveFont(Font.BOLD)));
				solverPanel.setLayout(new java.awt.GridBagLayout());

				optimizationMethodParameterTable = new ScrollTable();
				optimizationMethodParameterTableModel = new OptimizationMethodParameterTableModel(optimizationMethodParameterTable);
				optimizationMethodParameterTable.setModel(optimizationMethodParameterTableModel);
				
				computeProfileDistributionsCheckBox = new JCheckBox("Compute Profile Distributions");
				computeProfileDistributionsCheckBox.setEnabled(false);
				
				helpButton = new JButton("Help...");
				helpButton.addActionListener(eventHandler);
				
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
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				gbc.weightx = 1.0;
				gbc.anchor = GridBagConstraints.LINE_END;
				solverPanel.add(getSolveButton(), gbc);

				gbc = new java.awt.GridBagConstraints();
				gbc.gridx = 1; 
				gbc.gridy = 3;
				gbc.insets = new java.awt.Insets(4, 4, 4, 4);
				gbc.weightx = 1.0;
				gbc.anchor = GridBagConstraints.LINE_START;
				solverPanel.add(helpButton, gbc);
				
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return solverPanel;
	}
	
	private ProgressDialog getRunStatusDialog() {
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
	private javax.swing.JPanel getSolutionPanel() {
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
				panel.add(getEvaluateConfidenceIntervalButton());

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
	private javax.swing.JTextArea getOptimizeResultsTextPane() {
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
	private javax.swing.JButton getSaveSolutionAsNewSimButton() {
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
	private javax.swing.JButton getSolveButton() {
		if (solveButton == null) {
			try {
				solveButton = new javax.swing.JButton("Run");
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return solveButton;
	}

	/**
	 * Comment
	 */
	private java.lang.String getSolverMessageText() {
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
	private javax.swing.JComboBox getOptimizationMethodComboBox() {
		if (optimizationMethodComboBox == null) {
			try {
				optimizationMethodComboBox = new javax.swing.JComboBox();
				optimizationMethodComboBox.setName("SolverTypeComboBox");
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return optimizationMethodComboBox;
	}

	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {
		System.out.println("--------- UNCAUGHT EXCEPTION ---------");
		exception.printStackTrace(System.out);
	}


	/**
	 * Comment
	 */
	private void optimizationResultSet_This(OptimizationResultSet optResultSet) {
		String message = displayResults(optResultSet);
		parameterEstimationTask.appendSolverMessageText("\n"+message);
		if (optResultSet!=null){
			getSaveSolutionAsNewSimButton().setEnabled(true);
		}else{
			getSaveSolutionAsNewSimButton().setEnabled(false);
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
		}

		/* Listen for events from the new object */
		if (newValue != null) {
			newValue.addPropertyChangeListener(eventHandler);
		}
		getOptimizeResultsTextPane().setText(this.getSolverMessageText());
	}
	
	/**
	 * Comment
	 */
	private void optimizationMethodComboBox_ActionPerformed() {
		CopasiOptimizationMethodType methodType = (CopasiOptimizationMethodType)getOptimizationMethodComboBox().getSelectedItem();
		CopasiOptimizationMethod com = new CopasiOptimizationMethod(methodType);
		optimizationMethodParameterTableModel.setCopasiOptimizationMethod(com);
	}

	private void evaluateConfidenceInterval() {
		ProfileSummaryData[] summaryData = null;
		try {
			summaryData = getSummaryFromOptSolverResultSet(parameterEstimationTask.getOptimizationResultSet().getOptSolverResultSet());
		} catch (Exception e) {
			DialogUtils.showErrorDialog(this, e.getMessage());
			e.printStackTrace();
		}
		//put plotpanes of different parameters' profile likelihoods into a base panel
		JPanel basePanel= new JPanel();
		basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
		for(ProfileSummaryData aSumData : summaryData)
		{
			ConfidenceIntervalPlotPanel plotPanel = new ConfidenceIntervalPlotPanel();
			plotPanel.setProfileSummaryData(aSumData);
			plotPanel.setBorder(new EtchedBorder());
			
			ProfileDataPanel profileDataPanel = new ProfileDataPanel(plotPanel, aSumData.getParamName());
			basePanel.add(profileDataPanel);
		}
		JScrollPane scrollPane = new JScrollPane(basePanel);
		scrollPane.setAutoscrolls(true);
		scrollPane.setPreferredSize(new Dimension(620, 600));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//show plots in a dialog
		DialogUtils.showComponentCloseDialog(this, scrollPane, "Profile Likelihood of Parameters");
	}
	
	private void solve() {
		CopasiOptimizationMethod com = optimizationMethodParameterTableModel.copasiOptimizationMethod;
		OptimizationSolverSpec optSolverSpec = new OptimizationSolverSpec(com);
		parameterEstimationTask.setOptimizationSolverSpec(optSolverSpec);
		parameterEstimationTask.getModelOptimizationSpec().setComputeProfileDistributions(computeProfileDistributionsCheckBox.isSelected());

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

		AsynchClientTask task2 = new AsynchClientTask("solving", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {		
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				CopasiOptimizationSolver.solve(parameterEstimationTask);
			}

		};
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] {task1, task2}, getRunStatusDialog(), true, true, true, null, false);
	}
	
	private ProfileSummaryData[] getSummaryFromOptSolverResultSet(OptSolverResultSet osrs) throws MappingException, MathException, MatrixException, ExpressionException, ModelException 
	{
		ProfileSummaryData[] summaryData = null;
		ArrayList<ProfileDistribution> profileDistributionList = osrs.getProfileDistributionList();
		if(profileDistributionList != null && profileDistributionList.size() > 0)
		{
			//get parameter mapping specs from which can we have lower and upper bound
			SimulationContext simulationContext = parameterEstimationTask.getModelOptimizationSpec().getSimulationContext();
			StructureMapping structureMapping = simulationContext.getGeometryContext().getStructureMappings()[0];
			ParameterMappingSpec[] parameterMappingSpecs = parameterEstimationTask.getModelOptimizationSpec().getParameterMappingSpecs();
			MathMapping mathMapping = simulationContext.createNewMathMapping();
			MathDescription origMathDesc = mathMapping.getMathDescription();
//			mathSymbolMapping = mathMapping.getMathSymbolMapping();
			summaryData = new ProfileSummaryData[profileDistributionList.size()];
			for(int k=0; k < profileDistributionList.size(); k++)
			{
				ProfileDistribution profileDistribution = profileDistributionList.get(k);
				String fixedParamName = profileDistribution.getFixedParamName();
				ParameterMappingSpec fixedParamMappingSpec = null;
				for (ParameterMappingSpec pms : parameterMappingSpecs) {
					if (pms.isSelected()) {
						String mathSymbol = mathMapping.getMathSymbol(pms.getModelParameter(),structureMapping.getGeometryClass());
						if (mathSymbol.equals(fixedParamName)) {
							fixedParamMappingSpec = pms;
							break;
						}
					}
				}
				if(fixedParamMappingSpec == null)
				{
					throw new MappingException("Can not find parameter " + fixedParamName);
				}
				int paramValueIdx = osrs.getFixedParameterIndex(fixedParamName);
				if(paramValueIdx > -1)
				{
					ArrayList<OptSolverResultSet.OptRunResultSet> optRunRSList= profileDistribution.getOptRunResultSetList();
					double[] paramValArray = new double[optRunRSList.size()];
					double[] errorArray = new double[optRunRSList.size()];
					//profile likelihood curve
					for(int i=0; i<optRunRSList.size(); i++)
					{
						paramValArray[i] = Math.log10(optRunRSList.get(i).getParameterValues()[paramValueIdx]);//TODO: not sure if the paramvalue is calcualted by log10(). 
						errorArray[i] = optRunRSList.get(i).getObjectiveFunctionValue();
					}
					PlotData dataPlot = new PlotData(paramValArray, errorArray);
					//get confidence interval line
					//make array copy in order to not change the data orders afte the sorting
					double[] paramValArrayCopy = new double[paramValArray.length];
					System.arraycopy(paramValArray, 0, paramValArrayCopy, 0, paramValArray.length);
					double[] errorArrayCopy = new double[errorArray.length];
					System.arraycopy(errorArray, 0, errorArrayCopy, 0, errorArray.length);
					DescriptiveStatistics paramValStat = DescriptiveStatistics.CreateBasicStatistics(paramValArrayCopy);
					DescriptiveStatistics errorStat = DescriptiveStatistics.CreateBasicStatistics(errorArrayCopy);
					double[] xArray = new double[2];
					double[][] yArray = new double[ConfidenceInterval.NUM_CONFIDENCE_LEVELS][2];
					//get confidence level plot lines
					xArray[0] = paramValStat.getMin() -  (Math.abs(paramValStat.getMin()) * 0.2);
					xArray[1] = paramValStat.getMax() + (Math.abs(paramValStat.getMax()) * 0.2) ;
					for(int i=0; i<ConfidenceInterval.NUM_CONFIDENCE_LEVELS; i++)
					{
						yArray[i][0] = errorStat.getMin() + ConfidenceInterval.DELTA_ALPHA_VALUE[i];
						yArray[i][1] = yArray[i][0];
					}
					PlotData confidence80Plot = new PlotData(xArray, yArray[ConfidenceInterval.IDX_DELTA_ALPHA_80]);
					PlotData confidence90Plot = new PlotData(xArray, yArray[ConfidenceInterval.IDX_DELTA_ALPHA_90]);
					PlotData confidence95Plot = new PlotData(xArray, yArray[ConfidenceInterval.IDX_DELTA_ALPHA_95]);
					PlotData confidence99Plot = new PlotData(xArray, yArray[ConfidenceInterval.IDX_DELTA_ALPHA_99]);
					//generate plot2D data
					Plot2D plots = new Plot2D(null,new String[] {"profile Likelihood Data", "80% confidence", "90% confidence", "95% confidence", "99% confidence"}, 
							                  new PlotData[] {dataPlot, confidence80Plot, confidence90Plot, confidence95Plot, confidence99Plot},
							                  new String[] {"Profile likelihood of " + fixedParamName, "Log base 10 of "+fixedParamName, "Profile Likelihood"}, 
							                  new boolean[] {true, true, true, true, true});
					//get the best parameter for the minimal error
					int minErrIndex = -1;
					for(int i=0; i<errorArray.length; i++)
					{
						if(errorArray[i] == errorStat.getMin())
						{
							minErrIndex = i;
							break;
						}
					}
					double bestParamVal = Math.pow(10,paramValArray[minErrIndex]);
					//find confidence interval points
					ConfidenceInterval[] intervals = new ConfidenceInterval[ConfidenceInterval.NUM_CONFIDENCE_LEVELS];
					//half loop through the errors(left side curve)
					int[] smallLeftIdx = new int[ConfidenceInterval.NUM_CONFIDENCE_LEVELS]; 
					int[] bigLeftIdx = new int[ConfidenceInterval.NUM_CONFIDENCE_LEVELS];
					for(int i=0; i<ConfidenceInterval.NUM_CONFIDENCE_LEVELS; i++)
					{
						smallLeftIdx[i] = -1;
						bigLeftIdx[i] = -1;
						for(int j=1; j < minErrIndex+1 ; j++)//loop from bigger error to smaller error
						{
							if((errorArray[j] < (errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i])) &&
							   (errorArray[j-1] > (errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i])))
							{
								smallLeftIdx[i]= j-1;
								bigLeftIdx[i]=j;
								break;
							}
						}
					}
					//another half loop through the errors(right side curve)
					int[] smallRightIdx = new int[ConfidenceInterval.NUM_CONFIDENCE_LEVELS]; 
					int[] bigRightIdx = new int[ConfidenceInterval.NUM_CONFIDENCE_LEVELS];
					for(int i=0; i<ConfidenceInterval.NUM_CONFIDENCE_LEVELS; i++)
					{
						smallRightIdx[i] = -1;
						bigRightIdx[i] = -1;
						for(int j=(minErrIndex+1); j<errorArray.length; j++)//loop from bigger error to smaller error
						{
							if((errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i]) < errorArray[j] &&
							   (errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i]) > errorArray[j-1])
							{
								smallRightIdx[i]= j-1;
								bigRightIdx[i]=j;
								break;
							}
						}
					}
					//calculate intervals 
					for(int i=0; i<ConfidenceInterval.NUM_CONFIDENCE_LEVELS; i++)
					{
						double lowerBound = Double.NEGATIVE_INFINITY;
						boolean bLowerBoundOpen = true;
						double upperBound = Double.POSITIVE_INFINITY;
						boolean bUpperBoundOpen = true;
						if(smallLeftIdx[i] == -1 && bigLeftIdx[i] == -1)//no lower bound
						{
							
							lowerBound = fixedParamMappingSpec.getLow();//parameter LowerBound;
							bLowerBoundOpen = false;
						}
						else if(smallLeftIdx[i] != -1 && bigLeftIdx[i] != -1)//there is a lower bound
						{
							//x=x1+(x2-x1)*(y-y1)/(y2-y1);
							double x1 = paramValArray[smallLeftIdx[i]];
							double x2 = paramValArray[bigLeftIdx[i]];
							double y = errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i];
							double y1 = errorArray[smallLeftIdx[i]];
							double y2 = errorArray[bigLeftIdx[i]];
							lowerBound = x1+(x2-x1)*(y-y1)/(y2-y1);
							lowerBound = Math.pow(10,lowerBound);
							bLowerBoundOpen = false;
						}
						if(smallRightIdx[i] == -1 && bigRightIdx[i] == -1)//no upper bound
						{
							upperBound = fixedParamMappingSpec.getHigh();//parameter UpperBound;
							bUpperBoundOpen = false;
						}
						else if(smallRightIdx[i] != -1 && bigRightIdx[i] != -1)//there is a upper bound
						{
							//x=x1+(x2-x1)*(y-y1)/(y2-y1);
							double x1 = paramValArray[smallRightIdx[i]];
							double x2 = paramValArray[bigRightIdx[i]];
							double y = errorStat.getMin()+ConfidenceInterval.DELTA_ALPHA_VALUE[i];
							double y1 = errorArray[smallRightIdx[i]];
							double y2 = errorArray[bigRightIdx[i]];
							upperBound = x1+(x2-x1)*(y-y1)/(y2-y1);
							upperBound = Math.pow(10,upperBound);
							bUpperBoundOpen = false;
						}
						intervals[i] = new ConfidenceInterval(lowerBound, bLowerBoundOpen, upperBound, bUpperBoundOpen);
					}
					
					summaryData[k] =  new ProfileSummaryData(plots, bestParamVal, intervals, fixedParamName);
				}
			}
	    }
		return summaryData;
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
				dataSourceList.add(new DataSource.DataSourceReferenceData("refData", timeIndex, referenceData));
				String[] refColumnNames = referenceData.getColumnNames();
				for (int i = 0; i < refColumnNames.length; i ++) {
					if (i == timeIndex) {
						continue;
					}
					nameVector.add(refColumnNames[i]);			
				}
			}
	
			ODESolverResultSet odeSolverResultSet = parameterEstimationTask.getOdeSolverResultSet();
			if (odeSolverResultSet!=null){
				dataSourceList.add(new DataSource.DataSourceOdeSolverResultSet("estData", odeSolverResultSet));
				if (mappingSpecs != null) {
					for (int i = 0; i < mappingSpecs.length; i ++) {
						if (i == timeIndex) {
							continue;
						}
						Variable var = parameterEstimationTask.getMathSymbolMapping().getVariable(mappingSpecs[i].getModelObject());
						nameVector.add(var.getName());
					}
				}
			}
			DataSource[] dataSources = (DataSource[])BeanUtils.getArray(dataSourceList,DataSource.class);
			MultisourcePlotPane multisourcePlotPane = new MultisourcePlotPane();
			multisourcePlotPane.setDataSources(dataSources);	
	
			String[] nameArray = new String[nameVector.size()];
			nameArray = (String[])BeanUtils.getArray(nameVector, String.class);
			multisourcePlotPane.select(nameArray);
	
			DialogUtils.showComponentCloseDialog(this, multisourcePlotPane, "Data Plot");
		}catch (ExpressionException e){
			e.printStackTrace(System.out);
		} catch (InconsistentDomainException e) {
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
		return;
	}


}
