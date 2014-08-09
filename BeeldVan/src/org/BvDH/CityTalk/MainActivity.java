//is it possible to make a class from the crop activity? so we can reuse @preview activity?

package org.BvDH.CityTalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.BvDH.CityTalk.adapter.CropOptionAdapter;
import org.BvDH.CityTalk.adapter.NavDrawerListAdapter;
import org.BvDH.CityTalk.adapter.UserImagesAdapter;
import org.BvDH.CityTalk.asynctasks.GetImagesAsyncTask;
import org.BvDH.CityTalk.fragments.CommunityFragment;
import org.BvDH.CityTalk.fragments.FindPeopleFragment;
import org.BvDH.CityTalk.fragments.HomeFragment;
import org.BvDH.CityTalk.fragments.PagesFragment;
import org.BvDH.CityTalk.fragments.PhotosFragment;
import org.BvDH.CityTalk.fragments.WhatsHotFragment;
import org.BvDH.CityTalk.interfaces.ImageLoadInterface;
import org.BvDH.CityTalk.interfaces.ListItemClickedInterface;
import org.BvDH.CityTalk.model.CropOption;
import org.BvDH.CityTalk.model.NavDrawerItem;
import org.BvDH.CityTalk.model.NavImagesInfo;
import org.BvDH.CityTalk.utilities.Utilities;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, ImageLoadInterface, ListItemClickedInterface
{
	private static Uri mImageCaptureUri;
	private Uri tempURI;
	// Sliding menu objects
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

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
	View main_include_layout;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		// setContentView(R.layout.main);
		setContentView(R.layout.main_new);
		utils = new Utilities(this);
		main_include_layout = (View) findViewById(R.id.main_include_layout);
		postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);
		loadLocale();

		// load fonts
		// Typeface fontRegular = utils.loadTypeFace(0);
		// Typeface fontLight = utils.loadTypeFace(1);

		camaerIconImg = (ImageView) findViewById(R.id.camaerIconImg);
		camaerIconImg.setOnClickListener(this);

		// Slider Menu methods
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

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

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adaptermenu = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adaptermenu);

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
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null)
		{
			// on first time display view for first nav item
			displayView(0);
		}

		loadImagesList();
	}

	private void loadImagesList()
	{
		new GetImagesAsyncTask(MainActivity.this, this).execute();

	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			// display view for selected nav drawer item
			displayView(position);
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
	private void displayView(int position)
	{
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position)
		{
		case 0:
			fragment = new HomeFragment();
			break;
		case 1:
			fragment = new FindPeopleFragment();
			break;
		case 2:
			fragment = new PhotosFragment();
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
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
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
		final String[] items = new String[] { getString(R.string.CapturePhoto), getString(R.string.ChoosefromGallery) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.ChooseaTask));
		builder.setAdapter(adapter, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{ // pick from
			  // camera
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
				else
				{ // pick from file
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, getString(R.string.ChooseApp)), PICK_FROM_FILE);
				}
			}
		});
		builder.show();
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
			// mImageCaptureUri = data.getData();
			doCrop();

			break;

		case PICK_FROM_FILE:
			// mImageCaptureUri = data.getData();

			doCrop();

			break;

		case CROP_FROM_CAMERA:
			// Bundle extras = data.getExtras();
			Intent intent = new Intent(MainActivity.this, MessageActivity.class);
			if (tempURI != null)
			{

				// The photo path is sent to the message activity
				intent.putExtra("imagePath", tempURI.getPath());

				startActivity(intent);
			}

			File temp = new File(mImageCaptureUri.getPath());

			if (temp.exists())
				temp.delete(); // *//*//*// How does this work?

			break;
		}
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
			Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

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
			intent.putExtra("return-data", false); // don't send data back to
												   // prevent
												   // transactionTooLarge
			intent.putExtra(MediaStore.EXTRA_OUTPUT, tempURI); // save to file!

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
				// Cancels the Image Capture
				builder.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{

						if (mImageCaptureUri != null)
						{
							getContentResolver().delete(mImageCaptureUri, null, null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
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

		default:
			break;
		}

	}

	@Override
	public void imgListJSONCallback(JSONObject json)
	{
		if (json != null && json.optString("status").equals("OK"))
		{
			navImagesInfoList = utils.convertJSONToNavImagesInfoList(json.optJSONArray("data"));
			userImagesAdapter = new UserImagesAdapter(MainActivity.this, navImagesInfoList);
			postedImgsGridView.setAdapter(userImagesAdapter);
			postedImgsGridView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					utils.showToastMessage("Item Clicked", MainActivity.this);
				}
			});
		}
		else
		{
			// No ImagesList available
		}

	}

	@Override
	public void whichItemClicked(int position)
	{
		displayView(position);

	}

}
