/**
 * 
 */
package com.elitecore.jmx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.elitecore.util.commons.EnvironmentVarAndPathUtil;
import com.elitecore.util.k8s.CreateServerInstanceScript;
import com.elitecore.util.logger.Logger;


/**
 * @author Sunil Gulabani
 * Sep 1, 2015
 */
public class ServerManagement implements ServerManagementMBean{
	
	private static final String MODULE = "SERVER MANAGEMENT IMPL";
	private static final String SH_EXT = ".sh";
	private static final String BAT_EXT = ".bat";
	private static final String FILENAME = "FILENAME";
	private static final String PATH = "PATH";
	private static final String FILE = "FILE";
	
	public ServerManagement() {
		//initiating class object
	}
	
	/**
     * It creates the start server script
     * @param port
     * @param minMemory
     * @param maxMemory
     * @return
	 * @throws IOException 
	 * @throws InterruptedException 
     * @throws Exception
     */
	@Override
	public boolean createServerInstanceScript(String scriptName,Integer port, Integer minMemory,
			Integer maxMemory, String ipAddress) throws InterruptedException, IOException, Exception { //utr
		Logger.logInfo(MODULE, "Starting create server instance process with variables : " +
				"port: " + port + ", ipAddress " + ipAddress + ", minMemory: " + minMemory + ", maxMemory: " + maxMemory);
		
		boolean fileCreated = false;
		
		String sourceFilePath;
		String renameFilePath;
		String destFilePath;
		if(scriptName == null || scriptName.length()==0){
			sourceFilePath = getScriptFileName(port);
			renameFilePath = getScriptFileNameForArchive(port);
		}else{
			sourceFilePath = getScriptNameWithPath(scriptName);
			renameFilePath = getScriptNameWithPathForArchive(scriptName);
		}
		
		Logger.logInfo( MODULE, "Destination File Path is : " + renameFilePath);
		FileCopy fileMgmt = new FileCopy();
		
		if(fileMgmt.fileExist(sourceFilePath)){
			fileMgmt.renameFile(sourceFilePath,renameFilePath,false);
			destFilePath = sourceFilePath;
		} else {
			destFilePath = getDefaultScriptFileName();
		}

		fileMgmt.copyAndReplace(destFilePath, sourceFilePath,port,minMemory,maxMemory,ipAddress);

		if(fileMgmt.fileExist(sourceFilePath)){
			fileCreated =  true;
		}
		return fileCreated;
	}
	
	/**
     * It creates the start server script
     * @param port
     * @param minMemory
     * @param maxMemory
     * @return
	 * @throws IOException 
	 * @throws InterruptedException 
     * @throws Exception
     */
	@Override
	public boolean createServerInstanceScript(String scriptName,Integer port, Integer minMemory,
			Integer maxMemory) throws InterruptedException, IOException, Exception { //utr
		Logger.logInfo(MODULE, "Starting create server instance process with variables : " +
				"port: " + port + ", minMemory: " + minMemory + ", maxMemory: " + maxMemory);
		
		boolean fileCreated = false;
		
		String sourceFilePath;
		String renameFilePath;
		String destFilePath;
		if(scriptName == null || scriptName.length()==0){
			sourceFilePath = getScriptFileName(port);
			renameFilePath = getScriptFileNameForArchive(port);
		}else{
			sourceFilePath = getScriptNameWithPath(scriptName);
			renameFilePath = getScriptNameWithPathForArchive(scriptName);
		}
		
		Logger.logInfo( MODULE, "Destination File Path is : " + renameFilePath);
		FileCopy fileMgmt = new FileCopy();
		
		if(fileMgmt.fileExist(sourceFilePath)){
			fileMgmt.renameFile(sourceFilePath,renameFilePath,false);
			destFilePath = sourceFilePath;
		} else {
			destFilePath = getDefaultScriptFileName();
		}

		fileMgmt.copyAndReplace(destFilePath, sourceFilePath,port,minMemory,maxMemory,null);

		if(fileMgmt.fileExist(sourceFilePath)){
			fileCreated =  true;
		}
		return fileCreated;
	}
	
