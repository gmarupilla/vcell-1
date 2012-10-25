/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package org.vcell.util;
import java.io.*;

/**
 * Insert the type's description here.
 * Creation date: (10/22/2002 4:33:29 PM)
 * @author: Ion Moraru
 */
public class Executable {
	private String[] command = null;
	private Process process = null;
	private String outputString = "";
	private String errorString = "";
	private Integer exitValue = null;
	private ExecutableStatus status = null;
	private long timeoutMS = 0;
	private File workingDir = null;
	private String[] execEnvVars = null;
	
	public static final int MAX_OUTPUT_UNLIMITED = -1;
	private static final int MAX_OUTPUT_DEFAULT = 10000;
	private int maxOutputSize = MAX_OUTPUT_DEFAULT;
/**
 * sometimes command and input file name have space, we need to escape space;
 * So the easiest way to run the command is to call Runtime.exec(String[] cmd)
 * where cmd[0] is the exectuable and the rest are the arguments. 
 * If we use this method, we dont have to escape the space at all. 
 * 
 * if we submit the job to PBS, then we need a real unix command, then we must escape the space. 
 * This is done in getCommand(). 
 * 
 * These are tests I have done.
 * 
	// Mac doesn't work
	// Linux doesn't work
	// Windows doesn't work	
	String command = exe + " " + path;

	// Mac doesn't work
	// Linux doesn't work
	// Windows works, weird, 	
	String command = exe + " " + escapedPath;
	
	// Mac doesn't work
	// Linux doesn't work
	// Windows works
	String command = escapedExe + " " + escapedPath;

	//Mac works
	// Linux works
	// Windows works
	String[] command = new String[] {exe, path};

	//Mac  doesn't work
	// Linux doesnt work
	// Windows works
	String[] command = new String[] {escapedExe, escapedPath};

	// Mac works	
	// Linux doesnt work
	// Windows works
	String[] command = new String[] {exe, escapedPath};
 * 
 */

public Executable(String[] command) {
	this(command, 0);
}

/**
 * Executable constructor comment.
 */
public Executable(String[] command, long timeoutMS) {
	this(command,timeoutMS,MAX_OUTPUT_DEFAULT);
}

public Executable(String[] command, long timeoutMS,int maxOutputSize) {
	setCommand(command);
	setStatus(ExecutableStatus.READY);
	this.timeoutMS = timeoutMS;
	this.maxOutputSize = maxOutputSize;
}

/**
 * This method was created by a SmartGuide.
 */
protected void executeProcess() throws org.vcell.util.ExecutableException {
	
	System.out.println("Executable.executeProcess(" + getCommand() + ") starting...");
	try {
		// reset just in case
		setOutputString("");
		setErrorString("");
		setExitValue(null);
		// start the process
		if(workingDir == null && execEnvVars == null){
			setProcess(Runtime.getRuntime().exec(command));
		}else{
			setProcess(Runtime.getRuntime().exec(command,execEnvVars,workingDir));
		}
		// monitor the process; blocking call
		// will update the fields from StdOut and StdErr
		// will return the exit code once the process terminates
		int exitCode = monitorProcess(getProcess().getInputStream(), getProcess().getErrorStream(), 1000);
		setExitValue(new Integer(exitCode));
		// log what happened and update status
		if (getStatus().equals(org.vcell.util.ExecutableStatus.STOPPED)) {
			System.out.println("\nExecutable.executeProcess(" + getCommand() + ") STOPPED\n");
		} else if (getExitValue().intValue() == 0) {
			System.out.println("\nExecutable.executeProcess(" + getCommand() + ") executable successful\n");
			setStatus(ExecutableStatus.COMPLETE);
		} else {
			System.out.println("\nExecutable.executeProcess(" + getCommand() + ") executable failed, return code = " + getExitValue() + "\n");
			setStatus(ExecutableStatus.getError("executable failed, return code = " + getExitValue() + "\nstderr = '" + getErrorString() + "'"));
		}
		// log output
		System.out.println("Executable.executeProcess(" + getCommand() + ") stdout:\n" + getOutputString());
		// finally, throw if it was a failure
		if (getStatus().isError()) {
			throw new Exception(getErrorString());
		}
	} catch (Throwable e) {
		if (getStatus().isError()) {
			// process failed and we relay the exception thrown on error status finish above
			throw new ExecutableException(e.getMessage() + "\n\n(" + getCommand() + ")");
		} else {
			//something really unexpected happened, update status and log it before relaying...
			setStatus(ExecutableStatus.getError("error running executable " + e.getMessage()));
			e.printStackTrace(System.out);
			throw new ExecutableException("Unexpected error: " + e.getMessage() + "\n\n(" + getCommand() + ")");
		}			
	}
}


/**
 * This method was created in VisualAge.
 */
public void finalize() {
	stop();
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 4:34:39 PM)
 * @return java.lang.String
 */
public String getCommand() {
	StringBuffer commandLine = new StringBuffer();
	for (int i = 0; i < command.length; i ++) {
		if (i > 0) {
			commandLine.append(" ");
		}		
		commandLine.append(TokenMangler.getEscapedPathName(command[i]));		
	}
	return commandLine.toString();
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:35:11 PM)
 * @return java.lang.String
 */
private java.lang.String getErrorString() {
	return errorString;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:57:56 PM)
 * @return java.lang.Integer
 */
public java.lang.Integer getExitValue() {
	return exitValue;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:35:11 PM)
 * @return java.lang.String
 */
private java.lang.String getOutputString() {
	return outputString;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 4:46:10 PM)
 * @return java.lang.Process
 */
private java.lang.Process getProcess() {
	return process;
}


/**
 * Insert the method's description here.
 * Creation date: (10/23/2002 12:23:37 PM)
 * @return cbit.vcell.solvers.ExecutableStatus
 */
public org.vcell.util.ExecutableStatus getStatus() {
	return status;
}


/**
 * Insert the method's description here.
 * Creation date: (10/23/2002 3:12:48 PM)
 * @return java.lang.String
 */
public String getStderrString() {
	return getErrorString();
}


/**
 * Insert the method's description here.
 * Creation date: (10/23/2002 3:12:48 PM)
 * @return java.lang.String
 */
public String getStdoutString() {
	return getOutputString();
}


public static void main(java.lang.String[] args) {
	try {
		Executable executable = new Executable(args);
		executable.start();
	}catch (ExecutableException e) {
		System.out.println("\nExecutable Exception thrown, normally handled upstream by other classes...");
	}
}


/**
 * This method was created in VisualAge.
 */
protected final int monitorProcess(InputStream inputStreamOut, InputStream inputStreamErr, long pollingIntervalMS) throws ExecutableException {
	InputStreamReader inputStreamReaderOut = null;
	InputStreamReader inputStreamReaderErr = null;
	try{
		long t = System.currentTimeMillis();
		char charArrayOut[] = new char[MAX_OUTPUT_DEFAULT];
		char charArrayErr[] = new char[MAX_OUTPUT_DEFAULT];
		String outString = new String();
		String errString = new String();
		int numReadOut = 0; int numReadErr = 0; int exitValue = 0;
		inputStreamReaderOut = new InputStreamReader(inputStreamOut);
		inputStreamReaderErr = new InputStreamReader(inputStreamErr);
	
		boolean running = true;
		while (running || (numReadOut > 0) || (numReadErr > 0)) {
			if (timeoutMS > 0 && System.currentTimeMillis() - t > timeoutMS) {
				throw new ExecutableException("Process timed out");
			}
			try {
				exitValue = getProcess().exitValue();
				running = false;
			} catch (IllegalThreadStateException e) {
				// process didn't exit yet, do nothing
			}
			try {
				if (pollingIntervalMS > 0) Thread.sleep(pollingIntervalMS);
			} catch (InterruptedException e) {
			}
			try {
				if (inputStreamOut.available() > 0) {
					numReadOut = inputStreamReaderOut.read(charArrayOut, 0, charArrayOut.length);
				} else {
					numReadOut = 0;
				}
			} catch (IOException ioexc) {
				System.out.println("EXCEPTION (process " + getCommand() + ") - IOException while reading StdOut: " + ioexc.getMessage());
				numReadOut = 0;
			}
			try {
				if (inputStreamErr.available() > 0) {
					numReadErr = inputStreamReaderErr.read(charArrayErr, 0, charArrayErr.length);
				} else {
					numReadErr = 0;
				}
			} catch (IOException ioexc) {
				System.out.println("EXCEPTION (process " + getCommand() + ") - IOException while reading StdErr: " + ioexc.getMessage());
				numReadErr = 0;
			}
			if (numReadOut > 0) {
				outString = enforceOutputSize(charArrayOut, numReadOut, outString);
			}
			if (numReadErr > 0) {
				errString = enforceOutputSize(charArrayErr, numReadErr, errString);
			}
			setOutputString(outString);
			setErrorString(errString);
		}
		return exitValue;
	}finally{
		if(inputStreamReaderOut != null){try{inputStreamReaderOut.close();}catch(Exception e){e.printStackTrace();}}
		if(inputStreamReaderErr != null){try{inputStreamReaderErr.close();}catch(Exception e){e.printStackTrace();}}
	}
}

private String enforceOutputSize(char[] newlyRead,int newlyReadSize,String outAccum) throws ExecutableException{
	if((maxOutputSize != MAX_OUTPUT_UNLIMITED) && ((outAccum.length()+newlyReadSize) > maxOutputSize)){
		throw new ExecutableException("MaxOutputSize exceeded "+(outAccum.length()+newlyReadSize)+" > "+(maxOutputSize));
	}
	return outAccum + new String(newlyRead, 0, newlyReadSize);	
	
}

/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 4:34:39 PM)
 * @param newCommand java.lang.String
 */
