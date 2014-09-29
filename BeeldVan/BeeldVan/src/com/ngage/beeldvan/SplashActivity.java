package com.ngage.beeldvan;

import org.BvDH.CityTalk.R;

import com.ngage.beeldvan.asynctasks.Sync2Manager;
import com.ngage.beeldvan.model.CityData;
import com.ngage.beeldvan.model.ImageLoader;
import com.ngage.beeldvan.model.Locations;
import com.ngage.beeldvan.utilities.RESTClient;
import com.ngage.beeldvan.utilities.SportanStringUtil;
import com.ngage.beeldvan.utilities.Utilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Text;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.google.myjson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements LocationListener, Animation.AnimationListener
{
    Utilities utils;
    Sync2Manager s;
    Location mLocation;
    LocationManager mLocationManager;
    public static List<Locations> mLocationList;
    String baseUrl = "http://beeldvan.nu/";
    ImageView cityImage;
    TextView LocationTitleTv;
    TextView cityTitleTv;
    Animation fadeIn;

    public static ArrayList<CityData> mCityList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_new);
        utils = new Utilities(this);
        mLocationList = new ArrayList<Locations>();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        s = new Sync2Manager(this);
        s.getSync2Manager().getCurrentVersion();
        cityImage = (ImageView) findViewById(R.id.SplashImage);
        cityTitleTv = (TextView) findViewById(R.id.SplashCityName);
        LocationTitleTv = (TextView) findViewById(R.id.SplashScreenName);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setAnimationListener(this);

//        new MyTask().execute();
    }

    @Override
    protected void onResume(){
        super.onResume();




        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    private ArrayList<CityData> CityDataList;
    public void startLocationChecks(){
        CityDataList = utils.getAllCitiesList();

        // start location check.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        mLocationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());

    }

    @Override
    public void onLocationChanged(Location location)
    {

        mLocation = location;
        System.out.println(mLocation);

        //fill temp list
        for(int i = 0; i < CityDataList.size(); i++){
            List<Locations> l = CityDataList.get(i).getLocations();
            if(l.size() > 0) {
                for (int j = 0; j < l.size(); j++) {
                    mLocationList.add(l.get(j));
                }
            }
        }


        // ImageLoader class instance
        int loader = R.drawable.loader;
        String cityTitle = "";
        String screenTitle = "";
        String image_url = null;


        //order them on distance
        mLocationList = utils.setDistances(mLocationList, mLocation);

        for(int i = 0; i < mLocationList.size(); i++){
            System.out.println("#"+i+" = "+ mLocationList.get(i).getName());
        }

        for(int i = 0; i < CityDataList.size(); i++){
            List<Locations> l = CityDataList.get(i).getLocations();
            if(l.size() > 0) {
                for (int j = 0; j < l.size(); j++) {
                    if(mLocationList.get(0).getLid() == l.get(j).getLid()){
                        System.out.println("closest = " + l.get(j).getName());
                        utils.setSelectedLocation(this, l.get(j));
                        image_url = baseUrl+l.get(j).getSplashImageLocation();
                        cityTitle = CityDataList.get(i).getName();
                        screenTitle = l.get(j).getName();
                    }
                }
            }
        }

        Typeface fontHelv = Typeface.createFromAsset(getAssets(), "fonts/HelveticaBold.ttf");
        Typeface fontRobLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        cityTitleTv.setTypeface(fontHelv);
        LocationTitleTv.setTypeface(fontRobLight);
        //show splashimage and screen name
        ImageLoader imgLoader = new ImageLoader(this);
        imgLoader.DisplayImage(image_url, loader, cityImage);
        cityTitleTv.setText(cityTitle);
        LocationTitleTv.setText(screenTitle);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //start animation
                cityTitleTv.startAnimation(fadeIn);
                cityTitleTv.setVisibility(View.VISIBLE);
                LocationTitleTv.startAnimation(fadeIn);
                LocationTitleTv.setVisibility(View.VISIBLE);
                cityImage.startAnimation(fadeIn);
                cityImage.setVisibility(View.VISIBLE);
            }
        }, 2000);

        mLocationManager.removeUpdates(this); //might be obsolete
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

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == fadeIn){
            //wait 3 seconds and go to main
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, 5000);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}