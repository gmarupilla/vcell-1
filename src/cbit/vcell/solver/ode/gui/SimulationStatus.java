package cbit.vcell.solver.ode.gui;
import java.util.HashMap;

import org.vcell.util.MessageConstants;

import cbit.vcell.messaging.db.SimulationJobStatus;
import cbit.vcell.solver.*;
import cbit.rmi.event.*;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
/**
 * Insert the type's description here.
 * Creation date: (6/19/2001 3:00:45 PM)
 * @author: Ion Moraru
 */
public class SimulationStatus implements java.io.Serializable {
	// possible status values
	private static final int UNKNOWN = 0;
	private static final int NEVER_RAN = 1;
	private static final int START_REQUESTED = 2;
	private static final int DISPATCHED = 3;
	private static final int WAITING = 4;
	private static final int QUEUED = 5;
	private static final int RUNNING = 6;
	private static final int COMPLETED = 7;
	private static final int FAILED = 8;
	private static final int STOP_REQUESTED = 9;
	private static final int STOPPED = 10;
	private static final String STATUS_NAMES[] = {
		"unknown", "never ran", "submitted...", "dispatched...", "waiting: too many jobs",
		"queued", "running...", "completed", "failed", "stopping...", "stopped"
	};
	// actual info
	private int status = UNKNOWN;
	private HashMap<String, Double> progressHash = new HashMap<String, Double>();
	private String details = null;
	private boolean hasData = false;
	private SimulationJobStatus[] jobStatuses = null;

/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 1:28:48 PM)
 * @param status int
 * @param progress double
 * @param details java.lang.String
 * @param hasData boolean
 */
public SimulationStatus(SimulationJobStatus[] jobStatuses0) {
	this.jobStatuses = jobStatuses0;
	initStatus();
}


/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 1:28:48 PM)
 * @param status int
 * @param progress double
 * @param details java.lang.String
 * @param hasData boolean
 */
private SimulationStatus(int status, boolean hasData, int jobCount) {
	this.status = status;
	this.hasData = hasData;
	jobStatuses = new SimulationJobStatus[jobCount];
}


/**
 * Insert the method's description here.
 * Creation date: (10/18/2005 4:46:03 PM)
 * @param oldStatus cbit.vcell.solver.ode.gui.SimulationStatus
 */
private SimulationStatus(SimulationStatus oldStatus) {
	this.details = oldStatus.details;
	this.hasData = oldStatus.hasData;
	this.jobStatuses = oldStatus.jobStatuses;
	this.progressHash = oldStatus.progressHash;
	this.status = oldStatus.status;
}


/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 1:32:37 PM)
 * @return java.lang.String
 */
public java.lang.String getDetails() {
	return details != null ? details : getStatusString();
}


/**
 * Insert the method's description here.
 * Creation date: (6/28/2001 1:33:56 PM)
 * @return boolean
 */
public boolean getHasData() {
	return hasData;
}


/**
 * Insert the method's description here.
 * Creation date: (6/25/2001 1:22:22 PM)
 * @return java.lang.String
 */
public String getJob0StatusMessage() {
	return getJobStatuses()[0].getStatusMessage();
}


/**
 * Insert the method's description here.
 * Creation date: (8/21/2006 10:00:40 AM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus
 * @param index int
 */
public SimulationJobStatus getJobStatus(int index) {
	if (index >= jobStatuses.length) {
		return null;
	}
	return jobStatuses[index];
}


/**
 * Insert the method's description here.
 * Creation date: (9/29/2005 12:25:24 PM)
 * @return cbit.vcell.messaging.db.SimulationJobStatus[]
 */
private cbit.vcell.messaging.db.SimulationJobStatus[] getJobStatuses() {
	return jobStatuses;
}


/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 1:32:37 PM)
 * @return double
 */
