package com.elitecore.jmx.license;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.elitecore.util.logger.Logger;

public class LicenseUtility {

	private static final String ALGORITHMDESEDE="DESede";
	private static final String SYMKEYFILE ="des.key";
	private static final String ALGORITHM = "RSA";
	private static final String LICENSE_SPLIT_TOKEN = "=";
	private static final String MODULE = "LICENSE_UTILITY";
	public static final String LICENSE_STR = "License.key";
	public static final String PRIVATE_STR = "private.key";
	public static final String TRIAL_STR = "TrialVer.key";
	public static final String TEMP_LICENSE_STR = "Temp_License.key";
	
	public static final String DATEFORMAT="dd/MM/yyyy";
	public static final String TEMP_FILE_LICENSE = "Product_Info.txt";

	public static final String 	CUSTOMER_NAME = "Customer Name";
	public static final String	DEFAULT_CUSTOMER_NAME = "Elitecore";
	public static final String 	LOCATION = "Location";
	public static final String 	DEFAULT_LOCATION = "India";
	public static final String 	MACID = "MacID";
	public static final String 	PRODUCT = "Product";
	public static final String	DEFAULT_PRODUCT = "MEDIATION,IPLMS,CGF";
	public static final String 	VERSION = "Version";
	public static final String 	FULL = "Full";
	public static final String 	START_DATE = "Start Date";
	public static final String 	END_DATE = "End Date";
	public static final String 	HOSTNAME = "HostName";
	public static final String 	IP = "Ip";
	public static final String 	DAILY_RECORDS= "Daily Records";
	public static final int 	DEFAULT_DAILY_RECORDS = 0;
	public static final String 	MONTHLY_RECORDS = "Monthly Records";
	public static final int 	DEFAULT_MONTHLY_RECORDS = 0;
	public static final String 	TPS = "Tps";
	public static final String 	TRIAL_TPS="PARSING_SERVICE:100000,PROCESSING_SERVICE:100000,DISTRIBUTION_SERVICE:100000,IPLOG_PARSING_SERVICE:100000";
	
	public static final int 	DEFAULT_YEARS = 100;
	
	public static final String LICENSE_HOSTNAME_DEFAULT="crestel.mediation.com";

	/**
	 * @param macId - Mac id of the server
	 * @param host - Host name of the server
	 * @return - Server id as a combination of Mac id and Host name.
	 */
	public String generateServerId(String macId,String host) {
		String serverId = null;
		String [] macArray = macId.toUpperCase().split(":");
		int intlh1=10;
		int intlh2=10;
		int intlh3=10;
		if(macArray.length == 6) {
			String mid1 = new StringBuilder(macArray[0] + macArray[1]).reverse().toString();
			String mid2 = new StringBuilder(macArray[2] + macArray[3]).reverse().toString();
			String mid3 = new StringBuilder(macArray[4] + macArray[5]).reverse().toString();
			if(host != null) {
				host = host.replaceAll("\\.", "").trim();
				int hostLen = host.length();
				int factor = hostLen/3;
				String lh1 = host.substring(0,factor);
				String lh2 = host.substring(factor, 2*factor);
				String lh3 = host.substring(factor*2);
				intlh1 = Math.abs(lh1.hashCode())%100; //NOSONAR
				intlh2 = Math.abs(lh2.hashCode())%100; //NOSONAR
				intlh3 = Math.abs(lh3.hashCode())%100; //NOSONAR
			}
			serverId = intlh1 + mid1 + "-" + intlh2 + mid2 + "-" + intlh3 + mid3;
		}
		return serverId;
	}


