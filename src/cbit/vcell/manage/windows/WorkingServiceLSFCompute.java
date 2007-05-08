package cbit.vcell.manage.windows;

/**
 * Insert the type's description here.
 * Creation date: (6/20/2006 12:23:35 PM)
 * @author: Ion Moraru
 */
public class WorkingServiceLSFCompute extends AbstractVCellWorkingService {

/**
 * DaemonService constructor comment.
 */
public WorkingServiceLSFCompute() {
	// must be present for Class.newInstance() to succeed...
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (6/22/2006 1:45:49 PM)
 * @return java.lang.String
 */
public java.lang.String getServiceType() {
	return SERVICE_TYPE_COMPUTE_LSF;
}
/**
 * Insert the method's description here.
 * Creation date: (6/23/2006 7:31:30 PM)
 */
void startWork() throws java.lang.Throwable {
	// TODO
	int workerType = 0; // cbit.vcell.messaging.server.AbstractJmsWorker.LSF_WORKER should be public...
	double maxMemoryMB = 500.0; // should get this somehow from config... or maybe tuneable...
	new cbit.vcell.messaging.server.SimulationWorker(getName(), maxMemoryMB, workerType).start();
}
/**
 * Insert the method's description here.
 * Creation date: (6/23/2006 6:29:14 PM)
 */
void stopWork(int stopType) throws java.lang.Throwable {
	switch (stopType) {
		case STOP_SERVICE: {			
			// do something specific?
		}
		case STOP_SHUTDOWN: {			
			// do something specific?
		}
	}
	// could do something maybe...
}
}
