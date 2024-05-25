package com.elitecore.jmx.license;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.elitecore.util.commons.EnvironmentVarAndPathUtil;
import com.elitecore.util.logger.Logger;


public class LicenceManagement {

	private static final String MODULE = "LICENSE IMPL";
	
	public boolean applyDefaultFullVersion() throws Exception {
		boolean result = Boolean.FALSE;
		String repositoryPath;
		try {
			repositoryPath = EnvironmentVarAndPathUtil.getServerHomeLicensePath();
			File licenseKey = new File (repositoryPath + LicenseUtility.LICENSE_STR);
			if (!licenseKey.exists()) {
				StringBuilder message = new StringBuilder();
				LicenseUtility lu = new LicenseUtility();
				String encryptData = getStringWithDefaultLicenceValues();
				String res = lu.encryptLicenseDetail(encryptData,repositoryPath);
				Logger.logError(MODULE, "Default License creation status : " + res);
				message.append("License activated successfully\n");
				File trialKey = new File (repositoryPath + LicenseUtility.TRIAL_STR);
				if (trialKey.exists()) {
					trialKey.delete();
				}
			}
			result = Boolean.TRUE;
			return result;
		} catch (Exception e) {
			Logger.logError(MODULE, "Error while applying default full version, Reason : " + e.getMessage());
			Logger.logTrace(MODULE, e);
			throw new Exception(e.getMessage());
		}
	}
	
	public String getStringWithDefaultLicenceValues() {
		LicenseUtility lu = new LicenseUtility();
		
		String macId = "";
		String host = "";
		List<String> ipList = lu.getIpList();
		if(!ipList.isEmpty()) {
			String ip = ipList.get(0);
			macId = lu.getMacFromIP(ip).toUpperCase();
			host=lu.getHostName(ip);
		}
		String machineServerId = lu.generateServerId(macId, host);
		
		// Add years to Today's Date
		SimpleDateFormat sdf = new SimpleDateFormat(LicenseUtility.DATEFORMAT);
		Calendar date = Calendar.getInstance();
	    date.setTime(new Date());
	    date.add(Calendar.YEAR,LicenseUtility.DEFAULT_YEARS);
	    String endDate = sdf.format(date.getTime());
	    String startDate = sdf.format(new Date());	

	    StringBuilder str = new StringBuilder();
		str.append(LicenseUtility.CUSTOMER_NAME).append("=").append(LicenseUtility.DEFAULT_CUSTOMER_NAME).append("\n");
		str.append(LicenseUtility.LOCATION).append("=").append(LicenseUtility.DEFAULT_LOCATION).append("\n");
		str.append(LicenseUtility.MACID).append("=").append(machineServerId).append("\n");
		str.append(LicenseUtility.PRODUCT).append("=").append(LicenseUtility.DEFAULT_PRODUCT).append("\n");
		str.append(LicenseUtility.VERSION).append("=").append(LicenseUtility.FULL).append("\n");
		str.append(LicenseUtility.START_DATE).append("=").append(startDate).append("\n");
		str.append(LicenseUtility.END_DATE).append("=").append(endDate).append("\n");
		str.append(LicenseUtility.HOSTNAME).append("=").append(host).append("\n");
		str.append(LicenseUtility.DAILY_RECORDS).append("=").append(LicenseUtility.DEFAULT_DAILY_RECORDS).append("\n");
		str.append(LicenseUtility.MONTHLY_RECORDS).append("=").append(LicenseUtility.DEFAULT_MONTHLY_RECORDS).append("\n");
		str.append(LicenseUtility.TPS).append("=").append(LicenseUtility.TRIAL_TPS);
		return str.toString();
	}
	
	public String upgradeDefaultLicense(byte[] buffer) throws Exception {
		String result = "";
		boolean	isUpgraded = Boolean.FALSE;
		String repositoryPath = EnvironmentVarAndPathUtil.getServerHomeLicensePath();
		if(buffer!=null) {
			isUpgraded = new UpgradeDefaultLicense().upgradeDefault(repositoryPath, buffer);
		}
		if(isUpgraded) {
			result = "success";
		}
		return result;
	}
	
	public Map<String, String> getLicenseFullDetail() throws Exception {
		String repositoryPath = EnvironmentVarAndPathUtil.getServerHomeLicensePath();
		LicenseUtility lu = new LicenseUtility();
		return lu.getLicenseFullDetail(repositoryPath);
	}
	

}
