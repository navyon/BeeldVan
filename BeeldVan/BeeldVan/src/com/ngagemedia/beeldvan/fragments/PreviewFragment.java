package com.ngagemedia.beeldvan.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.ngagemedia.beeldvan.MainActivity;
import android.app.Fragment;
import com.ngagemedia.beeldvan.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ngagemedia.beeldvan.crop.CropImage;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.InternalStorageContentProvider;
import com.ngagemedia.beeldvan.utilities.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PreviewFragment extends Fragment implements Animation.AnimationListener
{
    private TextView txtview;
    public ImageView imagev;
    public ImageView aspectv;
    boolean hasphoto = false;

    String msg = null;
    Button btnChangePreviewPhoto;
    Button btnChangePreviewMessage;
    ImageButton btnRestartAnim;
    LinearLayout layBtns;

    String imagePath = null;
    Fragment fragment;
    ArrayAdapter<String> adapter;

    float textsize;

    // implementation of crop
    public static final String TAG = "PreviewFragment";
    private static String FOLDER_NAME;

    public static final int REQUEST_CODE_GALLERY      = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;

    private File      mFileTemp;
    Utilities utils;
    Locations screen;

    // Animation
    Animation animFadeTxt, animFadeImg, fadeIn;
    Animation slideUpIn, slideDownIn, slideUpOut, slideDownOut;
	public PreviewFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		final View rootView = inflater.inflate(R.layout.fragment_preview, container, false);
        // load font
        Typeface fontHelv = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaBold.ttf");


        imagev = (ImageView) rootView.findViewById(R.id.ImageViewPreview); //holds the selected image
        txtview = (TextView) rootView.findViewById(R.id.TextViewPreview); //holds the users text
        aspectv = (ImageView) rootView.findViewById(R.id.aspectFix); //view to fix aspect ratio for each resolution
        btnChangePreviewPhoto = (Button) rootView.findViewById(R.id.btnChangePreviewPhoto);
        btnChangePreviewMessage = (Button) rootView.findViewById(R.id.btnchangePreviewText);
        btnRestartAnim = (ImageButton) rootView.findViewById(R.id.btnRestartAnim);
        layBtns = (LinearLayout) rootView.findViewById(R.id.previewTxtOptionsLL);
        utils = new Utilities(getActivity());
        screen = utils.getSelectedLocation(getActivity());
        // set fonts
        txtview.setTypeface(fontHelv);

        // load the animation
        animFadeTxt = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in_and_out);
        animFadeImg = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in_and_out);
        fadeIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);

        slideUpIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_slide_in_bottom);
        slideDownIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_slide_in_top);
        slideUpOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_slide_out_top);
        slideDownOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_slide_out_bottom);

        // set animation listener

        animFadeTxt.setAnimationListener(this);
        animFadeImg.setAnimationListener(this);




        getActivity().setTitle("Preview");

        FOLDER_NAME = checkDir();

        String TEMP_PHOTO_FILE_NAME = getRandomFileName();

        //cropoption
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        }
        else {
            mFileTemp = new File(TEMP_PHOTO_FILE_NAME);
        }

        // load message and image, check if image exists
        LoadMsgImg();

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
        adapter.add(getString(R.string.CapturePhoto));
        adapter.add(getString(R.string.ChoosefromGallery));
        adapter.add(getString(R.string.cancel));

        if (hasphoto)
            adapter.add(getString(R.string.deletephoto));


        txtview.setText(msg);

