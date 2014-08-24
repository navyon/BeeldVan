package org.BvDH.CityTalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.BvDH.CityTalk.R;
import org.BvDH.CityTalk.adapter.CropOptionAdapter;
import org.BvDH.CityTalk.model.CropOption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PreviewActivity extends Activity implements Animation.AnimationListener
{
	private Uri mImageCaptureUri;
	private TextView txtview;
	private Uri tempURI;
	public ImageView imagev;
	public ImageView aspectv;
	public ImageView animView;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	boolean hasphoto = false;
	boolean hasmessage = false;

	String msg = null;
	Button btnChangePreviewPhoto;
	Button btnChangePreviewMessage;
	ImageButton btnRestartAnim;

	String image_path = null;

	ArrayAdapter<String> adapter;

	float textsize;

	// Animation
	Animation wipeIn, wipeOut, slideIn, slideOut, fadeIn, fadeOut, fadeInImg, fadeOutImg;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.previewtxt);
		setContentView(R.layout.previewtxt_new);

		// load fonts
		// Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface fontLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		Typeface fontHelv = Typeface.createFromAsset(getAssets(), "fonts/HelveticaBold.ttf");

		imagev = (ImageView) findViewById(R.id.ImageViewPreview);
		txtview = (TextView) findViewById(R.id.TextViewPreview);
		aspectv = (ImageView) findViewById(R.id.aspectFix);
		animView = (ImageView) findViewById(R.id.animView);
		btnChangePreviewPhoto = (Button) findViewById(R.id.btnChangePreviewPhoto);
		btnChangePreviewMessage = (Button) findViewById(R.id.btnchangePreviewText);
		btnRestartAnim = (ImageButton) findViewById(R.id.btnRestartAnim);

		// set fonts
		txtview.setTypeface(fontHelv);
		btnChangePreviewPhoto.setTypeface(fontLight);
		btnChangePreviewMessage.setTypeface(fontLight);

		// load the animation
		wipeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wipe_in);
		wipeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wipe_out);
		slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in);
		slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);
		fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
		fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
		fadeInImg = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
		fadeOutImg = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

		// set animation listener
		wipeIn.setAnimationListener(this);
		wipeOut.setAnimationListener(this);
		slideIn.setAnimationListener(this);
		slideOut.setAnimationListener(this);
		fadeOut.setAnimationListener(this);
		fadeIn.setAnimationListener(this);

		fadeOutImg.setAnimationListener(this);
		fadeInImg.setAnimationListener(this);
		// These Methods check whether photos or a message was added

		// load message and image, check if image exists
		LoadMsgImg();
		CheckPhotoExist();

		// final String [] items = new String [] {getString(R.string.CapturePhoto),
		// getString(R.string.ChoosefromGallery),getString(R.string.deletephoto)};
		adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
		adapter.add(getString(R.string.CapturePhoto));
		adapter.add(getString(R.string.ChoosefromGallery));
		adapter.add(getString(R.string.cancel));
		if (hasphoto)
			adapter.add(getString(R.string.deletephoto));

		if (hasmessage)
		{
			txtview.setText(msg);

			StartTextAnimation();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.ChooseaTask);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{ // pick from camera

				if (item == 0)
				{
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.valueOf(System.currentTimeMillis()) + "_app_upload.jpg"));

					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
					try
					{
						intent.putExtra("return-data", true);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					}
					catch (ActivityNotFoundException e)
					{
						e.printStackTrace();
					}
				}
			
				else if (item == 1)
				{ // pick from file
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, getString(R.string.ChooseApp)), PICK_FROM_FILE);

					ChangeButtons();
				}
				
				else if (item == 2)
				{
					dialog.dismiss();
					dialog.cancel();
//					try
//					{
//						// Deletes the stored file from the sd
//						if (image_path != null)
//						{
//							File file = new File(image_path);
//							if (file.exists())
//								file.delete();
//						}
//						imagev.setImageBitmap(null);
//						imagev.destroyDrawingCache();
//						hasphoto = false;
//						tempURI = null;
//						ChangeButtons();
//
//					}
//					catch (Exception e)
//					{
//						Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
//
//					}

				}

			}

		});

		// Cancels the Image Capture
		builder.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{

				CheckPhotoExist();
				CheckDelete();
			}
		});

		final AlertDialog dialog = builder.create();

		findViewById(R.id.btnSubmitmsgtxt).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{

				Intent intent = new Intent(PreviewActivity.this, ConfirmActivity.class);

				if (hasphoto)
					intent.putExtra("imagePath", image_path);
				intent.putExtra("msg", msg);
				intent.putExtra("hasphoto", hasphoto);
				PreviewActivity.this.startActivity(intent);

			}
		});

		// final AlertDialog dialog = builder.create();
		findViewById(R.id.btnChangePreviewPhoto).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				CheckDelete();
				dialog.show();
				ChangeButtons();
			}
		});

		findViewById(R.id.btnchangePreviewText).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{

				// imagev.setDrawingCacheEnabled(true);
				CheckPhotoExist();

				Intent i = new Intent(PreviewActivity.this, MessageActivity.class);
				if (hasphoto)
				{
					if (tempURI != null)
					{
						i.putExtra("imagePath", tempURI.getPath());
					}
					else
						i.putExtra("imagePath", getIntent().getStringExtra("imagePath"));
					i.putExtra("msg", msg);
					startActivity(i);
				}
				finish();

			}
		});

		btnRestartAnim.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				StartTextAnimation();
			}
		});

	}

	void setTextSizes(TextView txt)
	{
		// force aspect ratio for txtView
		Bitmap.Config conf = Bitmap.Config.ALPHA_8;
		Bitmap bmp = Bitmap.createBitmap(1024, 776, conf);// create transparent bitmap
		aspectv.setImageBitmap(bmp);
		// get display size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		Resources r = getResources();
		float marginpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
		float width = size.x - marginpx; // substract the margins (2x 5dp) from the width in px

		// convert width to textsize (120 at 1024 -> = 1024*0.117
		textsize = (float) (width * 0.1171875);
		int margin = (int) (width * 0.062);
		// set sizes
		txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txt.setPadding(margin, margin, margin, margin);
	}

	@SuppressWarnings("deprecation")
	void LoadMsgImg()
	{
		///////
		Bundle extras = getIntent().getExtras();
		final Bitmap photo = extras.getParcelable("data");
		if (photo != null)
		{
			imagev.setBackground(new BitmapDrawable(photo));
		}
		//////
		if (extras.getString("msg")!=null)
		{
			msg = extras.getString("msg");
			hasmessage = true;
			setTextSizes(txtview);
		}
//		if (getIntent().hasExtra("imagePath"))
//		{
//			image_path = getIntent().getStringExtra("imagePath");
//			Bitmap b = BitmapFactory.decodeFile(image_path);
//
//			if (b != null)
//			{
//				imagev.setImageBitmap(b);
//			}
//		}

	}

	void StartTextAnimation()
	{
		btnRestartAnim.setVisibility(View.INVISIBLE);
		// animView.setVisibility(View.VISIBLE);
		txtview.setVisibility(View.VISIBLE);
		txtview.startAnimation(fadeIn);
		// animView.startAnimation(wipeIn);

	}

	void StartImageAnimation()
	{
		imagev.setVisibility(View.VISIBLE);
		imagev.startAnimation(fadeInImg);
	}

	// This Method checks if a photo was added

	void CheckDelete()
	{
		if (adapter.getCount() == 2 && hasphoto)
			adapter.add(getString(R.string.deletephoto));
		else if (adapter.getCount() == 3 && !hasphoto)
			adapter.remove(getString(R.string.deletephoto));
	}

	void CheckPhotoExist()
	{
		if (imagev.getDrawable() != null)
		{
			hasphoto = true;
		}
		else
			hasphoto = false;
		ChangeButtons();
	}

	// this method changes the buttons text according to context
	void ChangeButtons()
	{
		String btnChangetxt = getString(R.string.PreviewbtnChangeTxt);
		btnChangePreviewMessage.setText(btnChangetxt);
		// checks whether user added a photo, then changes button text accordingly
		if (!hasphoto)
		{
			String btnaddphototxt = getString(R.string.PreviewbtnAddPhoto);
			btnChangePreviewPhoto.setText(btnaddphototxt);
		}
		else
		{
			String btnaddchangetxt = getString(R.string.PreviewbtnChangePhoto);
			btnChangePreviewPhoto.setText(btnaddchangetxt);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode)
		{
		case PICK_FROM_CAMERA:
			// CheckPhotoExist();
			doCrop();
			break;

		case PICK_FROM_FILE:
			mImageCaptureUri = data.getData();
			// CheckPhotoExist();
			doCrop();
			break;

		case CROP_FROM_CAMERA:

			Bundle extras = data.getExtras();

			if (extras != null)
			{
				String imagePath = tempURI.getPath();
				Log.d("Testing: ", imagePath);
				Bitmap photo = BitmapFactory.decodeFile(imagePath);
				if (photo != null)
				{
					imagev.setImageBitmap(photo);
					image_path = imagePath;
					getIntent().putExtra("imagePath", image_path);
				}
				else
				{
					// hasphoto = false;
				}

			}
			CheckPhotoExist();
			CheckDelete();
			File f = new File(mImageCaptureUri.getPath());

			if (f.exists())
				f.delete();
			StartTextAnimation();
			break;

		}
	}

	@Override
	public void onAnimationEnd(Animation animation)
	{
		// Take any action after completing the animation

		// check for zoom in animation
		if (animation == fadeIn && hasphoto)
		{ // only start image animation if there is one
			// animView.setVisibility(View.INVISIBLE);
			txtview.setVisibility(View.INVISIBLE);
			StartImageAnimation();
		}

		// else if (animation == fadeIn && !hasphoto){

		// }
		else if (animation == fadeIn && !hasphoto)
		{

			txtview.setVisibility(View.INVISIBLE);
			btnRestartAnim.setVisibility(View.VISIBLE); // else show restart button

		}

		else if (animation == fadeInImg)
		{
			imagev.setVisibility(View.INVISIBLE);
			btnRestartAnim.setVisibility(View.VISIBLE);
			// animView.setVisibility(View.VISIBLE);
			// animView.startAnimation(wipeOut);
		}
		else
		{
			imagev.setVisibility(View.INVISIBLE);
			btnRestartAnim.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onAnimationRepeat(Animation animation)
	{

	}

	@Override
	public void onAnimationStart(Animation animation)
	{

	}

	private void doCrop()
	{
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		if (size == 0)
		{
			Toast.makeText(this, "Geen app beschikbaar voor formaat aanpassen", Toast.LENGTH_SHORT).show();

			return;
		}
		else
		{
			// cropped picture is saved at tempURI location
			tempURI = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "bvdh/" + String.valueOf(System.currentTimeMillis()) + "_app_upload.jpg"));

			intent.setData(mImageCaptureUri);
			intent.putExtra("outputX", 1024);
			intent.putExtra("outputY", 776);
			intent.putExtra("aspectX", 1024);
			intent.putExtra("aspectY", 776);
			intent.putExtra("crop", true);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", false); // don't send data back to prevent transactionTooLarge
			intent.putExtra(MediaStore.EXTRA_OUTPUT, tempURI); // save to file!
			Log.d("Path", tempURI.getPath());
			// hasphoto =true;
			if (size == 1)
			{
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			}
			else
			{
				for (ResolveInfo res : list)
				{
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Pas formaat aan");
				builder.setAdapter(adapter, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int item)
					{
						startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
}
