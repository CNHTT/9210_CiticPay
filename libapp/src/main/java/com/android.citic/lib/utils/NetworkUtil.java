package com.android.citic.lib.utils;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;


/**
 * 网络助手类
 */
public class NetworkUtil {

	private Socket socket;//SSLSocket对象
	private InputStream is; // 输入流
	private OutputStream os; // 输出流
	private String ip;//连接IP地址
	private int port;//连接端口号
	private Context tcontext ;//上下文对象
	private int timeout ; //超时时间
	private int protocol; // 协议 0: 2字节长度+数据 1:stx协议
	private final String CLIENT_KEY_MANAGER = "X509"; // 密钥管理器
	private final String CLIENT_AGREEMENT = "TLSv1.2"; // 使用协议
	private final String CLIENT_KEY_KEYSTORE = "BKS"; // "JKS";//密库，这里用的是BouncyCastle密库
	private final String CLIENT_KEY_PASSWORD = "123456";// 密码

	/**
	 * @param ip 初始化连接的IP
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public NetworkUtil(String ip, int port, int timeout, Context context) {
		this.ip = ip;
		this.port = port;
		this.timeout = timeout;
		this.tcontext = context;
	}

	/**
	 * 连接socket
	 * @return
	 * @throws IOException
	 */
	public int Connect() {
		try {
			socket = new Socket();
			socket.setSoTimeout(timeout);
			socket.connect(new InetSocketAddress(ip, port), 20000);
			is = socket.getInputStream();
			os = socket.getOutputStream();
//				SSLFactory sslFactory  = new SSLFactory(tcontext);;
//				socket = (SSLSocket) sslFactory.createSocket();
//				// socket = (SSLSocket) sslFactory.createSocket(ip, port);
//				socket.setSoTimeout(timeout);
//				socket.connect(new InetSocketAddress(ip, port), 20000);
//				is = socket.getInputStream();
//				os = socket.getOutputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 * 关闭socket
	 */
	public int close() {
		try {
			socket.close();
		} catch (IOException e) {
			return -1;
		}
		return 0;
	}

	/**
	 * 发送数据包
	 * @param data
	 * @return
	 */
	public int Send(byte[] data) {
		byte[] newData = null;
		if (protocol == 0) {
			newData = new byte[data.length + 2];
			newData[0] = (byte) (data.length >> 8);
			newData[1] = (byte) data.length;// 丢失高位
			System.arraycopy(data, 0, newData, 2, data.length);
		}
		try {
			os.write(newData);
			os.flush();
		} catch (IOException e) {
			return -1;
		}
		return 0;
	}

	/**
	 * 接受数据包
	 * @return
	 * @throws IOException
	 */
	public byte[] Recive(int max, int timeout) throws IOException {
		ByteArrayOutputStream byteOs ;
		byte[] resP = null ;
		if(timeout < 5*1000 || timeout > 2*60*1000){
			timeout = 10*1000 ;
		}
		if (protocol == 0) {
			byte[] packLen = new byte[2];
			int len ;
			byte[] bb = new byte[2+max];
			int i ;
			byteOs = new ByteArrayOutputStream();
			try {
				if ((i = is.read(packLen)) != -1) {
					len = ISOUtil.byte2int(packLen);
					while (len > 0 && (i = is.read(bb)) != -1) {
						byteOs.write(bb, 0, i);
						len -= i;
					}
				}
			} catch (InterruptedIOException e) {
				// 读取超时处理
				Log.w("PAY_SDK" , "Recive：读取流数据超时异常");
				return null;
			}
			resP = byteOs.toByteArray();
		}
		return resP;
	}

	public SSLSocket getSSLSocket() throws KeyManagementException,NoSuchAlgorithmException,
			KeyStoreException, CertificateException,IOException, UnrecoverableKeyException {
		SSLContext ctx = SSLContext.getInstance(CLIENT_AGREEMENT);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(CLIENT_KEY_MANAGER);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(CLIENT_KEY_MANAGER);
		KeyStore ks = KeyStore.getInstance(CLIENT_KEY_KEYSTORE);
		KeyStore tks = KeyStore.getInstance(CLIENT_KEY_KEYSTORE);
		ks.load(tcontext.getAssets().open("client.bks"), CLIENT_KEY_PASSWORD.toCharArray());
		tks.load(tcontext.getAssets().open("root.bks"), CLIENT_KEY_PASSWORD.toCharArray());
		kmf.init(ks, CLIENT_KEY_PASSWORD.toCharArray());
		tmf.init(tks);
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return (SSLSocket) ctx.getSocketFactory().createSocket(ip, port);
	}
}
