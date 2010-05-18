package cbit.vcell.microscopy.gui.choosemodelwizard;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.microscopy.FRAPModel;
import cbit.vcell.microscopy.FRAPStudy;
import cbit.vcell.microscopy.FRAPWorkspace;
import cbit.vcell.microscopy.gui.VirtualFrapMainFrame;
import org.vcell.wizard.WizardPanelDescriptor;

public class ChooseModel_ModelTypesDescriptor extends WizardPanelDescriptor {
    
    public static final String IDENTIFIER = "ChooseModel_ModelTypes";
    private FRAPWorkspace frapWorkspace = null;
    private ChooseModel_ModelTypesPanel modelTypesPanel = new ChooseModel_ModelTypesPanel();
    
    public ChooseModel_ModelTypesDescriptor () {
    	super();
    	setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(modelTypesPanel);
    }
    
    public String getNextPanelDescriptorID() {
        return ChooseModel_RoiForErrorDescriptor.IDENTIFIER;
    }
    
    public String getBackPanelDescriptorID() {
        return null;
    }  
    
    public void setFrapWorkspace(FRAPWorkspace arg_FrapWorkspace)
    {
    	frapWorkspace = arg_FrapWorkspace;
    }
    
    public void aboutToDisplayPanel() 
    {
    	FRAPStudy fStudy = frapWorkspace.getFrapStudy();
    	//if there are models selected and saved, load the model types. otherwise, apply default(diffusion with one component is selected).
    	if(fStudy.getModels() != null && fStudy.getModels().length > 0 && fStudy.getSelectedModels().size() > 0)
    	{
    		modelTypesPanel.clearAllSelected();
    		FRAPModel[] models = fStudy.getModels();
			if(models[FRAPModel.IDX_MODEL_DIFF_ONE_COMPONENT] != null)
			{
				modelTypesPanel.setDiffOneSelected(true);
			}
			if(models[FRAPModel.IDX_MODEL_DIFF_TWO_COMPONENTS] != null)
			{
				modelTypesPanel.setDiffTwoSelected(true);
			}
//			if(models[FRAPModel.IDX_MODEL_DIFF_BINDING] != null)
//			{
//				modelTypesPanel.setDiffBindingSelected(true);
//			}
    	}
    	else //new frap document
    	{
    		modelTypesPanel.clearAllSelected();
    		modelTypesPanel.setDiffOneSelected(true);
    	}
    }
    
    public ArrayList<AsynchClientTask> preNextProcess()
    {   
    	ArrayList<AsynchClientTask> tasks = new ArrayList<AsynchClientTask>();
    	
    	AsynchClientTask aTask1 = new AsynchClientTask("Saving selected model types...", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) 
		{
			public void run(Hashtable<String, Object> hashTable) throws Exception
			{
				FRAPStudy fStudy = frapWorkspace.getFrapStudy();
				if(fStudy != null)
		    	{
					boolean[] models = modelTypesPanel.getModelTypes();
					boolean isOneSelected = false;
					for(int i = 0; i<models.length; i++)
					{
						if(models[i])
						{
							isOneSelected = true;
							break;
						}
					}
					if(isOneSelected)
					{
			    		//update selected models in FrapStudy
			    		fStudy.refreshModels(models);
					}
					else
					{
						throw new Exception("At least one model type has to be selected.");
					}
		    	}
				else
				{
					throw new Exception("FRAPStudy is null. Please load data.");
				}
			}
		};
		
        tasks.add(aTask1);

        return tasks;
    }
    
}//end of class

