package com.pinplanet.pintact.utility;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.security.KeyStore;

/**
 * Created by pranab on 10/24/14.
 */
public class AppConnectionManager {

  private static ClientConnectionManager ccm;
  private static HttpParams httpParams;
  private static HttpClient httpClient;

  public static HttpClient getHttpClient()throws Exception{

    if(httpClient == null)
    {
      httpClient =  new DefaultHttpClient(getConnectionManager(), getHttpParams());
    }

    return httpClient;

  }

  public static  ClientConnectionManager getConnectionManager()throws Exception{
    if (ccm == null)
    {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);

      SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
      sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

      HttpParams params = new BasicHttpParams();
      HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

      SchemeRegistry registry = new SchemeRegistry();
      registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
      registry.register(new Scheme("https", sf, 443));

      ccm = new ThreadSafeClientConnManager(params, registry);

    }
    return ccm;
  }

  public static HttpParams getHttpParams(){
    if(httpParams == null)
    {
      httpParams  = new BasicHttpParams();
      HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
      HttpConnectionParams.setTcpNoDelay(httpParams, true);
    }
    return httpParams;
  }

}
