package org.BvDH.CityTalk.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.BvDH.CityTalk.ConfirmActivity;
import org.BvDH.CityTalk.MainActivity;
import org.BvDH.CityTalk.MessageActivity;
import org.BvDH.CityTalk.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.BvDH.CityTalk.adapter.CropOptionAdapter;
import org.BvDH.CityTalk.model.CropOption;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PreviewFragment extends Fragment implements Animation.AnimationListener
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
    PreviewFragment mfa = PreviewFragment.this;
    String imagePath = null;

    ArrayAdapter<String> adapter;

    float textsize;

    // Animation
    Animation wipeIn, wipeOut, slideIn, slideOut, fadeIn, fadeOut, fadeInImg, fadeOutImg;
	public PreviewFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		final View rootView = inflater.inflate(R.layout.fragment_photos, container, false);
        // load fonts
        // Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface fontLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        Typeface fontHelv = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaBold.ttf");

        imagev = (ImageView) rootView.findViewById(R.id.ImageViewPreview);
        txtview = (TextView) rootView.findViewById(R.id.TextViewPreview);
        aspectv = (ImageView) rootView.findViewById(R.id.aspectFix);
        animView = (ImageView) rootView.findViewById(R.id.animView);
        btnChangePreviewPhoto = (Button) rootView.findViewById(R.id.btnChangePreviewPhoto);
        btnChangePreviewMessage = (Button) rootView.findViewById(R.id.btnchangePreviewText);
        btnRestartAnim = (ImageButton) rootView.findViewById(R.id.btnRestartAnim);

        // set fonts
        txtview.setTypeface(fontHelv);
        btnChangePreviewPhoto.setTypeface(fontLight);
        btnChangePreviewMessage.setTypeface(fontLight);

        // load the animation
        wipeIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.wipe_in);
        wipeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.wipe_out);
        slideIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_in);
        slideOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_out);
        fadeIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);
        fadeInImg = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);
        fadeOutImg = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);

        // set animation listener
        wipeIn.setAnimationListener(this);
        wipeOut.setAnimationListener(this);
        slideIn.setAnimationListener(this);
        slideOut.setAnimationListener(this);
        fadeOut.setAnimationListener(this);
        fadeIn.setAnimationListener(PreviewFragment.this);

        fadeOutImg.setAnimationListener(this);
        fadeInImg.setAnimationListener(this);
        // These Methods check whether photos or a message was added

        // load message and image, check if image exists
        LoadMsgImg();
        CheckPhotoExist();

        // final String [] items = new String [] {getString(R.string.CapturePhoto),
        // getString(R.string.ChoosefromGallery),getString(R.string.deletephoto)};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
                    try
                    {
                        // Deletes the stored file from the sd
                        if (MainActivity.imageLocation != null)
                        {
                            File file = new File(MainActivity.imageLocation);
                            if (file.exists())
                                file.delete();
                        }
                        imagev.setImageBitmap(null);
                        imagev.destroyDrawingCache();
                        hasphoto = false;
                        tempURI = null;
                        ChangeButtons();

                    }
                    catch (Exception e)
                    {
                        Toast.makeText(getActivity().getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();

                    }

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

        rootView.findViewById(R.id.btnSubmitmsgtxt).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

               /* Intent intent = new Intent(PreviewActivity.this, ConfirmActivity.class);

                if (hasphoto)
                    intent.putExtra("imagePath", getIntent().getStringExtra("imagePath"));
                intent.putExtra("msg", msg);
                intent.putExtra("hasphoto", hasphoto);
                PreviewActivity.this.startActivity(intent);*/

            }
        });

        // final AlertDialog dialog = builder.create();
        rootView.findViewById(R.id.btnChangePreviewPhoto).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                CheckDelete();
                dialog.show();
                ChangeButtons();
            }
        });

        rootView.findViewById(R.id.btnchangePreviewText).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {


                  // Call    Back stack here !!!!!!!!!!!!!!!!!!!!!!!

               // Intent i = new Intent(PreviewActivity.this, MessageActivity.class);
                if (hasphoto)
                {
                   /* if (tempURI != null)
                    {
                        i.putExtra("imagePath", tempURI.getPath());
                    }
                    else
                        i.putExtra("imagePath", getIntent().getStringExtra("imagePath"));
                    i.putExtra("msg", msg);
                    startActivity(i);*/
                }
               // finish();

            }
        });

        btnRestartAnim.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                StartTextAnimation();
            }
        });
        return rootView;
    }

    void setTextSizes(TextView txt)
    {
        // force aspect ratio for txtView
        Bitmap.Config conf = Bitmap.Config.ALPHA_8;
        Bitmap bmp = Bitmap.createBitmap(1024, 776, conf);// create transparent bitmap
        aspectv.setImageBitmap(bmp);
        // get display size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
        Bundle extras = getActivity().getIntent().getExtras();
        final Bitmap photo = extras.getParcelable("data");
        if (photo != null) {
            hasphoto = true;
            imagev.setImageBitmap(photo);
            imagev.setVisibility(View.INVISIBLE);
        }
        else
            ChangeButtons();
        //////
        if (extras.getString("msg")!=null)
        {
            msg = extras.getString("msg");
            hasmessage = true;
            setTextSizes(txtview);
        }


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
    @Override
    public void onAnimationRepeat(Animation animation)
    {}
    // This Method checks if a photo was added
    @Override
    public void onAnimationStart(Animation animation)
    {}

    @Override
    public void onAnimationEnd(Animation animation) {
       /* if (animation == move) {

        }*/
    }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != getActivity().RESULT_OK)
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
                if(data != null)
                {
                    imagePath = mImageCaptureUri.getPath();
                    final Bundle extras = data.getExtras();
                    if (extras != null)
                    {
                        try
                        {

                            /*Intent intent = new Intent(PreviewActivity.this, ConfirmActivity.class);

                            intent.putExtras(extras);
                            intent.putExtra("imagePath", imagePath);
                            startActivity(intent);*/

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                break;

        }
    }



    private void doCrop()
    {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0)
        {
            Toast.makeText(getActivity(), "Geen app beschikbaar voor formaat aanpassen", Toast.LENGTH_SHORT).show();

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

                    co.title = getActivity().getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getActivity().getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);

                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getActivity().getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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


