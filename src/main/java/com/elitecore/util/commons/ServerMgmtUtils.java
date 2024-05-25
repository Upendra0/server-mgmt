package com.elitecore.util.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import java.util.Properties;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import com.elitecore.util.logger.Logger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.net.URI;


public class ServerMgmtUtils {

	private static final String MODULE = "ServerMgmtUtils";
	
	public static byte[] convertBlobToByteArray(Blob blob) {
		
		int blobLength;
		byte[] blobAsBytes = null ;
		try {
			blobLength = (int) blob.length();
			blobAsBytes = blob.getBytes(1, blobLength);
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		
		return blobAsBytes;
		
	}
	
	public static boolean convertBlobToFileContent(String fileName, Blob fileContent) throws IOException {
		FileOutputStream os = null;
		InputStream is = null;
		try{
        	os = new FileOutputStream(fileName);
	        is = fileContent.getBinaryStream();
	        byte[] buffer = new byte[(int) fileContent.length()];
			while(is.read(buffer)!=-1){
				os.write(buffer);
			}
			os.flush();
        	os.close();
        	return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(is!=null){
				is.close();
			}
			if(os!=null){
				os.flush();
				os.close();
			}
		}
		return false;
	}
	
	public static boolean writeStreamToFile(String filePath, String fileName, byte[] fileContent) throws Exception {
		FileOutputStream os = null;
		File file = new File(filePath);
		if(!file.exists()) {
			file.mkdirs();
		}
		try{
        	os = new FileOutputStream(fileName);
			os.write(fileContent);
        	return true;
		} finally {
			if(os!=null){
				os.flush();
				os.close();
			}
		}
	}
	
	public static boolean writeStringToFile(String filePath, String fileName, String fileContent) throws Exception {
		FileWriter writer = null;
		File file = new File(filePath);
		if(!file.exists()) {
			file.mkdirs();
		}
		try{
			writer = new FileWriter(fileName);
			writer.write(fileContent);
		} finally {
			if(writer!=null){
				writer.flush();
				writer.close();
			}
		}
		return true;
	}
	
	private static boolean doLogin(FTPClient ftpClient,String username,String password) {
		boolean isLogged = false;
		try {
			if (username != null && username.length() != 0 && username != null && username.length() != 0) {
				isLogged = ftpClient.login(username, password);
			} else {
				Logger.logDebug(MODULE, "No Argument for username and password specified.");
			}
			
		} catch (Exception e) {
			Logger.logWarn(MODULE, "Problem logging in to FTP server " + ftpClient.toString());
			Logger.logTrace(MODULE, e);
		}
		return isLogged;
	}
	
	private static FTPClient getFTPClient(String strHost, String strPort,String username,String password) {
		boolean isFtpConnectionLost = false;
		FTPClient ftpClient = null;
		int iReply = 0;
		try {
			ftpClient = new FTPClient();
			if (strHost != null && strHost.length() > 0) {
				if (strPort != null && strPort.length() > 0) {
					ftpClient.connect(strHost, Integer.parseInt(strPort));
					if(isFtpConnectionLost){
						isFtpConnectionLost = false;
					}
				} else {
					ftpClient.connect(strHost);
					if(isFtpConnectionLost){
						isFtpConnectionLost = false;
					}
				}
			} else {
				ftpClient = null;
			}

			if (ftpClient != null) {
				ftpClient.enterLocalPassiveMode();
				iReply = ftpClient.getReplyCode();
				if (!FTPReply.isPositiveCompletion(iReply)) {
					ftpClient.disconnect();
					ftpClient = null;
				} else {
					boolean bLogin = doLogin(ftpClient,username,password);
					if(!bLogin) {
						ftpClient = null;
					}
				}
			}
		} catch (Exception e) {
			isFtpConnectionLost = true;
			ftpClient = null;
		}
		return ftpClient;
	}
	
	private static FTPClient getFTPConnection(String ipAddress,Integer port,String username,String password) {
		FTPClient ftpClient = null;
		try {
				ftpClient = getFTPClient(ipAddress, port.toString(),username,password);
				if (ftpClient != null)
					return ftpClient;
		}
		 catch (Exception e) {
			 Logger.logError(MODULE, "Exception while getting FTPClient."+ e.getMessage());
			 Logger.logTrace(MODULE, e);
		}
		finally {
			doLogout(ftpClient);
			disconnectFromServer(ftpClient);
			ftpClient = null;
		}
		return ftpClient;
	}
	
	private static boolean doLogout(FTPClient ftpClient) {
		boolean isLogout = false;
		try {
			if (ftpClient != null) {
				isLogout = ftpClient.logout();
			}
		} catch (Exception e) {
			Logger.logWarn(MODULE, "Error occured while Logging out from FTP"+e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return isLogout;
	}
	
	private static void disconnectFromServer(FTPClient ftpClient) {
		try {
			if (ftpClient != null && ftpClient.isConnected())
				ftpClient.disconnect();
		} catch (Exception e) {
			Logger.logWarn(MODULE, "Error occured while disconnecting from FTP."+e.getMessage());
			Logger.logTrace(MODULE, e);
		}
	}
	
	private static ChannelSftp getSftpChannel(Session session,Integer maxRetrycount) throws JSchException{
		Channel channel = null;
		int iRetryCnt = 0;
		while(iRetryCnt <= maxRetrycount){
			try {
				channel = session.openChannel("sftp");
				channel.connect();
				ChannelSftp channelSftp = (ChannelSftp)channel;
				
				return channelSftp;
			} catch (JSchException e) {
				Logger.logWarn(MODULE,"Error during making SFTP Connection, reason - " + e.getMessage());
				Logger.logTrace(MODULE,e);
				iRetryCnt++;
			}
		}
		return null;
	}
	
	
	private static Session openSession(String strHost,int port,String strUserName,String strPassword,String strKeyFileLocation,Integer maxRetrycount) throws JSchException{
		Session session = null;
		JSch jsch = new JSch();
			if(strHost != null && strHost.length() != 0) {
				try{
					int iRetryCnt = 0;
					while(iRetryCnt <= maxRetrycount){
						try{
							Logger.logInfo(MODULE, "Trying to connect host:" + strHost);
							session = jsch.getSession(strUserName, strHost, port);
							Properties config = new Properties();
							config.put("StrictHostKeyChecking", "no");
							session.setConfig(config);
							if (strPassword != null && !"".equals(strPassword)) {
						        Logger.logInfo(MODULE, "SFTP driver get connection using password.");
								session.setPassword(strPassword);
							}else{
								Logger.logInfo(MODULE, "SFTP driver get connection using public key.");
								File keyFileLocation = new File(strKeyFileLocation);
								URI keyFileURI = keyFileLocation.toURI();
								jsch.addIdentity(new File(keyFileURI).getAbsolutePath());
							}
							session.connect();
							Logger.logInfo(MODULE,"Host " + strHost + " connected.");
							break;
						}catch (JSchException e) {
							Logger.logError(MODULE,"Error occured while getting SFTP Session for host - " + strHost + " , reason - " + e.getMessage() + "");
							Logger.logTrace(MODULE,e);
							iRetryCnt++;
						}
					}
					return session;
				}catch(Exception e){
					Logger.logError(MODULE,"Error occured while getting SFTP Session for host - " + strHost + " , reason - " + e.getMessage() + "");
					Logger.logTrace(MODULE,e);
				}
			}
		throw new JSchException("Error occured while getting SFTP Session for host "+strHost.toString());
	}
	

	public static boolean testSFTPConnection(String ipAddress,Integer port,String username,String password,String strKeyFileLocation,Integer maxRetrycount) {
		ChannelSftp channelSftp = null;
		Session session = null;
		if(maxRetrycount<=0)
			maxRetrycount=1;
		try{
			session = openSession(ipAddress,port,username,password,strKeyFileLocation,maxRetrycount);
			channelSftp = getSftpChannel(session,maxRetrycount);
			if(session != null && channelSftp != null && session.isConnected() && channelSftp.isConnected()){
				return true;
			}else {
				return false;
			}
		}catch(JSchException e){
			Logger.logError(MODULE, "Error occurred while creating connection, reason - " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}finally{
			if(session != null)
				closeSession(session, channelSftp);
		}
		return false;
	}
	
	private static void closeSession(Session session, ChannelSftp channelSftp) {
		if(channelSftp != null && channelSftp.isConnected()){
			channelSftp.disconnect();
		}
		if(session != null && session.isConnected()){
			session.disconnect();
		}
	}
	
	public static boolean testFTPConnection(String ipAddress,Integer port,String username,String password) throws Exception {
		FTPClient ftpClient = getFTPConnection(ipAddress,port,username,password);
		if(ftpClient!=null) {
			return true;
		}
		return false;
	}


}
