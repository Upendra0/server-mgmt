/**
 * 
 */
package com.elitecore.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.elitecore.util.commons.EnvironmentVarAndPathUtil;
import com.elitecore.util.logger.Logger;

/**
 * @author Sunil Gulabani Sep 1, 2015
 */
public class FileCopy {
	
	private static final String MODULE = "FILE COPY";
	private static final String MIN_MEMORY = "-Xms";
	private static final String MAX_MEMORY = "-Xmx";
	private static final String SH_EXT = "sh";
	private static final String BAT_EXT = "bat";
	private static final String JMX_PASSWORD_AUTHENTICATION_ENABLE = "JMX_PASSWORD_AUTHENTICATION_ENABLE";
	/**
	 * Check if file exist
	 * @param fileName
	 * @return
	 */
	public boolean fileExist(String fileName){
		File f = new File(fileName);

		if(f.exists()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Renames file
	 * @param srcFilePath
	 * @return
	 */
	public boolean renameFile(String srcFilePath, String destFilePath, boolean isMove){
		File oldfile =new File(srcFilePath);
		File newfile =new File(destFilePath + "_DEL_" + new Date().getTime());
		
		File destinationFolder = newfile.getParentFile();
		if(!destinationFolder.exists()){
			destinationFolder.mkdir();
		}
		
		try {
			if(isMove){
				FileUtils.moveFile(oldfile, newfile);
			} else {
				FileUtils.copyFile(oldfile, newfile);
			}
			
			if (newfile.exists()) 
				return true;
			else 
				return false;

		} catch (IOException e) {
			Logger.logError(MODULE, "Error while file operation, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			return false;
		}
	}
	
	/**
	 * Copy the file and replace parameters
	 * @param sourceFilePath
	 * @param destinationFilePath
	 * @param port
	 * @param minMemory
	 * @param maxMemory
	 */
	public void copyAndReplace(
					String sourceFilePath, String destinationFilePath,
					int port, int minMemory, int maxMemory, String ipAddress) {
		Logger.logInfo(MODULE, "Copying file - " + sourceFilePath + 
				" to destination file path - " + destinationFilePath);
		
		InputStream inStream = null;
		OutputStream outStream = null;
		int startIdx = 0;
		int endIdx = 0;
		String oldstring = "";
		String finalContent = null;
		StringBuilder content = new StringBuilder();
		try{
			File sourceFile = new File(sourceFilePath);
			inStream = new FileInputStream(sourceFile);
			byte[] buffer = new byte[(int) sourceFile.length()];

			// copy the file content in bytes
			while (inStream.read(buffer) > 0) {
				content.append(new String(buffer));
			}
			finalContent = content.toString();

		} catch (IOException e) {
			Logger.logError(MODULE, "Error while file reading operation, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e) {
					Logger.logWarn(MODULE, "Error while closing input stream, Reason : " + e.getMessage());
				}
			}
		}
		try {
			File destinationFile = new File(destinationFilePath);
			outStream = new FileOutputStream(destinationFile);
			String extension = (destinationFile.getName().split("\\."))[1];
			
			startIdx = finalContent.indexOf("-Dcom.sun.management.jmxremote.port");
			endIdx = finalContent.indexOf(' ',startIdx);
			oldstring = finalContent.substring(startIdx,endIdx);
			finalContent = finalContent.replaceAll(oldstring, "-Dcom.sun.management.jmxremote.port="+ port + " ");
			
			if(finalContent.indexOf("-Djava.rmi.server.hostname")==-1){
				String tempString = "-Dcom.sun.management.jmxremote.port="+ port + " ";
				int length = tempString.length();
				String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf("-Dcom.sun.management.jmxremote.port="+ port + " ")+length);
	            String tempFinalContentLast = finalContent.substring(finalContent.indexOf("-Dcom.sun.management.jmxremote.port="+ port + " ")+length);
	            tempFinalContentFirst = tempFinalContentFirst + "-Djava.rmi.server.hostname=127.0.0.1 ";
                finalContent = tempFinalContentFirst + tempFinalContentLast;
            }
           
            if(finalContent.indexOf("-Dcom.sun.management.jmxremote.rmi.port")==-1){
            	String tempString = "-Djava.rmi.server.hostname=127.0.0.1 ";
				int length = tempString.length();
                String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf("-Djava.rmi.server.hostname=127.0.0.1 ")+length);
                String tempFinalContentLast = finalContent.substring(finalContent.indexOf("-Djava.rmi.server.hostname=127.0.0.1 ")+length);
                tempFinalContentFirst = tempFinalContentFirst + "-Dcom.sun.management.jmxremote.rmi.port="+ port + " ";
                finalContent = tempFinalContentFirst + tempFinalContentLast;
            }
			
			if(ipAddress!=null && !(ipAddress.trim().equals(""))) {
				startIdx = finalContent.indexOf("-Djava.rmi.server.hostname");
				endIdx = finalContent.indexOf(' ',startIdx);
				oldstring = finalContent.substring(startIdx,endIdx);
				finalContent = finalContent.replaceAll(oldstring, "-Djava.rmi.server.hostname="+ ipAddress + " ");
		    }
			
			startIdx = finalContent.indexOf("-Dcom.sun.management.jmxremote.rmi.port");
			endIdx = finalContent.indexOf(' ',startIdx);
			oldstring = finalContent.substring(startIdx,endIdx);
			finalContent = finalContent.replaceAll(oldstring, "-Dcom.sun.management.jmxremote.rmi.port="+ port + " ");
			
			String isPasswordEnable = System.getenv(JMX_PASSWORD_AUTHENTICATION_ENABLE);
			if(isPasswordEnable !=null && !"".equals(isPasswordEnable.trim()) && Boolean.parseBoolean(isPasswordEnable.toLowerCase())) {					
				startIdx = finalContent.indexOf("-Dcom.sun.management.jmxremote.authenticate");
				endIdx = finalContent.indexOf(' ',startIdx);
				oldstring = finalContent.substring(startIdx,endIdx);
				finalContent = finalContent.replaceAll(oldstring, "-Dcom.sun.management.jmxremote.authenticate="+ isPasswordEnable + " ");
			}
					
			if(minMemory>0){
				startIdx = finalContent.indexOf(MIN_MEMORY);
				endIdx = finalContent.indexOf(' ',startIdx);
				oldstring = finalContent.substring(startIdx,endIdx);
				finalContent = finalContent.replaceAll(oldstring, MIN_MEMORY +  minMemory + "m" );
			}

			if(maxMemory>0){
				startIdx = finalContent.indexOf(MAX_MEMORY);
				endIdx = finalContent.indexOf(' ',startIdx);
				oldstring = finalContent.substring(startIdx,endIdx);
				finalContent = finalContent.replaceAll(oldstring, MAX_MEMORY +  maxMemory + "m" );
			}
			
			if (extension != null && extension.equals(SH_EXT)) {
				
				if (finalContent.indexOf("JAR_HOME3=$CRESTEL_P_ENGINE_HOME/runtime_jars") == -1) {
					String tempString = "JAR_HOME2=$CRESTEL_P_ENGINE_HOME/jars";
					int length = tempString.length();
					String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf(tempString)+length);
		            String tempFinalContentLast = finalContent.substring(finalContent.indexOf(tempString)+length);
		            tempFinalContentFirst = tempFinalContentFirst + "\n" +"JAR_HOME3=$CRESTEL_P_ENGINE_HOME/runtime_jars";
	                finalContent = tempFinalContentFirst + tempFinalContentLast;
				}
				
				int idx1 = finalContent.indexOf("done");
				if (finalContent.indexOf("for i in ${JAR_HOME3}") == -1) {
					String tempString = "done";
					int length = tempString.length();
					String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf(tempString, idx1 +1)+length);
		            String tempFinalContentLast = finalContent.substring(finalContent.indexOf(tempString, idx1 +1)+length);
		            tempFinalContentFirst = tempFinalContentFirst + "\n\n" +"for i in ${JAR_HOME3}/*.jar ; do";
		            tempFinalContentFirst = tempFinalContentFirst + "\n" +"SERVERJARS=${SERVERJARS}:$i";
		            tempFinalContentFirst = tempFinalContentFirst + "\n" +"done" + "\n";
	                finalContent = tempFinalContentFirst + tempFinalContentLast;
				}
			}
			else if (extension != null && extension.equals(BAT_EXT)) {
				
				if (finalContent.indexOf("set JAR_HOME3=%CRESTEL_P_ENGINE_HOME%/runtime_jars") == -1) {
					String tempString = "set JAR_HOME2=%CRESTEL_P_ENGINE_HOME%/jars";
					int length = tempString.length();
					String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf(tempString)+length);
		            String tempFinalContentLast = finalContent.substring(finalContent.indexOf(tempString)+length);
		            tempFinalContentFirst = tempFinalContentFirst + "\n" +"set JAR_HOME3=%CRESTEL_P_ENGINE_HOME%/runtime_jars";
	                finalContent = tempFinalContentFirst + tempFinalContentLast;
				}
				