	private String appendBatExtentionForWindows(String scriptName){
		String tempStr = scriptName;
		if(scriptName != null){
			if(OSIdentification.isWindows()){
				if(scriptName.contains(SH_EXT)){
					tempStr = scriptName.replace(SH_EXT, BAT_EXT);
				}
			}
		}
		return tempStr;
	}
	/**
	 * Checks for Port is already running
	 * @param ipAddress
	 * @param port
	 * @return
	 */
	@Override
	public boolean portAvailable(Integer port){  // utr
		String localHost = "127.0.0.1";
		Logger.logInfo(MODULE, "Checking if port " + port + " is  available");
		boolean isPortFree = false; 
		boolean isUDPFree = false;
		Socket s = null;
		DatagramSocket socket = null;
		try {
			s = new Socket(localHost, port);
            // If the code makes it this far without an exception it means
            // something is using the port and has responded.
			isPortFree = false;
		} catch (IOException e) {
			isPortFree = true;
		} finally {
			if( s != null){
				try {
					s.close();
				} catch (IOException e) {
					Logger.logWarn(MODULE, "Error while closing stream, Reason " + e.getMessage()); 
				}
			}
		}
		
		if(isPortFree){
			Logger.logInfo(MODULE, "TCP port " + port + " is available.");
			try{
				Logger.logInfo(MODULE, "Checking if UDP port " + port + " is available.");
				socket = new DatagramSocket(port);
				isUDPFree = true;
				Logger.logInfo(MODULE, "UDP port " + port + " is available.");
			} catch(SocketException e){
				isUDPFree = false;
				Logger.logInfo(MODULE, "UDP port " + port + " is not available.");
			} finally {
				if( socket != null){
					socket.close();
				}
			}
		} else {
			Logger.logInfo(MODULE, "TCP port " + port + " is not available.");	
		}
		
		if(isPortFree && isUDPFree)
			return true;
		else
			return false;
	}
	
	
    /**
     * Provides start server script file name by suffix port provided
     * @param serverInstancePort
     * @return
     */
    private String getScriptFileName(Integer serverInstancePort) throws Exception{
    	String scriptName = null;
    	if(OSIdentification.isUnix()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinPath() + "startServer_" + serverInstancePort + SH_EXT;
    	}else if(OSIdentification.isWindows()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinPath() + "startServer_" + serverInstancePort + BAT_EXT;
    	}
		return scriptName;
	}
    
    /**
     * Provides start server script file name for archiving by suffix port provided
     * @param serverInstancePort
     * @return
     */
    private String getScriptFileNameForArchive(Integer serverInstancePort) throws Exception{
    	String scriptName = null;
    	if(OSIdentification.isUnix()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinArchivePath()+ "startServer_" + serverInstancePort + SH_EXT;
    	}else if(OSIdentification.isWindows()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinArchivePath() + "startServer_" + serverInstancePort + BAT_EXT;
    	}
		return scriptName;
	}
    
    private String getScriptNameWithPath(String scriptName) throws Exception{
    	String tmpScriptName = appendBatExtentionForWindows(scriptName);
		return EnvironmentVarAndPathUtil.getServerHomeBinPath() + tmpScriptName;
	}
    
    private String getScriptNameWithPathForArchive(String scriptName) throws Exception{
    	String tmpScriptName = appendBatExtentionForWindows(scriptName);
		return EnvironmentVarAndPathUtil.getServerHomeBinArchivePath() + tmpScriptName;
	}
    
    /**
     * Provides default start server script file name
     * @return
     */
    private String getDefaultScriptFileName() throws Exception{
    	String scriptName = null;
    	if(OSIdentification.isUnix()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinPath() + "startServer" + SH_EXT;
    	}else if(OSIdentification.isWindows()){
    		scriptName = EnvironmentVarAndPathUtil.getServerHomeBinPath() + "startServer" + BAT_EXT;
    	}
		return scriptName;
    }
	
