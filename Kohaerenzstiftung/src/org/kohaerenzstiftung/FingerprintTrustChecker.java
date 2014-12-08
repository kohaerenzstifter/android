package org.kohaerenzstiftung;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class FingerprintTrustChecker extends org.kohaerenzstiftung.TrustChecker {

	private ArrayList<String> mFingerprints = null;
	private String mFingerprint = null;
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new java.security.cert.CertificateException("NoSuchAlgorithmException");
		}
		byte[] der = chain[0].getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		String certFingerprint = hexify(digest);
		char[] array = certFingerprint.toCharArray();
		int i = 0;
		StringBuffer stringBuffer = new StringBuffer();
		for (char character : array) {
			if (i != 0) {
				if ((i % 2) == 0) {
					stringBuffer.append(' ');
				}
			}
			stringBuffer.append(Character.toUpperCase(character));
			i++;
		}
		certFingerprint = stringBuffer.toString();

		boolean found = false;
		for (String fingerprint : mFingerprints) {
			if (fingerprint.equals(certFingerprint)) {
				found = true;
				break;
			}
		}
		if (!found) {
			mFingerprint = certFingerprint;
			throw new CertificateException();
		}
		
	}

	public String getFingerprint() {
		String result = mFingerprint;
		mFingerprint = null;
		return result;
	}

	private static String hexify(byte[] digest) {
		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		StringBuffer buf = new StringBuffer(digest.length * 2);

		for (int i = 0; i < digest.length; ++i) {
			buf.append(hexDigits[(digest[i] & 0xf0) >> 4]);
			buf.append(hexDigits[digest[i] & 0x0f]);
		}

		return buf.toString();
	}

	public FingerprintTrustChecker(ArrayList<String> fingerprints) {
		super();
		mFingerprints = fingerprints;
	}
	
}