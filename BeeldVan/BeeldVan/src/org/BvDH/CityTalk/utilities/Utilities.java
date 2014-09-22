package org.BvDH.CityTalk.utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.BvDH.CityTalk.MainActivity;
import org.BvDH.CityTalk.model.LocationData;
import org.BvDH.CityTalk.model.Locations;
import org.BvDH.CityTalk.model.NavImagesInfo;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.google.myjson.reflect.TypeToken;

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
				String language = prefs.getString("allLocations", "");
				return language;
			}

		public ArrayList<LocationData> getAllLocationList()
			{

				String callbackJson = getAllLocations();

				Gson gson = new GsonBuilder().serializeNulls().create();
				Type collectionType = new TypeToken<List<LocationData>>()
					{
					}.getType();
				ArrayList<LocationData> locationDatas = gson.fromJson(callbackJson, collectionType);
				System.out.println();
				return locationDatas;
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

		public ArrayList<Locations> setDistnace(ArrayList<Locations> locationList, Location crntLocation)
			{
				for (int i = 0; i < locationList.size(); i++)
					{
						double lat = locationList.get(i).getLatitude();
						double lng = locationList.get(i).getLongitude();

						Location loc = new Location("");
						loc.setLatitude(lat);
						loc.setLongitude(lng);

						locationList.get(i).setDistance(loc.distanceTo(crntLocation));
					}
				Collections.sort(locationList, new CompareToSort());
				return locationList;
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


        //get screen width in pixels (minus the 40dp margin) and calculate correct font size
        public static float getFontSize(float w){

            float width = w;
            //get font size from selected screen
            float font = (float) MainActivity.mFontSize;
            //get width from selected screen
            float lWidth = (float) MainActivity.mAspectRatioWidth;

            //calculate ratio value
            float r = font/lWidth;

            //set fontsize (screenpixels * ratio)
            float fontSize = (width * r);
            System.out.println("fontsize = " + fontSize);
            return fontSize;
        }

        public static int getMarginSize(float w){

            float width = w;
            System.out.println("width = " + width);

            float lWidth = (float)MainActivity.mAspectRatioWidth;
            float lMargin = (float)MainActivity.mMargin;

            float r = lMargin/lWidth;
            int margin = (int)(width * r);
            System.out.println("margin = " + margin);
            return margin;
        }

        public static int getPreviewHeight(float w){

            float width = w;
            float height;
            float lWidth = (float)MainActivity.mAspectRatioWidth;
            float lHeight = (float)MainActivity.mAspectRatioHeight;

            height = (width/lWidth)*lHeight;

            return (int)height;
        }


	}
