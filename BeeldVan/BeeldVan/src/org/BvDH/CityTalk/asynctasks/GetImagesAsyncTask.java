package org.BvDH.CityTalk.asynctasks;

import org.BvDH.CityTalk.interfaces.ImageLoadInterface;
import org.BvDH.CityTalk.utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class GetImagesAsyncTask extends AsyncTask<Void, Void, Void>
	{
		Context context;
		ImageLoadInterface callback;
		JSONObject json;

		public GetImagesAsyncTask( Context context, ImageLoadInterface callback )
			{
				this.context = context;
				this.callback = callback;
			}

		@Override
		protected void onPreExecute()
			{
				super.onPreExecute();
			}

		@Override
		protected Void doInBackground(Void... params)
			{
				try
					{
						json = new JSONObject();
						json.put("status", "OK");
						json.put("message", "images loaded successfully");
						JSONArray imagesInfoJSONArray = new JSONArray();
						for (int i = 0; i < 10; i++)
							{
								JSONObject imageJSON = new JSONObject();
								imageJSON.put("imageurl", "http://learnthat.com/files/2008/06/people-network1.jpg");
								if (i % 2 == 0)
									{
										imageJSON.put("isOnline", true);
									}
								else
									{
										imageJSON.put("isOnline", false);
									}
								imagesInfoJSONArray.put(imageJSON);
							}
						json.put("data", imagesInfoJSONArray);
					}
				catch (Exception e)
					{
						new Utilities(context).printStactTrace(e);
					}
				return null;
			}

		@Override
		protected void onPostExecute(Void result)
			{
				callback.imgListJSONCallback(json);
				super.onPostExecute(result);
			}

	}
