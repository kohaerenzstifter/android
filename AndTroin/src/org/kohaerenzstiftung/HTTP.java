package org.kohaerenzstiftung;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

public class HTTP {

	public static final int HTTP_GET = 0;
	public static final int HTTP_PUT = 1;
	public static final int HTTP_POST = 2;
	public static final int HTTP_DEL = 3;

	public static HttpResponse doHttps(String server, int port, String url,
			InputStream instream, String keyStorePw, int method) throws Throwable {

		HttpClient httpClient = getHttpClient(instream, keyStorePw, port);

		String uriString = "https://" + server + ":" + port + "/" + url;
		URI uri;
		try {
			uri = new java.net.URI(uriString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		HttpUriRequest httpUriRequest = null;
		switch (method) {
		case HTTP_GET:
			httpUriRequest = new HttpGet(uri);
			break;
		case HTTP_PUT:
			httpUriRequest = new HttpPut(uri);
			break;
		case HTTP_POST:
			httpUriRequest = new HttpPost(uri);
			break;
		case HTTP_DEL:
			httpUriRequest = new HttpDelete(uri);
			break;
		default:
			throw new Exception("unknown HTTP method: " + method);
		}

		HttpResponse response = null;
		try {
			response = httpClient.execute(httpUriRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	private static HttpClient getHttpClient(InputStream instream, String keyStorePw, int port) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
		DefaultHttpClient result = new DefaultHttpClient();
		
		if (instream != null) {
			KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());        
	        trustStore.load(instream, keyStorePw.toCharArray());

			SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
			Scheme scheme = new Scheme("https", socketFactory, port);
			
			result.getConnectionManager().getSchemeRegistry().register(scheme);
		}

		return result;
	}
}
