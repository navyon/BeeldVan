// is it possible to make a class from the crop activity? so we can reuse @preview activity?

package org.BvDH.CityTalk;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import android.app.*;
import android.content.*;
import android.widget.*;

import org.BvDH.CityTalk.adapter.NavDrawerListAdapter;
import org.BvDH.CityTalk.adapter.UserImagesAdapter;
import org.BvDH.CityTalk.asynctasks.GetImagesAsyncTask;
import org.BvDH.CityTalk.asynctasks.Sync2Manager;
import org.BvDH.CityTalk.crop.CropImage;
import org.BvDH.CityTalk.fragments.*;
import org.BvDH.CityTalk.fragments.InfoFragment;
import org.BvDH.CityTalk.interfaces.ImageLoadInterface;
import org.BvDH.CityTalk.interfaces.ListItemClickedInterface;
import org.BvDH.CityTalk.model.*;
import org.BvDH.CityTalk.slide.app.SlidingActivity;
import org.BvDH.CityTalk.utilities.InternalStorageContentProvider;
import org.BvDH.CityTalk.utilities.RESTClient;
import org.BvDH.CityTalk.utilities.SportanStringUtil;
import org.BvDH.CityTalk.utilities.Utilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.google.myjson.reflect.TypeToken;

public class MainActivity extends BaseActivity implements OnClickListener,ImageLoadInterface, LocationListener
	{
		private static Uri mImageCaptureUri;
		// Sliding menu objects
		public static DrawerLayout mDrawerLayout;
		public static ExpandableListView mDrawerList;
		private ActionBarDrawerToggle mDrawerToggle;

        ExpandableListAdapter listAdapter;
        ExpandableListView expListView;
        List<String> listDataHeader;
        HashMap<String, List<String>> listDataChild;
		// nav drawer title
		private CharSequence mDrawerTitle;

		// used to store app title
		private CharSequence mTitle;

		// slide menu items
		private String[] navMenuTitles;
		private TypedArray navMenuIcons;

		private ArrayList<NavDrawerItem> navDrawerItems;
		private NavDrawerListAdapter adaptermenu;

		private static final int PICK_FROM_CAMERA = 1;
		private static final int CROP_FROM_CAMERA = 2;
		private static final int PICK_FROM_FILE = 3;

		Utilities utils;
		ImageView camaerIconImg;

		UserImagesAdapter userImagesAdapter;
		List<NavImagesInfo> navImagesInfoList;
		GridView postedImgsGridView;
//		public static View main_include_layout;
		String imagePath;
		public static String imageLocation;
		Location mLocation;
		LocationManager mLocationManager;
        public static ArrayList<LocationData> mList;

        public static int sgroupPosition;

        // implementation of crop
        public static final String TAG = "MainActivity";
        public static String TEMP_PHOTO_FILE_NAME;
        public static String FOLDER_NAME;

        public static final int REQUEST_CODE_GALLERY      = 0x1;
        public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
        public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;

        private ImageView mImageView;
        private File      mFileTemp;

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				setContentView(R.layout.main_new);
				utils = new Utilities(this);
				postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);
				TextView overslaanTV = (TextView) findViewById(R.id.overslaanTv);
				overslaanTV.setOnClickListener(this);

				TextView doneTV = (TextView) findViewById(R.id.doneTV);
				doneTV.setOnClickListener(this);

                FOLDER_NAME = checkDir();

                TEMP_PHOTO_FILE_NAME = getRandomFileName();
                // set folder name, create one if none exist.

				camaerIconImg = (ImageView) findViewById(R.id.camaerIconImg);

				camaerIconImg.setOnClickListener(this);

                //cropoption
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
                }
                else {
                    mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
                }

