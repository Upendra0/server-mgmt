/**
 * 
 */
package com.elitecore.jmx;

import com.elitecore.core.server.hazelcast.HazelcastCacheConstants;
import com.elitecore.core.server.hazelcast.HazelcastCacheManager;
import com.elitecore.util.k8s.CreateServerInstanceScript;
import com.elitecore.util.logger.Logger;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.net.httpserver.HttpServer;
/**
 * @author Sunil Gulabani
 * Sep 1, 2015
 */
public class ServerMgmtJMX {
	private static final String MODULE = "SERVER MANAGEMENT JMX" ;
	private static int offSet = 10000;
	

	public static void main(String[] args) throws Exception {
		
		initHazelcast();
		initRestWebService();
		Logger.logInfo(MODULE, "ServerMgmtJMX is running...");
		try {
			//String isAutoGenerateScript = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.KUBERNETES_ENV);
			//if(KubernateConstant.TRUE.equalsIgnoreCase(isAutoGenerateScript)) {
				CreateServerInstanceScript createServerInstanceScript = new CreateServerInstanceScript();
				createServerInstanceScript.autoGenerationBinConfigDictionary();
			//}
		} catch(Exception e) {
			Logger.logError(MODULE, "Error while getting environment variable, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			Logger.logError(MODULE, "Error while starting server, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
	}
	
	private static void initRestWebService() {
		String restPort = System.getProperty("com.sun.management.jmxremote.port");
		int port = Integer.parseInt(restPort) + offSet;
		String uri = "http://localhost:" + port + "/mediation/rest/";
		try {
			DefaultResourceConfig defaultConfig = new DefaultResourceConfig(RestServiceImpl.class);
			HttpServer server = HttpServerFactory.create(uri, defaultConfig);
			server.start();
			Logger.logInfo(MODULE, "Rest Service Started at " + uri);
		} catch (Exception e) {
			Logger.logWarn(MODULE, "Unable to intialize rest web Service. Please check -Dcom.sun.management.jmxremote.port entry in startup script");
			Logger.logTrace(MODULE, e);
		}
	}

	public static int getOffSet() {
		return offSet;
	}
	
	private static void initHazelcast(){
		try{
			String value = System.getenv(HazelcastCacheConstants.HAZELCAST_CACHE_TYPE);
			if(value != null && value.equalsIgnoreCase(HazelcastCacheConstants.HAZELCAST_LOCAL_CACHE)) {
				HazelcastCacheManager.HAZELCAST_CACHE_MANAGER_INSTANCE.initServerInstance();	
			}
			
		}catch(Exception ex){
			Logger.logWarn(MODULE, "Unable  to Hazelcast. Please check environment variable entry in startup script");
			Logger.logTrace(MODULE, ex);
		}
	}
	
}