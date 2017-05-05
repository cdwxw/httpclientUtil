package com.httpclient.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;

import com.httpclient.exception.HttpProcessException;

/**
 * 设置ssl
 * 
 * @author wxw
 * @date 2015年11月3日 下午3:11:54
 * @version 1.0
 */
public class SSLs {

    private static final SSLHandler simpleVerifier = new SSLHandler();
	private static SSLSocketFactory sslFactory ;
	private static SSLConnectionSocketFactory sslConnFactory ;
	private static SSLIOSessionStrategy sslIOSessionStrategy ;
	private static SSLs sslutil = new SSLs();
	private SSLContext sc;
	
	public static SSLs getInstance(){
		return sslutil;
	}
	public static SSLs custom(){
		return new SSLs();
	}

    /**
     * 內部类	重写X509TrustManager类的三个方法,信任服务器证书
     */
    private static class SSLHandler implements  X509TrustManager, HostnameVerifier{

		/**
		 * 该方法检查客户端的证书，若不信任该证书则抛出异常。由于我们不需要对客户端进行认证，因此我们只需要执行默认的信任管理器的这个方法。
		 * JSSE中，默认的信任管理器类为TrustManager。
		 */
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) 
				throws java.security.cert.CertificateException {
		}

		/**
		 * 该方法检查服务器的证书，若不信任该证书同样抛出异常。通过自己实现该方法，可以使之信任我们指定的任何证书。
		 * 在实现该方法时，也可以简单的不做任何处理，即一个空的函数体，由于不会抛出异常，它就会信任任何证书。
		 */
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) 
				throws java.security.cert.CertificateException {
		}

		/**
		 * 返回受信任的X509证书数组。
		 */
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[]{};
			//return null;
		}

		@Override
		public boolean verify(String paramString, SSLSession paramSSLSession) {
			return true;
		}
	};
    
	/**
	 * 信任主机
	 */
    public static HostnameVerifier getVerifier() {
        return simpleVerifier;
    }
    
	/**
	 * 获取SSLSocketFactory对象
	 */
    public synchronized SSLSocketFactory getSSLSF(SSLProtocolVersion sslpv) throws HttpProcessException {
        if (sslFactory != null)
            return sslFactory;
		try {
			SSLContext sc = getSSLContext(sslpv);
			sc.init(null, new TrustManager[] { simpleVerifier }, null);
			sslFactory = sc.getSocketFactory();
		} catch (KeyManagementException e) {
			throw new HttpProcessException(e);
		}
        return sslFactory;
    }
    
    public synchronized SSLConnectionSocketFactory getSSLCONNSF(SSLProtocolVersion sslpv) throws HttpProcessException {
    	if (sslConnFactory != null)
    		return sslConnFactory;
    	try {
    		//创建SSLContext对象，并使用我们指定的信任管理器初始化
	    	SSLContext sc = getSSLContext(sslpv);
	    	sc.init(null, new TrustManager[] { simpleVerifier }, new java.security.SecureRandom());
//	    	sc.init(null, new TrustManager[] { simpleVerifier }, null);
	    	
	    	//从上述SSLContext对象中得到SSLConnectionSocketFactory对象
	    	sslConnFactory = new SSLConnectionSocketFactory(sc, simpleVerifier);
		} catch (KeyManagementException e) {
			throw new HttpProcessException(e);
		}
    	return sslConnFactory;
    }
    
    public synchronized SSLIOSessionStrategy getSSLIOSS(SSLProtocolVersion sslpv) throws HttpProcessException {
    	if (sslIOSessionStrategy != null)
    		return sslIOSessionStrategy;
		try {
			SSLContext sc = getSSLContext(sslpv);
//			sc.init(null, new TrustManager[] { simpleVerifier }, null);
	    	sc.init(null, new TrustManager[] { simpleVerifier }, new java.security.SecureRandom());
			sslIOSessionStrategy = new SSLIOSessionStrategy(sc, simpleVerifier);
		} catch (KeyManagementException e) {
			throw new HttpProcessException(e);
		}
    	return sslIOSessionStrategy;
    }
    
    public SSLs customSSL(String keyStorePath, String keyStorepass) throws HttpProcessException{
    	FileInputStream instream =null;
    	KeyStore trustStore = null; 
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			instream = new FileInputStream(new File(keyStorePath));
			trustStore.load(instream, keyStorepass.toCharArray());
			// 相信自己的CA和所有自签名的证书
			sc = SSLContexts.custom()
					.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
					.build();
		} catch (KeyManagementException e) {
			throw new HttpProcessException(e);
		} catch (KeyStoreException e) {
			throw new HttpProcessException(e);
		} catch (FileNotFoundException e) {
			throw new HttpProcessException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new HttpProcessException(e);
		} catch (CertificateException e) {
			throw new HttpProcessException(e);
		} catch (IOException e) {
			throw new HttpProcessException(e);
		}finally{
			try {
				instream.close();
			} catch (IOException e) {}
		}
		return this;
    }
    
    public SSLContext getSSLContext(SSLProtocolVersion sslpv) throws HttpProcessException{
    	try {
    		if(sc==null){
    			sc = SSLContext.getInstance(sslpv.getName());
    		}
    		return sc;
    	} catch (NoSuchAlgorithmException e) {
    		throw new HttpProcessException(e);
    	}
    }
    
    /**
     * The SSL protocol version (SSLv3, TLSv1, TLSv1.1, TLSv1.2)
     * 
     * @author wxw
     * @date 2016年11月18日 上午9:35:37 
     * @version 1.0
     */
    public static enum SSLProtocolVersion{
    	SSL("SSL"),
    	SSLv3("SSLv3"),
    	TLSv1("TLSv1"),
    	TLSv1_1("TLSv1.1"),
    	TLSv1_2("TLSv1.2"),
    	;
    	private String name;
    	private SSLProtocolVersion(String name){
    		this.name = name;
    	}
    	public String getName(){
    		return this.name;
    	}
    	public static SSLProtocolVersion find(String name){
    		for (SSLProtocolVersion pv : SSLProtocolVersion.values()) {
				if(pv.getName().toUpperCase().equals(name.toUpperCase())){
					return pv;
				}
			}
    		throw new RuntimeException("未支持当前ssl版本号："+name);
    	}
    }
}