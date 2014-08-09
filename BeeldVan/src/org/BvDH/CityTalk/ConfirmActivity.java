package org.BvDH.CityTalk;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.BvDH.CityTalk.utilities.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ConfirmActivity extends Activity
{

	JSONParser jsonParser = new JSONParser();

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	// url to call php script
	// private static String url_create_message =
	// "http://beeldvandenhaag.daankrn.nl/android_api/create_message.php";
	private static String url_create_message = "http://beeldvandenhaag.nu/android/create_message.php";

	/************* Php script upload file ****************/
	// private static String upLoadServerUri =
	// "http://beeldvandenhaag.daankrn.nl/android_api/UploadToServer.php";
	private static String upLoadServerUri = "http://beeldvandenhaag.nu/android/upload_pictures.php";

	int uploadFinished = 0;
	String image_path;
	String ip_address = "";
	EditText edittx_email;
	CheckBox chkbox;
	// Progress Dialog
	private ProgressDialog pDialog;
	int serverResponseCode = 0;
	boolean hasphoto = false;

	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	protected void onPause()
	{
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.confirm_layout);

		Button submitbox = (Button) findViewById(R.id.btnfinalsubmit);
		edittx_email = (EditText) findViewById(R.id.editText_email);
		edittx_email.setTextColor(Color.BLACK);

		Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface fontLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

		edittx_email.setTypeface(fontLight);
		submitbox.setTypeface(fontLight);

		if (getIntent().hasExtra("imagePath"))
		{
			image_path = getIntent().getStringExtra("imagePath");
			hasphoto = true;
		}

		chkbox = (CheckBox) findViewById(R.id.checkBox);
		chkbox.setTypeface(fontRegular);
		if (!hasphoto)
			chkbox.setVisibility(View.INVISIBLE);

		submitbox.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{

				String email = edittx_email.getText().toString();
				if (checkEmail(email))
				{
					if (hasphoto)
					{
						if (chkbox.isChecked())
						{
							// Call the php upload method which starts a new
							// thread

							StartNewThreadUpload();
							// creating new message in background thread

						}
						else
						{
							String chkboxerror = getString(R.string.ConfirmCheckboxError);
							Toast.makeText(getApplicationContext(), chkboxerror, Toast.LENGTH_LONG).show();
						}
					}
					new CreateNewMessage().execute();
				}
				else
				{
					edittx_email.setTextColor(Color.RED);
					String emailerror = getString(R.string.ConfirmEmailError);
					Toast.makeText(getApplicationContext(), emailerror, Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	void uploadFinishedCheck()
	{
		if ((hasphoto && uploadFinished == 2) || (!hasphoto && uploadFinished == 1))
		{
			Intent i = new Intent(ConfirmActivity.this, FacebookLikeActivity.class);
			startActivity(i);
		}

	}

	/**
	 * Background Async Task to Create new Message
	 * */
	class CreateNewMessage extends AsyncTask<String, String, String>
	{

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{

					pDialog = new ProgressDialog(ConfirmActivity.this);
					pDialog.setMessage("We zijn aan het testen");
					pDialog.setIndeterminate(false);
					pDialog.setCancelable(true);
					pDialog.show();

				}
			});

		}

		// Create the Message
		protected String doInBackground(String... args)
		{
			ip_address = getPublicIP();
			String email = edittx_email.getText().toString();
			String foto = "";
			if (hasphoto)
			{
				File f = new File(image_path);
				foto = f.getName();
			}
			String bericht = getIntent().getStringExtra("msg");

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("bericht", bericht));
			// only add photo if there is one.
			params.add(new BasicNameValuePair("foto", foto));
			params.add(new BasicNameValuePair("ip", ip_address));

			// getting JSON Object

			JSONObject json = jsonParser.makeHttpRequest(url_create_message, "POST", params);

			// check log cat fro response
			Log.d("Create Response", json.toString());

			// check for success tag
			try
			{
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1)
				{
					// successfully created message can update UI here
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							uploadFinished += 1;
							uploadFinishedCheck();

						}
					});
				}
				else
				{
					// failed to create message
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url)
		{
			// dismiss the dialog once done
			pDialog.dismiss();
		}

	}

	private boolean checkEmail(String email)
	{
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

	// Start a New thread for the network activity
	public void StartNewThreadUpload()
	{

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{

					/********** Pick file from sdcard *******/
					// we can add a check for null here maybe
					File f = new File(image_path);

					// Upload sdcard file
					uploadFile(f.getPath());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		thread.start();
	}

	// Creates a http connection
	public int uploadFile(String sourceFileUri)
	{

		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile())
		{

			// if file dont exist we can update UI here

			return 0;

		}
		else
		{
			try
			{

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
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

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							uploadFinished += 1;
							uploadFinishedCheck();

							// can update UI here showing the file is uploaded
						}
					});
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			}
			catch (MalformedURLException ex)
			{

				ex.printStackTrace();

				runOnUiThread(new Runnable()
				{
					public void run()
					{

						// error can update UI here
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			}
			catch (Exception e)
			{

				e.printStackTrace();

				runOnUiThread(new Runnable()
				{
					public void run()
					{

						// error can update UI or log here
					}
				});
				Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
			}
			// dialog.dismiss();
			return serverResponseCode;

		}
	}

	public static String getPublicIP()
	{
		try
		{
			Document doc = Jsoup.connect("http://api.externalip.net/ip").get();
			return doc.body().text();
		}
		catch (IOException e)
		{
			return "0.0.0.0";
		}
	}
}