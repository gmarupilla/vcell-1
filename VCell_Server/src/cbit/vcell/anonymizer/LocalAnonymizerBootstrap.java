package cbit.vcell.anonymizer;
import cbit.gui.PropertyLoader;
import cbit.util.DataAccessException;
import cbit.util.SessionLog;
import cbit.util.StdoutSessionLog;
import cbit.util.User;
import cbit.vcell.server.WatchdogMonitor;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.server.VCellBootstrap;
import cbit.vcell.server.VCellConnection;
import cbit.vcell.server.AuthenticationException;
import cbit.vcell.server.VCellServer;
import cbit.vcell.server.PermissionException;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class LocalAnonymizerBootstrap extends UnicastRemoteObject implements VCellBootstrap {
	private SessionLog sessionLog = new StdoutSessionLog("AnonymizerBootstrap");
	private String localHost = null;
	private int localPort = 0;
	private String remoteHost = null;
	private int remotePort = 0;

	private AnonymizerSetup anonymizerSetup = null;

	private String softwareVersion = null;

/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
private LocalAnonymizerBootstrap(String argLocalHost, int argLocalPort, String argRemoteHost, int argRemotePort) throws RemoteException, FileNotFoundException, DataAccessException {
	super(argLocalPort);
	this.localHost = argLocalHost;
	this.localPort = argLocalPort;
	this.remoteHost = argRemoteHost;
	this.remotePort = argRemotePort;

	checkUpdate();
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:37:35 PM)
 */
private void checkUpdate() {
	Thread checkUpdateThread = new Thread() {
		public void run() {
			while (true) {
				try {
					Thread.sleep(3600 * 1000);
					if (softwareVersion == null) {
						softwareVersion = getRemoteVCellSoftwareVersion();						
					} 
					
					String remoteSoftwareVersion = getRemoteVCellSoftwareVersion();
					sessionLog.print("current softwareVersion is " + softwareVersion + ", remote software version is " + remoteSoftwareVersion);
					if (softwareVersion == null || !softwareVersion.equals(remoteSoftwareVersion)) { 
						sessionLog.print("----------------------------------------------------------");
						sessionLog.print("Can't synchronzie with the virtual cell server, exiting...");
						sessionLog.print("----------------------------------------------------------");
						System.exit(0);
					}					
				} catch (InterruptedException ex) {
					sessionLog.exception(ex);
				}
			}
		}
	};

	checkUpdateThread.start();
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:51:06 PM)
 * @return java.lang.String
 */
private java.lang.String getRemoteVCellSoftwareVersion() {
	return cbit.vcell.server.RMIVCellConnectionFactory.getVCellSoftwareVersion(remoteHost+":"+remotePort);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public VCellConnection getVCellConnection(String userid, String password) throws DataAccessException, AuthenticationException, RemoteException {
	try {
		cbit.vcell.server.RMIVCellConnectionFactory rmiVCellConnectionFactory = new cbit.vcell.server.RMIVCellConnectionFactory(remoteHost+":"+remotePort,userid,password);
		AnonymizerVCellConnection vcConn = new AnonymizerVCellConnection(rmiVCellConnectionFactory, new StdoutSessionLog(userid));
		return vcConn;
	}catch (cbit.vcell.server.ConnectionException e){
		e.printStackTrace(System.out);
		throw new RemoteException(e.getMessage(),e);
	}
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSetController
 * @exception java.lang.Exception The exception description.
 */
public VCellServer getVCellServer(User user, String password) throws DataAccessException, AuthenticationException, PermissionException {
	throw new AuthenticationException("Anonymous connection not allowed for administrative access");
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:51:06 PM)
 * @return java.lang.String
 */
public java.lang.String getVCellSoftwareVersion() throws RemoteException {
	return softwareVersion;
}


/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 12:29:59 PM)
 */
private void loadProperties() {
	try {
		PropertyLoader.loadAnonymizerProperties();
		
		String logfilename = PropertyLoader.getProperty(PropertyLoader.vcellAnonymizerBootstrapLogfile, null);		
		if (logfilename != null) {
			sessionLog.print("trying to log to file [" + logfilename + "]");	
			File logFile = new File(logfilename);
			if (logFile.getParentFile() != null && !logFile.getParentFile().exists()) {
				logFile.getParentFile().mkdirs();				
			}
			sessionLog.print("log is redirected to file [" + logfilename + "]");
			System.setOut(new PrintStream(new FileOutputStream(logFile)));
		}


		localHost = PropertyLoader.getRequiredProperty(PropertyLoader.vcellAnonymizerBootstrapLocalHost);
		localPort = Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.vcellAnonymizerBootstrapLocalPort));
		remoteHost = PropertyLoader.getRequiredProperty(PropertyLoader.vcellAnonymizerBootstrapRemoteHost);
		remotePort = Integer.parseInt(PropertyLoader.getRequiredProperty(PropertyLoader.vcellAnonymizerBootstrapRemotePort));

		sessionLog.print("local server : " + localHost + ":" + localPort);
		sessionLog.print("remote server : " + remoteHost + ":" + remotePort);		
	} catch (Exception ex) {
		ex.printStackTrace();
	}	
}


