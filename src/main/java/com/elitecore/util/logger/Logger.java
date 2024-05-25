package com.elitecore.util.logger;

/**
 * The Class Logger.
 * @author Sagar Shah
 * Sep 1, 2016
 */
public class Logger {
	
	/** The logger. */
	private static ILogger logger = new CrestelRollingFileLogger();
	
	/** The Constant REASON_TOKEN_MESSAGE. */
	private static final String REASON_TOKEN_MESSAGE = ", reason :";
	
	/** The Constant MESSAGE_PREFIX. */
	private static final String MESSAGE_PREFIX = "[ ";
	
	/** The Constant MESSAGE_CONCATOR. */
	private static final String MESSAGE_CONCATOR= " ] : " ;

	private Logger(){
		
	}

	/**
	 * Log error.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 */
	public static void logError(String moduleName, String strMessage) {
		logger.error(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage);
	}
	
	/**
	 * Log debug.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 */
	public static void logDebug(String moduleName, String strMessage){
		logger.debug(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage);
	}
	
	/**
	 * Log info.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 */
	public static void logInfo(String moduleName, String strMessage){
		logger.info(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage);
	}
	
	/**
	 * Log warn.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 */
	public static void logWarn(String moduleName, String strMessage){
		logger.warn(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage);
	}
	
	/**
	 * Log error.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 * @param t the t
	 */
	public static void logError(String moduleName, String strMessage, Throwable t) {
		logger.error(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage + REASON_TOKEN_MESSAGE + t.getMessage());
	}
	
	/**
	 * Log debug.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 * @param t the t
	 */
	public static void logDebug(String moduleName, String strMessage, Throwable t){
		logger.debug(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage + REASON_TOKEN_MESSAGE + t.getMessage());
	}
	
	/**
	 * Log info.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 * @param t the t
	 */
	public static void logInfo(String moduleName, String strMessage, Throwable t){
		logger.info(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage + REASON_TOKEN_MESSAGE + t.getMessage());
	}
	
	/**
	 * Log warn.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 * @param t the t
	 */
	public static void logWarn(String moduleName, String strMessage, Throwable t){
		logger.warn(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage + REASON_TOKEN_MESSAGE + t.getMessage());
	}
	
	/**
	 * Log trace.
	 *
	 * @param moduleName the module name
	 * @param strMessage the str message
	 */
	public static void logTrace(String moduleName, String strMessage){
		logger.trace(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR + strMessage);
	}

	/**
	 * Log trace.
	 *
	 * @param moduleName the module name
	 * @param exception the exception
	 */
	public static void logTrace(String moduleName, Throwable exception){
		logger.trace(MESSAGE_PREFIX + moduleName + MESSAGE_CONCATOR, exception);
	}

	/**
	 * Sets the logger.
	 *
	 * @param newlogger the new logger
	 */
	public static void setLogger(ILogger newlogger) {
		logger = newlogger;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static ILogger getLogger(){
		return logger;
	}
}