private void setCommand(String[] newCommand) {
	command = newCommand;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:35:11 PM)
 * @param newErrorString java.lang.String
 */
private void setErrorString(java.lang.String newErrorString) {
	errorString = newErrorString;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:57:56 PM)
 * @param newExitValue java.lang.Integer
 */
private void setExitValue(java.lang.Integer newExitValue) {
	exitValue = newExitValue;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 5:35:11 PM)
 * @param newOutputString java.lang.String
 */
protected void setOutputString(java.lang.String newOutputString) {
	outputString = newOutputString;
}


/**
 * Insert the method's description here.
 * Creation date: (10/22/2002 4:46:10 PM)
 * @param newProcess java.lang.Process
 */
private void setProcess(java.lang.Process newProcess) {
	process = newProcess;
}


/**
 * Insert the method's description here.
 * Creation date: (10/23/2002 12:23:37 PM)
 * @param newStatus cbit.vcell.solvers.ExecutableStatus
 */
private void setStatus(org.vcell.util.ExecutableStatus newStatus) {
	status = newStatus;
}


/**
 * This method was created in VisualAge.
 */
public final void start() throws org.vcell.util.ExecutableException {

    setStatus(ExecutableStatus.RUNNING);
    try {
        executeProcess();
    } catch (ExecutableException e) {
        e.printStackTrace(System.out);
        setStatus(ExecutableStatus.getError("Executable Exception: " + e.getMessage()));
        throw e;
    }
}


/**
 * This method was created in VisualAge.
 */
public final void stop() {
	setStatus(ExecutableStatus.STOPPED);
	if (getProcess() != null) {
		getProcess().destroy();
	}
}

public File getWorkingDir() {
	return workingDir;
}

public void setWorkingDir(File workingDir) {
	this.workingDir = workingDir;
}

public String[] getExecEnvVars() {
	return execEnvVars;
}

public void setExecEnvVars(String[] execEnvVars) {
	this.execEnvVars = execEnvVars;
}
}
