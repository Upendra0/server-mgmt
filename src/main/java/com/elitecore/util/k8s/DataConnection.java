package com.elitecore.util.k8s;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.elitecore.util.commons.EnvironmentVarAndPathUtil;
import com.elitecore.util.logger.Logger;

public class DataConnection {
	
	private static final String MODULE = "DATACONNECTION";
	private static Connection serverManagerConn = null;
	private static Connection iploggerConn = null;
	private static final String ORACLE="oracle";
	private static final String POSTGRESQL="postgresql";
	private static final String MYSQL="mysql";
	private static final String ORACLE_JDBC_DRIVER_ORACLEDRIVER="oracle.jdbc.driver.OracleDriver";
	private static final String ORG_POSTGRESQL_DRIVER="org.postgresql.Driver";
	private static final String COM_MYSQL_JDBC_DRIVER="com.mysql.jdbc.Driver";
	private static final String SM_DATASOURCE = "ServerManager";
	private static final String IPLMS_DATASOURCE = "iplogger";
	
	private DataConnection() {
		
	}
	
	public static Connection getInstance(String dataSource) throws ClassNotFoundException, SQLException{
		Connection conn = null;
		if(SM_DATASOURCE.equalsIgnoreCase(dataSource)) {
			conn = getSMConnection();
		} else if(IPLMS_DATASOURCE.equalsIgnoreCase(dataSource)){
			conn = getIPLMSConnection();
		}
		return conn;
	}
	
	private static void loadDatabaseDriver(String url) throws ClassNotFoundException {
		if(url.contains(ORACLE)) {
			Class.forName(ORACLE_JDBC_DRIVER_ORACLEDRIVER);
		} else if(url.contains(POSTGRESQL)) {
			Class.forName(ORG_POSTGRESQL_DRIVER);
		} else if(url.contains(MYSQL)) {
			Class.forName(COM_MYSQL_JDBC_DRIVER);
		}
	}

	private static Connection getSMConnection() throws ClassNotFoundException, SQLException {
		if(serverManagerConn==null) {
			try {
				String url = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.ENV_DB_URL);
				String username = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.ENV_DB_USERNAME);
				String password = EnvironmentVarAndPathUtil.getEnvironmentVariable(KubernateConstant.ENV_DB_PASSWORD);
				loadDatabaseDriver(url);
				serverManagerConn = DriverManager.getConnection(url,username,password);
			} catch (Exception e) {
				Logger.logError(MODULE, "Error while creating ServerManager database connection : " + e.getMessage());
				Logger.logError(MODULE, e.toString());
				e.printStackTrace();
			}
		}
		return serverManagerConn;
	}
	private static Connection getIPLMSConnection() throws ClassNotFoundException, SQLException {
		if(iploggerConn==null) {
			try {
				String url_iplms = System.getenv(KubernateConstant.ENV_DB_URL_IPLMS);
				String username_iplms = System.getenv(KubernateConstant.ENV_DB_USERNAME_IPLMS);
				String password_iplms = System.getenv(KubernateConstant.ENV_DB_PASSWORD_IPLMS);
				loadDatabaseDriver(url_iplms);
				iploggerConn = DriverManager.getConnection(url_iplms,username_iplms,password_iplms);
			} catch (Exception e) {
				Logger.logError(MODULE, "Error while creating Iplogger database connection : " + e.getMessage());
				Logger.logError(MODULE, e.toString());
				e.printStackTrace();
			}
		}
		return iploggerConn;
	}
	
}
