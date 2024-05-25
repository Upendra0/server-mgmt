package com.elitecore.util.k8s;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.elitecore.jmx.ServerManagement;
import com.elitecore.util.commons.EnvironmentVarAndPathUtil;
import com.elitecore.util.commons.ServerMgmtUtils;
import com.elitecore.util.logger.Logger;

public class CreateServerInstanceScript {

	private static final String MODULE = "CREATESERVERINSTANCESCRIPT";
	private static ServerManagement serverMgt = new ServerManagement();
	private static String serverMgmtPort = null;
	private static String ipAddress = null;
	private static String kubernetes_env = null;	
	private static String mongoServerInstancePort = null;	
	private static boolean isKubernetes = false;
	private static String mongoDBUrl = null;
	private static final String MINMEMORY = "MINMEMORY";
	private static final String MAXMEMORY = "MAXMEMORY";
	private static final String PORT = "PORT";
	private static String dictionoryPath = "";
	private static final String CORREALTION_PARTITION = " = ";
	private static final String NEW_LINE = "\n\n";
	private static final String SM_DATASOURCE = "ServerManager";
	private static final String IPLMS_DATASOURCE = "iplogger";
	private Map<Integer, Object> serverConfigMap = new HashMap<>();
	
	public void autoGenerationBinConfigDictionary() {
		try {
			readEnvironmentVarible();
			getServerInstanceWiseConfigData();
			writeDictionaryFiles();		
			writeKeystoreFiles();
			createSearchConfigPropertiesByPort();
			createAndRunServerInstancesScript();			
			createAndRunMongoDBServerInstancesScript();
		} catch (Exception e) {
			Logger.logError(MODULE, "Environment variable is not set : " + e.getMessage());
		}
	}

	private void readEnvironmentVarible() throws Exception {
		Logger.logInfo(MODULE,"Inside readEnvironmentVarible() method");
		ipAddress = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.ENV_SERVER_MGMT_IP_ADDRESS);

