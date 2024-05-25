package com.elitecore.util.logger;


import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.elitecore.util.commons.EnvironmentVarAndPathUtil;

/**
 * The Class CrestelRollingFileLogger.
 * @author Sagar Shah
 * Sep 1, 2016
 */
public class CrestelRollingFileLogger implements ILogger {

	/** The logger. */
	protected Logger logger = Logger.getRootLogger();

	/** The relation key. */
	private long relationKey;

	/**
	 * Instantiates a new crestel rolling file logger.
	 *
	 * @param serverLogPath the server log path
	 */
	public CrestelRollingFileLogger(){
		try{
			RollingFileAppender rollingFileAppender = (RollingFileAppender)logger.getAppender("file");
			String logFileName = EnvironmentVarAndPathUtil.getServerHomeLogPath() + File.separator + "serverMgmtJMX.log";
			rollingFileAppender.setFile(logFileName);
			rollingFileAppender.activateOptions();
		}catch(Exception e){
			System.out.println("Error : Cannot initialize file logger.");
			e.printStackTrace();
		}
	}

    
	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#error(java.lang.String)
	 */
	@Override
	public void error(String strMessage) {
		logger.error(strMessage);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#debug(java.lang.String)
	 */
	@Override
	public void debug(String strMessage) {
		logger.debug(strMessage);

	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#info(java.lang.String)
	 */
	@Override
	public void info(String strMessage) {
		logger.info(strMessage);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#warn(java.lang.String)
	 */
	@Override
	public void warn(String strMessage) {
		logger.warn(strMessage);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#fatal(java.lang.String)
	 */
	@Override
	public void fatal(String strMessage) {
		logger.fatal(strMessage);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#trace(java.lang.String)
	 */
	@Override
	public void trace(String strMessage) {
		logger.trace(strMessage);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#trace(java.lang.Throwable)
	 */
	@Override
	public void trace(Throwable exception) {
		trace("",exception);
	}

	/* (non-Javadoc)
	 * @see com.elitecore.logger.ILogger#trace(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void trace(String module, Throwable exception) {
		long localRelationKey = relationKey++;

		StringWriter stringWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stringWriter));
		logger.trace(module + " ST KEY : [" + localRelationKey + "] "  + exception.getMessage());
		logger.trace(module + " ST KEY : [" + localRelationKey + "] "  + stringWriter.toString());

	}

}
