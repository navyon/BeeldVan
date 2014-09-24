package org.BvDH.CityTalk;

import org.BvDH.CityTalk.asynctasks.Sync2Manager;
import org.BvDH.CityTalk.model.LocationData;
import org.BvDH.CityTalk.model.Locations;
import org.BvDH.CityTalk.utilities.RESTClient;
import org.BvDH.CityTalk.utilities.SportanStringUtil;
import org.BvDH.CityTalk.utilities.Utilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

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

public class SplashActivity extends BaseActivity implements LocationListener
{
	Utilities utils;

    Location mLocation;
    LocationManager mLocationManager;
    List<Locations> mLocationList;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_new);
		utils = new Utilities(this);
        mLocationList = new ArrayList<Locations>();
        final myApplication globalVariable = (myApplication) getApplication();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        globalVariable.setSelectedLocation(0);
	}

    @Override
            protected void onResume(){
        super.onResume();
        new MyTask().execute();

        //fill location list
        mList = new Utilities(this).getAllLocationList();
        if(mList!=null) {
            for (int i = 0; i < mList.size(); i++) { //loop through cities
                List<Locations> l = mList.get(i).getLocations();
                if (mList.get(i).getLocations().size() > 0) {//if city has location
                    for (int j = 0; j < mList.get(i).getLocations().size(); j++) {//loop through lid's
                        if(l.get(j) != null) {
                            mLocationList.add(l.get(j));
                        }
//                        System.out.println("l = "+l.size());
                    }
                }



            }
        }
//        System.out.println("mLocationList = "+mLocationList.size());
//        mLocationList = locationData.getLocations();
        if (mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//        String bestProvider = mLocationManager.getBestProvider(criteria,false);
        //mLocation = mLocationManager.getLastKnownLocation(bestProvider);
//        mLocationManager.requestLocationUpdates(bestProvider, 400, 1, this, Looper.getMainLooper());
        System.out.println("getting location");
//        mLocationManager.requestSingleUpdate(bestProvider,this,Looper.getMainLooper());
        mLocationManager.requestSingleUpdate(criteria,this,Looper.getMainLooper());
      //  mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this, Looper.getMainLooper());
    }

    private ArrayList<LocationData> locationDatas;

    private class MyTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            URI uri;
            try
            {
                uri = new URI("http://api.beeldvan.nu/1.0/locations/all.json");
                HttpResponse response = new RESTClient(RESTClient.MAX_KEEP_ALIVE).GETRequest(uri);
                if (response != null)
                {

                    HttpEntity entity = response.getEntity();
                    String callbackJson;
                    InputStream is = entity.getContent();
                    callbackJson = SportanStringUtil.ConvertStreamToString(is);
                    callbackJson = SportanStringUtil.StripJSONPCallback(callbackJson);

                    Gson gson = new GsonBuilder().serializeNulls().create();
                    Type collectionType = new TypeToken<List<LocationData>>()
                    {
                    }.getType();
                    locationDatas = gson.fromJson(callbackJson, collectionType);
                    System.out.println(locationDatas);
                    new Utilities(SplashActivity.this).setAllLocations(callbackJson);

                }
            }
            catch (URISyntaxException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    public void onLocationChanged(Location location)
    {

        mLocation = location;
        System.out.println(mLocation);
        //fill location list


        //order them on distance
        mLocationList = utils.setDistances(mLocationList, mLocation);
        //select correct screen
        for(int i = 0; i < mLocationList.size(); i++){
            System.out.println("#"+i+" = "+ mLocationList.get(i).getName());
        }

        int selTemp = mLocationList.get(0).getLid();
        System.out.println(selTemp);
        System.out.println(mLocationList.get(0).getName());
//        System.out.println(mLocationList.get(1).getName());
        final myApplication globalVariable = (myApplication) getApplication();
        globalVariable.setSelectedLocation(selTemp);

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

}
