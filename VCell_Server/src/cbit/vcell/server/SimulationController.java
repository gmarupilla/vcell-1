package cbit.vcell.server;
import cbit.util.DataAccessException;
import cbit.util.ObjectNotFoundException;
import cbit.vcell.server.PermissionException;
import cbit.vcell.solver.*;
import cbit.vcell.messaging.db.SimulationJobStatus;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.rmi.*;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public interface SimulationController extends java.rmi.Remote {
/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void startSimulation(VCSimulationIdentifier vcSimulationIdentifier) throws RemoteException;


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void stopSimulation(VCSimulationIdentifier vcSimulationIdentifier) throws RemoteException;
}