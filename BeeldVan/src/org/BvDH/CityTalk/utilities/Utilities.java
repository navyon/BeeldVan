package org.BvDH.CityTalk.utilities;

import java.util.ArrayList;
import java.util.List;

import org.BvDH.CityTalk.model.NavImagesInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Utilities
{
	Context context;

	public Utilities(Context context)
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

	public static void printStactTrace(Exception e, Context mContext)
	{
		e.printStackTrace();
		
	}
	
	public List<NavImagesInfo> convertJSONToNavImagesInfoList(JSONArray jsonArray)
	{
		
		List<NavImagesInfo> imagesInfoList=new ArrayList<NavImagesInfo>();
		try
		{
		for(int i=0;i<jsonArray.length();i++)
		{
			JSONObject json=jsonArray.getJSONObject(i);
			
			NavImagesInfo info=new NavImagesInfo();
			info.setImageUrl(json.optString("imageurl"));
			info.setOnline(json.optBoolean("isOnline"));
			imagesInfoList.add(info);
		}
		}
		catch(Exception e)
		{
			Utilities.printStactTrace(e, context);
		}
		
		return imagesInfoList;
	}

	public void showToastMessage(String message, Context  context)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

}
