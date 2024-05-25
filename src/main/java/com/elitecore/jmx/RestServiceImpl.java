package com.elitecore.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;

import com.elitecore.jmx.license.LicenceManagement;
import com.elitecore.jmx.license.LicenseUtility;
import com.elitecore.util.logger.Logger;

@Path("/services")
public class RestServiceImpl {

	private int responseCode = 200;
	private ServerManagement serverMgt = new ServerManagement();
	private LicenceManagement licenseMgt = new LicenceManagement();
	private static final String MODULE = "RESTSERVICEIMPL";
	private static List<String> otps = new ArrayList<> ();
	private static boolean isNextToken = true; 
	static {
		otps.add("123456");
		otps.add("654321");
		otps.add("909090");
		otps.add("567890");	
	}
	
	@POST
    @Path("/crestelPEngineHome")    
    @Produces({MediaType.APPLICATION_JSON})
    public String crestelPEngineHome() {
		Logger.logInfo(MODULE, "Getting CRESTEL_P_ENGINE_HOME");
		String pEngineHome = null;
		boolean status;
		try {
			pEngineHome = serverMgt.crestelPEngineHome();
			status = true;
		}catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while getting crestelPEngineHome. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,pEngineHome).toString();
    }
	
	@POST
    @Path("/portAvailable")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String portAvailable(String request) {
		Logger.logInfo(MODULE, "Checking if portAvailable or not");
		Boolean portAvailable = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			Integer port = (Integer) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + port);
			portAvailable = serverMgt.portAvailable(port);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while checking for portAvailable. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,portAvailable).toString();
    }
	
	@POST
    @Path("/confFolderExists")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String confFolderExists(String request) {
		Logger.logInfo(MODULE, "Checking if confFolderExists");
		Boolean folderExists = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			Integer port = (Integer) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + port);
			folderExists = serverMgt.confFolderExists(port);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while checking for confFolderExists. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,folderExists).toString();
    }
	
	
	@POST
    @Path("/renamePortFolder")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String renamePortFolder(String request) {
		Logger.logInfo(MODULE, "Renaming port folder");
		Boolean folderExists = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			Integer port = (Integer) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + port);
			folderExists = serverMgt.renamePortFolder(port);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while renamePortFolder. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,folderExists).toString();
    }
	

	@POST
    @Path("/createServerInstanceScript")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String createServerInstanceScript(String request) {
		Logger.logInfo(MODULE, "Creating Server Instance Script");
		Boolean folderExists = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String scriptName = (String) data.get("KEY#1");
			Integer port = (Integer) data.get("KEY#2");
			Integer minMemory = (Integer) data.get("KEY#3");
			Integer maxMemory = (Integer) data.get("KEY#4");
			String ipAddress = (String) data.get("KEY#5");
			Logger.logInfo(MODULE, "scriptName -  KEY#1 : " + scriptName);
			Logger.logInfo(MODULE, "port -  KEY#2 : " + port);
			Logger.logInfo(MODULE, "minMemory -  KEY#3 : " + minMemory);
			Logger.logInfo(MODULE, "maxMemory -  KEY#4 : " + maxMemory);
			Logger.logInfo(MODULE, "ipAddress -  KEY#5 : " + ipAddress);
			folderExists = serverMgt.createServerInstanceScript(scriptName,port,minMemory,maxMemory,ipAddress);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while createServerInstanceScript. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,folderExists).toString();
    }
	
	@POST
    @Path("/runStartScript")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String runStartScript(String request) {
		Logger.logInfo(MODULE, "Going to run start script");
		String response = null;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String scriptFileName = (String) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + scriptFileName);
			response = serverMgt.runStartScript(scriptFileName);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while runStartScript. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,response).toString();
    }
	
	
	@POST
    @Path("/getServicesListByPort")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String getServicesListByPort(String request) {
		Logger.logInfo(MODULE, "Getting Services List By Port");
		Map<String,ArrayList<String>> responseMap = null;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			Integer port = (Integer) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + port);
			@SuppressWarnings("unchecked")
			List<String> serviceTypeList = (List<String>) data.get("KEY#2");
			responseMap = serverMgt.getServicesListByPort(port,serviceTypeList);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while getServicesListByPort. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,responseMap).toString();
    }
	
	@POST
    @Path("/javaHome")    
    @Produces({MediaType.APPLICATION_JSON})
    public String javaHome() {
		Logger.logInfo(MODULE, "Getting Java Home");
		String javaHome = null;
		boolean status;
		try {
			javaHome = serverMgt.javaHome();
			status = true;
		}catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while getting javaHome. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,javaHome).toString();
    }
	
	@POST
    @Path("/renameStartupFile")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String renameStartupFile(String request) {
		Logger.logInfo(MODULE, "Going to rename Startup File");
		boolean response = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			Integer port = (Integer) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + port);
			response = serverMgt.renameStartupFile(port);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while renameStartupFile. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,response).toString();
    }
	
	@POST
    @Path("/checkScriptExists")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String checkScriptExists(String request) {
		Logger.logInfo(MODULE, "Going to check if script exists");
		boolean response = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String scriptFileName = (String) data.get("KEY#1");
			Logger.logInfo(MODULE, "KEY#1 : " + scriptFileName);
			response = serverMgt.checkScriptExists(scriptFileName);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while checkScriptExists. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,response).toString();
    }
	
	
	@POST
	@Path("/activateDefaultFullLicence")
	@Produces({MediaType.APPLICATION_JSON})
	public String activateDefaultFullLicence()
	{
		Logger.logInfo(MODULE, "Activate Default Full Licence");
		boolean status;
		boolean licenseApplied = false;
		try {
			licenseApplied = licenseMgt.applyDefaultFullVersion();
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while activateDefaultFullLicence. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,licenseApplied).toString();
	}
	
	@POST
	@Path("/upgradeDefaultLicense")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public String upgradeDefaultLicense(String reqJson) {
		Logger.logInfo(MODULE, "Licence - Upgrade Default License");
		String isUpgraded = null;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(reqJson);
			Logger.logInfo(MODULE, "KEY#1:" + data.get("KEY#1"));
			isUpgraded = licenseMgt.upgradeDefaultLicense((byte[]) data.get("KEY#1"));
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while upgradeDefaultLicense. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status, responseCode,isUpgraded);
	}
	
	@POST
	@Path("/licenseInfo")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public String licenseInfo() {
		Logger.logInfo(MODULE, "Getting License Information");
		Map<String,String> licenseHashMap = null;
		boolean status;
		try {
			licenseHashMap = licenseMgt.getLicenseFullDetail();
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while getting License Info. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status, responseCode,licenseHashMap);
	}
	
	private String createJSONResponce(Boolean status, int responceCode, Object data) {
		String jsonString = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		ResponseObject<Object> responseObject = new ResponseObject<Object>();
		responseObject.setSuccess(status);
		responseObject.setResponseCode(responceCode);
		responseObject.setData(data);	    
		try {
			jsonString = mapper.writeValueAsString(responseObject);
		} catch (Exception e) {
			Logger.logError(MODULE, " Error occured while createJSONResponce. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		Logger.logInfo(MODULE, "status : " + status + " -- responceCode : " + responceCode);
		Logger.logInfo(MODULE, "Data : " + jsonString);
		return jsonString;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> getRequestData(String response) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		Map<String,Object> data = null;
		try {
			RequestObject<Object> requestObj = mapper.readValue(response, RequestObject.class);
			data = (Map<String, Object>) requestObj.getData();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	@POST
	@Path("/serverIdDetails")
	@Produces({ MediaType.APPLICATION_JSON })
	public String serverIdDetails() {
		Logger.logInfo(MODULE, "Getting serverIdDetails");		
		int responseCode = 200;
		boolean status;
		Map<String, String> serverDetailMap = new HashMap<>();	
		LicenseUtility lu = new LicenseUtility();
		try {					
			String ip = "";
			String macId = "";
			String hostName = "";
			String serverid = "";
			List<String> ipList = lu.getIpList();
			if(!ipList.isEmpty()) {
				ip = ipList.get(0);
				macId = lu.getMacFromIP(ip);
				hostName = lu.getHostName(ip);
				serverid = lu.generateServerId(macId, hostName);
			}
			serverDetailMap.put(LicenseUtility.MACID, serverid);
			serverDetailMap.put(LicenseUtility.HOSTNAME, hostName);
			serverDetailMap.put(LicenseUtility.IP, ip);
			status = true;				
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while getting serverIdDetails. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status, responseCode, serverDetailMap);
	}
	
	@POST
    @Path("/syncDictionaries")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String syncDictionaries(String request) {
		Logger.logInfo(MODULE, "Sync Dictionary Files.");
		Boolean folderExists = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String ipAddress = (String) data.get("KEY#1");
			Integer port = (Integer) data.get("KEY#2");
			byte[] dicFile = (byte[]) data.get("KEY#3");
			String fileName = (String) data.get("KEY#4");
			String filePath = (String) data.get("KEY#5");
			Logger.logInfo(MODULE, "ipAddress -  KEY#1 : " + ipAddress);
			Logger.logInfo(MODULE, "utilityPort -  KEY#2 : " + port);
			//Logger.logInfo(MODULE, "dicFile -  KEY#3 : " + dicFile);
			Logger.logInfo(MODULE, "fileName -  KEY#4 : " + fileName);
			Logger.logInfo(MODULE, "filePath -  KEY#5 : " + filePath);
			folderExists = serverMgt.syncDictionaries(fileName, filePath,ipAddress,port, dicFile);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while syncDictionaries. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,folderExists).toString();
    }
	
	@POST
    @Path("/syncKeystoreFile")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String syncKeystoreFile(String request) {
		Logger.logInfo(MODULE, "Sync Keystore Files.");
		Boolean folderExists = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String fileName = (String) data.get("KEY#1");
			String filePath = (String) data.get("KEY#2");
			byte[] keystoreFile = (byte[]) data.get("KEY#3");
			Logger.logInfo(MODULE, "fileName -  KEY#1 : " + fileName);
			Logger.logInfo(MODULE, "filePath -  KEY#2 : " + filePath);
			Logger.logInfo(MODULE, "keystoreFile -  KEY#3 : " + keystoreFile);
			folderExists = serverMgt.syncKeystoreFile(fileName, filePath,keystoreFile);
			status = true;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while sync keystore file. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,folderExists).toString();
    }
	
    @POST
	@Path("/authenticateuser")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public String authenticateuser(String reqJson) {
		Logger.logInfo(MODULE, "authenticateuser");		
		JSONObject responseJson = new JSONObject();
		try {
			Logger.logInfo(MODULE, "KEY#1:" + reqJson);
			JSONObject json = new JSONObject(reqJson);			
			String userId = (String) json.get("userId");
			String token = (String) json.get("passcode");
			if(userId!=null && token!=null && otps.contains(token)) {					
				if(isNextToken) {
					responseJson.put("responseCode", 7);
					responseJson.put("responseDesc", "NEXT TOKEN REQUIRED.");					
				} else {
					responseJson.put("responseCode", 1);
					responseJson.put("responseDesc", "REQUEST SUCCESSFULLY COMPLETED.");
				}
			}else {
				responseJson.put("responseCode", 8);
				responseJson.put("responseDesc", "REQUEST ACCESS DENIED.");
			}			
		} catch (Exception e) {
			responseCode = 500;
			Logger.logError(MODULE, " Error occured while upgradeDefaultLicense. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return responseJson.toString();
	}
	
	@POST
	@Path("/authenticateuserinnextcodemode")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public String authenticateuserinnextcodemode(String reqJson) {
		Logger.logInfo(MODULE, "authenticateuserinnextcodemode");
		JSONObject responseJson = new JSONObject();
		try {				
			Logger.logInfo(MODULE, "KEY#1:" + reqJson);
			JSONObject json = new JSONObject(reqJson);			
			String userId = (String) json.get("userId");
			String token = (String) json.get("passcode");
			if(userId!=null && token!=null && otps.contains(token)) {					
				isNextToken = false;
				responseJson.put("responseCode", 1);
				responseJson.put("responseDesc", "REQUEST SUCCESSFULLY COMPLETED.");					
			}else {
				responseJson.put("responseCode", 8);
				responseJson.put("responseDesc", "REQUEST ACCESS DENIED.");
			}			
		} catch (Exception e) {
			responseCode = 500;
			Logger.logError(MODULE, " Error occured while upgradeDefaultLicense. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return responseJson.toString();
	}
	
	@POST
    @Path("/testFtpSftpConnection")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String testFtpSftpConnection(String request) {
		Logger.logInfo(MODULE, "test connection");
		Boolean isConnected = false;
		boolean status;
		try {
			Map<String,Object> data = getRequestData(request);
			String ipAddress = (String) data.get("KEY#1");
			Integer port = (Integer) data.get("KEY#2");
			String username = (String) data.get("KEY#3");
			String password = (String) data.get("KEY#4");
			String strKeyFileLocation=(String) data.get("KEY#5");
			Integer maxRetrycount=(Integer) data.get("KEY#6");
			String driverType=(String) data.get("KEY#7");
			Logger.logInfo(MODULE, "ipAddress -  KEY#1 : " + ipAddress);
			Logger.logInfo(MODULE, "port -  KEY#2 : " + port);
			Logger.logInfo(MODULE, "username -  KEY#3 : " + username);
			Logger.logInfo(MODULE, "password -  KEY#4 : " + password);
			Logger.logInfo(MODULE, "strKeyFileLocation -  KEY#5 : " + strKeyFileLocation);
			Logger.logInfo(MODULE, "maxRetrycount -  KEY#6 : " + maxRetrycount);
			Logger.logInfo(MODULE, "driverType -  KEY#7 : " + driverType);
			isConnected = serverMgt.testFtpSftpConnection(ipAddress, port,username,password,strKeyFileLocation,maxRetrycount,driverType);
			if(isConnected)
        			status = true;
			else
				status = false;
		} catch (Exception e) {
			responseCode = 500;
			status = false;
			Logger.logError(MODULE, " Error occured while testing connection. Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return createJSONResponce(status,responseCode,isConnected).toString();
    }
	

}
