package me.leoko.advancedlicense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdvancedLicense {

	private final String licenseKey;
	private final String validationServer;
	private LogType logType = LogType.NORMAL;
	private String securityKey = "YecoF0I6M05thxLeokoHuW8iUhTdIUInjkfF";
	private final LicenseActions licenseActions;
	private boolean debug = false;

	public AdvancedLicense(String licenseKey, String validationServer, LicenseActions licenseActions) {
		this.licenseKey = licenseKey;
		this.validationServer = validationServer;
		this.licenseActions = licenseActions;
	}

	public AdvancedLicense setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
		return this;
	}

	public AdvancedLicense setConsoleLog(LogType logType) {
		this.logType = logType;
		return this;
	}

	public AdvancedLicense debug() {
		debug = true;
		return this;
	}

	public String getToken(final String userIp) {
		final ServerCom serverCom = new ServerCom(securityKey);
		final String licenseKeyBin = serverCom.toBinary(licenseKey);


		final String v1 = serverCom.getV1();
		final String v2 = serverCom.encryptBin(licenseKeyBin);
		final String v3 = serverCom.encrypt(userIp);

		String response;
		try {
			response = requestServer(v1, v2, v3);
		} catch (IOException ex) {
			if (debug)
				ex.printStackTrace();
			return null;
		}

		if (response.startsWith("<")) {
			log(1, "The License-Server returned an invalid response!");
			log(1, "In most cases this is caused by:");
			log(1, "1) Your Web-Host injects JS into the page (often caused by free hosts)");
			log(1, "2) Your ValidationServer-URL is wrong");
			log(1, "SERVER-RESPONSE: " +
					(response.length() < 150 || debug ? response : response.substring(0, 150) + "..."));
			return null;
		}

		try {
			ValidationType.valueOf(response);
			return null;
		} catch (IllegalArgumentException ex) {
			final String decrypted = serverCom.decrypt(response);
			if (decrypted != null && !decrypted.startsWith(licenseKey) && decrypted.startsWith("TOKEN")) {
				return decrypted.substring(5);
			}
			return null;
		}
	}

	public boolean register() {
		log(0, "[]==========[License-System]==========[]");
		log(0, "Connecting to License-Server...");
		ValidationType vt = isValid();
		if (vt == ValidationType.VALID) {
			log(1, "License valid!");
			log(0, "[]==========[License-System]==========[]");
			return true;
		} else {
			log(1, "License is NOT valid!");
			log(1, "Failed as a result of " + vt.toString());
			log(1, "Disabling plugin!");
			log(0, "[]==========[License-System]==========[]");

			licenseActions.doLicenseInvalidAction();
			return false;
		}
	}

	public boolean isValidSimple() {
		return (isValid() == ValidationType.VALID);
	}

	private String requestServer(String v1, String v2, String v3) throws IOException {
		URL url = new URL(validationServer + "?v1=" + v1 + "&v2=" + v2 +
				"&pl=" + licenseActions.getProductName() + (v3 != null ? "&v3=" + v3 : ""));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();
		if (debug) {
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
		}

		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			return response.toString();
		}
	}

	public ValidationType isValid() {
		final ServerCom serverCom = new ServerCom(securityKey);
		final String licenseKeyBin = serverCom.toBinary(licenseKey);

		try {
			String response = requestServer(serverCom.getV1(), serverCom.encryptBin(licenseKeyBin), null);

			if (response.startsWith("<")) {
				log(1, "The License-Server returned an invalid response!");
				log(1, "In most cases this is caused by:");
				log(1, "1) Your Web-Host injects JS into the page (often caused by free hosts)");
				log(1, "2) Your ValidationServer-URL is wrong");
				log(1,
						"SERVER-RESPONSE: " + (response.length() < 150 || debug ? response : response.substring(0, 150) + "..."));
				return ValidationType.PAGE_ERROR;
			}

			try {
				return ValidationType.valueOf(response);
			} catch (IllegalArgumentException exc) {
				if (serverCom.validate(response, licenseKeyBin)) return ValidationType.VALID;
				return ValidationType.WRONG_RESPONSE;
			}
		} catch (IOException e) {
			if (debug)
				e.printStackTrace();
			return ValidationType.PAGE_ERROR;
		}
	}

	//
	// Enums
	//

	public enum LogType {
		NORMAL, LOW, NONE;
	}

	public enum ValidationType {
		WRONG_RESPONSE, PAGE_ERROR, URL_ERROR, KEY_OUTDATED, KEY_NOT_FOUND, NOT_VALID_IP, INVALID_PLUGIN, VALID;
	}

	//
	// Console-Log
	//

	private void log(int type, String message) {
		if (logType == LogType.NONE || (logType == LogType.LOW && type == 0))
			return;
		System.out.println(message);
	}
}
