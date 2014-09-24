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
import org.BvDH.CityTalk.adapter.CropOptionAdapter;
import org.BvDH.CityTalk.adapter.NavDrawerListAdapter;
import org.BvDH.CityTalk.adapter.UserImagesAdapter;
import org.BvDH.CityTalk.asynctasks.GetImagesAsyncTask;
import org.BvDH.CityTalk.asynctasks.Sync2Manager;
import org.BvDH.CityTalk.fragments.*;
import org.BvDH.CityTalk.fragments.InfoFragment;
import org.BvDH.CityTalk.interfaces.ImageLoadInterface;
import org.BvDH.CityTalk.interfaces.ListItemClickedInterface;
import org.BvDH.CityTalk.model.*;
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

public class MainActivity extends Activity implements OnClickListener,ImageLoadInterface, ListItemClickedInterface, LocationListener
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
        FragmentManager fm1 = MainActivity.this.getFragmentManager();
        Fragment fragment = null;
        public static int sgroupPosition;

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
                setContentView(R.layout.main_new);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				// setContentView(R.layout.main);

				utils = new Utilities(this);
				main_include_layout = (View) findViewById(R.id.main_include_layout);
				postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);
				TextView overslaanTV = (TextView) findViewById(R.id.overslaanTv);
				overslaanTV.setOnClickListener(this);

				TextView doneTV = (TextView) findViewById(R.id.doneTV);
				doneTV.setOnClickListener(this);
				loadLocale();

				camaerIconImg = (ImageView) findViewById(R.id.camaerIconImg);

				camaerIconImg.setOnClickListener(this);



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
                        for (int j = 0; j < mList.get(i).getLocations().size(); j++) {

                            listDataHeader.add(mList.get(i).getName() + " " + l.get(j).getName());

//                            System.out.println(mList.get(i));

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
		void checkDir()
			{
				File dir = new File(Environment.getExternalStorageDirectory() + "/bvdh");
				if (!dir.exists())
					{
						boolean result = dir.mkdir();
						if (result)
							{
								System.out.println("created a DIR");
							}
					}
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
										Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
										String fileName = getRandomFileName();
										mImageCaptureUri = Uri.fromFile(new File(fileName));

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
									{
										// pick from gallery
										Intent pickImageIntent = new Intent();
										pickImageIntent.setType("image/*");
										pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
										startActivityForResult(Intent.createChooser(pickImageIntent, getString(R.string.ChooseApp)), PICK_FROM_FILE);

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

		private String getRandomFileName()
			{
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);
				String fileName = Environment.getExternalStorageDirectory() + File.separator + String.valueOf(n) + "_beelvan.jpg";
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
					case PICK_FROM_CAMERA:
						try
							{
								doCrop();
							}
						catch (NullPointerException e)
							{
								// TODO: handle exception
							}
						break;

					case PICK_FROM_FILE:
						if (data != null)
							{
								mImageCaptureUri = data.getData();

								doCrop();
							}
						break;

					case CROP_FROM_CAMERA:
						if (data != null)
							{
								imagePath = mImageCaptureUri.getPath();
								final Bundle extras = data.getExtras();
								Bitmap picture = (Bitmap) data.getExtras().get("data");
								imageLocation = writeBitmap(picture);
								if (extras != null)
									{
										try
											{

												/*Intent intent = new Intent(MainActivity.this, MessageActivity.class);
												intent.putExtras(extras);
												intent.putExtra("imagePath", imagePath);
												startActivity(intent);*/


                                                FragmentTransaction ft1 = fm1.beginTransaction();
                                                fragment = new MessageFragment();

                                                extras.putString("imagePath", imagePath);
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

		// This is Crop Method.

		/**
		 * Method for apply Crop .
		 */
		private void doCrop()
			{
				try
					{

						final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
						Intent intent = new Intent("com.android.camera.action.CROP");
						intent.setType("image/*");
						List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

						int size = list.size();
						if (size == 0)
							{
								Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
								return;
							}
						else
							{
								intent.setData(mImageCaptureUri);
								intent.putExtra("outputX", 300);
								intent.putExtra("outputY", 300);
								intent.putExtra("aspectX", 1);
								intent.putExtra("aspectY", 1);
								intent.putExtra("scale", true);
								intent.putExtra("return-data", true);
								imagePath = mImageCaptureUri.getPath();
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
										AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogSlideAnim);
										builder.setTitle("Choose Crop App");
										builder.setAdapter(adapter, new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int item)
													{
														startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
													}
											});

										builder.setOnCancelListener(new DialogInterface.OnCancelListener()
											{
												public void onCancel(DialogInterface dialog)
													{
														if (mImageCaptureUri != null)
															{

																try
																	{
																		getContentResolver().delete(mImageCaptureUri, null, null);
																	}
																catch (Exception e)
																	{
																		utils.printStactTrace(e);
																	}
																mImageCaptureUri = null;
															}
													}
											});
										AlertDialog alert = builder.create();
										alert.show();
									}
							}
					}
				catch (Exception ex)
					{
						ex.printStackTrace();
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

								/*Intent intent = new Intent(MainActivity.this, MessageActivity.class);
								startActivity(intent);*/
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
