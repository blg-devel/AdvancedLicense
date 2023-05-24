package me.leoko.advancedlicense.userValidation;

import me.leoko.advancedlicense.LicenseActions;
import me.leoko.advancedlicense.ServerCom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserValidator {

	private final String token;
	private final String validationServer;
	private String securityKey = "rHnVh9RsoYLmU6P5SrvaBT43d5z9vPiLzFLc";
	private final LicenseActions licenseActions;

	public UserValidator(final String token, String validationServer, LicenseActions actions) {
		this.token = token;
		this.validationServer = validationServer;
		this.licenseActions = actions;
	}

	public UserValidator setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
		return this;
	}

	public boolean isValidSimple() {
		return isValid() == UserValidationType.VALID;
	}

	public UserValidationType isValid() {
		final ServerCom serverCom = new ServerCom(securityKey);

		final String v1 = serverCom.getV1();
		final String v2 = serverCom.encrypt(token);
		final String product = licenseActions.getProductName();

		String response;
		try {
			response = requestServer(v1, v2, product);
		} catch (IOException ex) {
			return UserValidationType.PAGE_ERROR;
		}

		if (response.startsWith("<")) {
			return UserValidationType.PAGE_ERROR;
		}

		try {
			return UserValidationType.valueOf(response);
		} catch (IllegalArgumentException ex) {
			if (serverCom.validate(response, token)) {
				return UserValidationType.VALID;
			}
			return UserValidationType.WRONG_RESPONSE;
		}

	}

	private String requestServer(String v1, String v2, String product) throws IOException {
		URL url = new URL(validationServer + "?v1=" + v1 + "&v2=" + v2 + "&pl=" + product);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			return response.toString();
		}
	}

	public enum UserValidationType {
		VALID,
		TOKEN_NOT_FOUND,
		TOKEN_EXPIRED,
		INVALID_IP,
		INVALID_PRODUCT,
		WRONG_RESPONSE,
		PAGE_ERROR,
		URL_ERROR
	}
}