				int idx1 = finalContent.indexOf("do call jars.bat %%i");
				if (finalContent.indexOf("for %%i in (%JAR_HOME3%\\*.jar)") == -1) {
					String tempString = "do call jars.bat %%i";
					int length = tempString.length();
					String tempFinalContentFirst = finalContent.substring(0, finalContent.indexOf(tempString, idx1 +1)+length);
		            String tempFinalContentLast = finalContent.substring(finalContent.indexOf(tempString, idx1 +1)+length);
		            tempFinalContentFirst = tempFinalContentFirst + "\n" +"for %%i in (%JAR_HOME3%\\*.jar) do call jars.bat %%i" + "\n";
	                finalContent = tempFinalContentFirst + tempFinalContentLast;
				}
			}
			
			outStream.write(finalContent.getBytes(), 0, finalContent.length());
			Logger.logInfo(MODULE, "File is copied successful!");
			Logger.logInfo(MODULE, "Destination content length is : "+finalContent.length() +
					" and source content length is : "+ content.length());
		} catch (IOException e) {
			Logger.logError(MODULE, "Error while file copy operation, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			if(outStream != null){
				try {
					outStream.close();
				} catch (IOException e) {
					Logger.logWarn(MODULE, "Error while closing output stream, Reason : " + e.getMessage());
				}
			}
			
		}
	}
	
	/**
	 * Write File in Dictionary Folder
	 * 
	 * @param fileName
	 * @param filePath
	 * @param fileContent
	 * @return
	 * @throws Exception 
	 */
	public static boolean writeDictionaryFiles(String fileName, String filePath, byte[] fileContent) throws Exception {
		String dictionaryname = EnvironmentVarAndPathUtil.getServerHomeDictionaryPath() + filePath + File.separator + fileName;
		return false;
	}
}
