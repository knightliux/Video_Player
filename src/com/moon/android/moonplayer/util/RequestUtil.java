package com.moon.android.moonplayer.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

public class RequestUtil {
	private static final int REQUEST_TIMEOUT = 20000;
	private static final int SO_TIMEOUT = 20000;
	private static HttpClient client;
	private static RequestUtil util;

	private RequestUtil() {
		BasicHttpParams localBasicHttpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(localBasicHttpParams, 20000);
		HttpConnectionParams.setSoTimeout(localBasicHttpParams, 20000);
		SchemeRegistry localSchemeRegistry = new SchemeRegistry();
		localSchemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		localSchemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(
				localBasicHttpParams, localSchemeRegistry),
				localBasicHttpParams);
	}

	private HttpClient getHttpClient() {
		return client;
	}

	public static RequestUtil getInstance() {
		if (util == null)
			util = new RequestUtil();
		return util;
	}

	public String request(String paramString) {
		HttpGet httpGet = new HttpGet(paramString);
		httpGet.setHeader("User-Agent", "cwhttp/v1.0");
		String str = null;
		try {
			str = EntityUtils.toString(getHttpClient().execute(httpGet)
					.getEntity(), "UTF-8");
		} catch (Exception localException) {
			return "";
		}
		return str;
	}
}