//            StartTextAnimation();

        //Animation start
        startAnimation();



        rootView.findViewById(R.id.btnSubmitmsgtxt).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragment = new ConfirmFragment();
                ft1.addToBackStack(null);
                ft1.replace(R.id.frame_container, fragment);
                Bundle extras = getArguments();
                if (extras != null) {
                    extras.putString("msg", msg);
                    extras.putString("imagePath", imagePath);
                    extras.putBoolean("hasphoto", hasphoto);
                    fragment.setArguments(extras);
                }

                ft1.commit();

            }
        });

        // final AlertDialog dialog = builder.create();
        btnChangePreviewPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPhotoOptionsDialog();
                ChangeButtons();
            }
        });

        btnChangePreviewMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                // Call    Back stack here !!!!!!!!!!!!!!!!!!!!!!! (not if photo was added here)
                FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragment = new MessageFragment();
                Bundle extras = new Bundle();
                ft1.addToBackStack(null);
                ft1.replace(R.id.frame_container, fragment);

                if (hasphoto) {
                    extras.putString("imagePath", imagePath);
                } else {
                    extras.putString("imagePath", extras.getString("imagePath"));
                }

                extras.putString("msg", msg);
                extras.putBoolean("hasphoto", hasphoto);
                fragment.setArguments(extras);

                ft1.commit();


            }
        });

        btnRestartAnim.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startAnimation();
                layBtns.startAnimation(slideDownOut);
            }
        });
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        screen = utils.getSelectedLocation(getActivity());
    }

    void setTextSizes(TextView txt)
    {
        float width = utils.getScreenWidth(getActivity());

        textsize = utils.getFontSize(width,screen);
        int margin = utils.getMarginSize(width, screen);

        // set sizes
        txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        txt.setPadding(margin, margin, margin, margin);

    }

    /**
     * loads message and images, sets aspect fix etc.
     */
    @SuppressWarnings("deprecation")
    void LoadMsgImg()
    {
        Bundle extras = this.getArguments();
        imagePath = extras.getString("imagePath");
        hasphoto = extras.getBoolean("hasphoto", false);

        float width = utils.getScreenWidth(getActivity());
        int height = utils.getPreviewHeight(width,screen);
//        Bitmap.Config conf = Bitmap.Config.ALPHA_8;
//        Bitmap bmp = Bitmap.createBitmap((int)width, height, conf);
//        aspectv.setImageBitmap(bmp);
        aspectv.setMinimumHeight(height);

        if(hasphoto) {
            final Bitmap photo = BitmapFactory.decodeFile(imagePath);
            if (photo != null) {
                imagev.setImageBitmap(photo);
                imagev.setVisibility(View.INVISIBLE);
            }
        }
        else {
            ChangeButtons();
        }

        if (extras.getString("msg")!=null)
        {
            msg = extras.getString("msg");
            setTextSizes(txtview);
        }

        imagev.setMinimumHeight(height);
        txtview.setHeight(height);
        imagev.setMaxHeight(height);
    }

    void animateImage()
    {
        btnRestartAnim.setVisibility(View.GONE);

        imagev.startAnimation(animFadeImg);
    }

    void animateText()
    {
        txtview.setVisibility(View.VISIBLE);
        txtview.startAnimation(animFadeTxt);
    }
    @Override
    public void onAnimationEnd(Animation animation)
    {
        // Take any action after completing the animation

        if (animation == animFadeImg)
        {
            imagev.setVisibility(View.INVISIBLE);

            animateText();
        }

        else if (animation == animFadeTxt)
        {
            txtview.setVisibility(View.INVISIBLE);
            btnRestartAnim.startAnimation(fadeIn);
            btnRestartAnim.setVisibility(View.VISIBLE); // else show restart button
            layBtns.setVisibility(View.VISIBLE);
            layBtns.startAnimation(slideUpIn);
        }

        else if(animation == slideDownOut){
            layBtns.setVisibility(View.INVISIBLE);

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
            case REQUEST_CODE_GALLERY:
                try
                {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    Utilities.CopyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();

                    startCropImage();
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Error while creating temp file", e);
                    // TODO: handle exception
                }
                break;

            case REQUEST_CODE_TAKE_PICTURE:
                startCropImage();
                break;
            case REQUEST_CODE_CROP_IMAGE:
                imagePath = data.getStringExtra(CropImage.IMAGE_PATH);
                if (imagePath != null)
                {
                    final Bundle extras = data.getExtras();
                    MainActivity.imageLocation = imagePath;

                    if (extras != null)
                    {
                        try
                        {
                            FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                            ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                            fragment = new PreviewFragment();

                            ft1.addToBackStack(null);
                            ft1.replace(R.id.frame_container, fragment);
                            extras.putString("msg", msg);
                            extras.putString("imagePath", imagePath);
                            extras.putBoolean("hasphoto", true);
                            fragment.setArguments(extras);
                            ft1.commit();

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


    private void showPhotoOptionsDialog() {
        final String[] items;
        if(hasphoto) {
            items = new String[]{getString(R.string.CapturePhoto), getString(R.string.ChoosefromGallery),getString(R.string.deletephoto), getString(R.string.cancel)};
        } else {
            items = new String[]{getString(R.string.CapturePhoto), getString(R.string.ChoosefromGallery), getString(R.string.cancel)};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_arrayadapter, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogSlideAnim);
        builder.setInverseBackgroundForced(true);


        builder.setTitle(getString(R.string.ChooseaTask));
        builder.setAdapter(adapter, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item) { // pick from
                if (item == 0) {
                    Log.d("dialog", String.valueOf(item));
                    takePicture();
                } else if (item == 1) {
                    Log.d("dialog", String.valueOf(item));
                    openGallery();
                } else if (item == 2) {
                    Log.d("dialog", String.valueOf(item));
                    if (hasphoto) {
                        try {
                            // Deletes the stored file from the sd
                            if (MainActivity.imageLocation != null) {
                                File file = new File(MainActivity.imageLocation);
                                if (file.exists())
                                    file.delete();
                            }
                            imagev.setImageBitmap(null);
                            imagev.destroyDrawingCache();
                            hasphoto = false;
                            ChangeButtons();

                        } catch (Exception e) {
//                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                } else if (hasphoto && item == 3) {
                    Log.d("dialog", String.valueOf(item));
                    dialog.cancel();
                    dialog.dismiss();
                }
            }

        });
        builder.show();
    }
    //cropimage lib
    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            String fileName = getRandomFileName();
        try {

            String state = Environment.getExternalStorageState();
            Uri mImageCaptureUri;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            }
            else {

                mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {

            Log.d(TAG, "cannot take picture", e);
        }
    }
    //cropimage lib
    private void openGallery() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    //cropimage lib
    private void startCropImage() {

        Intent intent = new Intent(getActivity(), CropImage.class);
        intent.putExtra(CropImage.MSG, msg);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, screen.getAspectRatioWidth());
        intent.putExtra(CropImage.ASPECT_Y, screen.getAspectRatioHeight());
        intent.putExtra(CropImage.OUTPUT_X, screen.getAspectRatioWidth());
        intent.putExtra(CropImage.OUTPUT_Y, screen.getAspectRatioHeight());

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    private String getRandomFileName()
    {
        long n = System.currentTimeMillis();
        return FOLDER_NAME + File.separator + String.valueOf(n) + "_beeldvan.jpg";
    }

    private String checkDir()
    {
        String dirname = "BeeldVan";
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + dirname);
        if (!dir.exists())
        {
            boolean result = dir.mkdir();
            if (result)
            {
                Log.d("checkDir", "dir created");
            } else Log.d("checkDir", "failed to create dir");
        }
        return dirname;
    }

    private void startAnimation(){
        layBtns.setVisibility(View.INVISIBLE);
        btnRestartAnim.setVisibility(View.INVISIBLE);
        if(hasphoto){
            animateImage();
        } else {
            animateText();
        }
    }
}


