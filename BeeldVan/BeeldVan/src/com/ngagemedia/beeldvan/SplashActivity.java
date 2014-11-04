package com.ngagemedia.beeldvan;

import com.ngagemedia.beeldvan.lazyloader.LazyImageLoader;
import com.ngagemedia.beeldvan.model.CityData;
import com.ngagemedia.beeldvan.R;


import com.ngagemedia.beeldvan.asynctasks.Sync2Manager;
import com.ngagemedia.beeldvan.model.ImageLoader;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.utilities.getLocation;
import com.ngagemedia.beeldvan.views.Rotate3dAnimation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
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
    Animation fadeIn;
    ScaleAnimation skewYsize;
    Rotate3dAnimation flipTopAnim;
    Rotate3dAnimation flipBottomAnim;
    Rotate3dAnimation skew;
    AnimationSet finalAnimSet;
    LazyImageLoader imageLoader;
    Handler handler;
    TextView topLogo;
    TextView bottomLogo;
    TextView fixedTop, fixedBottom;
    Typeface fontHelv;
    boolean locationSet, changeText;
    String cityTitle;
    long animSpeed;
    float animDepth;
    int animNumCount;


    public static ArrayList<CityData> mCityList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        animSpeed = 70;
        animDepth = 3.0f;
        animNumCount = 0;
        locationSet = changeText = false;
        imageLoader = new LazyImageLoader(this);
        handler = new Handler();
        myLocation = new getLocation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_new);
        utils = new Utilities(this);
        cityImage = (ImageView) findViewById(R.id.SplashImage);
        LocationTitleTv = (TextView) findViewById(R.id.SplashScreenName);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setAnimationListener(this);
        fadeIn.setDuration(2000);

        fontHelv = Typeface.createFromAsset(getAssets(), "fonts/HelveticaBold.ttf");
        topLogo = (TextView) findViewById(R.id.toplogo);
        bottomLogo = (TextView) findViewById(R.id.bottomlogo);
        fixedBottom = (TextView) findViewById(R.id.staticbottom);
        fixedTop = (TextView) findViewById(R.id.statictop);
        topLogo.setTypeface(fontHelv);
        bottomLogo.setTypeface(fontHelv);
        fixedTop.setTypeface(fontHelv);
        fixedBottom.setTypeface(fontHelv);
        finalAnimSet = new AnimationSet(false);

    }

    @Override
    protected void onResume(){
        super.onResume();
        mLocationList = new ArrayList<Locations>();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (networkInfo != null && networkInfo.isConnected()) {
            s = new Sync2Manager(this);
            s.getSync2Manager().getCurrentVersion();
        } else {
            Log.v("SplashActivity", "No network connection available.");
            //check location anyways
            if(utils.getAllCitiesList()!= null) {
                startLocationChecks();
            } else {
                //kill app, needs notice for user
                finish();
            }
        }
    }

    private ArrayList<CityData> CityDataList;
    
    public void startLocationChecks(){
        CityDataList = utils.getAllCitiesList();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Location", "No location found, skipping");
                startMainActivity();
            }
        }, 40000);
        Log.d("Location", "checking normal location");

        // start location check.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,Looper.getMainLooper());
//        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,Looper.getMainLooper());
        mLocationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());


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


            Typeface fontRobLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
            LocationTitleTv.setTypeface(fontRobLight);

            LocationTitleTv.setText(cityTitle + " " + screenTitle);
            //show splashimage and screen name
//                imageLoader.DisplayImage(image_url, loader, cityImage);
            imageLoader.DisplayImage(image_url,cityImage,cityImage.getWidth(), cityImage.getHeight());
            imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
                @Override
                public void onImageLoad() {
                    //start animation
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
        cityTitle = "";
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
        LocationTitleTv.setTypeface(fontRobLight);
        LocationTitleTv.setText(cityTitle + " " + screenTitle);

        //show splashimage and screen name
        imageLoader.DisplayImage(image_url,cityImage,cityImage.getWidth(), cityImage.getHeight());
        imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
            @Override
            public void onImageLoad() {
                //start animation

                LocationTitleTv.startAnimation(fadeIn);
                LocationTitleTv.setVisibility(View.VISIBLE);
                cityImage.startAnimation(fadeIn);
                cityImage.setVisibility(View.VISIBLE);
            }
        });
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
        if(animation == flipTopAnim){
            topLogo.setVisibility(View.VISIBLE);
        }
        if(animation == flipBottomAnim){
            bottomLogo.setVisibility(View.VISIBLE);
        }
        if(animation == skew){
            topLogo.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == fadeIn){
            locationSet = true;
            //wait 3 seconds and go to main
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMainActivity();
                }
            }, 6000);
        }
        if(animation == flipTopAnim) {
            if(locationSet){
                bottomLogo.setText(cityTitle);
            }
            bottomLogo.startAnimation(flipBottomAnim);
            topLogo.setVisibility(View.INVISIBLE);
        }
        if(animation == flipBottomAnim){
            bottomLogo.setVisibility(View.INVISIBLE);
            if(locationSet){
                if(changeText) {
                    fixedBottom.setText(cityTitle);
                    if(animNumCount == 1) {
                        topLogo.startAnimation(finalAnimSet);
                    } else {
                        animNumCount++;
                        topLogo.startAnimation(flipTopAnim);
                    }
                } else{
                    changeText = true;
                    topLogo.startAnimation(flipTopAnim);
                }

            }
            else {
                animSpeed += 20;
                flipTopAnim.setDuration(animSpeed);
                flipBottomAnim.setDuration(animSpeed);
                topLogo.startAnimation(flipTopAnim);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        float cxtop = topLogo.getWidth()/2;
        float cytop = topLogo.getHeight()+5;
        flipTopAnim = new Rotate3dAnimation(-5,-90,cxtop,cytop,animDepth, false);
        flipTopAnim.setDuration(animSpeed);
        flipTopAnim.setAnimationListener(this);
        topLogo.startAnimation(flipTopAnim);
        float cxbot = bottomLogo.getWidth()/2;
        float cybot = -5;
        flipBottomAnim = new Rotate3dAnimation(90,5,cxbot,cybot,animDepth, false);
        flipBottomAnim.setDuration(animSpeed);
        flipBottomAnim.setAnimationListener(this);
        skew = new Rotate3dAnimation(0,-15,cxtop,cytop,animDepth,false);
        skew.setFillAfter(true);
        skew.setDuration(700);
        skewYsize = new ScaleAnimation(1,1,1,0.85f,cxtop,topLogo.getHeight());
        skewYsize.setDuration(700);
        skewYsize.setFillAfter(true);
        finalAnimSet.addAnimation(skew);
        finalAnimSet.addAnimation(skewYsize);
        finalAnimSet.setFillAfter(true);
    }
}