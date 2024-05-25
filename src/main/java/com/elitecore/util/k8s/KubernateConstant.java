package com.elitecore.util.k8s;

public class KubernateConstant {

	public static final String ENV_SERVER_MGMT_PORT = "SERVER_MGMT_PORT";
	public static final String ENV_SERVER_MGMT_IP_ADDRESS = "SERVER_MGMT_IPADDRESS";
	public static final String ENV_DB_URL = "SERVER_MANAGER_DB_URL";
	public static final String ENV_DB_USERNAME = "SERVER_MANAGER_DB_USERNAME";
	public static final String ENV_DB_PASSWORD = "SERVER_MANAGER_DB_PASSWORD"; //NOSONAR
	
	public static final String ENV_DB_URL_IPLMS = "IPLMS_DB_URL";
	public static final String ENV_DB_USERNAME_IPLMS = "IPLMS_DB_USERNAME";
	public static final String ENV_DB_PASSWORD_IPLMS = "IPLMS_DB_PASSWORD"; //NOSONAR
	
	public static final String KUBERNETES_ENV = "KUBERNETES_ENV";
	public static final String MONGO_DB_URL = "MONGO_DB_URL";
	public static final String MONGO_SERVER_INSTANCE_PORT = "MONGO_SERVER_INSTANCE_PORT";
		
	public static final String COMMA = ",";
	public static final String SCRIPT_PREFIX = "startServer_";
	public static final String SCRIPT_SUFFIX = ".sh";
	public static final String TRUE = "true";
		
	private KubernateConstant() {
		
	}
}
