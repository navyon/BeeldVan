// is it possible to make a class from the crop activity? so we can reuse @preview activity?

package com.ngagemedia.beeldvan;

import java.util.*;

import android.app.*;
import android.content.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.ngagemedia.beeldvan.adapter.NavDrawerListAdapter;
import com.ngagemedia.beeldvan.asynctasks.GetImagesAsyncTask;
import com.ngagemedia.beeldvan.fragments.*;
import com.ngagemedia.beeldvan.fragments.InfoFragment;
import com.ngagemedia.beeldvan.interfaces.ImageLoadInterface;
import com.ngagemedia.beeldvan.interfaces.ListItemClickedInterface;
import com.ngagemedia.beeldvan.model.*;
import com.ngagemedia.beeldvan.utilities.Utilities;

import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity implements ImageLoadInterface, ListItemClickedInterface {
    // Sliding menu objects
    public static DrawerLayout mDrawerLayout;
    public static ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    ExpandableListAdapter listAdapter;
    List<Integer> listDataLid;
    List<Integer> listDataCid;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;

    public static final String TAG = "MainActivity";
    public static String TEMP_PHOTO_FILE_NAME;



    Utilities utils;

    GridView postedImgsGridView;
    public static String imageLocation;
    int lastExpandedGroupPosition = -1;
    FragmentManager fm1 = getFragmentManager();
    Fragment fragment = null;
    public static int sgroupPosition;

    Locations screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_new);
        utils = new Utilities(this);
        postedImgsGridView = (GridView) findViewById(R.id.postedImgsGridView);


        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.list_slidermenu);

        // preparing list data
        prepareListData();


        if (savedInstanceState == null)
        {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            String tag = "HomeFragment";
            fragment = new HomeFragment();
            ft.addToBackStack(tag);
            ft.add(R.id.frame_container, fragment, tag).commit();
        }


        listAdapter = new NavDrawerListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        mDrawerList.setAdapter(listAdapter);
        mDrawerList.setGroupIndicator(null);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    Log.d("drawer", "should hide keyboard");
                invalidateOptionsMenu();
            }
        };

        loadImagesList();
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(utils.getSelectedLocation(this) !=null) {
            screen = utils.getSelectedLocation(this);
        } else {
            mDrawerLayout.openDrawer(MainActivity.mDrawerList);
        }

        // Listview Group click listener
        mDrawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                        /* Toast.makeText(getApplicationContext(),
                        "Group Clicked " + listDataHeader.get(groupPosition),
                         Toast.LENGTH_SHORT).show();*/

                lastExpandedGroupPosition = groupPosition;
                String tag = "GenInfoFragment";
                if (listDataHeader.size() - 1 == groupPosition) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                    fragment = new GenInfoFragment();
                    ft.replace(R.id.frame_container, fragment, tag);
                    ft.addToBackStack(null);
                    ft.commit();
                }
                return false;
            }
        });

        // Listview Group expanded listener
        mDrawerList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (mDrawerList.getCount() != groupPosition) {
                    for (int i = 0; i < mDrawerList.getCount(); i++) {
                        if (i != groupPosition) {
                            mDrawerList.collapseGroup(i);
                        }
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
                //save selected screen
                utils.setSelectedLocation(MainActivity.this, utils.getLocFromLid(listDataLid.get(sgroupPosition)));
                screen = utils.getSelectedLocation(MainActivity.this);
                displayView(childPosition, groupPosition);



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


        mDrawerLayout.setDrawerListener(mDrawerToggle);
                /*//mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_L);
                if (savedInstanceState == null)
					{
						// on first time display view for first nav item
						displayView(0);
					}*/


    }

    @Override
    protected void onStop() {
        super.onStop();
//        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void loadImagesList() {
        new GetImagesAsyncTask(MainActivity.this, this).execute();
    }

    public void openMenu(){
        if(!mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.openDrawer(mDrawerList);
        }
    }

    private void prepareListData() {

        // Adding child data
        List<String> childItems = new ArrayList<String>();
        childItems.add("Nieuw Bericht");
        childItems.add("Informatie");
        childItems.add("Nieuws");

        listDataHeader = new ArrayList<String>();
        listDataLid = new ArrayList<Integer>();
        listDataCid = new ArrayList<Integer>();
        listDataChild = new HashMap<String, List<String>>();

        ArrayList<CityData> cities = utils.getAllCitiesList();
        if (cities != null) {
            for (int i = 0; i < cities.size(); i++) {
                List<Locations> l = cities.get(i).getLocations();
                if (l.size() > 0) {
                    for (int j = 0; j < l.size(); j++) {
                        listDataHeader.add(cities.get(i).getName() + " " + l.get(j).getName());
                        listDataLid.add(l.get(j).getLid());
                    }
                }
            }
            listDataHeader.add("Informatie over de App");
            for (int i = 0; i < listDataHeader.size() - 1; i++) {
                listDataChild.put(listDataHeader.get(i), childItems);
            }
            listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), new ArrayList<String>());

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        menu.findItem(R.id.action_settings).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int childposition, int groupPosition) {
        // update the main content by replacing fragments
        String tag = "fragment";
        switch (childposition) {
            case 0:
                fragment = new HomeFragment();
                tag = "HomeFragment";
                break;
            case 1:
                fragment = new InfoFragment();
                tag = "InfoFragment";
                break;
            case 2:
                fragment = new TwitterFragment();
                tag = "TwitterFragment";
                break;
            case 3:
                break;
            default:
                break;
        }
        Fragment mHomeFragment = getFragmentManager().findFragmentByTag("HomeFragment");
        Fragment mInfoFragment = getFragmentManager().findFragmentByTag("InfoFragment");
        Fragment mTwitterFragment = getFragmentManager().findFragmentByTag("TwitterFragment");
        Fragment mFinalFragment = getFragmentManager().findFragmentByTag("FinalFragment");


        if(fragment != null){
            if ((mHomeFragment != null && mHomeFragment.isVisible()) || (mInfoFragment != null && mInfoFragment.isVisible()) || (mTwitterFragment != null && mTwitterFragment.isVisible()) || (mFinalFragment != null && mFinalFragment.isVisible())) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                ft.addToBackStack(null);
                ft.replace(R.id.frame_container, fragment, tag).commit();
                // update selected item and title, then close the drawer
                mDrawerList.setItemChecked(childposition, true);
                mDrawerList.setSelection(childposition);
                setTitle(navMenuTitles[childposition]);
                mDrawerLayout.closeDrawer(mDrawerList);
            } else{
                final String tag_ = tag;
                final int child_ = childposition;
                Log.d("Menu", tag_);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // set title
                alertDialogBuilder.setTitle(R.string.message_alert_title);

                // set dialog message
                alertDialogBuilder
                        .setMessage(R.string.message_alert_text)
                        .setCancelable(false)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FragmentManager fm = getFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                                ft.addToBackStack(null);
                                ft.replace(R.id.frame_container, fragment, tag_).commit();
                                // update selected item and title, then close the drawer
                                mDrawerList.setItemChecked(child_, true);
                                mDrawerList.setSelection(child_);
                                setTitle(navMenuTitles[child_]);
                                mDrawerLayout.closeDrawer(mDrawerList);
                            }
                        })
                        .setNegativeButton("Nee", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                mDrawerLayout.closeDrawer(mDrawerList);
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }
        else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("state", "onDestroy");
    }


    //started to implement image grid
    @Override
    public void imgListJSONCallback(JSONObject json) {
        if (json != null && json.optString("status").equals("OK")) {

        } else {
            // No ImagesList available
        }
    }

    @Override
    public void whichItemClicked(int position) {
        displayView(position, sgroupPosition);
    }


}
