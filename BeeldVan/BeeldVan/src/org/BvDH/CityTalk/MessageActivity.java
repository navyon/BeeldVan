package org.BvDH.CityTalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.BvDH.CityTalk.utilities.Utilities;

// The Message Activity
public class MessageActivity extends Activity
	{
		Intent intent;
		private EditText txtView_msg;
		private TextView txtView_maxLines;
		private TextView txtView_msgTip;
        private TextView txtView_continue;
		private ImageView aspectv;
        private ImageView progress2;
		String msg = null;
		float textsize;
		RelativeLayout msgRL;
		String imagePath;
		Bundle extras;
		Bitmap photo;
        Animation slideUpIn;
        Animation slideDownIn;

		@SuppressWarnings("deprecation")
		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.message_layout);
				msgRL = (RelativeLayout) findViewById(R.id.msgRL);

				// build alert dialog for max line check
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Er passen maar 4 regels tekst op het scherm");
				if (getIntent().getExtras() != null)
					{
						extras = getIntent().getExtras();
						photo = extras.getParcelable("data");
						if (photo != null)
							{
								imagePath = getIntent().getStringExtra("imagePath");
							}

					}
				txtView_msg = (EditText) findViewById(R.id.txtView_msg);
//				txtView_maxLines = (TextView) findViewById(R.id.txtView_maxLine);
//				txtView_msgTip = (TextView) findViewById(R.id.txtView_msgTip);
				aspectv = (ImageView) findViewById(R.id.aspectv);
                progress2 = (ImageView) findViewById(R.id.progress_2Img);
				setTextSizes(txtView_msg);

                slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_dialog);
                slideDownIn = AnimationUtils.loadAnimation(this, R.anim.slide_out_down);

				builder.setMessage("Er passen maximaal 4 regels op het scherm!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
							{
							}
					});
				final AlertDialog alert = builder.create();

				Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
				Typeface fontLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
				Typeface fontHelv = Typeface.createFromAsset(getAssets(), "fonts/HelveticaBold.ttf");
				txtView_msg.setTypeface(fontHelv);
//				txtView_maxLines.setTypeface(fontRegular);
//				txtView_msgTip.setTypeface(fontLight);

                txtView_continue = (TextView) findViewById(R.id.txtpreview); //text above button
				final Button btnPrev = (Button) findViewById(R.id.btnpreview);
				btnPrev.setTypeface(fontLight);
				final Button btnhidekeyb = (Button) findViewById(R.id.btnhidekey);
                final RelativeLayout layhidekeyb = (RelativeLayout) findViewById(R.id.layouthidekey);

				if (getIntent().hasExtra("msg"))
					{
						msg = getIntent().getStringExtra("msg");
						txtView_msg.setText(msg);
					}
				btnPrev.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
							{
								String msg = txtView_msg.getText().toString();

								if (!msg.isEmpty())
									{

										intent = new Intent(MessageActivity.this, PreviewActivity.class);

										if (photo == null)
											extras = new Bundle();
										else
											extras = getIntent().getExtras();
										extras.putString("msg", msg);
										extras.putString("imagePath", imagePath);
										intent.putExtras(extras);
										startActivity(intent);
									}

								else
									{
										Toast.makeText(getApplicationContext(), "Het toevoegen van een bericht is verplicht.", Toast.LENGTH_LONG).show();
									}

							}
					});

				// some online hack that should limit the Edit box, dont think its any better than the first enter hack lets see
				txtView_msg.addTextChangedListener(new TextWatcher()
					{

						public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
							{
							}

						public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
							{
							}

						public void afterTextChanged(Editable arg0)
							{
								int lineCount = txtView_msg.getLineCount();
								if (lineCount > 4)
									{
										txtView_msg.setText(txtView_msg.getText().delete(txtView_msg.length() - 1, txtView_msg.length()));
										txtView_msg.setSelection(txtView_msg.length());
										alert.show();
									}
							}
					});

				btnhidekeyb.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
							{
								// hide keyboard
								InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                txtView_msg.clearFocus();
                                btnPrev.startAnimation(slideUpIn);
                                txtView_continue.startAnimation(slideUpIn);
                                btnPrev.setVisibility(View.VISIBLE);
                                txtView_continue.setVisibility(View.VISIBLE);
                                progress2.startAnimation(slideDownIn);
                                progress2.setVisibility(View.VISIBLE);

							}
					});


				txtView_msg.setOnFocusChangeListener(new View.OnFocusChangeListener()
					{
						@Override
						public void onFocusChange(View v, boolean hasFocus)
							{
								if (hasFocus)
									{
                                        layhidekeyb.setVisibility(View.VISIBLE);
                                        btnPrev.setVisibility(View.GONE);
                                        txtView_continue.setVisibility(View.GONE);
                                        progress2.setVisibility(View.GONE);



                                    }
								else
                                    layhidekeyb.setVisibility(View.GONE);
//                                    btnPrev.setVisibility(View.VISIBLE);
//                                    txtView_continue.setVisibility(View.VISIBLE);

                            }
					});

			}

		// function to mimic text size in relation with the Haagse Toren
		void setTextSizes(EditText txt)
			{
				// force aspect ratio for txtView
				Bitmap.Config conf = Bitmap.Config.ALPHA_8;
                //TODO change to createBitmap(lid.width, lid.height) to use location aspect
				Bitmap bmp = Bitmap.createBitmap(1024, 776, conf);// create transparent bitmap
				aspectv.setImageBitmap(bmp);
				// get display size
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);

				Resources r = getResources();


				float marginpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
				float width = size.x - marginpx; // substract the margins (2x 5dp) from the width in px
                //TODO call utility for setting font size and margin (also on preview activity)
                //textsize = Utilities.getFontSize(width);
                // int margin = Utilities.getMarginSize(width);

				// ->this can be deleted convert width to textsize (120 at 1024 -> = 1024*0.117
				textsize = (float) (width * 0.1171875); //old hardcoded
				int margin = (int) (width * 0.062); //old hardcoded
				// set sizes
				txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
				txt.setPadding(margin, margin, margin, margin);
			}

	}
