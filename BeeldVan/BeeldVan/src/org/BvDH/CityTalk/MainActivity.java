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

public class MainActivity extends BaseActivity implements OnClickListener,ImageLoadInterface, ListItemClickedInterface, LocationListener
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
		public static View main_include_layout;
		String imagePath;
		public static String imageLocation;
        int lastExpandedGroupPosition =-1;
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
				// setContentView(R.layout.main);
				setContentView(R.layout.main_new);
				utils = new Utilities(this);
				main_include_layout = (View) findViewById(R.id.main_include_layout);
				postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);
				TextView overslaanTV = (TextView) findViewById(R.id.overslaanTv);
				overslaanTV.setOnClickListener(this);

				TextView doneTV = (TextView) findViewById(R.id.doneTV);
				doneTV.setOnClickListener(this);
				loadLocale();

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


				// Slider Menu methods
				mTitle = mDrawerTitle = getTitle();

				// load slide menu items
				navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

				// nav drawer icons from resources
				navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

				mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				mDrawerList = (ExpandableListView) findViewById(R.id.list_slidermenu);
                // preparing list data
                prepareListData();

                listAdapter = new NavDrawerListAdapter(this, listDataHeader, listDataChild);
                // setting list adapter
                mDrawerList.setAdapter(listAdapter);
                mDrawerList.setGroupIndicator(null);
                // Listview Group click listener
                mDrawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v,
                                                int groupPosition, long id) {
                        /* Toast.makeText(getApplicationContext(),
                        "Group Clicked " + listDataHeader.get(groupPosition),
                         Toast.LENGTH_SHORT).show();*/
                        lastExpandedGroupPosition =groupPosition;
                        return false;
                    }
                });

                // Listview Group expanded listener
                mDrawerList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                    @Override
                    public void onGroupExpand(int groupPosition) {
                        for (int i = 0; i < mDrawerList.getCount(); i++)
                        {
                            if (i != groupPosition)
                            {
                                mDrawerList.collapseGroup(i);
                            }
                        }
                    }
                });

                // Listview Group collasped listener
                mDrawerList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                    @Override
                    public void onGroupCollapse(int groupPosition) {
                       /* Toast.makeText(getApplicationContext(),
                                listDataHeader.get(groupPosition) + " Collapsed",
                                Toast.LENGTH_SHORT).show();*/

                    }
                });

                // Listview on child click listener
                mDrawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v,
                                                int groupPosition, int childPosition, long id) {
                        sgroupPosition = groupPosition;
                        displayView(childPosition,groupPosition);



                      /*  Toast.makeText(
                                getApplicationContext(),
                                listDataHeader.get(groupPosition)
                                        + " : "
                                        + listDataChild.get(
                                        listDataHeader.get(groupPosition)).get(
                                        childPosition), Toast.LENGTH_SHORT)
                                .show();*/
                        return false;
                    }
                });



               /* navDrawerItems = new ArrayList<NavDrawerItem>();

				// adding nav drawer items to array
				// Home
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
				// Find People
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
				// Photos
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
				// Communities, Will add a counter here
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));
				// Pages
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
				// What's hot, We will add a counter here
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));

				// Recycle the typed array
				navMenuIcons.recycle();

				//mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

				// setting the nav drawer list adapter
				//adaptermenu = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
				mDrawerList.setAdapter(adaptermenu);*/

				// enabling action bar app icon and behaving it as toggle button
				getActionBar().setDisplayHomeAsUpEnabled(true);
				getActionBar().setHomeButtonEnabled(true);

				mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav menu toggle icon
						R.string.app_name, // nav drawer open - description for accessibility
						R.string.app_name // nav drawer close - description for accessibility
				)
					{
						public void onDrawerClosed(View view)
							{
								getActionBar().setTitle(mTitle);
								// calling onPrepareOptionsMenu() to show action bar icons
								invalidateOptionsMenu();
							}

						public void onDrawerOpened(View drawerView)
							{
								getActionBar().setTitle(mDrawerTitle);
								// calling onPrepareOptionsMenu() to hide action bar icons
								Sync2Manager.getSync2Manager().getCurrentVersion();
								invalidateOptionsMenu();
							}
					};
				mDrawerLayout.setDrawerListener(mDrawerToggle);
				/*//mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_L);
				if (savedInstanceState == null)
					{
						// on first time display view for first nav item
						displayView(0);
					}*/

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


        private void prepareListData() {

            // Adding child data
            List<String> childItems = new ArrayList<String>();
            childItems.add("Nieuwe Bericht");
            childItems.add("Informatie");
            childItems.add("Nieuws");

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();


             mList = new Utilities(this).getAllLocationList();
            if(mList!=null){
                for (int i = 0; i < mList.size(); i++) {
                    List<Locations> l = mList.get(i).getLocations();
                    if (mList.get(i).getLocations().size() > 0) {
                        for (int j = 0; j < mList.get(j).getLocations().size(); j++) {

                            listDataHeader.add(mList.get(i).getName() + " " + l.get(j).getName());

                            System.out.println(mList.get(i));

                        }
                    }
                }
                for (int c = 0; c < 3; c++) {
                    listDataChild.put(listDataHeader.get(c), childItems);



                }
            }

        }





		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.main, menu);
				return true;
			}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
			{
				// toggle nav drawer on selecting action bar app icon/title
				if (mDrawerToggle.onOptionsItemSelected(item))
					{
						return true;
					}
				// Handle action bar actions click
				switch (item.getItemId())
					{
					case R.id.action_settings:
						return true;
					default:
						return super.onOptionsItemSelected(item);
					}
			}

		/* *
		 * Called when invalidateOptionsMenu() is triggered
		 */
		@Override
		public boolean onPrepareOptionsMenu(Menu menu)
			{
				// if nav drawer is opened, hide the action items
				boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
				menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
				return super.onPrepareOptionsMenu(menu);
			}

		/**
		 * Diplaying fragment view for selected nav drawer list item
		 * */
		private void displayView(int childposition,int groupPosition)
			{
				// update the main content by replacing fragments
				Fragment fragment = null;
				switch (childposition)
					{
					case 0:
						fragment = new HomeFragment();

                        /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);*/
						break;
					case 1:
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();

                        fragment = new InfoFragment(childposition,groupPosition);
                        ft.addToBackStack(null);
                        ft.commit();

						break;
					case 2:
						Intent intent = new Intent(MainActivity.this, TwitterListActivity.class);
                        startActivity(intent);
						break;
					case 3:
						fragment = new CommunityFragment();
						break;
					case 4:
						fragment = new PagesFragment();
						break;
					case 5:
						fragment = new WhatsHotFragment();
						break;

					default:
						break;
					}

				if (fragment != null)
					{
						if (fragment instanceof HomeFragment)
							{
								main_include_layout.setVisibility(View.VISIBLE);
							}
						else
							{
								main_include_layout.setVisibility(View.GONE);

							}
						FragmentManager fragmentManager = getFragmentManager();
						fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
						// update selected item and title, then close the drawer
						mDrawerList.setItemChecked(childposition, true);
						mDrawerList.setSelection(childposition);
						setTitle(navMenuTitles[childposition]);
						mDrawerLayout.closeDrawer(mDrawerList);

					}
				else
					{
						// error in creating fragment
						Log.e("MainActivity", "Error in creating fragment");
					}
			}

		@Override
		public void setTitle(CharSequence title)
			{
				mTitle = title;
				getActionBar().setTitle(mTitle);
			}

		/**
		 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
		 */

		@Override
		public void onPostCreate(Bundle savedInstanceState)
			{
				super.onPostCreate(savedInstanceState);
				// Sync the toggle state after onRestoreInstanceState has occurred.
				mDrawerToggle.syncState();
			}

		@Override
		public void onConfigurationChanged(Configuration newConfig)
			{
				super.onConfigurationChanged(newConfig);
				// Pass any configuration change to the drawer toggls
				mDrawerToggle.onConfigurationChanged(newConfig);
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


		public void loadLocale()
			{
				String langPref = "Language";
				String language = utils.getSharedPrefValue(langPref);
				System.out.println("selected lang =" + language);
				changeLang(language);

			}

		public void changeLang(String lang)
			{
				if (lang.equalsIgnoreCase(""))
					return;
				Locale myLocale = new Locale(lang);
				Locale.setDefault(myLocale);
				android.content.res.Configuration config = new android.content.res.Configuration();
				config.locale = myLocale;
				getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
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
						// navImagesInfoList = utils.convertJSONToNavImagesInfoList(json.optJSONArray("data"));
						// userImagesAdapter = new UserImagesAdapter(MainActivity.this, navImagesInfoList);
						// postedImgsGridView.setAdapter(userImagesAdapter);
						// postedImgsGridView.setOnItemClickListener(new OnItemClickListener()
						// {
						//
						// @Override
						// public void onItemClick(AdapterView<?> parent, View view, int position, long id)
						// {
						// utils.showToastMessage("Item Clicked", MainActivity.this);
						// }
						// });
					}
				else
					{
						// No ImagesList available
					}

			}

		@Override
		public void whichItemClicked(int position)
			{
				displayView(position,sgroupPosition);

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
