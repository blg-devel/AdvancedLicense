package me.leoko.advancedlicense;

import java.util.UUID;

public class ServerCom {

	private final String randomBin;
	private final String securityKeyBin;
	private final String v1;

	public ServerCom(final String securityKey) {
		this.securityKeyBin = toBinary(securityKey);
		this.randomBin = toBinary(UUID.randomUUID().toString());
		this.v1 = genV1();
	}


	public String encrypt(String text) {
		return encryptBin(toBinary(text));
	}

	public String encryptBin(String textBin) {
		return xor(randomBin, textBin);
	}

	public String decrypt(String encryptedBin) {
		return fromBinary(xor(randomBin, xor(securityKeyBin, encryptedBin)));
	}

	public boolean validate(String encryptedBin, String expectedBin) {
		return randomBin.startsWith(xor(xor(encryptedBin, expectedBin), securityKeyBin));
	}

	public String getV1() {
		return v1;
	}

	private String genV1() {
		return xor(randomBin, securityKeyBin);
	}

	private String xor(String s1, String s2) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < (Math.min(s1.length(), s2.length())); i++)
			result.append(Byte.parseByte("" + s1.charAt(i)) ^ Byte.parseByte(s2.charAt(i) + ""));
		return result.toString();
	}

	public String toBinary(String s) {
		byte[] bytes = s.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int val = b;
			for (int i = 0; i < 8; i++) {
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
		}
		return binary.toString();
	}

	public String fromBinary(String s) {
		String str = "";
		int a = 0;
		while (a < s.length()) {
			str += (char) Integer.parseInt(s.substring(a, a + 8), 2);
			a = a + 8;
		}
		return str;
	}

}