	/**
	 * @return - Return the ip list of the system.
	 */
	public List<String> getIpList() {
		List<String> ipList = new ArrayList<String>();
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			String ip = socket.getLocalAddress().getHostAddress();
			ipList.add(ip);			
		} catch (SocketException ex) {
			Logger.logError(MODULE, "Error occured while getting IP List, reason : " + ex.getMessage());
			Logger.logTrace(MODULE, ex);
		} catch (UnknownHostException ex) {
			Logger.logError(MODULE, "Error occured while getting IP List, reason : " + ex.getMessage());
			Logger.logTrace(MODULE, ex);
		}		
		return ipList;
	}

	/**
	 * @param ip - Ip of the system
	 * @return - Host name for the specified IP.
	 */
	public String getHostName(String ip) {
		String hostName = LICENSE_HOSTNAME_DEFAULT;
		return hostName;
	}

	/**
	 * @param Ip - Ip of the system.
	 * @return - Mac id for the specified IP.
	 */
	public String getMacFromIP(String iP) {
		InetAddress ip;
		StringBuilder sb = new StringBuilder();
		try {
			ip = InetAddress.getByName(iP);
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Error occured while getting MAC Id, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return sb.toString();
	}

	/**
	 * @param msg - Accepts the license data in form of String
	 * @return - License data in form of hash map.
	 */
	public Map<String,String> getData(String msg) {
		Map<String,String> hm = new HashMap<String, String>();
		StringTokenizer token = new StringTokenizer(msg, "\n");
		while(token.hasMoreTokens()) {
			String []temp = token.nextToken().split(LICENSE_SPLIT_TOKEN);
			if(temp !=null && temp.length >=2) {
				if((temp[0].trim().equalsIgnoreCase(LicenseUtility.MONTHLY_RECORDS) || temp[0].trim().equalsIgnoreCase(LicenseUtility.DAILY_RECORDS)) && ("0".equalsIgnoreCase(temp[1].trim())))
					temp[1]="NA";
				hm.put(temp[0].trim(), temp[1].trim());
			}
		}
		return hm;
	}

	/**
	 * @param theFile - Represents destination file in which data will be written.
	 * @param bytes - Takes the data in form of byte array.
	 */
	public void writeBytesToFile(File theFile, byte[] bytes)  {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(theFile);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);
		} catch (IOException e) {
			Logger.logError(MODULE, "Error occured while writeBytesToFile, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				Logger.logError(MODULE, "Error occured while writeBytesToFile, reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
	}

	 /**
     * Encrypt license detail.
     *
     * @param licData the lic data
     * @param repositoryPath the repository path
     * @return the string
     */
    public String encryptLicenseDetail(String licData,String repositoryPath) {
    	String result = "false";
    	String message = licData;
    	String privateKeyPath = repositoryPath + PRIVATE_STR;
    	String actualData = repositoryPath + TEMP_FILE_LICENSE;  
    	String licenseKey = repositoryPath + LICENSE_STR;
    	File productInfo = new File(repositoryPath + TEMP_FILE_LICENSE);
    	byte [] b1 = encryptSymMsg(message,repositoryPath);
    	writeBytesToFile(productInfo,b1);
    	try {
			result = encryptFile(privateKeyPath, new FileInputStream(actualData), new FileOutputStream(licenseKey));
			productInfo.delete();
		} catch (FileNotFoundException e) {
			Logger.logError(MODULE, "Error while applying default full version -- encryptLicenseDetail, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		if(!result.equalsIgnoreCase("false")) {
			result = licenseKey;
		}
    	return result;
    }
    
    /**
     * Encrypt file.
     *
     * @param privateKeyPath the private key path
     * @param in the in
     * @param out the out
     * @return the string
     */
    private String encryptFile(String privateKeyPath, InputStream in, OutputStream out) {  
        byte[] b = new byte[128];  
        // Create a cipher object and use the generated key to initialize it
        ObjectInputStream inputStream = null;
        PrivateKey privateKey = null;
        Cipher cipher = null;
        CipherOutputStream cos = null;
        String result = "false";
		try {
			inputStream = new ObjectInputStream(new FileInputStream(privateKeyPath));
			privateKey = (PrivateKey) inputStream.readObject();
		        cipher = Cipher.getInstance(ALGORITHM);  
		        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
		        cos = new CipherOutputStream(out, cipher);  
		        int i;  
		        while ((i = in.read(b)) >= 0) {  
		            cos.write(b, 0, i);
		        } 
		    result = "true";
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while applying default full version -- encryptFile, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			try {
				if(inputStream != null){
					inputStream.close();
				}
				if(cos!=null) {
					cos.flush();
					cos.close();  
				}
				if(in!=null) 
					in.close();
			} catch (IOException e) {
				Logger.logError(MODULE, "Error while applying default full version -- encryptFile, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}  
	         
		}
		return result;
    }
    
    /**
     * Encrypt sym msg.
     *
     * @param str the str
     * @param repositoryPath the repository path
     * @return the byte[]
     */
    private byte[] encryptSymMsg(String str,String repositoryPath) {
    	ObjectInputStream inputStream=null;
    	Key symKey = null;
    	Cipher c = null;
    	String symKeyFile =repositoryPath + SYMKEYFILE;
    	byte [] encryptedMessage = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(new File(symKeyFile)));
			symKey = (Key) inputStream.readObject();
			c = Cipher.getInstance(ALGORITHMDESEDE);
			c.init(Cipher.ENCRYPT_MODE, symKey);
			encryptedMessage = c.doFinal(str.getBytes());
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while applying default full version -- encryptSymMsg, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			try {
				if(inputStream!=null)
				 inputStream.close();
			} catch (IOException e) {
				Logger.logError(MODULE, "Error while applying default full version -- encryptSymMsg, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
		return encryptedMessage;
    }
    
	/**
	 * @param startDate - Represent Start Date.
	 * @param endDate - Represent End Date.
	 * @return - status of whether system date lies between start date and end date.
	 */
	public boolean validateLicDate(String startDate,String endDate) {
		boolean result = Boolean.FALSE;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
			Date stopDate = sdf.parse(endDate);
			Date beginDate = sdf.parse(startDate);
			Date date = new Date();
			if(date.after(stopDate) || date.before(beginDate)) {
				result = Boolean.FALSE;
			}else{
				result = Boolean.TRUE;
			}
		} catch (ParseException e) {
			Logger.logError(MODULE, "Error occured while validating license dates, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return result;
	}
	
	/**
	 * @param licenseFilePath - Absolute path of the license file
	 * @param repositoryPath - Repository path for the license folder.
	 * @return - decrypted message in form of string.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws ClassNotFoundException
	 * @throws BadPaddingException
	 */
	public String decrypt(String licenseFilePath,String repositoryPath) throws IOException,
	InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
	ClassNotFoundException, BadPaddingException{

		// Start for https://jira.crestel.in/browse/MED-3516
		long nanoTime = System.nanoTime();
		String textFormattedFileName = repositoryPath + File.separator + nanoTime + TEMP_FILE_LICENSE;
		// End for https://jira.crestel.in/browse/MED-3516
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		File tempFile = new File(textFormattedFileName);
		String licenseKey = licenseFilePath;
		String publicKeyPath = repositoryPath + File.separator + "public.key";
		String decryptedString = "";
		try{
			fileInputStream = new FileInputStream(licenseKey);
			fileOutputStream = new FileOutputStream(textFormattedFileName);
			decryptFile(publicKeyPath, fileInputStream, fileOutputStream);
			// Start for https://jira.crestel.in/browse/MED-3360
			if(tempFile.exists()){
				byte[] b = readBytesFromFile(tempFile);
				decryptedString = decryptSymMsg(b, repositoryPath);
			}else{
				Logger.logError(MODULE, "Problem while reading " + textFormattedFileName + " file, file does not exists at location ");
				Logger.logDebug(MODULE, "Problem while reading " + textFormattedFileName + " file, file does not exists at location ");
			}
			// End for https://jira.crestel.in/browse/MED-3360
		}catch (IOException e) {
			Logger.logError(MODULE, "Error while applying upgrading full version -- decrypt, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw e;
		}finally{
			if(tempFile.exists())
				tempFile.delete();
			try{
				if(fileInputStream != null)
					fileInputStream.close();
			}catch (Exception e) {
				Logger.logError(MODULE, "Error while applying upgrading full version -- decrypt, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
			try{
				if(fileOutputStream != null)
					fileOutputStream.close();
			}catch (Exception e) {
				Logger.logError(MODULE, "Error while applying upgrading full version -- decrypt, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
		return decryptedString;
	}

	/**
	 * @param strByte - Encrypted data in form of byte array.
	 * @param repositoryPath - Repository path for license directory.
	 * @return - Decrypted data in form of string format.
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decryptSymMsg(byte [] strByte,String repositoryPath) throws ClassNotFoundException,
	IOException,NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException, BadPaddingException {

		String str = null;
		ObjectInputStream inputStream = null;
		Key symKey = null;
		FileInputStream fis = null;
		Cipher c = null;
		try {
			File file = new File(repositoryPath+File.separator+SYMKEYFILE);
			fis = new FileInputStream(file);
			inputStream = new ObjectInputStream(fis);
			symKey = (Key) inputStream.readObject();
			c = Cipher.getInstance(ALGORITHMDESEDE);
			c.init(Cipher.DECRYPT_MODE, symKey);
			str = new String(c.doFinal(strByte));
		}finally {
			try{
				if(fis != null)
					fis.close();
				if (inputStream != null)
					inputStream.close();
			}catch (Exception e) {
				Logger.logError(MODULE, "Error while applying upgrading full version -- decryptSymMsg, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
		return str;
	}

	/**
	 * @param file - Source file from which data has to be read.
	 * @return - File contents in form of byte array.
	 * @throws IOException
	 */
	public byte[] readBytesFromFile(File file) throws IOException {
		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		InputStream is = null;
		try{
			is = new FileInputStream(file);
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}
		}catch (IOException e) {
			Logger.logError(MODULE, "Error while applying upgrading full version -- readBytesFromFile, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw e;
		}finally{
			try{
				if(is != null)
					is.close();
			}catch (Exception e) {
				Logger.logError(MODULE, "Error while applying upgrading full version -- decryptSymMsg, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
		return bytes;
	}
	
	/**
	 * @param publicKeyPath - Represents the path where public key is available.
	 * @param in - Represent the source to read the data.
	 * @param out - Represents the destination where data will be written.
	 */
	public void decryptFile(String publicKeyPath, InputStream in, OutputStream out) {
		byte[] b = new byte[128];
		ObjectInputStream inputStream = null;
		PublicKey publicKey = null;
		Cipher cipher = null;
		CipherInputStream cis =null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(publicKeyPath);
			inputStream = new ObjectInputStream(fis);
			publicKey = (PublicKey) inputStream.readObject();
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			cis = new CipherInputStream(in, cipher);
			int i;
			while ((i = cis.read(b)) >= 0) {
				out.write(b, 0, i);
			}
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while applying upgrading full version -- decryptFile, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		} finally {
			try{
				if(fis != null)
					fis.close();
				if (inputStream != null)
					inputStream.close();
				if (cis != null)
					cis.close();
				if (out != null)
					out.close();
			}catch (Exception e) {
				Logger.logError(MODULE, "Error while applying upgrading full version -- decryptSymMsg, Reason : " + e.getMessage());
				Logger.logTrace(MODULE, e);
			}
		}
	}
	
	public String convertLicenseDetailsToString(Map<String,String> licenseMap) {
	    StringBuilder str = new StringBuilder();
		str.append(CUSTOMER_NAME).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(CUSTOMER_NAME)).append("\n");
		str.append(LOCATION).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(LOCATION)).append("\n");
		str.append(MACID).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(MACID)).append("\n");
		str.append(PRODUCT).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(PRODUCT)).append("\n");
		str.append(VERSION).append(LICENSE_SPLIT_TOKEN).append(FULL).append("\n");
		str.append(START_DATE).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(START_DATE)).append("\n");
		str.append(END_DATE).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(END_DATE)).append("\n");
		str.append(HOSTNAME).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(HOSTNAME)).append("\n");
		str.append(DAILY_RECORDS).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(DAILY_RECORDS)).append("\n");
		str.append(MONTHLY_RECORDS).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(MONTHLY_RECORDS)).append("\n");
		str.append(TPS).append(LICENSE_SPLIT_TOKEN).append(licenseMap.get(TPS));
		return str.toString();
	}
	
	public Map<String, String> getLicenseFullDetail(String repositoryPath) throws Exception {
		Map<String, String> licenseInfoMap = new HashMap<String, String>();
		File licenseKey = new File (repositoryPath + LicenseUtility.LICENSE_STR);
		try{
			if(licenseKey.exists()) {
				String decryptedData = decrypt(repositoryPath + LicenseUtility.LICENSE_STR, repositoryPath);
				licenseInfoMap = getData(decryptedData);
			}
		}catch(Exception e){
			throw new Exception(e);
		}
		return licenseInfoMap;
	}
	
	
	public boolean validateServerId(String serverId) {
		boolean result = Boolean.FALSE;
		try {
			String macId = "";
			String host = "";
			List<String> ipList = getIpList();
			if(!ipList.isEmpty()) {
				String ip = ipList.get(0);
				macId = getMacFromIP(ip).toUpperCase();
				host= getHostName(ip);
			}
			String thisServerId = generateServerId(macId, host);
			Logger.logInfo(MODULE, "This Servers Server Id : " + thisServerId);
			Logger.logInfo(MODULE, "License File Server Id : " + serverId);
			
			if (thisServerId.equalsIgnoreCase(serverId))
				result = Boolean.TRUE;
			else
				result = Boolean.FALSE;
			
		} catch (Exception e) {
			Logger.logError(MODULE, "Error occured while validating license server id, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
		}
		return result;
	}
}
