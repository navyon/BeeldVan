package org.BvDH.CityTalk;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;


import org.BvDH.CityTalk.adapter.NavDrawerListAdapter;
import org.BvDH.CityTalk.fragments.CommunityFragment;
import org.BvDH.CityTalk.fragments.HomeFragment;
import org.BvDH.CityTalk.fragments.InfoFragment;
import org.BvDH.CityTalk.fragments.PagesFragment;
import org.BvDH.CityTalk.fragments.WhatsHotFragment;
import org.BvDH.CityTalk.model.LocationData;
import org.BvDH.CityTalk.model.Locations;
import org.BvDH.CityTalk.slide.app.SlidingActivity;
import org.BvDH.CityTalk.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daankrijnen on 23/09/14.
 */
public class BaseActivity extends SlidingActivity {



    ExpandableListAdapter listAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    public static ArrayList<LocationData> mList;
    public static ExpandableListView mDrawerList;

    public static View main_include_layout;

    int lastExpandedGroupPosition =-1;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    public static int mAspectRatioHeight;
    public static int mAspectRatioWidth;
    public static int mMargin;
    public static int mFontSize;

    public static int sgroupPosition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBehindContentView(R.layout.new_slider);
        getSlidingMenu().setBehindOffsetRes(R.dimen.slidingmenu_offset);

        main_include_layout = (View) findViewById(R.id.main_include_layout);

        mDrawerList = (ExpandableListView) findViewById(R.id.list_slidermenu);

        prepareListData();

//Slider Menu methods
				mTitle = mDrawerTitle = getTitle();

//        load slide menu items
				navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);


        listAdapter = new NavDrawerListAdapter(this, listDataHeader, listDataChild);

        setAspectRatio(0);

        mDrawerList.setAdapter(listAdapter);
        mDrawerList.setGroupIndicator(null);
        mDrawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                                                @Override
                                                public boolean onGroupClick(ExpandableListView parent, View v,
                                                                            int groupPosition, long id) {
                        /* Toast.makeText(getApplicationContext(),
                        "Group Clicked " + listDataHeader.get(groupPosition),
                         Toast.LENGTH_SHORT).show();*/
                                                    lastExpandedGroupPosition = groupPosition;
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
//                    System.out.println("Groupposition = "+sgroupPosition);
//                    displayView(childPosition,groupPosition);

                    setAspectRatio(sgroupPosition);

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

        }


    private void prepareListData() {

        // Adding child data
        List<String> childItems = new ArrayList<String>();
        childItems.add("Nieuw Bericht");
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
						Intent intent = new Intent(this, TwitterListActivity.class);
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
						toggle();

					}
				else
					{
						// error in creating fragment
						Log.e("MainActivity", "Error in creating fragment");
					}
			}
    public void setAspectRatio(int i){
        mAspectRatioHeight = mList.get(i).getLocations().get(0).getAspectRatioHeight();
        mAspectRatioWidth = mList.get(i).getLocations().get(0).getAspectRatioWidth();
        mMargin = mList.get(i).getLocations().get(0).getHorizontalTextInset();
        mFontSize = mList.get(i).getLocations().get(0).getFontSize();
        System.out.println("height = "+mAspectRatioHeight+ " Width = "+ mAspectRatioWidth);
    }
}
