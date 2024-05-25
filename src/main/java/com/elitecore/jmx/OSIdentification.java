/**
 * 
 */
package com.elitecore.jmx;

/**
 * @author Sunil Gulabani
 * Sep 1, 2015
 */
public class OSIdentification {
	
	private static final String OS = System.getProperty("os.name").toLowerCase();

	private OSIdentification(){
		
	}
	
	public static boolean isWindows() {
		return OS.indexOf("win") >= 0;
	}

	public static boolean isMac() {
		return OS.indexOf("mac") >= 0;
	}

	public static boolean isUnix() {
		return OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 ;
	}

	public static boolean isSolaris() {
		return OS.indexOf("sunos") >= 0;
	}
}