/**
 * main entrypoint - starts the application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	if (args.length != 5) {
		System.out.println("usage: cbit.vcell.anonymizer.AnonymizerBootstrap localHost localPort remoteHost remotePort (logfile | -) \n");
		System.out.println(" example -  cbit.vcell.anonymizer.AnonymizerBootstrap ms3.vcell.uchc.edu 80 server.log");
		System.exit(1);
	}
	try {
		//
		// Redirect output to the logfile (append if exists)
		//
		if (!args[4].equals("-")){
			System.setOut(new PrintStream(new FileOutputStream(args[4], true), true));
		}
		
		//
		// Create and install a security manager
		//
		System.setSecurityManager(new RMISecurityManager());

		
		Thread.currentThread().setName("AnonymizerBootstrapThread");


		//
		// get LocalHost and LocalPort
		//
		String localhost = args[0];
		if (localhost.equals("localhost")){
			try {
				localhost = java.net.InetAddress.getLocalHost().getHostName();
			}catch (java.net.UnknownHostException e){
				// do nothing, "localhost" is ok
			}
		}
		int localPort = Integer.parseInt(args[1]);
		
		System.getProperties().put(PropertyLoader.rmiPortAdminDbServer,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortDataSetController,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortMessageHandler,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortRegistry,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortSimulationController,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortSolverController,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortUserMetaDbServer,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortVCellBootstrap,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortVCellConnection,Integer.toString(localPort));
		System.getProperties().put(PropertyLoader.rmiPortVCellServer,Integer.toString(localPort));

		String remoteHost = args[2];
		if (remoteHost.equals("localhost")){
			try {
				remoteHost = java.net.InetAddress.getLocalHost().getHostName();
			}catch (java.net.UnknownHostException e){
				// do nothing, "localhost" is ok
			}
		}
		int remotePort = Integer.parseInt(args[3]);

		
		SessionLog log = new StdoutSessionLog("local(unauthenticated)_administrator");
		
		LocalAnonymizerBootstrap anonymizerBootstrap = new LocalAnonymizerBootstrap(localhost,localPort,remoteHost,remotePort);
		
		//
		// spawn the WatchdogMonitor (which spawns the RMI registry, and binds the anonymizerBootstrap)
		//
		long minuteMS = 60000;
		long monitorSleepTime = 20*minuteMS;
		String rmiUrl = "//" + localhost + ":" + localPort + "/VCellBootstrapServer";
		Thread watchdogMonitorThread = new Thread(new WatchdogMonitor(monitorSleepTime,localPort,rmiUrl,anonymizerBootstrap,"slave"),"WatchdogMonitor");
		watchdogMonitorThread.setDaemon(true);
		watchdogMonitorThread.setName("WatchdogMonitor");
		watchdogMonitorThread.start();

	} catch (Throwable e) {
		System.out.println("LocalVCellBootstrap err: " + e.getMessage());
		e.printStackTrace();
	}
}
}