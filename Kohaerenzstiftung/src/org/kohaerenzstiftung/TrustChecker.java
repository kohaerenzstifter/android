package org.kohaerenzstiftung;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public abstract class TrustChecker {
	public abstract void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException;
}