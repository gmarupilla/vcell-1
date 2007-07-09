// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package cbit.vcell.server;

import org.vcell.util.CacheStatus;

public final class LocalVCellServer_Stub
    extends java.rmi.server.RemoteStub
    implements cbit.vcell.server.VCellServer, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_createSolverController_0;
    private static java.lang.reflect.Method $method_getAdminDatabaseServer_1;
    private static java.lang.reflect.Method $method_getCacheStatus_2;
    private static java.lang.reflect.Method $method_getConnectedUsers_3;
    private static java.lang.reflect.Method $method_getConnectionPoolStatus_4;
    private static java.lang.reflect.Method $method_getProcessStatus_5;
    private static java.lang.reflect.Method $method_getServerInfo_6;
    private static java.lang.reflect.Method $method_getSlaveServerInfos_7;
    private static java.lang.reflect.Method $method_getSlaveVCellServer_8;
    private static java.lang.reflect.Method $method_getSolverControllerInfos_9;
    private static java.lang.reflect.Method $method_getVCellConnection_10;
    private static java.lang.reflect.Method $method_isPrimaryServer_11;
    private static java.lang.reflect.Method $method_shutdown_12;
    
    static {
	try {
	    $method_createSolverController_0 = cbit.vcell.server.VCellServer.class.getMethod("createSolverController", new java.lang.Class[] {org.vcell.util.document.User.class, cbit.vcell.solvers.SimulationJob.class});
	    $method_getAdminDatabaseServer_1 = cbit.vcell.server.VCellServer.class.getMethod("getAdminDatabaseServer", new java.lang.Class[] {});
	    $method_getCacheStatus_2 = cbit.vcell.server.VCellServer.class.getMethod("getCacheStatus", new java.lang.Class[] {});
	    $method_getConnectedUsers_3 = cbit.vcell.server.VCellServer.class.getMethod("getConnectedUsers", new java.lang.Class[] {});
	    $method_getConnectionPoolStatus_4 = cbit.vcell.server.VCellServer.class.getMethod("getConnectionPoolStatus", new java.lang.Class[] {});
	    $method_getProcessStatus_5 = cbit.vcell.server.VCellServer.class.getMethod("getProcessStatus", new java.lang.Class[] {});
	    $method_getServerInfo_6 = cbit.vcell.server.VCellServer.class.getMethod("getServerInfo", new java.lang.Class[] {});
	    $method_getSlaveServerInfos_7 = cbit.vcell.server.VCellServer.class.getMethod("getSlaveServerInfos", new java.lang.Class[] {});
	    $method_getSlaveVCellServer_8 = cbit.vcell.server.VCellServer.class.getMethod("getSlaveVCellServer", new java.lang.Class[] {java.lang.String.class});
	    $method_getSolverControllerInfos_9 = cbit.vcell.server.VCellServer.class.getMethod("getSolverControllerInfos", new java.lang.Class[] {});
	    $method_getVCellConnection_10 = cbit.vcell.server.VCellServer.class.getMethod("getVCellConnection", new java.lang.Class[] {org.vcell.util.document.User.class});
	    $method_isPrimaryServer_11 = cbit.vcell.server.VCellServer.class.getMethod("isPrimaryServer", new java.lang.Class[] {});
	    $method_shutdown_12 = cbit.vcell.server.VCellServer.class.getMethod("shutdown", new java.lang.Class[] {});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public LocalVCellServer_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of createSolverController(User, SimulationJob)
    public cbit.vcell.solvers.SolverController createSolverController(org.vcell.util.document.User $param_User_1, cbit.vcell.solvers.SimulationJob $param_SimulationJob_2)
	throws cbit.vcell.solvers.SolverException, cbit.vcell.solvers.SimExecutionException, java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_createSolverController_0, new java.lang.Object[] {$param_User_1, $param_SimulationJob_2}, 7441696505586765892L);
	    return ((cbit.vcell.solvers.SolverController) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (cbit.vcell.solvers.SolverException e) {
	    throw e;
	} catch (cbit.vcell.solvers.SimExecutionException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getAdminDatabaseServer()
    public cbit.vcell.modeldb.AdminDatabaseServer getAdminDatabaseServer()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getAdminDatabaseServer_1, null, 2401880152551856929L);
	    return ((cbit.vcell.modeldb.AdminDatabaseServer) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getCacheStatus()
    public CacheStatus getCacheStatus()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getCacheStatus_2, null, 7428034578312212220L);
	    return ((CacheStatus) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getConnectedUsers()
    public org.vcell.util.document.User[] getConnectedUsers()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getConnectedUsers_3, null, -6164111988825886192L);
	    return ((org.vcell.util.document.User[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getConnectionPoolStatus()
    public cbit.vcell.server.ConnectionPoolStatus getConnectionPoolStatus()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getConnectionPoolStatus_4, null, -810103427626191279L);
	    return ((cbit.vcell.server.ConnectionPoolStatus) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getProcessStatus()
    public cbit.vcell.server.ProcessStatus getProcessStatus()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getProcessStatus_5, null, 8183689588962228220L);
	    return ((cbit.vcell.server.ProcessStatus) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getServerInfo()
    public cbit.vcell.server.ServerInfo getServerInfo()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getServerInfo_6, null, 5171391575148593086L);
	    return ((cbit.vcell.server.ServerInfo) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getSlaveServerInfos()
    public cbit.vcell.server.ServerInfo[] getSlaveServerInfos()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getSlaveServerInfos_7, null, -1770714785429986682L);
	    return ((cbit.vcell.server.ServerInfo[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getSlaveVCellServer(String)
    public cbit.vcell.server.VCellServer getSlaveVCellServer(java.lang.String $param_String_1)
	throws cbit.vcell.server.AuthenticationException, cbit.vcell.server.ConnectionException, org.vcell.util.DataAccessException, java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getSlaveVCellServer_8, new java.lang.Object[] {$param_String_1}, 4346839914193805136L);
	    return ((cbit.vcell.server.VCellServer) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (cbit.vcell.server.AuthenticationException e) {
	    throw e;
	} catch (cbit.vcell.server.ConnectionException e) {
	    throw e;
	} catch (org.vcell.util.DataAccessException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getSolverControllerInfos()
    public cbit.vcell.solvers.SolverControllerInfo[] getSolverControllerInfos()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getSolverControllerInfos_9, null, 2179582928488209502L);
	    return ((cbit.vcell.solvers.SolverControllerInfo[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getVCellConnection(User)
    public cbit.vcell.server.VCellConnection getVCellConnection(org.vcell.util.document.User $param_User_1)
	throws org.vcell.util.DataAccessException, java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getVCellConnection_10, new java.lang.Object[] {$param_User_1}, -4865898240361431869L);
	    return ((cbit.vcell.server.VCellConnection) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (org.vcell.util.DataAccessException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of isPrimaryServer()
    public boolean isPrimaryServer()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_isPrimaryServer_11, null, -4473269658099639864L);
	    return ((java.lang.Boolean) $result).booleanValue();
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of shutdown()
    public void shutdown()
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_shutdown_12, null, -7207851917985848402L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
