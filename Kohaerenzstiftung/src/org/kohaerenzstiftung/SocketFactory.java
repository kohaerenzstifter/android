package org.kohaerenzstiftung;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class SocketFactory implements org.apache.http.conn.scheme.SocketFactory, LayeredSocketFactory {
	private SSLContext mSslContext = null;
	private TrustChecker mTrustChecker = null;

	public void setmTrustChecker(TrustChecker mTrustChecker) {
		this.mTrustChecker = mTrustChecker;
	}

	SocketFactory() throws Throwable {
		mSslContext = SSLContext.getInstance("TLS");
		TrustManager trustManager = new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				mTrustChecker.checkServerTrusted(chain, authType);
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
		};

		mSslContext.init(null, new TrustManager[]{ trustManager }, new SecureRandom());
	}

	@Override
	public Socket connectSocket(Socket socket, String host, int port,
			InetAddress localAddress, int localPort, HttpParams params)
					throws IOException, UnknownHostException, ConnectTimeoutException {

		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);

		SocketAddress socketAddress = new InetSocketAddress(localAddress, localPort);
		socket.bind(socketAddress);
		socket.connect(new InetSocketAddress(host, port), connTimeout);
		socket.setSoTimeout(soTimeout);

		return socket;
	}

	@Override
	public Socket createSocket() throws IOException {
		return mSslContext.getSocketFactory().createSocket();
	}

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return true;
	}



	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {

		return mSslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

}