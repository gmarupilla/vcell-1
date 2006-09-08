package cbit.vcell.client.data;
import cbit.util.DataAccessException;
import cbit.vcell.simdata.MergedDataInfo;
/**
 * Insert the type's description here.
 * Creation date: (11/30/2005 5:26:31 PM)
 * @author: Anuradha Lakshminarayana
 */
import cbit.vcell.server.VCDataIdentifier;
import cbit.vcell.client.server.VCDataManager;

public class MergedDynamicDataManager implements DynamicDataManager {
	private VCDataManager vcDataManager = null;
	private VCDataIdentifier mergedDataIdentifier = null;
	private MergedDataViewer mergedDataViewer = null;

/**
 * MergedDynamicDataManager constructor comment.
 */
public MergedDynamicDataManager(VCDataManager argVCDataManager, VCDataIdentifier argMergedDataID) {
	super();
	vcDataManager = argVCDataManager;
	mergedDataIdentifier = argMergedDataID;
}


/**
 * Insert the method's description here.
 * Creation date: (10/16/2005 2:42:43 PM)
 * @return javax.swing.JPanel
 */
public cbit.vcell.client.data.DataViewer createViewer(boolean expectODEData) throws cbit.util.DataAccessException {
	mergedDataViewer = new MergedDataViewer(vcDataManager, mergedDataIdentifier, expectODEData);
	return mergedDataViewer;
}


/**
 * 
 * @param event cbit.vcell.desktop.controls.SimulationEvent
 */
public void newData(cbit.vcell.desktop.controls.DataEvent event) {
	if (event.getVcDataIdentifier() instanceof MergedDataInfo) {
		try {
			refreshData();
		} catch (DataAccessException exc) {
			exc.printStackTrace(System.out);
		}
	}
}


/**
 * Insert the method's description here.
 * Creation date: (6/11/2004 2:43:49 PM)
 * @exception cbit.util.DataAccessException The exception description.
 */
public void refreshData() throws cbit.util.DataAccessException {
	mergedDataViewer.refreshData();
}
}