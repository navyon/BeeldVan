package org.BvDH.CityTalk.utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser_test
{

	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	int serverResponseCode = 0;

	// constructor
	public JSONParser_test()
	{

	}

	// function get json from url
	// by making HTTP POST or GET mehtod
	public JSONObject makeHttpRequest(String url, String tempFileUrl, String method, String sourceFileUri, List<NameValuePair> params)
	{

		// upload file implementation
		String fileName = sourceFileUri;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile;
		if (sourceFileUri != null)
		{
			sourceFile = new File(sourceFileUri);
		}
		else
			sourceFile = null;

		// Making HTTP request
		try
		{

			// check for request method
			if (method == "POST")
			{
				// request method is POST
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(new UrlEncodedFormEntity(params));

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

				// upload function/////////////////
				if (sourceFile != null)
				{

					URL fileUrl = new URL(tempFileUrl);
					FileInputStream fileInputStream = new FileInputStream(sourceFile);

					// Open a HTTP connection to the URL
					conn = (HttpURLConnection) fileUrl.openConnection();
					conn.setDoInput(true); // Allow Inputs
					conn.setDoOutput(true); // Allow Outputs
					conn.setUseCaches(false); // Don't use a Cached Copy
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("ENCTYPE", "multipart/form-data");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
					conn.setRequestProperty("uploaded_file", fileName);

					dos = new DataOutputStream(conn.getOutputStream());

					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
					dos.writeBytes(lineEnd);

					// create a buffer of maximum size
					bytesAvailable = fileInputStream.available();

					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					// read file and write it into form...
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0)
					{

						dos.write(buffer, 0, bufferSize);
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					}

					// send multipart form data necesssary after file data...
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					// Responses from the server (code and message)
					serverResponseCode = conn.getResponseCode();
					String serverResponseMessage = conn.getResponseMessage();

					Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

					if (serverResponseCode == 200)
					{

					}

					// close the streams //
					fileInputStream.close();
					dos.flush();
					dos.close();
				}
				// //////// end of upload function

			}
			else if (method == "GET")
			{
				// request method is GET
				DefaultHttpClient httpClient = new DefaultHttpClient();
				String paramString = URLEncodedUtils.format(params, "utf-8");
				url += "?" + paramString;
				HttpGet httpGet = new HttpGet(url);

				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
			}

		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace(); // for upload file
			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		}
		catch (Exception e)
		{
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try
		{
			jObj = new JSONObject(json);
		}
		catch (JSONException e)
		{
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}
}
