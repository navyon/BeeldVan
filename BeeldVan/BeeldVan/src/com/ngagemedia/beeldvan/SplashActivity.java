package com.ngagemedia.beeldvan;

import com.ngagemedia.beeldvan.lazyloader.LazyImageLoader;
import com.ngagemedia.beeldvan.model.CityData;


import com.ngagemedia.beeldvan.asynctasks.Sync2Manager;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.utilities.getLocation;
import com.ngagemedia.beeldvan.views.Rotate3dAnimation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
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

    // logo animation
    ScaleAnimation skewYsize; //needed to get the final animation part
    Rotate3dAnimation flipTopAnim;
    Rotate3dAnimation flipBottomAnim;
    Rotate3dAnimation skew;
    AnimationSet finalAnimSet;
    long animSpeed;
    long animSpeedMax;
    float animDiff;
    float animDepth;
    int animNumCount;
    TextView topLogo;
    TextView bottomLogo;
    TextView fixedTop, fixedBottom;
    View blurredView;
    String cityTitle; //title of city in logo

    //background image and texts with animation
    ImageView cityImage;
    TextView LocationTitleTv; //bottom bar title
    Animation fadeIn;
    LazyImageLoader imageLoader;

    Handler handler; //handler for starting mainactivity

    Typeface fontHelv; //correct font

    boolean locationSet, changeText;


    public static ArrayList<CityData> mCityList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_new);

        //set animation variables, rest is set at OnWindowFocusedChanged
        animSpeed = 100;
        animSpeedMax = 1000;
        animDepth = 3.0f;
        animDiff = 1.1f;
        animNumCount = 0;
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setAnimationListener(this);
        fadeIn.setDuration(2000);
        finalAnimSet = new AnimationSet(false);

        locationSet = changeText = false;
        imageLoader = new LazyImageLoader(this);
        handler = new Handler();
        myLocation = new getLocation();
        utils = new Utilities(this);

        //init views
        cityImage = (ImageView) findViewById(R.id.SplashImage);

        LocationTitleTv = (TextView) findViewById(R.id.SplashScreenName);
        topLogo = (TextView) findViewById(R.id.toplogo);
        bottomLogo = (TextView) findViewById(R.id.bottomlogo);
        fixedBottom = (TextView) findViewById(R.id.staticbottom);
        fixedTop = (TextView) findViewById(R.id.statictop);
        blurredView = findViewById(R.id.blurImage);


        int width = (int) utils.getScreenWidth(this);
        topLogo.setWidth(width/3);
        topLogo.setHeight(width/12);
        bottomLogo.setWidth(width/3);
        bottomLogo.setHeight(width/12);
        fixedTop.setWidth(width/3);
        fixedTop.setHeight(width/12);
        fixedBottom.setWidth(width/3);
        fixedBottom.setHeight(width/12);

        //load font
        fontHelv = Typeface.createFromAsset(getAssets(), "fonts/HelveticaBold.ttf");
        //set font
        topLogo.setTypeface(fontHelv);
        bottomLogo.setTypeface(fontHelv);
        fixedTop.setTypeface(fontHelv);
        fixedBottom.setTypeface(fontHelv);
    }

    @Override
    protected void onResume(){
        super.onResume();

        mLocationList = new ArrayList<Locations>();
        cityImage.setDrawingCacheEnabled(true);
        cityImage.buildDrawingCache(true);

        //init location check
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //internet check
        if (networkInfo != null && networkInfo.isConnected()) {
            s = new Sync2Manager(this);
            s.getSync2Manager().getCurrentVersion(); //check for new version
        } else {
            Log.v("SplashActivity", "No network connection available.");
            //no internet, check location anyways if we downloaded 'm before
            if(utils.getAllCitiesList()!= null) {
                startLocationChecks();
            } else {
                //kill app, needs notice for user
                finish();
            }
        }
    }

    private ArrayList<CityData> CityDataList;


    //this is called after API update getCurrentVersion()
    public void startLocationChecks(){
        CityDataList = utils.getAllCitiesList();
        //40 second time-out for location check
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Location", "No location found, skipping");
                startMainActivity();
            }
        }, 40000);
        Log.d("Location", "updating location");

        // start location check with coarse accuracy.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        mLocationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());
    }



    //location found, set all things
    @Override
    public void onLocationChanged(Location location)
    {
        handler.removeCallbacksAndMessages(null); //delete timeout function
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

        LocationTitleTv.setText(cityTitle + " " + screenTitle);

        //show splashimage and screen name
        imageLoader.DisplayImage(image_url,cityImage,cityImage.getWidth(), cityImage.getHeight());
        imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
            @Override
            public void onImageLoad() {

                //blur
                cityImage.buildDrawingCache(true);
                Bitmap bmp = Bitmap.createBitmap(cityImage.getDrawingCache());
                cityImage.setDrawingCacheEnabled(false);
                blur(bmp, blurredView, 20);

                //start animation
                LocationTitleTv.startAnimation(fadeIn);
                LocationTitleTv.setVisibility(View.VISIBLE);
                cityImage.startAnimation(fadeIn);
                cityImage.setVisibility(View.VISIBLE);
                blurredView.setVisibility(View.VISIBLE);
                blurredView.startAnimation(fadeIn);
                locationSet = true;
                //start main activity after few seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMainActivity();
                    }
                }, 9000);
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
                animSpeed = 250;
                flipTopAnim.setDuration(animSpeed);
                flipBottomAnim.setDuration(animSpeed);
                fixedBottom.setText(cityTitle);
                if(changeText) {
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
                //slow down till quite slow
                if(animSpeed < animSpeedMax){
                    animSpeed *= animDiff;
                }
                flipTopAnim.setDuration(animSpeed);
                flipBottomAnim.setDuration(animSpeed);
                topLogo.startAnimation(flipTopAnim);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    /******
     Use this function to set animations for logo,
     necessary because view needs to be initialised
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        float cxtop = topLogo.getWidth()/2;
        float cytop = topLogo.getHeight()+5;
        flipTopAnim = new Rotate3dAnimation(0,-90,cxtop,cytop,animDepth, false);
        flipTopAnim.setDuration(animSpeed);
        flipTopAnim.setInterpolator(new AccelerateInterpolator());
        flipTopAnim.setAnimationListener(this);
        topLogo.startAnimation(flipTopAnim);
        float cxbot = bottomLogo.getWidth()/2;
        float cybot = -5;
        flipBottomAnim = new Rotate3dAnimation(90,0,cxbot,cybot,animDepth, false);
        flipBottomAnim.setDuration(animSpeed);
        flipBottomAnim.setInterpolator(new DecelerateInterpolator());
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



    /*****
    Blur code found from google. Blurs the bottom bar at splash
     */
    private void blur(Bitmap bkg, View view, float radius) {
        Bitmap overlay = Bitmap.createBitmap(
                view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(overlay);

        canvas.drawBitmap(bkg, -view.getLeft(),
                -view.getTop(), null);

        RenderScript rs = RenderScript.create(this);

        Allocation overlayAlloc = Allocation.createFromBitmap(
                rs, overlay);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, overlayAlloc.getElement());

        blur.setInput(overlayAlloc);

        blur.setRadius(radius);

        blur.forEach(overlayAlloc);

        overlayAlloc.copyTo(overlay);

        view.setBackground(new BitmapDrawable(
                getResources(), overlay));

        rs.destroy();
    }
}