package org.kohaerenzstiftung;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.Uri;
import android.net.Uri.Builder;

public class HTTP {

	public static final int HTTP_GET = 0;
	public static final int HTTP_PUT = 1;
	public static final int HTTP_POST = 2;
	public static final int HTTP_DEL = 3;

	public static HttpResponse doHttps(String server, int port, String url,
			String username, String password,
			List<BasicNameValuePair> parameters, InputStream instream, String keyStorePw,
			int method) throws Throwable {

		HttpResponse result = null;
		Throwable throwable = null;

		try {
			DefaultHttpClient httpClient = getHttpClient(instream, keyStorePw, port);

			Builder uriBuilder =
					Uri.parse("https://" + server + ":" + port + "/" + url).buildUpon();

			result = doHttp(httpClient, username, password,
					uriBuilder, parameters, method);
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	public static HttpResponse doHttp(String server, int port, String url,
			String username, String password,
			List<BasicNameValuePair> parameters, int method) throws Throwable {

		HttpResponse result = null;
		Throwable throwable = null;

		try {
			DefaultHttpClient httpClient = getHttpClient(null, null, port);

			Builder uriBuilder =
					Uri.parse("http://" + server + ":" + port + "/" + url).buildUpon();

			result =
					doHttp(httpClient, username, password, uriBuilder, parameters, method);
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	private static HttpResponse doHttp(DefaultHttpClient httpClient,
			String username, String password,
			Builder uriBuilder, List<BasicNameValuePair> parameters, int method) throws Throwable {
		HttpResponse result = null;
		Throwable throwable = null;
		try {
			if (parameters != null) {
				for (NameValuePair parameter : parameters) {
					uriBuilder.appendQueryParameter(parameter.getName(), parameter.getValue());
				}
			}

			String uri = uriBuilder.build().toString();

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

			if ((username != null)&&(password != null)) {
				UsernamePasswordCredentials usernamePasswordCredentials =
						new UsernamePasswordCredentials(username, password);
			    BasicScheme basicScheme = new BasicScheme();
			    Header authorizationHeader =
			    		basicScheme.authenticate(usernamePasswordCredentials, httpUriRequest);
			    httpUriRequest.addHeader(authorizationHeader);
			}

			result = httpClient.execute(httpUriRequest);
		} catch (Throwable t) {
			throwable = t;
		} finally {
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;
	}

	private static DefaultHttpClient getHttpClient(
			InputStream instream, String keyStorePw, int port)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
			KeyManagementException, UnrecoverableKeyException {

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
		DefaultHttpClient result = new DefaultHttpClient(httpParameters);

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
