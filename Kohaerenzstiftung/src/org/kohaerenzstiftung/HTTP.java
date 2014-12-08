package org.kohaerenzstiftung;


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
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
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
	private static SocketFactory socketFactory;

	public static HttpResponse doHttps(String server, int port, String url,
			String username, String password, List<BasicNameValuePair> headers,
			List<BasicNameValuePair> parameters, TrustChecker trustChecker,
			AbstractHttpEntity entity, int method) throws Throwable {

		HttpResponse result = null;
		Throwable throwable = null;

		try {
			DefaultHttpClient httpClient = getHttpClient(port, trustChecker);

			Builder uriBuilder =
					Uri.parse("https://" + server + ":" + port + "/" + url).buildUpon();

			result = doHttp(httpClient, username, password,
					uriBuilder, headers, parameters, entity, method);
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
			String username, String password, List<BasicNameValuePair> headers,
			List<BasicNameValuePair> parameters, AbstractHttpEntity entity,
			int method) throws Throwable {

		HttpResponse result = null;
		Throwable throwable = null;

		try {
			DefaultHttpClient httpClient = getHttpClient(port, null);

			Builder uriBuilder =
					Uri.parse("http://" + server + ":" + port + "/" + url).buildUpon();

			result =
					doHttp(httpClient, username, password, uriBuilder, headers, parameters, entity, method);
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
			Builder uriBuilder, List<BasicNameValuePair> headers,
			List<BasicNameValuePair> parameters, AbstractHttpEntity entity, int method) throws Throwable {
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
				HttpGet httpGet = new HttpGet(uri);
				httpUriRequest = httpGet;
				break;
			case HTTP_PUT:
				HttpPut httpPut = new HttpPut(uri);
				if (entity != null) {
					httpPut.setEntity(entity);
				}
				httpUriRequest = httpPut;
				break;
			case HTTP_POST:
				HttpPost httpPost = new HttpPost(uri);
				if (entity != null) {
					httpPost.setEntity(entity);
				}
				httpUriRequest = httpPost;
				break;
			case HTTP_DEL:
				HttpDelete httpDelete = new HttpDelete(uri);
				httpUriRequest = httpDelete;
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

			if (headers != null) {
				for (NameValuePair header : headers) {
					BasicHeader basicHeader =
							new BasicHeader(header.getName(), header.getValue());
					httpUriRequest.addHeader(basicHeader);
				}
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


	private static DefaultHttpClient getHttpClient(int port, TrustChecker trustChecker) throws Throwable  {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
		DefaultHttpClient result = new DefaultHttpClient(httpParameters);

		if (trustChecker != null) {
			if (socketFactory == null) {
				socketFactory = new SocketFactory();
			}
			socketFactory.setmTrustChecker(trustChecker);

			Scheme scheme = new Scheme("https", socketFactory, port);

			result.getConnectionManager().getSchemeRegistry().register(scheme);
		}

		return result;		
	}

	/*public static HttpResponse doHttps(String server, int port, String url,
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
	}*/

	/*private static DefaultHttpClient getHttpClient(
			InputStream instream, String keyStorePw, int port)
			throws Throwable {

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
	}*/
}