	/**
     * Executes the command line commands provided
     * @param command
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private String executeCommand(String command) throws InterruptedException, IOException {
    	if(OSIdentification.isWindows())
    		command = "cmd.exe /c Start " + command;
    	Logger.logInfo(MODULE, "Command to Execute is : " + command);
    	Process p;
    	if(OSIdentification.isUnix()){
    		StringBuilder output = new StringBuilder();
    		/*p = Runtime.getRuntime().exec(command);
    		p.waitFor();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	String line;
        	while ((line = reader.readLine())!= null) {
        		output.append(line + "\n");
        	}
        	Logger.logInfo(MODULE," Output for command : " + command + ", is : " + output.toString());
        	
			Process process;*/
			try {
				p = new ProcessBuilder("bash", "-c", command).redirectErrorStream(true).directory(new File(EnvironmentVarAndPathUtil.getServerHomeBinPath())).start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null)
					output.append(line);
				Logger.logInfo(MODULE," Output for command : " + command + ", is : " + output.toString());
				return output.toString();
			} catch (Exception e) {
				Logger.logError(MODULE, "Error while executing command : " + command +" , Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
				throw new IOException(e);
			}
    	}else if(OSIdentification.isWindows()){
    		try {
				File workingDir = new File(EnvironmentVarAndPathUtil.getServerHomeBinPath());
				Runtime runtime = Runtime.getRuntime();
				p = runtime.exec(command, null, workingDir);
				int waitFor = p.waitFor();
				String strWait = String.valueOf(waitFor);
				Logger.logInfo(MODULE," Output for command : " + command + ", is : " + strWait);
				return strWait;
			} catch (Exception e) {
				Logger.logError(MODULE, "Error while executing command : " + command + 
						" , Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
				throw new IOException(e);
			}
    	}
    	return "";
    }
	
    /**
	 * Executes the run script
	 * @param port
	 * @return
	 * @throws Exception 
	 */
    @Override
	public String runStartScript(String file) throws Exception{ //utr
		String tempFile = appendBatExtentionForWindows(file);
		Logger.logInfo(MODULE, "Running start script : " + tempFile);
		String response = null;
		try {
			if(OSIdentification.isUnix()){
				response = executeCommand("sh " + EnvironmentVarAndPathUtil.getServerHomeBinPath() + tempFile);
			}else if(OSIdentification.isWindows()){
				response = executeCommand(EnvironmentVarAndPathUtil.getServerHomeBinPath() + tempFile);
			}else{
				response = "UNKNOWN_OS";
			}
		} catch (InterruptedException e) {
			Logger.logError(MODULE, "Error while running start script, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		} catch (IOException e) {
			Logger.logError(MODULE, "Error while running start script, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		}
		Logger.logInfo(MODULE, "Run script response : " + response);
		return response;
	}
	
	/**
	 * Check script with name exists or not
	 * @param file
	 * @return 
	 * @throws Exception 
	 */
    @Override
	public boolean checkScriptExists(String file) throws Exception{ //utr
		String tempfile = appendBatExtentionForWindows(file);
		Logger.logInfo(MODULE, "Checking if script file - " + file + " exists");
		
		File f = new File(EnvironmentVarAndPathUtil.getServerHomeBinPath() + tempfile);
		if(f.exists() && !f.isDirectory()) { 
			Logger.logInfo(MODULE, "Script file exists");
			return true;
		}
		Logger.logInfo(MODULE, "Script file do not exists");
		return false;
	}
	
    
	/* (non-Javadoc)
	 * @see com.elitecore.jmx.ServerManagementMBean#renamePortFolder(java.lang.Integer)
	 */
    @Override
	public boolean renamePortFolder(Integer port) throws Exception{  //utr
		Logger.logInfo(MODULE, "Starting process to rename folder for port - " + port );
		
		String sourceFilePath=EnvironmentVarAndPathUtil.getServerHomeConfigPath();
		String destinatiFilePath=EnvironmentVarAndPathUtil.getServerHomeConfigArchivePath();
		String originalFileName = sourceFilePath+port;
		String destinationRenameFileName = destinatiFilePath + port + "_DEL_"+new Date().getTime();	
		boolean isSuccess=false;
		try {
			File originFile = new File(originalFileName);
			if(!originFile.exists()){
				Logger.logWarn(MODULE, "No such port Folder present named::"+originFile.getName());
				return true;
			}
			File destinationFile = new File(destinatiFilePath);
			if(!destinationFile.exists()){
				destinationFile.mkdir();
			}
			
			File tempDestinationFile = new File(destinationRenameFileName);
			FileUtils.moveDirectory(originFile, tempDestinationFile);
			if (tempDestinationFile.exists()) 
				isSuccess = true;
			else
				isSuccess = false;

			/*isSuccess = originFile.renameTo(tempDestinationFile);*/
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while renaming port folder, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		}
		if(isSuccess)
			Logger.logInfo(MODULE, "Folder for port - " + port + " renamed successfully.");
		else
			Logger.logWarn(MODULE, "Folder for port - " + port + " is not renamed successfully.");
		return isSuccess;
	}
	
	/* (non-Javadoc)
	 * @see com.elitecore.jmx.ServerManagementMBean#renameStartupFile(java.lang.Integer)
	 */
    @Override
	public boolean renameStartupFile(Integer port) throws Exception{  //utr
		Logger.logInfo(MODULE, "Starting process to rename startup file for port - " + port );
		
		boolean fileRenamed;
		
		String sourceFilePath = getScriptFileName(port);
		String renameFilePath = getScriptFileNameForArchive(port);
		
		Logger.logInfo(MODULE, "Startup file to rename is : " + sourceFilePath );
		FileCopy fileMgmt = new FileCopy();
		
		if(fileMgmt.fileExist(sourceFilePath)){
			fileMgmt.renameFile(sourceFilePath,renameFilePath,true);
		}

		if(!fileMgmt.fileExist(sourceFilePath)){
			Logger.logInfo(MODULE, "File renamed successfully.");
			fileRenamed =  true;
		}else{
			Logger.logInfo(MODULE, "File rename operation is not successful.");
			fileRenamed =  false;
		}

		return fileRenamed;
	}
	
	/* (non-Javadoc)
	 * @see com.elitecore.jmx.ServerManagementMBean#confFolderExists(java.lang.Integer)
	 */
    @Override
	public boolean confFolderExists(Integer port) throws Exception{ //utr
		
		Logger.logInfo(MODULE, "Starting process to check if config folder exist for port - " + port );
		File f = new File(EnvironmentVarAndPathUtil.getServerHomeConfigPath() + port);
		if(f.exists() && f.isDirectory()) {
			Logger.logInfo(MODULE, "Config folder - " + f.getAbsolutePath() + " exists");
			return true;
		}
		Logger.logInfo(MODULE, "Config folder - " + f.getAbsolutePath() + " not exists");
		return false;
	}

	@Override
	public String crestelPEngineHome() throws Exception {
		return EnvironmentVarAndPathUtil.getCrestelPEngineHome();
	}
	@Override
	public String javaHome() throws Exception {
		return EnvironmentVarAndPathUtil.getJavaHome();
	}
	
	@Override
	public Map<String,ArrayList<String>> getServicesListByPort(Integer port,List<String> serviceTypeList) throws Exception{  //utr
		Map<String,ArrayList<String>> serviceList=new HashMap<String, ArrayList<String>>();
		Logger.logInfo(MODULE, "Starting process to check if config folder exist for port - " + port );
		File f = new File(EnvironmentVarAndPathUtil.getServerHomeConfigPath() + port+ File.separator+ "services"+File.separator);
		if(f.exists() && f.isDirectory()) {
			Logger.logInfo(MODULE, "Config folder - " + f.getAbsolutePath() + " exists");
			String[] serviceNames = f.list();
			for(String serviceType:serviceTypeList ){
				ArrayList<String> serviceInstanceIdList =(ArrayList<String>) fetchServiceInstanceIdBySvcType(serviceType, serviceNames, port);
				serviceList.put(serviceType, serviceInstanceIdList);
			}
			
		}
		else{
		Logger.logInfo(MODULE, "Config folder - " + f.getAbsolutePath() + " not exists");
		}
		return serviceList;
	}
	
	public List<String> fetchServiceInstanceIdBySvcType (String svcType, String[] serviceNames, Integer port) throws Exception {
		List<String> serviceInstanceIdList = new ArrayList<String>();
		
		for (String serviceName : serviceNames) {
			if (serviceName.trim().toLowerCase().equalsIgnoreCase(svcType.trim().toLowerCase())) {
				String directoryPath=EnvironmentVarAndPathUtil.getServerHomeConfigPath() + port + File.separator + "services"+File.separator+serviceName;
				File file = new File(directoryPath);
				if(file.exists() && file.isDirectory()) {
					Logger.logInfo(MODULE, "Config folder - " + file.getAbsolutePath() + " exists");
				}
				String[] svcInstanceIds = file.list();
				Logger.logInfo(MODULE, "List of folders inside port folder::"+svcInstanceIds);
				serviceInstanceIdList=fetchServiceInstanceIdList(svcInstanceIds, directoryPath);

			}
		}

		return serviceInstanceIdList;
	}
	
	public List<String> fetchServiceInstanceIdList(String[] svcInstanceIds,String directoryPath){
		List<String> serviceInstanceIdList = new ArrayList<String>();
		for (String svcInstanceId : svcInstanceIds) {
			if (new File(directoryPath + File.separator	+ svcInstanceId).isDirectory()) {
				serviceInstanceIdList.add(svcInstanceId);
			}
			Logger.logInfo(MODULE, "for serviceInstanceID::"+new File(directoryPath + File.separator	+ svcInstanceId).getAbsolutePath()+"exists");
			Logger.logInfo(MODULE, "List of folders inside port folder inside serviceType::"+svcInstanceIds);

		}
		return serviceInstanceIdList;
	}

	@Override
	public boolean syncDictionaries(String fileName, String filePath, String ipAddress, Integer port, byte[] dicFile) throws Exception {
		try {
			CreateServerInstanceScript createServerInstanceScript = new CreateServerInstanceScript();
			createServerInstanceScript.syncDictionaryFiles(fileName, filePath,ipAddress, port, dicFile);
		} catch(Exception e) {
			Logger.logError(MODULE, "Exception while sync dictionary folder for ipAddress = "+ipAddress+" and utilityPort "+ port+".");
			return false;
		}
		return true;
	}
	
	@Override
	public boolean syncKeystoreFile(String fileName, String filePath, byte[] keystorefile) throws Exception {
		try {
			CreateServerInstanceScript createServerInstanceScript = new CreateServerInstanceScript();
			createServerInstanceScript.syncKeystoreFile(fileName, filePath, keystorefile);
		} catch(Exception e) {
			Logger.logError(MODULE, "Exception while sync keystore file " + fileName);
			return false;
		}
		return true;
	}
		
	@Override
	public boolean testFtpSftpConnection(String ipAddress,Integer port,String username,String password,String strKeyFileLocation,Integer maxRetrycount,String driverType) throws Exception {
		try {
			CreateServerInstanceScript createServerInstanceScript = new CreateServerInstanceScript();
			boolean result=(boolean) createServerInstanceScript.testFtpSftpConnection(ipAddress, port,username, password,strKeyFileLocation,maxRetrycount,driverType);
			return result;
		} catch(Exception e) {
			Logger.logError(MODULE, "Exception while Performing Test Connection."+e.getMessage());
			Logger.logTrace(MODULE, e);
			return false;
		}
	}

}