public Double getProgress() {
	if (jobStatuses == null) return null;
	boolean bAllNullProgress = true;
	double progress = 0;
	for (int i = 0; i < jobStatuses.length; i++){
		if (jobStatuses[i] != null) {
			if (jobStatuses[i].isDone()) {
				progress += 1;
				bAllNullProgress = false;
			} else {
				Double jobProgress = progressHash.get(Integer.toString(jobStatuses[i].getJobIndex()));
				if (jobProgress != null) {
					bAllNullProgress = false;
					progress += jobProgress.doubleValue();
				}
			}
		}
	}
	if (bAllNullProgress) {
		return null;
	}
	return new Double(progress);
}


/**
 * Insert the method's description here.
 * Creation date: (8/21/2006 10:13:56 AM)
 * @return java.lang.Double
 * @param index int
 */
public Double getProgressAt(int index) {
	return progressHash.get(Integer.toString(index));
}


/**
 * Insert the method's description here.
 * Creation date: (6/25/2001 1:22:22 PM)
 * @return java.lang.String
 */
private String getStatusString() {
	return STATUS_NAMES[status];
}


/**
 * Insert the method's description here.
 * Creation date: (10/5/2005 12:50:07 PM)
 * @return cbit.vcell.solver.VCSimulationIdentifier
 */
