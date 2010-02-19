package cbit.vcell.microscopy.gui.choosemodelwizard;

import java.util.ArrayList;
import java.util.Hashtable;

import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.microscopy.FRAPStudy;
import cbit.vcell.microscopy.FRAPSingleWorkspace;
import cbit.vcell.microscopy.gui.loaddatawizard.LoadFRAPData_SummaryPanel;

import org.vcell.wizard.Wizard;
import org.vcell.wizard.WizardPanelDescriptor;

public class ChooseModel_RoiForErrorDescriptor extends WizardPanelDescriptor{
    
    public static final String IDENTIFIER = "ChooseModel_RoiForError";
    public FRAPSingleWorkspace frapWorkspace = null;
    
    public ChooseModel_RoiForErrorDescriptor () {
        super(IDENTIFIER, new ChooseModel_RoiForErrorPanel());
    }
    
    public String getNextPanelDescriptorID() {
        return Wizard.FINISH.getPanelDescriptorIdentifier();
    }
    
    public String getBackPanelDescriptorID() {
        return ChooseModel_ModelTypesDescriptor.IDENTIFIER;
    }
    
    public void aboutToDisplayPanel() 
    {
    	((ChooseModel_RoiForErrorPanel)getPanelComponent()).refreshCheckboxes();
    	((ChooseModel_RoiForErrorPanel)getPanelComponent()).refreshROIImage();
	}
    
    public void setFrapWorkspace(FRAPSingleWorkspace arg_FrapWorkspace)
    {
    	frapWorkspace = arg_FrapWorkspace;
    	((ChooseModel_RoiForErrorPanel)getPanelComponent()).setFrapWorkspace(arg_FrapWorkspace);
    }
    
    public ArrayList<AsynchClientTask> preNextProcess()
    {   
    	ArrayList<AsynchClientTask> tasks = new ArrayList<AsynchClientTask>();
    	
    	AsynchClientTask aTask1 = new AsynchClientTask("Saving selected model types...", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) 
		{
			public void run(Hashtable<String, Object> hashTable) throws Exception
			{
				boolean[] selectedROIs = ((ChooseModel_RoiForErrorPanel)getPanelComponent()).getSelectedROIs();
				boolean isOneSelected = false;
				for(int i=0; i<selectedROIs.length; i++)
				{
					if(selectedROIs[i])
					{
						isOneSelected = true;
						break;
					}
				}
				if(isOneSelected)
				{
					frapWorkspace.getWorkingFrapStudy().setSelectedROIsForErrorCalculation(selectedROIs);
				}
				else
				{
					throw new Exception("At least one ROI has to be selected.");
				}
			}
		};
		tasks.add(aTask1);

        return tasks;
    }
}
