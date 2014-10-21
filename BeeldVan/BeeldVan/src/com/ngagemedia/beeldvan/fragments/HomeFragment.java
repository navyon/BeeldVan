package com.ngagemedia.beeldvan.fragments;

import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import com.ngagemedia.beeldvan.crop.CropImage;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.InternalStorageContentProvider;
import com.ngagemedia.beeldvan.utilities.Utilities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class HomeFragment extends Fragment implements View.OnClickListener
{

    private static Uri mImageCaptureUri;
    public static final int REQUEST_CODE_GALLERY = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE = 0x3;
    public static final String TAG = "MainActivity";
    public static String TEMP_PHOTO_FILE_NAME;
    public static String FOLDER_NAME;

    private File mFileTemp;

    Utilities utils;
    ImageButton camaerIconImg;
    Button overslaanTV;

    String imagePath;
    FragmentManager fm1;
    Fragment fragment = null;

    Locations screen;

	public HomeFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        //new implementation
        fm1 = getActivity().getFragmentManager();
        utils = new Utilities(getActivity());
        //get selected screen
        screen = utils.getSelectedLocation(getActivity());
        overslaanTV = (Button) rootView.findViewById(R.id.overslaanTv);
        camaerIconImg = (ImageButton) rootView.findViewById(R.id.camaerIconImg);
        //crop option implementation
        FOLDER_NAME = checkDir();

        TEMP_PHOTO_FILE_NAME = getRandomFileName();

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }

        getActivity().setTitle("Afbeelding");


        camaerIconImg.setOnClickListener(this);

        overslaanTV.setOnClickListener(this);
        return rootView;
    }

    // check if directory exists. if not, create.
    private String checkDir() {
        String dirname = "BeeldVan";
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + dirname);
        if (!dir.exists()) {
            boolean result = dir.mkdir();
            if (result) {
            }
        }
        return dirname;
    }

    private void showPhotoOptionsDialog() {
        final String[] items = new String[]{getString(R.string.CapturePhoto), getString(R.string.ChoosefromGallery), getString(R.string.cancel)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_arrayadapter, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogSlideAnim);
        builder.setInverseBackgroundForced(true);

        builder.setTitle(getString(R.string.ChooseaTask));
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { // pick from
                // camera
                if (item == 0) {
                    takePicture();
                } else if (item == 1) {
                    openGallery();
                } else if (item == 2) {
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
        try {

            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            } else {

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
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.SCALE_UP_IF_NEEDED, true);
        intent.putExtra(CropImage.ASPECT_X, screen.getAspectRatioWidth());
        intent.putExtra(CropImage.ASPECT_Y, screen.getAspectRatioHeight());
        intent.putExtra(CropImage.OUTPUT_X, screen.getAspectRatioWidth());
        intent.putExtra(CropImage.OUTPUT_Y, screen.getAspectRatioHeight());

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    private String getRandomFileName() {
        long n = System.currentTimeMillis();
        String fileName = FOLDER_NAME + File.separator + String.valueOf(n) + "_beeldvan.jpg";
        return fileName;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_GALLERY:
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    Utilities.CopyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();

                    startCropImage();
                } catch (Exception e) {
                    Log.e(TAG, "Error while creating temp file", e);
                    // TODO: handle exception
                }
                break;

            case REQUEST_CODE_TAKE_PICTURE:
                startCropImage();
                break;

            case REQUEST_CODE_CROP_IMAGE:
                if (data != null) {
                    imagePath = data.getStringExtra(CropImage.IMAGE_PATH);
                }
                if (imagePath != null) {
                    final Bundle extras = data.getExtras();
                    MainActivity.imageLocation = imagePath;
                    boolean hasphoto = true;

                    if (extras != null) {
                        try {
                            String tag = "MessageFragment";
                            FragmentTransaction ft1 = fm1.beginTransaction();
                            fragment = new MessageFragment();
                            ft1.addToBackStack(null);
                            extras.putString("imagePath", imagePath);
                            extras.putBoolean("hasphoto", hasphoto);
                            ft1.replace(R.id.frame_container, fragment, tag);
                            fragment.setArguments(extras);
                            ft1.commit();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camaerIconImg:
                if(screen != null) {
                    showPhotoOptionsDialog();
                } else MainActivity.mDrawerLayout.openDrawer(MainActivity.mDrawerList);
                break;
            case R.id.overslaanTv:
                Log.d("button", "overslaan");
                if(screen != null) {
                    try {
                        String tag = "MessageFragment";
                        FragmentTransaction ft1 = fm1.beginTransaction();
                        fragment = new MessageFragment();
                        ft1.addToBackStack(null);

                        ft1.replace(R.id.frame_container, fragment, tag);
                        ft1.commit();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else  MainActivity.mDrawerLayout.openDrawer(MainActivity.mDrawerList);
                break;

            default:
                break;
        }

    }
}
