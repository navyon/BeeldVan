package com.ngagemedia.beeldvan.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.google.myjson.*;
import com.google.myjson.internal.LinkedTreeMap;
import com.google.myjson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ngagemedia.beeldvan.SplashActivity;
import com.ngagemedia.beeldvan.model.CityData;
import com.ngagemedia.beeldvan.utilities.RESTClient;
import com.ngagemedia.beeldvan.utilities.SportanStringUtil;
import com.ngagemedia.beeldvan.utilities.Utilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sync2Manager
	{
		private static Sync2Manager sync2Manager;
        public static JSONObject cVersion;
        public boolean isUpdated =false;
		// public static String BASEURL = "";
        private Context context;
        Utilities utils;
        ArrayList<CityData> mCityList;
        private SplashActivity mInstance;

        private static final String TAG_nameValuePair = "nameValuePairs";
        private static final String TAG_lastupdate = "lastUpdate";
        public Sync2Manager(Context context){

            mInstance = (SplashActivity) context;
            this.context=context;
            utils = new Utilities(this.context);
        }

        public Sync2Manager getSync2Manager()
			{
				if (sync2Manager == null)
					{
						sync2Manager = new Sync2Manager(context);
					}
				return sync2Manager;

			}

		public void getCurrentVersion()
			{
				AsyncHttpClient asyncClient = new AsyncHttpClient();
				// asyncClient.setTimeout(300000);
				asyncClient.get("http://api.beeldvan.nu/1.0/version/current.json", new JsonHttpResponseHandler()
					{
						@Override
						public void onSuccess(int arg0, JSONObject arg1)
							{
								// TODO Auto-generated method stub
                                // Get the current version from shared pre if Exist
                                String  currentVersion =  utils.getCurrentVersion();
                                // save the latest version from the API
                                Map<String, Object> lmap = new HashMap<String, Object>();
                                Gson cvgson = new Gson();
                                String lastestVersion = cvgson.toJson(arg1);
                                lmap = (Map<String, Object>) cvgson.fromJson(lastestVersion, lmap.getClass());
                                LinkedTreeMap<String, Double> updateMap1 = (LinkedTreeMap<String, Double>) lmap.get(TAG_nameValuePair);
                                Double latestversion = updateMap1.get(TAG_lastupdate);
                                // if no current version is saved, store it in shared pref
                                if(TextUtils.isEmpty(currentVersion)) {
                                    Gson cv1gson = new Gson();
                                    String json = cv1gson.toJson(arg1);
                                    utils.saveCurrentVersion(json);
                                    //and download the locations
                                    new UpdateLocations(new CallBack()).execute();
                                }else {
                                    // get the current version value
                                    Map<String, Object> cmap = new HashMap<String, Object>();
                                    Gson gson = new GsonBuilder().serializeNulls().create();
                                    cmap = (Map<String, Object>) gson.fromJson(currentVersion, cmap.getClass());
                                    LinkedTreeMap<String, Double> updateMap = (LinkedTreeMap<String, Double>) cmap.get(TAG_nameValuePair);
                                    Double currentversion = updateMap.get(TAG_lastupdate);
                                    // check current version against the latest version
                                    // if newer save to Shared Pref
                                    if(currentversion < latestversion) {
                                        System.out.println("new version found");
                                        utils.saveCurrentVersion(lastestVersion);
                                        //do location update with callback
                                        new UpdateLocations(new CallBack()).execute();

                                    } else {
                                        // call location check in Splash
                                        System.out.println("old version found");
                                        mInstance.startLocationChecks();
                                    }



                                }
                                super.onSuccess(arg0, arg1);

							}
					});

			}
        private ArrayList<CityData> locationDatas;


        public interface OnTaskCompleted {
            void OnTaskCompleted();
        }

        public class CallBack implements OnTaskCompleted
        {
            @Override
            public void OnTaskCompleted(){
                System.out.println("Finished with api checks");
                //start location check. (call function in splash)
                mInstance.startLocationChecks();
            }
        }

        private class UpdateLocations extends AsyncTask<Void, Void, Void>
        {
            private OnTaskCompleted mListener;

            public UpdateLocations(OnTaskCompleted listener){
                this.mListener = listener;
            }


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
                        Type collectionType = new TypeToken<List<CityData>>()
                        {
                        }.getType();
                        locationDatas = gson.fromJson(callbackJson, collectionType);
//                    System.out.println(locationDatas);
                        utils.setAllLocations(callbackJson);

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
            @Override
            protected void onPostExecute(Void params) {
                mListener.OnTaskCompleted();
//                mCityList = new Utilities(context).getAllCitiesList();
//                if(mCityList !=null) {
//                    for (int i = 0; i < mCityList.size(); i++) { //loop through cities
//                        List<Locations> l = mCityList.get(i).getLocations();
//                        if (mCityList.get(i).getLocations().size() > 0) {//if city has location
//                            for (int j = 0; j < mCityList.get(i).getLocations().size(); j++) {//loop through lid's
//                                if(l.get(j) != null) {
//                                    SplashActivity.mLocationList.add(l.get(j));
//                                }
//                            }
//                        }
//                    }
//                }
            }

        }

            public void getAllLocations()
			{
				AsyncHttpClient asyncClient = new AsyncHttpClient();
				RequestParams params = new RequestParams();
				// params.put("clientType", "" + 2);
				// asyncClient.setTimeout(300000);
				asyncClient.post("http://api.beeldvan.nu/1.0/locations/all.json", new JsonHttpResponseHandler()
					{
						@Override
						public void onSuccess(int arg0, String arg1)
							{
								// TODO Auto-generated method stub
								super.onSuccess(arg0, arg1);
							}

						@Override
						public void onFailure(Throwable arg0, String arg1)
							{
								// TODO Auto-generated method stub
								super.onFailure(arg0, arg1);
							}

						@Override
						protected void handleSuccessMessage(int arg0, String arg1)
							{
								// TODO Auto-generated method stub
								super.handleSuccessMessage(arg0, arg1);
							}

						@Override
						protected void handleFailureMessage(Throwable arg0, String arg1)
							{
								// TODO Auto-generated method stub
								super.handleFailureMessage(arg0, arg1);
							}

					});

			}

	}
