package org.BvDH.CityTalk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.BvDH.CityTalk.model.Message;
import org.BvDH.CityTalk.utilities.JSONParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class ConfirmActivity extends Activity
{

	JSONParser jsonParser = new JSONParser();

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	// url to call php script
	// private static String url_create_message =
	// "http://beeldvandenhaag.daankrn.nl/android_api/create_message.php";
	private static String url_create_message = "http://api.beeldvan.nu/1.0/messages/post.json";

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

	DatePicker datePicker1;
	TimePicker timePicker1;
    String imagePath;
    Bundle extras;
    Bitmap photo;
	TextView dateTimeTv;
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

        if(getIntent().getExtras()!=null) {
             extras = getIntent().getExtras();
            photo = extras.getParcelable("data");
            if (photo != null) {
                imagePath = getIntent().getStringExtra("imagePath");
                hasphoto = true;
            }

        }
		dateTimeTv = (TextView) findViewById(R.id.dateTimeTv);
		timePicker1 = (TimePicker) findViewById(R.id.timePicker1);
		datePicker1 = (DatePicker) findViewById(R.id.datePicker1);

		showDateTime(datePicker1.getDayOfMonth(), (datePicker1.getMonth() + 1),  datePicker1.getYear(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute());
		timePicker1.setOnTimeChangedListener(new OnTimeChangedListener()
		{

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
			{
				showDateTime(datePicker1.getDayOfMonth(), (datePicker1.getMonth() + 1),  datePicker1.getYear(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute());
			}
		});

		datePicker1.init(datePicker1.getYear(), datePicker1.getMonth(), datePicker1.getDayOfMonth(), new OnDateChangedListener()
		{

			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				showDateTime(datePicker1.getDayOfMonth(), (datePicker1.getMonth() + 1),  datePicker1.getYear(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute());
				
			}
		});
		Button submitbox = (Button) findViewById(R.id.btnfinalsubmit);
		edittx_email = (EditText) findViewById(R.id.editText_email);
		edittx_email.setTextColor(Color.BLACK);

		Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface fontLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

		edittx_email.setTypeface(fontLight);
		submitbox.setTypeface(fontLight);



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
        public void POST(Message msg){

            String result = "";
            File f = new File(imagePath);

            try
            {
                HttpClient client = new DefaultHttpClient();

                HttpPost post = new HttpPost(url_create_message);

                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                FileBody file = new FileBody(f);
                StringBody email = new StringBody(msg.getEmail(), ContentType.TEXT_PLAIN);
                StringBody message = new StringBody(msg.getMsg(), ContentType.TEXT_PLAIN);
                StringBody ip = new StringBody(msg.getIp_address(), ContentType.TEXT_PLAIN);
                StringBody source = new StringBody("Android",ContentType.TEXT_PLAIN);
                StringBody location = new StringBody("1", ContentType.TEXT_PLAIN);



                entityBuilder.addPart("message",message);
                entityBuilder.addPart("email",email);
                entityBuilder.addPart("photo", file);
                entityBuilder.addPart("ip",ip);
                entityBuilder.addPart("source",source);
                entityBuilder.addPart("locationId",location);


                post.setHeader("enctype","multipart/form-data");

               /* if(msg.getFoto() != null)
                {
                    entityBuilder.addBinaryBody(Bitmap,msg.getFoto());
                }*/

                HttpEntity entity = entityBuilder.build();

                post.setEntity(entity);

                HttpResponse response = client.execute(post);

                HttpEntity httpEntity = response.getEntity();

                result = EntityUtils.toString(httpEntity);

                Log.v("result", result);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
           /* Intent i = new Intent(ConfirmActivity.this, FacebookLikeActivity.class);
            startActivity(i);*/
        }


        // Create the Message
		protected String doInBackground(String... args)
		{
			ip_address = getPublicIP();
            String email ="";
            boolean isvalid = checkEmail(edittx_email.getText().toString());
            if(isvalid)
            {
               email = edittx_email.getText().toString();
            }

			String foto = "";
			if (hasphoto)
			{
				File f = new File(image_path);
				foto = f.getName();
			}
			String bericht = getIntent().getStringExtra("msg");
            Message msg = new Message(bericht,ip_address,email,foto,4);

            POST(msg);

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

	private void showDateTime(int day, int month, int year, int hour, int minutes)
	{
		String dateTimeStr = "";
		if (day < 10)
		{
			dateTimeStr += "0" + day;
		}
		else
		{
			dateTimeStr += day;
		}

		if (month < 10)
		{
			dateTimeStr += "-0" + month;
		}
		else
		{
			dateTimeStr += "-" + month;
		}

		dateTimeStr += "-" + year;
		if (hour < 10)
		{
			dateTimeStr += "   0" + hour;
		}
		else
		{
			dateTimeStr += "   " + hour;
		}
		if (minutes < 10)
		{
			dateTimeStr += ":0" + minutes;
		}
		else
		{
			dateTimeStr += ":" + minutes;
		}
		dateTimeTv.setText(dateTimeStr);
	}
}