package com.elitecore.jmx.license;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.elitecore.util.logger.Logger;

public class UpgradeDefaultLicense {

	private static final String MODULE = "UPGRADE_DEFAULT_VERSION";

	public boolean upgradeDefault(String repositoryPath,byte [] buffer) {
		boolean result = Boolean.FALSE;
		StringBuilder message = new StringBuilder();
		LicenseUtility lu = new LicenseUtility();
		File tempLicenseFile = new File(repositoryPath + System.nanoTime() +LicenseUtility.TEMP_LICENSE_STR);
		File trialFile = new File(repositoryPath + LicenseUtility.TRIAL_STR);
		File licenseFile = new File(repositoryPath + LicenseUtility.LICENSE_STR);
		lu.writeBytesToFile(tempLicenseFile, buffer);
		try {
			if(!tempLicenseFile.canRead()) {
				tempLicenseFile.setReadable(true);
			}

			// Uploaded License key to Map
			String tempLicenseData = lu.decrypt(tempLicenseFile.getPath(),repositoryPath);
			Map<String,String> tempLicenseHashMap= lu.getData(tempLicenseData);
			if(tempLicenseHashMap == null || tempLicenseHashMap.size()==0) {
				message.append("Problem while reading uploaded license data, Please contact system team\n");
				Logger.logError(MODULE, "Problem while reading uploaded license data, Please contact system team");
				result = Boolean.FALSE;
				tempLicenseFile.delete();
				return result;
			}
						
			// Validate the start date and end date for uploaded license
			result = lu.validateLicDate(tempLicenseHashMap.get(LicenseUtility.START_DATE).trim(), tempLicenseHashMap.get(LicenseUtility.END_DATE).trim());
			if(result==Boolean.FALSE) {
				message.append("License Key upgradation Failed, Date is Expired ==> Please contact System Team...\n");
				Logger.logError(MODULE, "License Key upgradation Failed, Date is Expired ==> Please contact System Team...");
				tempLicenseFile.delete();
				return result;
			}
			
			// Validate Physical Machine Server Id and License Key server Id
			result = lu.validateServerId(tempLicenseHashMap.get(LicenseUtility.MACID).trim());
			if(result==Boolean.FALSE) {
				message.append("License Key upgradation Failed, Server Id not matched ==> Please contact System Team...\n");
				Logger.logError(MODULE, "License Key upgradation Failed, Server Id not matched ==> Please contact System Team...");
				tempLicenseFile.delete();
				return result;
			}
			
			if(trialFile.exists() || !(licenseFile.exists())) {
				FileUtils.moveFile(tempLicenseFile, licenseFile);
			} else if (licenseFile.exists()) {
				// Existing License key
				String licenseData = lu.decrypt(licenseFile.getPath(),repositoryPath);
				Map<String,String> licenseHashMap= lu.getData(licenseData);
				if(licenseHashMap == null || licenseHashMap.size()==0) {
					message.append("Problem while reading existing license data, Please contact system team\n");
					Logger.logError(MODULE, "Problem while reading existing license data, Please contact system team");
					result = Boolean.FALSE;
					return result;
				}

				if (compareLicenseDetails(tempLicenseHashMap,licenseHashMap)) {
					String encString = lu.convertLicenseDetailsToString(tempLicenseHashMap);
					String isLicenseCreated = lu.encryptLicenseDetail(encString,repositoryPath);
					if (isLicenseCreated == null || isLicenseCreated.equals("false")) {
						message.append("License Key upgradation Failed. License File is corrupted. ==> Please contact System Team...\n");
						Logger.logError(MODULE, "License Key upgradation Failed. License File is corrupted. ==> Please contact System Team...");
						tempLicenseFile.delete();
						result = Boolean.FALSE;
						return result;
					}
				}
				else {
					message.append("License Key upgradation Failed, MAC Id Validation Failed ==> Please contact System Team...\n");
					Logger.logError(MODULE, "License Key upgradation Failed, MAC Id Validation Failed ==> Please contact System Team...");
					tempLicenseFile.delete();
					result = Boolean.FALSE;
					return result;
				}
			}
		}
		catch (Exception e) {
			message.append("License upgradation Failed, License File is corrupted, please contact system team...\n");
			Logger.logError(MODULE, "Error occured while reading data from license file, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			if(tempLicenseFile.exists())
				tempLicenseFile.delete();
			result = Boolean.FALSE;
			return result;
		}finally {
			File file = new File(repositoryPath+LicenseUtility.TEMP_FILE_LICENSE);
			if(file.exists())
				file.delete();
			
			if(tempLicenseFile.exists())
				tempLicenseFile.delete();
		}
		if(trialFile.exists()) {
			trialFile.delete();
		}
		message.append("License upgraded successfully\n");
		Logger.logInfo(MODULE, "License upgraded successfully");
		result = Boolean.TRUE;
		return result;
	}
	
	public boolean compareLicenseDetails(Map<String,String> newLicenseHashMap , Map<String,String> licenseHashMap) throws Exception {
		// Min of both start dates
		String startDate = getMinStartDate(newLicenseHashMap.get(LicenseUtility.START_DATE),licenseHashMap.get(LicenseUtility.START_DATE));
		// Max of both end dates
		String endDate = getMaxEndDate(newLicenseHashMap.get(LicenseUtility.END_DATE), licenseHashMap.get(LicenseUtility.END_DATE));
		newLicenseHashMap.put(LicenseUtility.START_DATE, startDate);
		newLicenseHashMap.put(LicenseUtility.END_DATE, endDate);
		
		String newTPS = newLicenseHashMap.get(LicenseUtility.TPS);
		String oldTPS = licenseHashMap.get(LicenseUtility.TPS);
		String finalTPS = getUpdatedTPS(newTPS, oldTPS);
		newLicenseHashMap.put(LicenseUtility.TPS, finalTPS);
		return true;
	}
	
	public String getMinStartDate(String newLicenseDate, String existingLiceneseDate) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(LicenseUtility.DATEFORMAT);
		String returnDate = existingLiceneseDate;
		try {
			Date newDate = sdf.parse(newLicenseDate);
			Date oldDate = sdf.parse(existingLiceneseDate);
			
			if (newDate.before(oldDate))
				returnDate = newLicenseDate;
			else
				returnDate =  existingLiceneseDate;
		} catch (ParseException e) {
			Logger.logError(MODULE, "Error occured while validating license start dates, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		}
		return returnDate;
	}
	
	public String getMaxEndDate(String newLicenseDate, String existingLiceneseDate) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(LicenseUtility.DATEFORMAT);
		String returnDate = existingLiceneseDate;
		try {
			Date newDate = sdf.parse(newLicenseDate);
			Date oldDate = sdf.parse(existingLiceneseDate);
			
			if (newDate.after(oldDate))
				returnDate = newLicenseDate;
			else
				returnDate =  existingLiceneseDate;
		} catch (ParseException e) {
			Logger.logError(MODULE, "Error occured while validating license end dates, reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		}
		return returnDate;
	}
	
	public String getUpdatedTPS(String newTPS, String oldTPS) {

		StringBuilder b = new StringBuilder();
		String[] newTpsArray = newTPS.split(",");
		Map<String, String> newMap = new HashMap<String,String>();
		for(String tps : newTpsArray) {
			newMap.put(tps.split(":")[0], tps.split(":")[1]);
		}
		
		if (oldTPS != null && !(oldTPS.equals("")) && !(oldTPS.equals("0"))) {
			
			String[] oldTpsArray = oldTPS.split(",");
			Map<String, String> oldMap = new HashMap<String,String>();
			for(String tps : oldTpsArray) {
				oldMap.put(tps.split(":")[0], tps.split(":")[1]);
			}
			
			if (oldMap != null && !oldMap.isEmpty()) {
				for (String key : oldMap.keySet()) {
					if (!newMap.containsKey(key)) {
						newMap.put(key, oldMap.get(key));
					}
				}
			}
			
			b.append(oldTPS);
			if (newMap != null && !newMap.isEmpty()) {
				b.setLength(0);
				for (Entry<String, String> entry : newMap.entrySet()) {
					if (b.length() == 0)
						b.append(entry.getKey()).append(":").append(entry.getValue());
					else
						b.append(",").append(entry.getKey()).append(":").append(entry.getValue());
				}
			}
		}
		else {
			b.append(newTPS);
		}
		
		return b.toString();
	}
}
