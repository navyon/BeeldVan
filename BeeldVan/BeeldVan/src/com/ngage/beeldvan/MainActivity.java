// is it possible to make a class from the crop activity? so we can reuse @preview activity?

package com.ngage.beeldvan;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.widget.*;

import org.BvDH.CityTalk.R;

import com.ngage.beeldvan.adapter.NavDrawerListAdapter;
import com.ngage.beeldvan.adapter.UserImagesAdapter;
import com.ngage.beeldvan.asynctasks.GetImagesAsyncTask;
import com.ngage.beeldvan.asynctasks.Sync2Manager;
import com.ngage.beeldvan.crop.CropImage;
import com.ngage.beeldvan.fragments.*;
import com.ngage.beeldvan.fragments.InfoFragment;
import com.ngage.beeldvan.interfaces.ImageLoadInterface;
import com.ngage.beeldvan.interfaces.ListItemClickedInterface;
import com.ngage.beeldvan.model.*;
import com.ngage.beeldvan.utilities.InternalStorageContentProvider;
import com.ngage.beeldvan.utilities.Utilities;
import org.json.JSONObject;

import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener,ImageLoadInterface, ListItemClickedInterface
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

		private ArrayList<NavDrawerItem> navDrawerItems;
		private NavDrawerListAdapter adaptermenu;

        public static final int REQUEST_CODE_GALLERY      = 0x1;
        public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
        public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;
        public static final String TAG = "MainActivity";
        public static String TEMP_PHOTO_FILE_NAME;
        public static String FOLDER_NAME;

        private File      mFileTemp;

		Utilities utils;
		ImageView camaerIconImg;

		UserImagesAdapter userImagesAdapter;
		List<NavImagesInfo> navImagesInfoList;
		GridView postedImgsGridView;
		public static View main_include_layout;
		String imagePath;
		public static String imageLocation;
        int lastExpandedGroupPosition =-1;
        FragmentManager fm1 = MainActivity.this.getFragmentManager();
        Fragment fragment = null;
        public static int sgroupPosition;

        Locations screen;

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
                setContentView(R.layout.main_new);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                utils = new Utilities(this);
                screen = utils.getSelectedLocation(this);
                sgroupPosition =  utils.getPositionFromLoc(utils.getSelectedLocation(this));
                System.out.println("group is "+ sgroupPosition);



				main_include_layout = (View) findViewById(R.id.main_include_layout);
				postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);
				TextView overslaanTV = (TextView) findViewById(R.id.overslaanTv);
				overslaanTV.setOnClickListener(this);

				TextView doneTV = (TextView) findViewById(R.id.doneTV);
				doneTV.setOnClickListener(this);


                //crop option implementation
                FOLDER_NAME = checkDir();

                TEMP_PHOTO_FILE_NAME = getRandomFileName();

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
                }
                else {
                    mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
                }


				camaerIconImg = (ImageView) findViewById(R.id.camaerIconImg);

				camaerIconImg.setOnClickListener(this);



				// Slider Menu methods
				mTitle = mDrawerTitle = getTitle();

				// load slide menu items
				navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

				// nav drawer icons from resources

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
                        utils.setSelectedLocation(MainActivity.this, utils.getLocFromPosition(sgroupPosition));
                        screen = utils.getSelectedLocation(MainActivity.this);
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
//				new MyTask().execute();

            }

		@Override
		protected void onStop()
			{
				super.onStop();
			}

		private void loadImagesList()
			{
				new GetImagesAsyncTask(MainActivity.this, this).execute();

			}


        private void prepareListData() {

            // Adding child data
            List<String> childItems = new ArrayList<String>();
            childItems.add("Nieuw Bericht");
            childItems.add("Informatie");
            childItems.add("Nieuws");

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();


//             mList = new Utilities(this).getAllLocationList();
            if(SplashActivity.mList!=null){
                for (int i = 0; i < SplashActivity.mList.size(); i++) {
                    List<Locations> l = SplashActivity.mList.get(i).getLocations();
                    if (SplashActivity.mList.get(i).getLocations().size() > 0) {
                        for (int j = 0; j < SplashActivity.mList.get(i).getLocations().size(); j++) {
                            listDataHeader.add(SplashActivity.mList.get(i).getName() + " " + l.get(j).getName());
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

				switch (childposition)
					{
					case 0:
						fragment = new HomeFragment();
						break;
					case 1:
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();

                        fragment = new InfoFragment(childposition,groupPosition);
                        ft.addToBackStack(null);
                        ft.commit();

						break;
					case 2:
                        fragment = new TwitterFragment();
						break;
					case 3:
						fragment = new FacebookFragment();
						break;
					case 4:
						fragment = new MessageFragment();
						break;
					case 5:
						fragment = new ConfirmFragment();
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
		protected void onPostCreate(Bundle savedInstanceState)
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
            intent.putExtra(CropImage.SCALE_UP_IF_NEEDED, true);
            intent.putExtra(CropImage.ASPECT_X, screen.getAspectRatioWidth());
            intent.putExtra(CropImage.ASPECT_Y, screen.getAspectRatioHeight());
            intent.putExtra(CropImage.OUTPUT_X, screen.getAspectRatioWidth());
            intent.putExtra(CropImage.OUTPUT_Y, screen.getAspectRatioHeight());
            System.out.println("aspect = " +screen.getAspectRatioHeight()+" "+screen.getAspectRatioWidth());

            startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
        }

		private String getRandomFileName()
			{
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);
				String fileName = FOLDER_NAME + File.separator + String.valueOf(n) + "_beeldvan.jpg";
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
				// if (resultCode != RESULT_OK)
				// return;

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

                                                FragmentTransaction ft1 = fm1.beginTransaction();
                                                fragment = new MessageFragment();

                                                extras.putString("imagePath", imagePath);
                                                extras.putBoolean("hasPhoto", true);
                                                ft1.replace(R.id.frame_container, fragment);
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

		String writeBitmap(Bitmap bmp)
			{
				String imgPath = getRandomFileName();
				File f = new File(imgPath);
				if (f.exists())
					f.delete();
				FileOutputStream out = null;
				try
					{
						f.createNewFile();
						out = new FileOutputStream(f);
						bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
					}
				catch (Exception e)
					{
						Toast.makeText(MainActivity.this, "Exception saving image", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
				finally
					{
						try
							{
								if (out != null)
									{
										out.close();
									}
							}
						catch (IOException e)
							{
								e.printStackTrace();
							}
					}
				return imgPath;
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

                                FragmentTransaction ft1 = fm1.beginTransaction();
                                fragment = new MessageFragment();
                                ft1.addToBackStack(null);

                                ft1.replace(R.id.frame_container, fragment);
                                ft1.commit();

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

	}