public VCSimulationIdentifier getVCSimulationIdentifier() {
	if (jobStatuses != null && jobStatuses.length > 0) {
		return jobStatuses[0].getVCSimulationIdentifier();
	} else {
		return null;
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 11:25:17 AM)
 * @param newStatus int
 */
private void initStatus() {
	
	boolean allNull = true;
	for (int i = 0; i < jobStatuses.length; i++){
		if (jobStatuses[i] != null) {
			allNull = false;
			break;
		}
	}
	if (allNull) {
		status = UNKNOWN;
		return;
	} else {
		int highStatusIndex = -1;
		boolean bRunning = false;
		
		for (int i = 0; i < jobStatuses.length; i++){

			hasData = hasData  || jobStatuses[i].hasData();			
			
			int currentStatus = status;
			if (jobStatuses[i].isWaiting()) {
				status = Math.max(status, WAITING);
				bRunning = true;
			} else if (jobStatuses[i].isQueued()) {
				status = Math.max(status, QUEUED);
				bRunning = true;
			} else if (jobStatuses[i].isDispatched()) {			
				status = Math.max(status, DISPATCHED);
				bRunning = true;
			} else if (jobStatuses[i].isRunning()) {
				status = Math.max(status, RUNNING);
				bRunning = true;
			} else if (jobStatuses[i].isCompleted()) {
				status = Math.max(status, COMPLETED);
			} else if (jobStatuses[i].isStopped()) {
				status = Math.max(status, STOPPED);
			} else if (jobStatuses[i].isFailed()) {
				status = Math.max(status, FAILED);
			}
			if (status > currentStatus) highStatusIndex = i;
		}

		details = jobStatuses[highStatusIndex].getStatusMessage();

		if ((status == COMPLETED || status == STOPPED || status == FAILED) && bRunning) status = RUNNING;
		
	}
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isDispatched() {
	return status == DISPATCHED;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isFailed() {
	return status == FAILED;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isJob0Completed() {
	//
	// Convenience method used in TestingFrameworkWindowManager : 
	// In method 'updateSimRunningStatus', there is a need to check if simulation is completed. Since for the testing framework
	// only one simulation is considered, the jobindex is 0; hence we are checking the first jobStatus in the list of
	// SimulationJobStatuses corresponding to this simulationstatus.
	//
	return getJobStatuses()[0].isCompleted();
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isNeverRan() {
	return status == NEVER_RAN;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isRunnable() {
	//public static final int UNKNOWN = 0;
	//public static final int NEVER_RAN = 1;
	//public static final int START_REQUESTED = 2;
	//public static final int RUNNING = 3;
	//public static final int STOP_REQUESTED = 4;
	//public static final int STOPPED_BY_USER = 5;
	//public static final int COMPLETED = 6;
	//public static final int FAILED = 7;
	//public static final int DISPATCHED = 8;
	//public static final int QUEUED = 9;  
	//public static final int WAITING = 10;
	
	return status == NEVER_RAN || status == COMPLETED || status == FAILED
		|| status == STOPPED || status == UNKNOWN;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isRunning() {
	return status == RUNNING;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isStartRequested() {
	return status == START_REQUESTED;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isStoppable() {
	//public static final int UNKNOWN = 0;
	//public static final int NEVER_RAN = 1;
	//public static final int START_REQUESTED = 2;
	//public static final int RUNNING = 3;
	//public static final int STOP_REQUESTED = 4;
	//public static final int STOPPED_BY_USER = 5;
	//public static final int COMPLETED = 6;
	//public static final int FAILED = 7;
	//public static final int DISPATCHED = 8;
	//public static final int QUEUED = 9;  
	//public static final int WAITING = 10;
	
	return status == DISPATCHED || status == RUNNING || status == WAITING || status == QUEUED;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isStopRequested() {
	return status == STOP_REQUESTED;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public boolean isUnknown() {
	return status == UNKNOWN;
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 12:45:40 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param jobStatus0 cbit.vcell.messaging.db.SimulationJobStatus
 * @param progress java.lang.Double
 */
public static SimulationStatus newNeverRan(int jobCount) {
	SimulationStatus newStatus = new SimulationStatus(NEVER_RAN, false, jobCount);
	return newStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 12:45:40 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param jobStatus0 cbit.vcell.messaging.db.SimulationJobStatus
 * @param progress java.lang.Double
 */
public static SimulationStatus newStartRequest(int jobCount) {
	SimulationStatus newStatus = new SimulationStatus(START_REQUESTED, false, jobCount);
	return newStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 12:45:40 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param jobStatus0 cbit.vcell.messaging.db.SimulationJobStatus
 * @param progress java.lang.Double
 */
public static SimulationStatus newStartRequestFailure(String failMsg, int jobCount) {
	SimulationStatus newStatus = new SimulationStatus(FAILED, false, jobCount);
	newStatus.details = failMsg;
	return newStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 12:45:40 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param jobStatus0 cbit.vcell.messaging.db.SimulationJobStatus
 * @param progress java.lang.Double
 */
public static SimulationStatus newStopRequest(SimulationStatus currentStatus) {
	SimulationStatus newStatus = new SimulationStatus(currentStatus);
	newStatus.status = STOP_REQUESTED;
	return newStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (2/10/2004 12:45:40 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param jobStatus0 cbit.vcell.messaging.db.SimulationJobStatus
 * @param progress java.lang.Double
 */
public static SimulationStatus newUnknown(int jobCount) {
	SimulationStatus newStatus = new SimulationStatus(UNKNOWN, false, jobCount);
	return newStatus;
}


/**
 * Insert the method's description here.
 * Creation date: (7/3/2003 10:28:24 AM)
 * @return boolean
 */
public int numberOfJobsDone() {
	int done = 0;
	for (int i = 0; i < getJobStatuses().length; i++){
		if (getJobStatuses()[i] != null && getJobStatuses()[i].isDone()) {
			done ++;
		}
	}
	return done;
}


/**
 * Insert the method's description here.
 * Creation date: (8/3/2001 9:58:46 PM)
 * @return java.lang.String
 */
public String toString() {
	return "SimulationStatus=\""+getStatusString()+"\", hasData="+getHasData()+", progress="+getProgress()+", details="+((getDetails()!=null)?(getDetails()):("null"));
}


/**
 * Insert the method's description here.
 * Creation date: (10/6/2005 8:10:46 PM)
 * @return cbit.vcell.solver.ode.gui.SimulationStatus
 * @param oldStatus cbit.vcell.solver.ode.gui.SimulationStatus
 * @param simJobStatusEvent cbit.rmi.event.SimulationJobStatusEvent
 */
public static SimulationStatus updateFromJobEvent(SimulationStatus oldStatus, SimulationJobStatusEvent simJobStatusEvent) {

	// ignore empty messages
	if (simJobStatusEvent.getJobStatus() == null) {
		return oldStatus;
	}

	SimulationJobStatus newJobStatus = simJobStatusEvent.getJobStatus();
	Double newProgress = simJobStatusEvent.getProgress();
	
	SimulationStatus newSimStatus = null;	
	// update when no existing status
	
	if (oldStatus == null || oldStatus.getJobStatuses().length == 0) {
		newSimStatus = new SimulationStatus(new SimulationJobStatus[] {newJobStatus});
		newSimStatus.progressHash.put(Integer.toString(newJobStatus.getJobIndex()), newProgress);
		return newSimStatus;
	}
	
	/* there's a status in message, as well as some existing status(es), check which is more recent */

	// figure out task ID ordinality...
	int someOldID = oldStatus.getJobStatuses()[0].getTaskID(); // doesn't matter which one, should all be on same block
	int newID = newJobStatus.getTaskID();
	if (newID - newID % MessageConstants.TASKID_USERINCREMENT > someOldID) {
		// upper block; event comes from a new submission; discard all old stuff
		newSimStatus = new SimulationStatus(new SimulationJobStatus[] {newJobStatus});
		newSimStatus.progressHash.put(Integer.toString(newJobStatus.getJobIndex()), newProgress);
		return newSimStatus;
	} else if (someOldID - someOldID % MessageConstants.TASKID_USERINCREMENT > newID) {
		// lower block; event comes from an old submission; ignore
		return oldStatus;
	}

	/* same block, so compare individual job statuses */

	// do we already have a status for this job?
	SimulationJobStatus oldJobStatus = null;
	int oldJobStatusIndex = -1;
	for (int i = 0; i < oldStatus.getJobStatuses().length; i++){
		SimulationJobStatus sjs = oldStatus.getJobStatuses()[i];
		if (sjs.getJobIndex() == newJobStatus.getJobIndex()) {
			oldJobStatus = sjs;
			oldJobStatusIndex = i;
		}
	}
	if (oldJobStatus == null) {
		// we have nothing for this job, update
		SimulationJobStatus[] newJobStatuses = (SimulationJobStatus[])org.vcell.util.BeanUtils.addElement(oldStatus.getJobStatuses(), newJobStatus);
		newSimStatus = new SimulationStatus(newJobStatuses);
		newSimStatus.progressHash = oldStatus.progressHash;
		newSimStatus.progressHash.put(Integer.toString(newJobStatus.getJobIndex()), newProgress);
		return newSimStatus;
	}
	
	/* we have already a status for this job, check which is more recent */

	if (newID < oldJobStatus.getTaskID()) {
		// ignore, we know about a newer retry running
		return oldStatus;
	} else if (newID > oldJobStatus.getTaskID()) {
		// update with new instance status
		oldStatus.getJobStatuses()[oldJobStatusIndex] = newJobStatus;
		newSimStatus = new SimulationStatus(oldStatus.getJobStatuses());
		newSimStatus.progressHash = oldStatus.progressHash;
		newSimStatus.progressHash.put(Integer.toString(newJobStatus.getJobIndex()), newProgress);
		return newSimStatus;
	}

	/* same instance, so compare event details */

	// ignore out of order events
	if (oldJobStatus.isDone()) {
		return oldStatus;
	} else {
		// figure out old progress
		Double jobProgress = oldStatus.progressHash.get(Integer.toString(oldJobStatus.getJobIndex()));
		// now check
		if (oldJobStatus.isRunning() &&	newJobStatus.isRunning() &&	jobProgress != null &&
			newProgress != null &&	jobProgress.doubleValue() > newProgress.doubleValue()) {
			return oldStatus;
		}
	}
				
	// update with new status
	oldStatus.getJobStatuses()[oldJobStatusIndex] = newJobStatus;
	newSimStatus = new SimulationStatus(oldStatus.getJobStatuses());
	newSimStatus.progressHash = oldStatus.progressHash;
	newSimStatus.progressHash.put(Integer.toString(newJobStatus.getJobIndex()), newProgress);
	return newSimStatus;
}
}