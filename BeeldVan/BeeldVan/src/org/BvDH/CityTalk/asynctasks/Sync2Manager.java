package org.BvDH.CityTalk.asynctasks;

import com.loopj.android.http.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;

public class Sync2Manager 
{
	private static Sync2Manager sync2Manager;
	//public static String BASEURL = "";

	public static Sync2Manager getSync2Manager() {
		if(sync2Manager == null)
		{
			sync2Manager = new Sync2Manager();
		}
		return sync2Manager;
	}
	public void getCurrentVersion()
	{
		AsyncHttpClient asyncClient = new AsyncHttpClient();
		//asyncClient.setTimeout(300000);
		asyncClient.get("http://api.beeldvan.nu/1.0/version/current.json", new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(int arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
				super.onSuccess(arg0, arg1);
			}
		});

	}
	
	public void getAllLocations()
	{
		AsyncHttpClient asyncClient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		//params.put("clientType", "" + 2);
		//asyncClient.setTimeout(300000);
		asyncClient.post("http://api.beeldvan.nu/1.0/locations/all.json" , new JsonHttpResponseHandler()
		{
			@Override
			public void onSuccess(int arg0, String arg1) {
				// TODO Auto-generated method stub
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1);
			}
			@Override
			protected void handleSuccessMessage(int arg0, String arg1) {
				// TODO Auto-generated method stub
				super.handleSuccessMessage(arg0, arg1);
			}
			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				super.handleFailureMessage(arg0, arg1);
			}
			
		});
	}

	
	

}
