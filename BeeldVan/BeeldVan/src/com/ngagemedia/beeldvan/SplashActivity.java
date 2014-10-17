package com.ngagemedia.beeldvan;

import com.ngagemedia.beeldvan.lazyloader.LazyImageLoader;
import com.ngagemedia.beeldvan.model.CityData;
import com.ngagemedia.beeldvan.R;


import com.ngagemedia.beeldvan.asynctasks.Sync2Manager;
import com.ngagemedia.beeldvan.model.ImageLoader;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.utilities.getLocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements LocationListener, Animation.AnimationListener
{
    Utilities utils;
    Sync2Manager s;
    Location mLocation;
    getLocation myLocation;
    LocationManager mLocationManager;
    public static List<Locations> mLocationList;

    String baseUrl = "http://beeldvan.nu/";
    ImageView cityImage;
    TextView LocationTitleTv;
    TextView cityTitleTv;
    Animation fadeIn;
    ImageView locLoading;
    AnimationDrawable locLoadingAnim;
    LazyImageLoader imageLoader;
    Handler handler;
    public static ArrayList<CityData> mCityList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        imageLoader = new LazyImageLoader(this);
        handler = new Handler();
        myLocation = new getLocation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_new);
        utils = new Utilities(this);
        cityImage = (ImageView) findViewById(R.id.SplashImage);
        cityTitleTv = (TextView) findViewById(R.id.SplashCityName);
        LocationTitleTv = (TextView) findViewById(R.id.SplashScreenName);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setAnimationListener(this);
        locLoading = (ImageView) findViewById(R.id.locloader);
        locLoadingAnim = (AnimationDrawable) locLoading.getDrawable();
        locLoadingAnim.start();



    }

    @Override
    protected void onResume(){
        super.onResume();
        mLocationList = new ArrayList<Locations>();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        s = new Sync2Manager(this);
        s.getSync2Manager().getCurrentVersion();
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private ArrayList<CityData> CityDataList;
    
    public void startLocationChecks(){


        CityDataList = utils.getAllCitiesList();
//        findCurrentLocation();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Location", "getting last known location");
                mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }, 50000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Location", "No location found, skipping");
                startMainActivity();
            }
        }, 60000);
        Log.d("Location", "checking normal location");

        // start location check.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,Looper.getMainLooper());
        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,Looper.getMainLooper());


    }


    private void findCurrentLocation() {
        myLocation.getLocation(this, locationResult);
    }

    public getLocation.LocationResult locationResult = new getLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            // TODO Auto-generated method stub
            setLocations(location);

        }
    };

    public void setLocations(Location location){



        if (location != null) {
            mLocation = location;
            Log.d("location", mLocation.toString());

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
                Log.d("location", "#" + i + " = " + mLocationList.get(i).getName());
            }

            for(int i = 0; i < CityDataList.size(); i++){
                List<Locations> l = CityDataList.get(i).getLocations();
                if(l.size() > 0) {
                    for (int j = 0; j < l.size(); j++) {
                        if(mLocationList.get(0).getLid() == l.get(j).getLid()){
                            Log.d("location","closest = " + l.get(j).getName());
                            utils.setSelectedLocation(SplashActivity.this, l.get(j));
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
            cityTitleTv.setText(cityTitle);
            LocationTitleTv.setText(cityTitle + " " + screenTitle);
            //show splashimage and screen name
//                imageLoader.DisplayImage(image_url, loader, cityImage);
            imageLoader.DisplayImage(image_url,cityImage,cityImage.getWidth(), cityImage.getHeight());
            imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
                @Override
                public void onImageLoad() {
                    //start animation
                    locLoadingAnim.stop();
                    locLoading.setVisibility(View.GONE);
                    cityTitleTv.startAnimation(fadeIn);
                    cityTitleTv.setVisibility(View.VISIBLE);
                    LocationTitleTv.startAnimation(fadeIn);
                    LocationTitleTv.setVisibility(View.VISIBLE);
                    cityImage.startAnimation(fadeIn);
                    cityImage.setVisibility(View.VISIBLE);
                }
            });

        } else {
            startMainActivity();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {

        handler.removeCallbacksAndMessages(null);
        mLocation = location;
        Log.d("location", mLocation.toString());

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
            Log.d("location", "#" + i + " = " + mLocationList.get(i).getName());
        }

        for(int i = 0; i < CityDataList.size(); i++){
            List<Locations> l = CityDataList.get(i).getLocations();
            if(l.size() > 0) {
                for (int j = 0; j < l.size(); j++) {
                    if(mLocationList.get(0).getLid() == l.get(j).getLid()){
                        Log.d("location","closest = " + l.get(j).getName());
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
        LocationTitleTv.setText(cityTitle + " " + screenTitle);
        cityTitleTv.setText(cityTitle);
        //show splashimage and screen name
        imageLoader.DisplayImage(image_url,cityImage,cityImage.getWidth(), cityImage.getHeight());
        imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
            @Override
            public void onImageLoad() {
                //start animation
                locLoadingAnim.stop();
                locLoading.setVisibility(View.GONE);
                cityTitleTv.startAnimation(fadeIn);
                cityTitleTv.setVisibility(View.VISIBLE);
                LocationTitleTv.startAnimation(fadeIn);
                LocationTitleTv.setVisibility(View.VISIBLE);
                cityImage.startAnimation(fadeIn);
                cityImage.setVisibility(View.VISIBLE);
            }
        });
//
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //start animation
//                locLoadingAnim.stop();
//                locLoading.setVisibility(View.GONE);
//                cityTitleTv.startAnimation(fadeIn);
//                cityTitleTv.setVisibility(View.VISIBLE);
//                LocationTitleTv.startAnimation(fadeIn);
//                LocationTitleTv.setVisibility(View.VISIBLE);
//                cityImage.startAnimation(fadeIn);
//                cityImage.setVisibility(View.VISIBLE);
//            }
//        }, 2000);
    }


    public void startMainActivity(){
        mLocationManager.removeUpdates(this);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
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
//                    imageLoader.clearCache();
                    startMainActivity();
                }
            }, 5000);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}