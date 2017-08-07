package com.android.citic.lib.utils;

import android.content.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLFactory extends SSLSocketFactory {
	private static final String CLIENT_KEY_MANAGER = "X509"; // 密钥管理器
	private static final String CLIENT_AGREEMENT = "TLSv1.2"; // 使用协议
	private static final String CLIENT_KEY_KEYSTORE = "BKS"; // "JKS";//密库，这里用的是BouncyCastle密库
	private static final String CLIENT_KEY_PASSWORD = "123456";// 密码
	private SSLSocketFactory internalSSLSocketFactory;

	public SSLFactory(Context context) throws NoSuchAlgorithmException,
			KeyStoreException, CertificateException, IOException,
			UnrecoverableKeyException, KeyManagementException {
		SSLContext ctx = SSLContext.getInstance(CLIENT_AGREEMENT);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(CLIENT_KEY_MANAGER);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(CLIENT_KEY_MANAGER);

		KeyStore ks = KeyStore.getInstance(CLIENT_KEY_KEYSTORE);
		KeyStore tks = KeyStore.getInstance(CLIENT_KEY_KEYSTORE);

		ks.load(context.getAssets().open("client.bks"), CLIENT_KEY_PASSWORD.toCharArray());
		tks.load(context.getAssets().open("root.bks"), CLIENT_KEY_PASSWORD.toCharArray());

		kmf.init(ks, CLIENT_KEY_PASSWORD.toCharArray());
		tmf.init(tks);
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		internalSSLSocketFactory = ctx.getSocketFactory();
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return internalSSLSocketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return internalSSLSocketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
	}

	@Override
	public Socket createSocket(Socket s, String host, int port,
							   boolean autoClose) throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host,
				port, autoClose));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost,
							   int localPort) throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host,
				port, localHost, localPort));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host,
				port));
	}

	@Override
	public Socket createSocket(InetAddress address, int port,
							   InetAddress localAddress, int localPort) throws IOException {
		return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address,
				port, localAddress, localPort));
	}

	private Socket enableTLSOnSocket(Socket socket) throws SocketException {
		if (socket != null && (socket instanceof SSLSocket)) {
			((SSLSocket) socket).setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_AES_128_CBC_SHA256" });
			((SSLSocket) socket).setEnabledProtocols(new String[] { "TLSv1.2" });
		}
		return socket;
	}
}
