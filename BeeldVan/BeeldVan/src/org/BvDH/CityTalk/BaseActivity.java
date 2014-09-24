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
import android.widget.Toast;


import org.BvDH.CityTalk.adapter.NavDrawerListAdapter;
import org.BvDH.CityTalk.fragments.CommunityFragment;
import org.BvDH.CityTalk.fragments.HomeFragment;
import org.BvDH.CityTalk.fragments.InfoFragment;
import org.BvDH.CityTalk.fragments.PagesFragment;
import org.BvDH.CityTalk.fragments.WhatsHotFragment;
import org.BvDH.CityTalk.model.LocationData;
import org.BvDH.CityTalk.model.Locations;
import org.BvDH.CityTalk.slide.SlidingMenu;
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
        getSlidingMenu().toggle();

        mDrawerList = (ExpandableListView) findViewById(R.id.list_slidermenu);

        prepareListData();


        //set selected screen
        final myApplication globalVariable = (myApplication) getApplication();
        sgroupPosition = globalVariable.getSelectedLocation();
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
                         Toast.makeText(getApplicationContext(),
                        "Group Clicked "+groupPosition,
                         Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(),
                                groupPosition + " Collapsed",
                                Toast.LENGTH_SHORT).show();

                }
            });

            // Listview on child click listener
            mDrawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                int groupPosition, int childPosition, long id) {
                    sgroupPosition = groupPosition;
                    displayView(childPosition,groupPosition);

                    setAspectRatio(sgroupPosition);
                    globalVariable.setSelectedLocation(sgroupPosition);

//                        Toast.makeText(
//                                getApplicationContext(),
//                                listDataHeader.get(groupPosition)
//                                        + " : "
//                                        + listDataChild.get(
//                                        listDataHeader.get(groupPosition)).get(
//                                        childPosition), Toast.LENGTH_SHORT)
//                                .show();
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
            for (int i = 0; i < mList.size(); i++) { //city size
                List<Locations> l = mList.get(i).getLocations(); //locations per city
                if (mList.get(i).getLocations().size() > 0) {
                    System.out.println("city = "+mList.get(i).getName());
                    System.out.println("#screens = "+l.size());//mList.get(i).getLocations().size());
                    for (int j = 0; j < mList.get(i).getLocations().size(); j++) {
                        System.out.println(mList.get(i).getName() + " " + l.get(j).getName());
                        listDataHeader.add(mList.get(i).getName() + " " + l.get(j).getName());
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
				switch (childposition)
					{
					case 0:

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
						break;
					case 1:
                        Intent infoIntent = new Intent(this, InfoFragment.class);
                        startActivity(infoIntent);


						break;
					case 2:
						Intent twitIntent = new Intent(this, TwitterListActivity.class);
                        startActivity(twitIntent);
						break;

					default:
						break;
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
