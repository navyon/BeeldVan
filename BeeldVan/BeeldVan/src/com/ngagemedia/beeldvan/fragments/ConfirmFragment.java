package com.ngagemedia.beeldvan.fragments;

import java.io.Console;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.model.Message;
import com.ngagemedia.beeldvan.utilities.JSONParser;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.views.MyDatePicker;
import com.ngagemedia.beeldvan.views.MyTimePicker;
import com.ngagemedia.customwheel.OnWheelChangedListener;
import com.ngagemedia.customwheel.WheelView;

public class ConfirmFragment extends Fragment implements Animation.AnimationListener
{
	JSONParser jsonParser = new JSONParser();

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static String url_create_message = "http://api.beeldvan.nu/1.0/messages/post.json";

	Utilities utils;
	Locations screen;
	String ip_address = "";
	EditText edittx_email;
	CheckBox chkbox;
	// Progress Dialog
	private ProgressDialog pDialog;
	boolean hasphoto = false;

	String imagePath;
	Bundle extras = null;
    Long unixPublishDate;
	TextView dateTimeTv;
    Button submitbox;
    ImageButton questionMark;
    LinearLayout confirmOptions;
    RelativeLayout dateTimePicker;
    ScrollView mScrollView;
    RelativeLayout emailQuestionRL;
    ImageView mProgress;

	FragmentManager fm1 = ConfirmFragment.this.getFragmentManager();

	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	MyDatePicker mDp;
	MyTimePicker mTp;
	String mPublishDate;


//    Animation slideUpIn;
//    Animation slideDownOut;
    Animation fadeIn, fadeOut;

	public ConfirmFragment()
		{
		}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{

			View rootView = inflater.inflate(R.layout.fragment_confirm, container, false);
			extras = getArguments();
			if (extras != null)
				{
					// extras = getActivity().getIntent().getExtras();
					// photo = extras.getParcelable("data");
					imagePath = extras.getString("imagePath");
					if (imagePath != null)
						hasphoto = true;

				}
			dateTimeTv = (TextView) rootView.findViewById(R.id.dateTimeTv);
			mDp = new MyDatePicker(getActivity(), Calendar.getInstance(), rootView.findViewById(R.id.fcLlDate));
			mTp = new MyTimePicker(getActivity(), Calendar.getInstance(), rootView.findViewById(R.id.fcLlTime));
			submitbox = (Button) rootView.findViewById(R.id.btnfinalsubmit);
			edittx_email = (EditText) rootView.findViewById(R.id.editText_email);
			edittx_email.setTextColor(Color.BLACK);

			Typeface fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
			Typeface fontLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

			edittx_email.setTypeface(fontLight);
			submitbox.setTypeface(fontLight);

//            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            dateTimePicker = (RelativeLayout) rootView.findViewById(R.id.dateTimePicker);
            mScrollView = (ScrollView) rootView.findViewById(R.id.confirmsv);
            mProgress = (ImageView) rootView.findViewById(R.id.progress_4Img);

			utils = new Utilities(getActivity());
			screen = utils.getSelectedLocation(getActivity());
			chkbox = (CheckBox) rootView.findViewById(R.id.checkBox);
			chkbox.setTypeface(fontRegular);
            confirmOptions = (LinearLayout) rootView.findViewById(R.id.confirmOptionsLL);
            questionMark = (ImageButton) rootView.findViewById(R.id.confirmQuestion);

            fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            fadeOut.setAnimationListener(this);
//            slideUpIn.setAnimationListener(this);

//
//            btnhidekeyb.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    // hide keyboard
//                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                    edittx_email.clearFocus();
////                    confirmOptions.startAnimation(slideUpIn);
//                    chkbox.setVisibility(View.VISIBLE);
//                    submitbox.setVisibility(View.VISIBLE);
//                }
//            });

//edittx_email.setImeOptions(EditorInfo.IME_ACTION_DONE);
//
            edittx_email.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (hasFocus)
                    {
//                        mScrollView.scrollTo(0, y);
//                        mProgress.startAnimation(fadeOut);
//                        dateTimePicker.startAnimation(fadeOut);
                        mScrollView.post(new Runnable() {
                            public void run() {
                                mScrollView.smoothScrollTo(0,confirmOptions.getBottom());
                            }
                        });
                    } else {
                        if(mProgress.getVisibility() == View.INVISIBLE){
//                            mProgress.startAnimation(fadeIn);
//                            mProgress.setVisibility(View.VISIBLE);
//                            dateTimePicker.startAnimation(fadeIn);
//                            dateTimePicker.setVisibility(View.VISIBLE);
                        }
                    }

                }
            });

