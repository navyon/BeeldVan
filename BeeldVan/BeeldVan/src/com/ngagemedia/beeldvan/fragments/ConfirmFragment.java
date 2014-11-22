package com.ngagemedia.beeldvan.fragments;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.model.Message;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.views.MyDatePicker;
import com.ngagemedia.beeldvan.views.MyTimePicker;

public class ConfirmFragment extends Fragment implements Animation.AnimationListener
{

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
    LinearLayout confirmOptions;
    RelativeLayout dateTimePicker;
    ScrollView mScrollView;
    ImageView mProgress;
    ImageButton emailQuestion;
    Button emailClose;
    RelativeLayout emailInfoRL;


	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\._%\\-\\+]{1,256}" + "@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	MyDatePicker mDp;
	MyTimePicker mTp;
	String mPublishDate;


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
					imagePath = extras.getString("imagePath");
					if (imagePath != null)
						hasphoto = true; //check for image
				}

            //date time picker
			dateTimeTv = (TextView) rootView.findViewById(R.id.dateTimeTv);
			mDp = new MyDatePicker(getActivity(), Calendar.getInstance(), rootView.findViewById(R.id.fcLlDate));
			mTp = new MyTimePicker(getActivity(), Calendar.getInstance(), rootView.findViewById(R.id.fcLlTime));
            dateTimePicker = (RelativeLayout) rootView.findViewById(R.id.dateTimePicker);

            //other buttons and input
			submitbox = (Button) rootView.findViewById(R.id.btnfinalsubmit);
			edittx_email = (EditText) rootView.findViewById(R.id.editText_email);
			edittx_email.setTextColor(Color.BLACK);
            emailQuestion = (ImageButton) rootView.findViewById(R.id.confirmQuestion);
            emailClose = (Button) rootView.findViewById(R.id.email_question_closebtn);
            emailInfoRL = (RelativeLayout) rootView.findViewById(R.id.emailInfo);

            edittx_email.setImeOptions(EditorInfo.IME_ACTION_DONE);

            mScrollView = (ScrollView) rootView.findViewById(R.id.confirmsv);
            mProgress = (ImageView) rootView.findViewById(R.id.progress_4Img);
            confirmOptions = (LinearLayout) rootView.findViewById(R.id.confirmOptionsLL);
            chkbox = (CheckBox) rootView.findViewById(R.id.checkBox);


			utils = new Utilities(getActivity());
			screen = utils.getSelectedLocation(getActivity());

            //animations
            fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

            fadeOut.setAnimationListener(this);

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            //onclicklisteners
            emailQuestion.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    emailInfoRL.setVisibility(View.VISIBLE);
                    emailInfoRL.startAnimation(fadeIn);
                }
            });

            emailClose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    emailInfoRL.startAnimation(fadeOut);
                }
            });

//            edittx_email.setOnFocusChangeListener(new View.OnFocusChangeListener()
//            {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus)
//                {
//                    if (hasFocus)
//                    {
//                        mScrollView.post(new Runnable() {
//                            public void run() {
//                                mScrollView.smoothScrollTo(0,confirmOptions.getBottom());
//                            }
//                        });
//                    } else {
//                        if(mProgress.getVisibility() == View.INVISIBLE){
//                        }
//                    }
//                }
//            });

            //load email adress if saved
            String mail = utils.getSharedPrefValue("email");
			if (mail != null)
				{
					edittx_email.setText(mail);
				}

			getActivity().setTitle("Verzenden");


            //Submit listener -> posts the message to API
			submitbox.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
						{

                            //get date and time
                            mPublishDate = mDp.getDate()+ ", " + mTp.getTime();

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

                            // if the message is not in the past, post
                            if(unixPublishDate > (System.currentTimeMillis()-300000)/1000) {

                                String email = edittx_email.getText().toString();

                                //check valid email
                                if (checkEmail(email)) {
                                    utils.saveValueToSharedPrefs("email", email);
                                    //check checkbox
                                    if (chkbox.isChecked()) {
                                        // creating new message in background thread
                                        new CreateNewMessage().execute();


                                    } else { //foutmeldingen
                                        String chkboxerror = getString(R.string.ConfirmCheckboxError);
                                        Toast.makeText(getActivity().getApplicationContext(), chkboxerror, Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    edittx_email.setTextColor(Color.RED);
                                    String emailerror = getString(R.string.ConfirmEmailError);
                                    Toast.makeText(getActivity().getApplicationContext(), emailerror, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                String DateError = getString(R.string.ConfirmDateError);
                                Toast.makeText(getActivity().getApplicationContext(), DateError, Toast.LENGTH_LONG).show();
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
            emailInfoRL.setVisibility(View.GONE);
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

				String result;

				File f = null;

				if (hasPhoto)
					f = new File(MainActivity.imageLocation);

				try
					{
						HttpClient client = new DefaultHttpClient();

                        String url_create_message = "http://api.beeldvan.nu/1.0/messages/post.json";
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
						StringBody location = new StringBody(Integer.toString(msg.getLocationid()), ContentType.TEXT_PLAIN);
						StringBody publishDate = new StringBody(Long.toString(msg.getTimestamp()), ContentType.TEXT_PLAIN);

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
                ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
				Fragment fragment = new FinalFragment();

				ft1.replace(R.id.frame_container, fragment);
				ft1.commit();
			}

		// Create the Message
		protected String doInBackground(String... args)
			{
				ip_address = "0.0.0.0"; //don't log IP address
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
				Message msg = new Message(bericht, ip_address, email, foto, screen.getLid(), unixPublishDate);
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


}