		serverMgmtPort = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.ENV_SERVER_MGMT_PORT);
		
		kubernetes_env = System.getenv(KubernateConstant.KUBERNETES_ENV);
		if (kubernetes_env != null && Boolean.TRUE.toString().equals(kubernetes_env)) {
			isKubernetes = true;
		}
		mongoDBUrl = System.getenv(KubernateConstant.MONGO_DB_URL);
		mongoServerInstancePort = System.getenv(KubernateConstant.MONGO_SERVER_INSTANCE_PORT);
		 
	}

	private void createAndRunServerInstancesScript() {
		Logger.logInfo(MODULE,"Inside createAndRunServerInstancesScript() method");
		String serverQuery = "SELECT ID FROM TBLTSERVER WHERE STATUS='ACTIVE' AND IPADDR = ? AND UTILITYPORT = ? ";
		String serverInstanceQuery = "SELECT MINMEMORY, MAXMEMORY, PORT FROM TBLTSERVERINSTANCE WHERE STATUS='ACTIVE' AND SERVERID = ?";
		if (ipAddress != null && serverMgmtPort !=null) {

			Connection conn = null;
			PreparedStatement stmt = null;
			PreparedStatement stmt2 = null;
			ResultSet rs = null;
			ResultSet rs2 = null;
			try {
				conn = DataConnection.getInstance(SM_DATASOURCE);
				stmt = conn.prepareStatement(serverQuery);
				stmt.setString(1, ipAddress);
				stmt.setInt(2, Integer.parseInt(serverMgmtPort));
				rs = stmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt("ID");
					try {
						stmt2 = conn.prepareStatement(serverInstanceQuery);
						stmt2.setInt(1, id);
						rs2 = stmt2.executeQuery();
						while (rs2.next()) {
							int minMemory = rs2.getInt(MINMEMORY);
							int maxMemory = rs2.getInt(MAXMEMORY);
							int instancePort = rs2.getInt(PORT);
							String scriptFileName = KubernateConstant.SCRIPT_PREFIX + instancePort
									+ KubernateConstant.SCRIPT_SUFFIX;
							 
							boolean fileCreated = serverMgt.createServerInstanceScript(scriptFileName, instancePort,
									minMemory, maxMemory, ipAddress);
							if (fileCreated) {
								serverMgt.runStartScript(scriptFileName);
							} else {
								Logger.logInfo(MODULE, "No script file is created with name : " + scriptFileName);
							}
						}
					} catch (Exception e) {
						Logger.logError(MODULE, "Error while creating server instance script : " + e.getMessage());
					} finally {
						if (rs2 != null) {
							rs2.close();
						}
						if (stmt2 != null) {
							stmt2.close();
						}
					}
				}
			} catch (Exception e) {
				Logger.logError(MODULE,
						"Error while creating database connection or executing database query : " + e.getMessage());
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					Logger.logError(MODULE, "Error while closing resources : " + e.getMessage());
				}
			}
		}
	}

	private void createAndRunMongoDBServerInstancesScript() {
		Logger.logInfo(MODULE,"Inside createAndRunMongoDBServerInstancesScript() method");
		if (ipAddress != null && serverMgmtPort !=null && mongoDBUrl!=null && !mongoDBUrl.isEmpty()) {
			try {
				int minMemory = 1024;
				int maxMemory = 2056;
				int defaultInstancePort = 8802;
				if(mongoServerInstancePort!=null && !mongoServerInstancePort.isEmpty()) {
					try {
						defaultInstancePort=Integer.parseInt(mongoServerInstancePort.trim());
					}catch (Exception e) {
						defaultInstancePort = 8802;
					}
				}
				String scriptFileName = KubernateConstant.SCRIPT_PREFIX + defaultInstancePort
						+ KubernateConstant.SCRIPT_SUFFIX;
				Logger.logInfo(MODULE,"scriptFileName : " + scriptFileName);
				boolean fileCreated = serverMgt.createServerInstanceScript(scriptFileName, defaultInstancePort,
							minMemory, maxMemory, ipAddress);				
				Logger.logInfo(MODULE,"scriptFileName : " + scriptFileName + " is created");
				if (fileCreated) {
					serverMgt.runStartScript(scriptFileName);					
				} else {
					Logger.logInfo(MODULE, "Error while creating server instance script : " + scriptFileName);
				}
			} catch (Exception e) {
				Logger.logError(MODULE, "Error while creating server instance script : " + e.getMessage());
			}
		}
	}

	private void getServerInstanceWiseConfigData() {
		Logger.logInfo(MODULE,"Inside getServerInstanceWiseConfigData() method");
		String configQuery = null;
		configQuery = "SELECT CRESTELNETSERVERDATA, SIPORT FROM TBLTAUTOCONFIGSI WHERE IPADDRESS=? AND UTILITYPORT=? AND STATUS <> 'DELETED'";
		if (ipAddress != null && serverMgmtPort !=null) {
				Connection conn = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
			try {
				conn = DataConnection.getInstance(SM_DATASOURCE);
				stmt = conn.prepareStatement(configQuery);
				stmt.setString(1, ipAddress);
				stmt.setInt(2, Integer.parseInt(serverMgmtPort));
				rs = stmt.executeQuery();
				while (rs.next()) {
					byte[] serverConfigData = rs.getBytes("CRESTELNETSERVERDATA");
					int serverInstancePort = rs.getInt("SIPORT");
					serverConfigMap.put(new Integer(serverInstancePort), serverConfigData);
				}
			} catch (Exception e) {
				Logger.logError(MODULE,
						"Error while creating database connection or executing database query : " + e.getMessage());
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					Logger.logError(MODULE, "Error while closing resources : " + e.getMessage());
				}
			}
		}
	}
	
	private void writeKeystoreFiles() throws Exception {
		Logger.logInfo(MODULE,"Inside writeKeystoreFiles() method");
		String crestelPEngineHome = EnvironmentVarAndPathUtil.getCrestelPEngineHome();
		String keyStoreQuery = null;
		keyStoreQuery = "SELECT http2svc.keystorefile, http2svc.keystorefilepath FROM TBLTHTTP2COLLSVC http2svc, TBLTSERVICE svc, " + 
				"TBLTSERVERINSTANCE si, TBLTSERVER server WHERE http2svc.SERVICEID=svc.ID AND svc.SERVERINSTANCEID=si.ID AND si.SERVERID=server.ID AND svc.STATUS <> 'DELETED' AND server.IPADDR= ? AND server.UTILITYPORT= ? "; 

		if (ipAddress != null && serverMgmtPort !=null) {
				Connection conn = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
			try {
				conn = DataConnection.getInstance(SM_DATASOURCE);
				stmt = conn.prepareStatement(keyStoreQuery);
				stmt.setString(1, ipAddress);
				stmt.setInt(2, Integer.parseInt(serverMgmtPort));
				rs = stmt.executeQuery();
				while (rs.next()) {
					Blob blob = rs.getBlob("KEYSTOREFILE");
					byte[] keystoreFile = null;
			        if(blob!=null)
			        	keystoreFile = ServerMgmtUtils.convertBlobToByteArray(blob);
					String keystoreFilePath = rs.getString("KEYSTOREFILEPATH");
					String keystoreFileName = crestelPEngineHome + keystoreFilePath;
					keystoreFilePath = keystoreFileName.substring(0,keystoreFileName.lastIndexOf(File.separator));
					ServerMgmtUtils.writeStreamToFile(keystoreFilePath,keystoreFileName,keystoreFile);
				}
			} catch (Exception e) {
				Logger.logError(MODULE,
						"Error while creating database connection or executing database query : " + e.getMessage());
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					Logger.logError(MODULE, "Error while closing resources : " + e.getMessage());
				}
			}
		}
	}
	
	private void writeIPLMSSearchConfigProperty(String port, Map<String, String> correlationParameterMap, Map<String, String> correlationParameterValueMap) {
		if(port!=null && port!="" && !correlationParameterMap.isEmpty() && !correlationParameterValueMap.isEmpty()) {
			try {
				StringBuilder correlatioConfigData = new StringBuilder("");
				Set<Map.Entry<String, String>> entrySet = correlationParameterValueMap.entrySet();
				for(Map.Entry<String, String> entry : entrySet) {
					correlatioConfigData.append(prepareParameterString(correlationParameterMap.get(entry.getKey()), entry.getValue()));
				}
				String correlationPath = EnvironmentVarAndPathUtil.getCorrelationPath();
				String filePath = correlationPath + File.separator + port;
				String fileName = filePath + File.separator + "search-config.property";
				boolean isFileWrite = ServerMgmtUtils.writeStringToFile(filePath,fileName,correlatioConfigData.toString());
				if(!isFileWrite) {
					Logger.logError(MODULE, "Error while writing search-config property file for port - "+ port);
				} else {
					Logger.logInfo(MODULE, "search-config property file for port - "+ port + " uploaded successfully.");
				}
			} catch (Exception e) {
				Logger.logError(MODULE, "Environment variable is not set : "+e.getMessage());
			}
		}
	}
	
	public void writeDictionaryFiles() throws Exception {
		Logger.logInfo(MODULE,"Inside writeDictionaryFiles() method");
		try {
			dictionoryPath = EnvironmentVarAndPathUtil.getServerHomeDictionaryPath();
			String query = "SELECT FILENAME, PATH, DICFILE FROM TBLTFILEINFO WHERE STATUS='ACTIVE' AND IPADDRESS = ? AND UTILITYPORT = ? AND ISDEFAULT = 'N' ";
			if(ipAddress!=null && serverMgmtPort!=null){
				Connection conn = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
				// Get the Large Object Manager to perform operations with				
				try {
					conn = DataConnection.getInstance(SM_DATASOURCE);
					conn.setAutoCommit(false);
					stmt = conn.prepareStatement(query);
					stmt.setString(1,ipAddress);
					stmt.setInt(2,Integer.parseInt(serverMgmtPort));
					rs = stmt.executeQuery();
					while(rs.next()) {
						String fileName = rs.getString("FILENAME");
						String filePath = rs.getString("PATH");				        
				        Blob blob = rs.getBlob("DICFILE");
				        byte[] dicFile = null;
				        if(blob!=null)
				            dicFile = blob.getBytes(1, (int) blob.length());
						filePath = dictionoryPath + filePath;
						fileName = filePath + File.separator + fileName;
						boolean isFileWrite = ServerMgmtUtils.writeStreamToFile(filePath,fileName,dicFile);
						if(!isFileWrite) {
							Logger.logError(MODULE, "Error while writing dictionary file : "+fileName);
						} else {
							Logger.logInfo(MODULE, "Dictionary file : "+fileName+" uploaded successfully.");
						}
					}
					conn.commit();
				} finally {
					try {
						if(rs!=null) {
							rs.close();
						}
						if(stmt!=null) {
							stmt.close();
						}						
					} catch (SQLException e) {
						Logger.logError(MODULE, "Error while closing resources : "+e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Environment variable is not set : "+e.getMessage());
		}
	}
	
	public void createSearchConfigPropertiesByPort() throws Exception {
		Logger.logInfo(MODULE,"Inside createSearchConfigPropertiesByPort() method");
		try {
			if(ipAddress!=null){
				String query = "SELECT NETSERVERID, ADMINPORT FROM TBLMNETSERVERINSTANCE WHERE ADMINHOST=? AND ADMINPORT>0 AND"
						+ "	ENGINETYPE IN (SELECT (ID-1) FROM TBLMSEARCHENGINETYPECONFIG WHERE SERVERTYPE='Mediation') ";
				String searchParameterQuery = "SELECT PARAMETERID, ALIAS FROM TBLMSEARCHPARAMS ";
				String searchParameterValuesQuery = "SELECT PARAMETERID, PARAMTERVALUES FROM TBLMSEARCHVALUES WHERE NETSERVERID=? ";
				Map<String, String> correlationParameterMap = new HashMap<String, String>();
				Map<String, String> correlationParameterValueMap = new HashMap<String, String>();
				Connection conn = null;
				PreparedStatement stmt = null;
				PreparedStatement stmtSearchParameter = null;
				PreparedStatement stmtSearchParameterValues = null;
				ResultSet rs = null;
				ResultSet rsSearchParameter = null;
				ResultSet rsSearchParameterValues = null;
				try {
					conn = DataConnection.getInstance(IPLMS_DATASOURCE);
					conn.setAutoCommit(false);
					stmtSearchParameter = conn.prepareStatement(searchParameterQuery);
					rsSearchParameter = stmtSearchParameter.executeQuery();
					while(rsSearchParameter.next()) {
						String parameterId = rsSearchParameter.getString("PARAMETERID");
						String alias = rsSearchParameter.getString("ALIAS");
						correlationParameterMap.put(parameterId, alias);
					}
					stmt = conn.prepareStatement(query);
					stmt.setString(1,ipAddress);
					rs = stmt.executeQuery();
					while(rs.next()) {
						String netServerId = rs.getString("NETSERVERID");
						String netServerPort = rs.getString("ADMINPORT");
						stmtSearchParameterValues = conn.prepareStatement(searchParameterValuesQuery);
						stmtSearchParameterValues.setString(1,netServerId);
						rsSearchParameterValues = stmtSearchParameterValues.executeQuery();
						while(rsSearchParameterValues.next()) {
							String parameterId = rsSearchParameterValues.getString("PARAMETERID");
							String parameterValue = rsSearchParameterValues.getString("PARAMTERVALUES");
							correlationParameterValueMap.put(parameterId, parameterValue);
						}
						writeIPLMSSearchConfigProperty(netServerPort, correlationParameterMap, correlationParameterValueMap);
					}
					conn.commit();
				} catch (Exception e) {
					Logger.logError(MODULE, "Error occured while preparing search-config property data : "+e.getMessage());
				}finally {
					try {
						if(rsSearchParameterValues!=null) {
							rsSearchParameterValues.close();
						}
						if(stmtSearchParameterValues!=null) {
							stmtSearchParameterValues.close();
						}						
						if(rsSearchParameter!=null) {
							rsSearchParameter.close();
						}
						if(stmtSearchParameter!=null) {
							stmtSearchParameter.close();
						}						
						if(rs!=null) {
							rs.close();
						}
						if(stmt!=null) {
							stmt.close();
						}						
					} catch (SQLException e) {
						Logger.logError(MODULE, "Error while closing resources : "+e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Environment variable is not set : "+e.getMessage());
		}
	}
	
	public void syncDictionaryFiles(String fileName, String filePath, String iPAddress, int uPort, byte[] dicFile) throws Exception {
		try {
			dictionoryPath = EnvironmentVarAndPathUtil.getServerHomeDictionaryPath();
			if(iPAddress!=null && uPort!=0 && dicFile !=null){
				filePath = dictionoryPath + filePath;
				fileName = filePath + File.separator + fileName;
				boolean isFileWrite = ServerMgmtUtils.writeStreamToFile(filePath,fileName,dicFile);
				if(!isFileWrite) {
					Logger.logError(MODULE, "Error while writing dictionary file : "+fileName);
				}
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while writing dictionary file : "+fileName);
		}
	}
	
	public void syncKeystoreFile(String fileName, String filePath, byte[] keystorefile) throws Exception {
		try {
			String crestelPEngineHome = EnvironmentVarAndPathUtil.getCrestelPEngineHome();
			if(keystorefile !=null){
				fileName = crestelPEngineHome + filePath;
				filePath = crestelPEngineHome + filePath.substring(0,filePath.lastIndexOf(File.separator));
				boolean isFileWrite = ServerMgmtUtils.writeStreamToFile(filePath,fileName,keystorefile);
				if(!isFileWrite) {
					Logger.logError(MODULE, "Error while writing keystore file : "+fileName);
				}
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while writing keystore file : "+fileName);
		}
	}
	
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
    	Object obj = null;
        try{
        	obj = is.readObject();	
        }catch(Exception ex){
        	Logger.logError(MODULE, "Error while updating cache");
        }finally{
        	if(is != null){
        		is.close();
        	}
        	if(in != null){
        		in.close();
        	}
        }
        return obj;
    }
	
	public boolean testFtpSftpConnection(String ipAddress,Integer port,String username,String password,String strKeyFileLocation,Integer maxRetrycount,String driverType) throws Exception {
		try {
			   boolean isConnected=false;
			   if(driverType.contains("SFTP")) {
				   isConnected=ServerMgmtUtils.testSFTPConnection(ipAddress,port,username,password,strKeyFileLocation,maxRetrycount);
			   }else if(driverType.contains("FTP")){
				   isConnected = ServerMgmtUtils.testFTPConnection(ipAddress,port,username,password);
			   }
			if(!isConnected) {
					Logger.logError(MODULE, "Error while testing connection");
					return isConnected;
			}else {
					return true;
			}
			  
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while testing connection."+e.getMessage());
			Logger.logTrace(MODULE, e);
			return false;
		}
	}
	
	private String prepareParameterString(String key,String value){
		StringBuilder stringBuilder = new StringBuilder(key);
		stringBuilder.append(CORREALTION_PARTITION);
		stringBuilder.append(value);
		stringBuilder.append(NEW_LINE);
		return stringBuilder.toString();
	}

}