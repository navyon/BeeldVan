/*
 * Copyright © 2012 Proximus Mobility LLC
 */
package com.ngagemedia.beeldvan.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * 
 * @author Eric Johansson <ejohansson@proximusmobility.com>
 */
public class RESTClient implements ConnectionKeepAliveStrategy {

	public static final String TAG = RESTClient.class.getSimpleName();
	private long keepAlive;
	private DefaultHttpClient httpClient;
	public static final String UTF8 = "UTF-8";
	public static final long MAX_KEEP_ALIVE = 1000 * 15;
	
	
	

	public RESTClient(long keepAlive) {
		this.keepAlive = keepAlive;
		this.httpClient = new DefaultHttpClient();
		this.httpClient.setKeepAliveStrategy(this);
	}
	
	
	/*
	 * @see RESTClient.BuildUri
	 */
	@Deprecated
	public URI buildUri(String base, List<NameValuePair> params) throws URISyntaxException {
		if (params != null) {
			String encodedParams = URLEncodedUtils.format(params, UTF8);
			URI uri = new URI(base + "?" + encodedParams);
//			String call;
//			try {
//				call = URLDecoder.decode(uri.toString(), UTF8);
//				Log.w(getClass().getSimpleName(), call);
//			} catch (UnsupportedEncodingException e) {
//			}

			return uri;
		}
		return new URI(base);

	}
	
	public static URI BuildUri(String base, List<NameValuePair> params) throws URISyntaxException {
		if (params != null) {
			String encodedParams = URLEncodedUtils.format(params, UTF8);
			URI uri = new URI(base + "?" + encodedParams);
//			String call;
//			try {
//				call = URLDecoder.decode(uri.toString(), UTF8);
//				Log.w(getClass().getSimpleName(), call);
//			} catch (UnsupportedEncodingException e) {
//			}

			return uri;
		}
		return new URI(base);

	}

	public HttpResponse GETRequest(URI uri) throws IOException {
		HttpGet httpGet = new HttpGet(uri);
		HttpResponse response = null;
		httpGet.setHeader("Accept", "application/json;");
		
		response = httpClient.execute(httpGet);
		return response;
	}

	public HttpResponse POSTRequest(URI uri, HttpEntity contentEntity) throws IOException {
		HttpResponse response;
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(contentEntity);
		response = httpClient.execute(httpPost);
		return response;
	}

	public HttpResponse PUTRequest(URI uri, HttpEntity contentEntity) throws IOException {
		HttpResponse response;
		HttpPut httpPut = new HttpPut(uri);
		httpPut.setEntity(contentEntity);
		response = httpClient.execute(httpPut);
		return response;
	}

	public HttpResponse DELETERequest(URI uri) throws IOException {
		HttpResponse response;
		HttpDelete httpDelete = new HttpDelete(uri);
		response = httpClient.execute(httpDelete);
		return response;
	}

	/**
	 * 
	 * @throws Throwable
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.httpClient.getConnectionManager().shutdown();
	}

	@Override
	public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		// Honor 'keep-alive' header
		HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		while (it.hasNext()) {
			HeaderElement he = it.nextElement();
			String param = he.getName();
			String value = he.getValue();
			if (value != null && param.equalsIgnoreCase("timeout")) {
				try {
					return Long.parseLong(value) * 1000;
				} catch (NumberFormatException ignore) {
				}
			}
		}
		// HttpHost target = (HttpHost)
		// context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		return this.keepAlive;
	}
}
