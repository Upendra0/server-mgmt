package com.elitecore.util.commons;

import java.io.File;

import com.elitecore.jmx.ServerManagement;

/**
 * The Class EnvironmentVarAndPathUtil.
 * @author Sagar Shah
 * Sep 1, 2016
 */
public class EnvironmentVarAndPathUtil {

	private static final String MEDIATION_CONST = "mediation";
	private static final String MODULE_CONST = "modules";
	private static final String DICTIONARY = "dictionary";
	private static final String CORRELATION = "correlation";

	private EnvironmentVarAndPathUtil(){
	}
	
	/**
     * Provides the Environment variable value based on the key provided.
     * @param key
     * @return
     * @throws Exception 
     */
    public static String getEnvironmentVariable(String key) throws Exception{
    	String value = System.getenv(key);
    	if(value==null || "".equals(value))
    		throw new Exception("ENVIRONMENT_VARIABLE_NOT_SET ===>>  Key = "+key);
    	return value;
    }

    
	 /**
     * Provide the server's bin directory path
     * @param serverHome
     * @return
     */
    public static String getServerHomeBinPath() throws Exception{
    	return getCrestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "bin" + File.separator ;
    }
    
    /**
     * Provide the server's bin archive directory path
     * @param serverHome
     * @return
     */
    public static String getServerHomeBinArchivePath() throws Exception{
    	return getCrestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "bin_archive" + File.separator ;
    }
    
    /**
     * Provide the server's config directory path
     * @param serverHome
     * @return
     */
    public static String getServerHomeConfigPath() throws Exception{
    	return getCrestelPEngineHome() +  File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "config" + File.separator;
    }
    
    /**
     * Provide the server's config archive directory path
     * @param serverHome
     * @return
     */
    public static String getServerHomeConfigArchivePath() throws Exception{
    	return getCrestelPEngineHome() +  File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "config_archive" + File.separator;
    }
    
    /**
     * Gets the server home log path.
     *
     * @return the server home log path
     * @throws Exception the exception
     */
    public static String getServerHomeLogPath() throws Exception{
    	return new ServerManagement().crestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "logs" + File.separator ;
    }
    
    /**
     * Gets the server home license path.
     *
     * @return the server home license path
     * @throws Exception the exception
     */
    public static String getServerHomeLicensePath() throws Exception{
    	return new ServerManagement().crestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + "license" + File.separator ;
    }
    
    /**
     * Provides crestel p engine home from the environment variables
     */
    public static String getCrestelPEngineHome() throws Exception{  
    	return  getEnvironmentVariable("CRESTEL_P_ENGINE_HOME");
    }
    
    /**
     * Provides java home from the environment variables
     */
    public static String getJavaHome() throws Exception{ 
    	return getEnvironmentVariable("JAVA_HOME");
    }
    
    /**
     * Provide the server's dictionary directory path
     * @return
     * @throws Exception
     */
    public static String getServerHomeDictionaryPath() throws Exception{
    	return getCrestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator;
    }
    
    /**
     * Provide the server's correlation directory path
     * @return
     * @throws Exception
     */
    public static String getCorrelationPath() throws Exception{
    	return getCrestelPEngineHome() + File.separator + MODULE_CONST + File.separator +
    			MEDIATION_CONST + File.separator + DICTIONARY + File.separator + CORRELATION;
    }
}
