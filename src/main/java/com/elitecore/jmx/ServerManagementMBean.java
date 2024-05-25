/**
 * 
 */
package com.elitecore.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Interface ServerManagementMBean.
 *
 * @author Sunil Gulabani
 * Sep 1, 2015
 */
public interface ServerManagementMBean {
	
	/**
	 * Port available.
	 *
	 * @param port the port
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean portAvailable(Integer port) throws Exception;
	
	/**
	 * Creates the server instance script.
	 *
	 * @param scriptName the script name
	 * @param port the port
	 * @param minMemory the min memory
	 * @param maxMemory the max memory
	 * @return true, if successful
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public boolean createServerInstanceScript(String scriptName,Integer port, Integer minMemory, Integer maxMemory, String ipAddress) throws InterruptedException, IOException, Exception;
	
	public boolean createServerInstanceScript(String scriptName,Integer port, Integer minMemory, Integer maxMemory) throws InterruptedException, IOException, Exception;
	
	/**
	 * Crestel P engine home.
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	public String crestelPEngineHome() throws Exception;
	
	/**
	 * Java home.
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	public String javaHome() throws Exception;
	
	/**
	 * Run start script.
	 *
	 * @param file the file
	 * @return the string
	 * @throws Exception the exception
	 */
	public String runStartScript(String file) throws Exception;
	
	/**
	 * Check script exists.
	 *
	 * @param file the file
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean checkScriptExists(String file) throws Exception;
	
	/**
	 * Rename port folder.
	 *
	 * @param port the port
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean renamePortFolder(Integer port) throws Exception;
	
	/**
	 * Rename startup file.
	 *
	 * @param port the port
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean renameStartupFile(Integer port) throws Exception;
	
	/**
	 * Conf folder exists.
	 *
	 * @param port the port
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean confFolderExists(Integer port) throws Exception;

	public Map<String, ArrayList<String>> getServicesListByPort(Integer port, List<String> serviceTypeList) throws Exception;
	
	public boolean syncDictionaries(String fileName, String filePath, String ipAddress, Integer port, byte[] dicFile) throws Exception;
	
	public boolean syncKeystoreFile(String fileName, String filePath, byte[] keystorefile) throws Exception;

	public boolean testFtpSftpConnection(String ipAddress,Integer port,String username,String password,String strKeyFileLocation,Integer maxRetrycount,String driverType) throws Exception;
	
}
