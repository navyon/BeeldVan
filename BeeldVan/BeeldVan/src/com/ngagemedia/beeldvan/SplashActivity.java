package com.ngagemedia.beeldvan;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ngagemedia.beeldvan.lazyloader.LazyImageLoader;
import com.ngagemedia.beeldvan.model.CityData;


import com.ngagemedia.beeldvan.asynctasks.Sync2Manager;
import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.LocationUtils;
import com.ngagemedia.beeldvan.utilities.Utilities;
import com.ngagemedia.beeldvan.utilities.getLocation;
import com.ngagemedia.beeldvan.views.Rotate3dAnimation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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

import com.google.android.gms.location.*;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements LocationListener, Animation.AnimationListener, com.google.android.gms.location.LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{
    Utilities utils;
    Sync2Manager s;
    Location mLocation;
    getLocation myLocation;
    LocationManager mLocationManager;
    public static List<Locations> mLocationList;
    private ArrayList<CityData> CityDataList;
    /**play services implementation*/

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;



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
//        super.onCreate(null);
        setContentView(R.layout.splash_new);

        Log.d("state", "onCreate");
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
        cityImage.setDrawingCacheEnabled(true);

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

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);


    }

    /*
 * Called when the Activity is no longer visible at all.
 * Stop updates and disconnect.
 */
    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null); //delete timeout function
        if(mLocationClient!=null && (mLocationClient.isConnected() || mLocationClient.isConnecting())) {
            stopPeriodicUpdates();
            mLocationClient.disconnect();
        }
        Log.d("state", "onStop");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("state", "onResume");
        mLocationList = new ArrayList<Locations>();



        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

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

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }



    //this is called after API update getCurrentVersion()
    public void startLocationChecks(){
        CityDataList = utils.getAllCitiesList();
        //40 second time-out for location check
        Log.d("apiFinished", "true");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Location", "No location found, skipping");
                startMainActivity();
            }
        }, 15000);
        Log.d("Location", "updating location");

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    }



    //location found, set all things
    @Override
    public void onLocationChanged(Location location)
    {
        handler.removeCallbacksAndMessages(null); //delete timeout function
        mLocation = location;
        Log.d("location", mLocation.toString());
        stopPeriodicUpdates();
        mLocationClient.disconnect();
        //fill temp list
        for(int i = 0; i < CityDataList.size(); i++){
            List<Locations> l = CityDataList.get(i).getLocations();
            if(l.size() > 0) {
                for (int j = 0; j < l.size(); j++) {
                    mLocationList.add(l.get(j));
                }
            }
        }
        locationSet = true;

        // ImageLoader class instance
        cityTitle = "";
        String screenTitle = "";
        String image_url = null;


        //order them on distance
        mLocationList = utils.setDistances(mLocationList, mLocation);

//        for(int i = 0; i < mLocationList.size(); i++){
//            Log.d("location", "#" + i + " = " + mLocationList.get(i).getName());
//        }

        for(int i = 0; i < CityDataList.size(); i++){
            List<Locations> l = CityDataList.get(i).getLocations();
            if(l.size() > 0) {
                for (int j = 0; j < l.size(); j++) {
                    if(mLocationList.get(0).getLid() == l.get(j).getLid()){
//                        Log.d("location","closest = " + l.get(j).getName());
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

                //start animation
                startAnimation();

                //start main activity after few seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMainActivity();
                    }
                }, 4000);
            }
        });
    }


    public void startMainActivity(){
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // TODO Auto-generated method stub
        Log.d("state", "onStatusChanged");

    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Log.d("state", "onProviderEnabled");
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Log.d("state", "onProviderDisabled");
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
            Log.d("locationSet", " "+locationSet);
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
        view.setBackgroundDrawable(new BitmapDrawable(
                getResources(), overlay));

        rs.destroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("state", "onConnected");
            startPeriodicUpdates();
    }

    @Override
    public void onDisconnected() {
//        connected = false;
        Log.d("state", "onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        Log.d("state", "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        Log.d("GoogleAPI", "should update now");
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("state", "onDestroy");
    }


    void startAnimation(){
        cityImage.setDrawingCacheEnabled(true);
        cityImage.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createBitmap(cityImage.getDrawingCache());
        cityImage.setDrawingCacheEnabled(false);
        blur(bmp, blurredView, 20);

        LocationTitleTv.startAnimation(fadeIn);
        LocationTitleTv.setVisibility(View.VISIBLE);
        cityImage.startAnimation(fadeIn);
        cityImage.setVisibility(View.VISIBLE);
        blurredView.setVisibility(View.VISIBLE);
        blurredView.startAnimation(fadeIn);
        locationSet = true;
    }
}