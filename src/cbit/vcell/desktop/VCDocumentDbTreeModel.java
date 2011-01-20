package cbit.vcell.desktop;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.ArrayList;

import javax.swing.JTree;

import org.vcell.util.DataAccessException;
import org.vcell.util.document.VCDocumentInfo;

import cbit.vcell.client.desktop.DatabaseSearchPanel.SearchCriterion;
import cbit.vcell.clientdb.DatabaseListener;
import cbit.vcell.clientdb.DocumentManager;
/**
 * Insert the type's description here.
 * Creation date: (2/14/01 3:33:23 PM)
 * @author: Jim Schaff
 */
@SuppressWarnings("serial")
public abstract class VCDocumentDbTreeModel extends javax.swing.tree.DefaultTreeModel implements DatabaseListener {
	protected boolean fieldLatestOnly = false;
	private transient java.beans.PropertyChangeSupport propertyChange;
	protected DocumentManager fieldDocumentManager = null;
	protected ArrayList<SearchCriterion> searchCriterionList = null;
	protected BioModelNode rootNode = null;
	protected BioModelNode myModelsNode = null;
	protected BioModelNode sharedModelsNode = null;
	private JTree ownerTree = null;
	
	public static final String USER_tutorial = "tutorial";
	public static final String USER_Education = "Education";
	public static final String USER_CellMLRep = "CellMLRep";
	
	protected BioModelNode tutorialModelsNode = null;
	protected BioModelNode educationModelsNode = null;
	protected BioModelNode cellMLModelsNode = null;
	protected BioModelNode publicModelsNode = null;

/**
 * BioModelDbTreeModel constructor comment.
 * @param root javax.swing.tree.TreeNode
 */
public VCDocumentDbTreeModel(JTree tree) {
	super(new BioModelNode("not connected", true),true);
	ownerTree = tree;
	rootNode = (BioModelNode)root;
	myModelsNode = new BioModelNode("My Models", true);
	sharedModelsNode = new BioModelNode("Shared Models", true);
}
/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}
/**
 * Insert the method's description here.
 * Creation date: (11/28/00 1:06:51 PM)
 * @return cbit.vcell.desktop.BioModelNode
 * @param docManager cbit.vcell.clientdb.DocumentManager
 */
protected abstract void createBaseTree() throws DataAccessException;

protected boolean meetSearchCriteria(VCDocumentInfo vcDocumentInfo) {
	if (searchCriterionList == null) {
		return true;		
	}
	boolean bPass = true;
	for (SearchCriterion sc : searchCriterionList) {
		if (!sc.meetCriterion(vcDocumentInfo)) {
			bPass = false;
			break;
		}		
	}
	return bPass;
}

/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * Gets the documentManager property (cbit.vcell.clientdb.DocumentManager) value.
 * @return The documentManager property value.
 * @see #setDocumentManager
 */
public DocumentManager getDocumentManager() {
	return fieldDocumentManager;
}
/**
 * Gets the latestOnly property (boolean) value.
 * @return The latestOnly property value.
 * @see #setLatestOnly
 */
public boolean getLatestOnly() {
	return fieldLatestOnly;
}

/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}
/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(java.lang.String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}

public void refreshTree() {
	if (getDocumentManager() != null && getDocumentManager().getUser() != null){
		try {
			createBaseTree();
		}catch (DataAccessException e){
			e.printStackTrace(System.out);
		}
	} else {
		rootNode.setUserObject("not connected");
		rootNode.removeAllChildren();
	}
	nodeStructureChanged(rootNode);
}
/**
 * Insert the method's description here.
 * Creation date: (2/14/01 3:50:24 PM)
 */
public void refreshTree(ArrayList<SearchCriterion> newFilterList) {
	searchCriterionList = newFilterList;
	refreshTree();
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}
/**
 * Sets the documentManager property (cbit.vcell.clientdb.DocumentManager) value.
 * @param documentManager The new value for the property.
 * @see #getDocumentManager
 */
public void setDocumentManager(DocumentManager documentManager) {
	DocumentManager oldValue = fieldDocumentManager;
	fieldDocumentManager = documentManager;

	if (oldValue != null){
		oldValue.removeDatabaseListener(this);
	}
	if (documentManager != null){
		documentManager.addDatabaseListener(this);
	}

	firePropertyChange("documentManager", oldValue, documentManager);

	if (documentManager != oldValue){
		refreshTree();
	}
}
/**
 * Sets the latestOnly property (boolean) value.
 * @param latestOnly The new value for the property.
 * @see #getLatestOnly
 */
public void setLatestOnly(boolean latestOnly) {
	boolean oldValue = fieldLatestOnly;
	fieldLatestOnly = latestOnly;
	firePropertyChange("latestOnly", new Boolean(oldValue), new Boolean(latestOnly));
	if (latestOnly != oldValue){
		refreshTree();
	}
}
}
