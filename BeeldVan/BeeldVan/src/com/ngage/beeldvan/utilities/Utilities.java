package com.ngage.beeldvan.utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ngage.beeldvan.SplashActivity;
import com.ngage.beeldvan.model.CityData;
import com.ngage.beeldvan.model.Locations;
import com.ngage.beeldvan.model.NavImagesInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.google.myjson.reflect.TypeToken;
import com.ngage.beeldvan.myApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

public class Utilities
{
    Context context;

    public Utilities( Context context )
    {
        this.context = context;
    }

    public boolean isNetWorkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public String getCurrentVersion()
    {
        SharedPreferences prefs = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String savedversion = prefs.getString("currentVersion", "");
        return savedversion;
    }
    public void saveCurrentVersion(String response)
    {
        SharedPreferences sp = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("currentVersion", response);
        editor.commit();

    }

    public void setAllLocations(String response)
    {
        SharedPreferences sp = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("allLocations", response);
        editor.commit();

    }

    public String getAllLocations()
    {
        SharedPreferences prefs = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String savedLocations = prefs.getString("allLocations", "");
        return savedLocations;
    }

    public ArrayList<CityData> getAllCitiesList()
    {

        String callbackJson = getAllLocations();

        Gson gson = new GsonBuilder().serializeNulls().create();
        Type collectionType = new TypeToken<List<CityData>>()
        {
        }.getType();
        ArrayList<CityData> cityDatas = gson.fromJson(callbackJson, collectionType);
        System.out.println();
        return cityDatas;
    }

    public void saveValueToSharedPrefs(String key, String value)
    {
        SharedPreferences sp = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();

    }

    public String getSharedPrefValue(String key)
    {
        SharedPreferences prefs = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(key, "");
        return language;
    }

    public Typeface loadTypeFace(int WHICH_TYPE_FACE)
    {
        Typeface typeFace;
        switch (WHICH_TYPE_FACE)
        {
            case 0:// font regular
                typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
                break;
            case 1: // font light
                typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
                break;
            default:
                typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");

                break;

        }
        return typeFace;
    }

    public void startNewActivity(Class<?> activityName)
    {
        context.startActivity(new Intent(context, activityName));
    }

    public void printStactTrace(Exception e)
    {
        e.printStackTrace();

    }

    public void printLog(String TAG, String msg)
    {
        Log.e(TAG, msg);
    }

    public List<NavImagesInfo> convertJSONToNavImagesInfoList(JSONArray jsonArray)
    {

        List<NavImagesInfo> imagesInfoList = new ArrayList<NavImagesInfo>();
        try
        {
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject json = jsonArray.getJSONObject(i);

                NavImagesInfo info = new NavImagesInfo();
                info.setImageUrl(json.optString("imageurl"));
                info.setOnline(json.optBoolean("isOnline"));
                imagesInfoList.add(info);
            }
        }
        catch (Exception e)
        {
            printStactTrace(e);
        }

        return imagesInfoList;
    }

    public void showToastMessage(String message, Context context)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public List<Locations> setDistances(List<Locations> locationList, Location crntLocation)
    {
        for (int i = 0; i < locationList.size(); i++)
        {
            double lat = locationList.get(i).getLatitude();
            double lng = locationList.get(i).getLongitude();
            System.out.println(locationList.get(i).getName()+"lat = "+lat);
            System.out.println(locationList.get(i).getName()+"lng = "+lng);
            Location loc = new Location("");
            loc.setLatitude(lat);
            loc.setLongitude(lng);

            locationList.get(i).setDistance(loc.distanceTo(crntLocation));
        }
        Collections.sort(locationList, new CompareToSort());
        return locationList;
    }

    // returns groupposition from an Location
    public int getPositionFromLoc(Locations loc) {
        int position = 0;
        int n = 0;

        ArrayList<CityData> cities = getAllCitiesList();
        if(cities !=null){
            for (int i = 0; i < cities.size(); i++) {
                List<Locations> l = cities.get(i).getLocations();
                if (l.size() > 0) {
                    for (int j = 0; j < l.size(); j++) {
                        if (l.get(j).getLid() == loc.getLid()) {
                            System.out.println("lid " + loc.getLid()+ " = " + l.get(j).getName());
                            position = n;
                        }
                        n++; // count number of screens
                    }
                }
            }

        } return position;
    }

    public Locations getLocFromPosition(int position) {
        Locations loc = null;
        int n = 0;

        ArrayList<CityData> cities = getAllCitiesList();
        if(cities !=null){
            for (int i = 0; i < cities.size(); i++) {
                List<Locations> l = cities.get(i).getLocations();
                if (l.size() > 0) {
                    for (int j = 0; j < l.size(); j++) {
                        if (n == position) {
                            System.out.println("position" + n + " = " + l.get(j).getName());
                            loc = l.get(j);
                        }
                        n++; // count number of screens
                    }
                }
            }

        } return loc;
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }


    //get screen width in pixels (minus the 10dp margin) and calculate correct font size
    public static float getFontSize(float w, Locations l){


        float width = w;
        //get font size from selected screen
        float font = (float) l.getFontSize();
        //get width from selected screen
        float lWidth = (float) l.getAspectRatioWidth();

        //calculate ratio value
        float r = font/lWidth;

        //set fontsize (screenpixels * ratio)
        float fontSize = (width * r);
        System.out.println("fontsize = " + fontSize);
        return fontSize;
    }

    public static int getMarginSize(float w, Locations l){

        float width = w;

        float lWidth = (float) l.getAspectRatioWidth();
        float lMargin = (float) l.getHorizontalTextInset();

        float r = lMargin/lWidth;
        int margin = (int)(width * r);
        System.out.println("margin = " + margin);
        return margin;
    }

    public static int getPreviewHeight(float w, Locations l){

        float width = w;
        float height;
        //todo add LID width etc.
        float lWidth = (float) l.getAspectRatioWidth();
        float lHeight = (float) l.getAspectRatioHeight();

        height = (width/lWidth)*lHeight;

        return (int)height;
    }

    public float getScreenWidth(Activity activityContext){
        float width;

        Display display = activityContext.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Resources r = activityContext.getResources();


        float marginpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        width = size.x - marginpx;

        return width;
    }

    //get current selected location
    public Locations getSelectedLocation(Activity activityContext){
        final myApplication globalVariable = (myApplication)  activityContext.getApplication();
        return globalVariable.getSelectedLocation();
    }
    //save current selected location
    public void setSelectedLocation(Activity activityContext, Locations loc){
        final myApplication globalVariable = (myApplication)  activityContext.getApplication();
        globalVariable.setSelectedLocation(loc);
    }

}