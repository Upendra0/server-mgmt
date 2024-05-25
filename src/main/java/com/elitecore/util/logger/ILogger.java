package com.elitecore.util.logger;

/**
 * The Interface ILogger.
 * @author Sagar Shah
 * Sep 1, 2016
 */
public interface ILogger {

	/**
	 * Error.
	 *
	 * @param strMessage the str message
	 */
	public void error(String strMessage);

	/**
	 * Debug.
	 *
	 * @param strMessage the str message
	 */
	public void debug(String strMessage);

	/**
	 * Info.
	 *
	 * @param strMessage the str message
	 */
	public void info(String strMessage);

	/**
	 * Warn.
	 *
	 * @param strMessage the str message
	 */
	public void warn(String strMessage);

	/**
	 * Fatal.
	 *
	 * @param strMessage the str message
	 */
	public void fatal(String strMessage);

	/**
	 * Trace.
	 *
	 * @param strMessage the str message
	 */
	public void trace(String strMessage);

	/**
	 * Trace.
	 *
	 * @param exception the exception
	 */
	public void trace(Throwable exception);

	/**
	 * Trace.
	 *
	 * @param module the module
	 * @param exception the exception
	 */
	public void trace(String module, Throwable exception);

}
