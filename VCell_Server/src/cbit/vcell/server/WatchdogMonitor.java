package cbit.vcell.server;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.rmi.*;

import cbit.gui.PropertyLoader;
/**
 * This type was created in VisualAge.
 */
public class WatchdogMonitor implements Runnable {
	private VCellBootstrap vcellBootstrap = null;
	private int rmiPort = 0;
	private String rmiUrl = null;
	private long sleepTimeMS = 0;
	private String serverConfig = null;

/**
 * ThreadMonitor constructor comment.
 */
public WatchdogMonitor(long argSleepTimeMS, int argRmiPort, String argRmiURL, VCellBootstrap argVCellBootstrap, String argServerConfig) {
	super();
	this.rmiPort = argRmiPort;
	this.rmiUrl = argRmiURL;
	this.vcellBootstrap = argVCellBootstrap;
	this.sleepTimeMS = argSleepTimeMS;
	this.serverConfig = argServerConfig;
}


/**
 * This method was created in VisualAge.
 */
private void printThreads() {
	//
	// get parent threadGroup
	//
	ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
	while (threadGroup.getParent()!=null){
		threadGroup = threadGroup.getParent();
	}

	//
	// display threadGroups recursively
	//
	threadGroup.list();
}


/**
 * This method was created in VisualAge.
 */
public void run() {
	try {
		java.rmi.registry.LocateRegistry.createRegistry(rmiPort); // start rmiregistry manually
	}catch (Throwable e){
		e.printStackTrace(System.out);
	}
	while (true){
		try {
			//
			// check that the RMI Registry exists on this port, or create new one if necessary
			//
			java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(rmiPort);
			boolean registryGood = true;
			if (registry==null){
				registryGood = false;
			}else{
				try {
					registry.list();
					registryGood = true;
				}catch (java.rmi.ConnectException e){
					e.printStackTrace(System.out);
					System.out.println("....RMI registry.list() failed");
				}
			}

			
			if (!registryGood){
				System.out.println("RMI Registry either doesn't exist or is sick on port "+rmiPort);
				//
				// spawn the RMIRegistry
				//
				registry = java.rmi.registry.LocateRegistry.createRegistry(rmiPort); // start rmiregistry manually
				if (registry == null){
					throw new RemoteException("cannot get RMI Registry at port "+rmiPort);
				}else{
					System.out.println("RMI Registry created on port "+rmiPort);
				}
			}


			//
			// make sure that the LocalVCellBootstrap is mapped 
			//
			try {
				System.out.println("attempting to lookup remote object at URL \""+rmiUrl+"\"");
				Naming.lookup(rmiUrl);
				System.out.println("localVCellBootstrap was already bound to URL "+rmiUrl);
			}catch (UnexpectedException e){
				System.out.println("localVCellBootstrap was not bound to URL "+rmiUrl+", rebinding now");
				Naming.rebind(rmiUrl,vcellBootstrap);
				String time = java.util.Calendar.getInstance().getTime().toString();
				System.out.println("VCellBootstrapServer bound in registry as " + rmiUrl + " at " + time + " as " + serverConfig);
			}catch (NotBoundException e){
				System.out.println("localVCellBootstrap was not bound to URL "+rmiUrl+", rebinding now");
				Naming.rebind(rmiUrl,vcellBootstrap);
				String time = java.util.Calendar.getInstance().getTime().toString();
				System.out.println("VCellBootstrapServer bound in registry as " + rmiUrl + " at " + time + " as " + serverConfig);
			}
			
			
		}catch (RemoteException e){
			e.printStackTrace(System.out);
			
		}catch (java.net.MalformedURLException e){
			e.printStackTrace(System.out);
			System.exit(0);

		}catch (Throwable e){
			e.printStackTrace(System.out);

		}finally{
			//
			// print threads
			//
			printThreads();
		}
		
		//
		// re-read the property file
		//
		try {
			PropertyLoader.loadProperties();
		}catch (Throwable e){
			e.printStackTrace(System.out);
		}

		//
		// sleep for a while
		//
		try {
			Thread.sleep(sleepTimeMS);
		}catch (InterruptedException e){
		}
	}	
}
}