//            mTp.hour.setCyclic(true);

            mTp.hour.addChangingListener(new OnWheelChangedListener() {
                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    wheel.invalidateWheel(true);
                    ((TextView) wheel.getItemView(wheel.getCurrentItem())).setTextColor(0xFFFFFFFF);
                    ((TextView) wheel.getItemView(wheel.getCurrentItem())).getText();
                    Log.d("wheel", "changing from " + oldValue + " to " + newValue);
                    wheel.invalidate();
                }
            });
            String mail = utils.getSharedPrefValue("email");
			if (mail != null)
				{
					edittx_email.setText(mail);
				}

			getActivity().setTitle("Verzenden");

            questionMark.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }
            );

			submitbox.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
						{
                            mPublishDate = mDp.getDate()+ ", " + mTp.getTime();
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            DateTimeZone zone = DateTimeZone.forID("Europe/Amsterdam");
                            DateTimeZone.setDefault(zone);
                            DateTimeFormatter format = DateTimeFormat.forPattern("dd/MM/yyyy, HH:mm");
                            try {
                                DateTime dateTime = format.parseDateTime(mPublishDate);
                                unixPublishDate = dateTime.getMillis()/1000;
                            }
                            catch (Exception e)
                            {
                                unixPublishDate = System.currentTimeMillis()/1000;
                            }

							String email = edittx_email.getText().toString();
							if (checkEmail(email))
								{
									utils.saveValueToSharedPrefs("email", email);
									if (chkbox.isChecked())
										{
											// creating new message in background thread
											new CreateNewMessage().execute();

										}
									else
										{
											String chkboxerror = getString(R.string.ConfirmCheckboxError);
											Toast.makeText(getActivity().getApplicationContext(), chkboxerror, Toast.LENGTH_LONG).show();
										}

								}
							else
								{
									edittx_email.setTextColor(Color.RED);
									String emailerror = getString(R.string.ConfirmEmailError);
									Toast.makeText(getActivity().getApplicationContext(), emailerror, Toast.LENGTH_LONG).show();
								}

						}
				});
			return rootView;
		}

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == fadeOut){
            mProgress.setVisibility(View.INVISIBLE);
            dateTimePicker.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

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

				try
					{
						pDialog = new ProgressDialog(getActivity());
						pDialog.setMessage(getString(R.string.UploadDialog));
						pDialog.setIndeterminate(false);
						pDialog.setCancelable(true);
						pDialog.show();
					}
				catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}

		public void POST(Message msg, boolean hasPhoto)
			{

				String result = "";

				File f = null;

				if (hasPhoto)
					f = new File(MainActivity.imageLocation);

				try
					{
						HttpClient client = new DefaultHttpClient();

						HttpPost post = new HttpPost(url_create_message);

						MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
						entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
						FileBody file = null;
						if (hasPhoto)
							file = new FileBody(f, ContentType.create("image/jpeg"), f.getName());
						StringBody email = new StringBody(msg.getEmail(), ContentType.TEXT_PLAIN);
						StringBody message = new StringBody(msg.getMsg(), ContentType.TEXT_PLAIN);
						StringBody ip = new StringBody(msg.getIp_address(), ContentType.TEXT_PLAIN);
						StringBody source = new StringBody("Android", ContentType.TEXT_PLAIN);
						StringBody location = new StringBody(Integer.toString(screen.getLid()), ContentType.TEXT_PLAIN);
						StringBody publishDate = new StringBody(Long.toString(unixPublishDate), ContentType.TEXT_PLAIN);

						entityBuilder.addPart("message", message);
						entityBuilder.addPart("email", email);
						if (hasPhoto)
							entityBuilder.addPart("photo", file);
						entityBuilder.addPart("ip", ip);
						entityBuilder.addPart("source", source);
						entityBuilder.addPart("locationId", location);
						entityBuilder.addPart("publishDate", publishDate);

						HttpEntity entity = entityBuilder.build();

						post.setEntity(entity);

						HttpResponse response = client.execute(post);

						HttpEntity httpEntity = response.getEntity();

						result = EntityUtils.toString(httpEntity);

						Log.v("result", result);
					}
				catch (Exception e)
					{
						e.printStackTrace();
					}
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft1 = fm.beginTransaction();
				Fragment fragment = new FacebookFragment();

				ft1.replace(R.id.frame_container, fragment);
				ft1.commit();
			}

		// Create the Message
		protected String doInBackground(String... args)
			{
				ip_address = getPublicIP();
				String email = "";
				boolean isvalid = checkEmail(edittx_email.getText().toString());
				if (isvalid)
					{
						email = edittx_email.getText().toString();
					}

				String foto = "";
				if (hasphoto)
					{
						File f = new File(MainActivity.imageLocation);
						foto = f.getName();
					}
				String bericht = extras.getString("msg");
				Message msg = new Message(bericht, ip_address, email, foto, 4);
				POST(msg, hasphoto);

				return null;
			}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url)
			{
				// dismiss the dialog once done
				try
					{
						pDialog.dismiss();
					}
				catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

	}

	private boolean checkEmail(String email)
		{
			return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
		}


	// Creates a http connection

	public static String getPublicIP()
		{
			// Document doc = Jsoup.connect("http://api.externalip.net/ip").get();
			// return doc.body().text();
			return getIPAddress(true);
		}

	public static String getIPAddress(boolean useIPv4)
		{
			try
				{
					List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
					for (NetworkInterface intf : interfaces)
						{
							List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
							for (InetAddress addr : addrs)
								{
									if (!addr.isLoopbackAddress())
										{
											String sAddr = addr.getHostAddress().toUpperCase();
											boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
											if (useIPv4)
												{
													if (isIPv4)
														return sAddr;
												}
											else
												{
													if (!isIPv4)
														{
															int delim = sAddr.indexOf('%'); // drop ip6 port suffix
															return delim < 0 ? sAddr : sAddr.substring(0, delim);
														}
												}
										}
								}
						}
				}
			catch (Exception ex)
				{
				} // for now eat exceptions
			return "";
		}



}