//
				// enabling action bar app icon and behaving it as toggle button
				getActionBar().setDisplayHomeAsUpEnabled(true);
				getActionBar().setHomeButtonEnabled(true);


				loadImagesList();
			}


		@Override
		protected void onResume()
			{
				// TODO Auto-generated method stub
				super.onResume();
				// Sync2Manager.getSync2Manager().getAllLocations();
				new MyTask().execute();
				if (mLocationManager == null)
					mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this, Looper.getMainLooper());
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this, Looper.getMainLooper());

            }

		@Override
		protected void onStop()
			{
				if (mLocationManager != null)
					mLocationManager.removeUpdates(this);
				super.onStop();
			}

		private void loadImagesList()
			{
				new GetImagesAsyncTask(MainActivity.this, this).execute();

			}





		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.main, menu);
				return true;
			}


		// check if directory exists. if not, create.
		private String checkDir()
			{
                String dirname = "BeeldVan";
				File dir = new File(Environment.getExternalStorageDirectory() + "/" + dirname);
				if (!dir.exists())
					{
						boolean result = dir.mkdir();
						if (result)
							{
								System.out.println("created a DIR");
							}
					}
                return dirname;
			}

        private void showPhotoOptionsDialog()
        {
            final String[] items = new String[] {getString(R.string.CapturePhoto), getString(R.string.ChoosefromGallery), getString(R.string.cancel)};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogSlideAnim);
            builder.setInverseBackgroundForced(true);

            builder.setTitle(getString(R.string.ChooseaTask));
            builder.setAdapter(adapter, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                { // pick from
                    // camera
                    if (item == 0)
                    {
                        takePicture();
                    }
                    else if (item == 1)
                    {
                        openGallery();
                    }
                    else if (item == 2)
                    {
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

            Intent intent = new Intent(MainActivity.this, CropImage.class);
            intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
            intent.putExtra(CropImage.SCALE, true);
            //TODO add dynamic resolution here!
            intent.putExtra(CropImage.ASPECT_X, 1024);
            intent.putExtra(CropImage.ASPECT_Y, 768);

            startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
        }


		private String getRandomFileName()
			{
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);
				String fileName = FOLDER_NAME +"/"+ String.valueOf(n) + "_beeldvan.jpg";
				return fileName;
			}

		@Override
		protected void onPause()
			{
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				super.onPause();
			}

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_GALLERY:
                    try
                    {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
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
                        imageLocation = imagePath;

                        if (extras != null)
                        {
                            try
                            {
                                System.out.println("image cropped and added "+imagePath);
                                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                                intent.putExtras(extras);
                                intent.putExtra("imagePath", imagePath);
                                intent.putExtra("hasPhoto", true);
                                startActivity(intent);

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

		@Override
		public void onClick(View v)
			{
				switch (v.getId())
					{
					case R.id.camaerIconImg:
						showPhotoOptionsDialog();
						break;
					case R.id.overslaanTv:
						try
							{

								Intent intent = new Intent(MainActivity.this, MessageActivity.class);
								startActivity(intent);

							}
						catch (Exception e)
							{
								e.printStackTrace();
							}
						break;
					case R.id.doneTV:

						break;

					default:
						break;
					}

			}

		private List<LocationData> locationDatas;

		private class MyTask extends AsyncTask<Void, Void, Void>
			{

				@Override
				protected Void doInBackground(Void... params)
					{
						URI uri;
						try
							{
								uri = new URI("http://api.beeldvan.nu/1.0/locations/all.json");
								HttpResponse response = new RESTClient(RESTClient.MAX_KEEP_ALIVE).GETRequest(uri);
								if (response != null)
									{

										HttpEntity entity = response.getEntity();
										String callbackJson;
										InputStream is = entity.getContent();
										callbackJson = SportanStringUtil.ConvertStreamToString(is);
										callbackJson = SportanStringUtil.StripJSONPCallback(callbackJson);

										Gson gson = new GsonBuilder().serializeNulls().create();
										Type collectionType = new TypeToken<List<LocationData>>()
											{
											}.getType();
										locationDatas = gson.fromJson(callbackJson, collectionType);
										System.out.println();
										new Utilities(MainActivity.this).setAllLocations(callbackJson);
									}
							}
						catch (URISyntaxException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						// TODO Auto-generated method stub
						return null;
					}

			}

		@Override
		public void imgListJSONCallback(JSONObject json)
			{
				if (json != null && json.optString("status").equals("OK"))
					{

					}
				else
					{
					}

			}


		@Override
		public void onLocationChanged(Location location)
			{
				mLocation = location;
			}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
			{
				// TODO Auto-generated method stub

			}

		@Override
		public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub

			}

		@Override
		public void onProviderDisabled(String provider)
			{
				// TODO Auto-generated method stub

			}

	}
