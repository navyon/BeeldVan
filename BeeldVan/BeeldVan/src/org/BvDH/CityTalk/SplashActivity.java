package org.BvDH.CityTalk;

import org.BvDH.CityTalk.asynctasks.Sync2Manager;
import org.BvDH.CityTalk.utilities.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends BaseActivity
{
	long splashTimeInSeconds = 4000; // 3 seconds
	Utilities utils;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_new);
		utils = new Utilities(this);
		new Handler().postAtTime(runnable, splashTimeInSeconds);
	}
	Runnable runnable = new Runnable()
	{

		@Override
		public void run()
		{
			// utils.startNewActivity(MainActivity.class);
			startActivity(new Intent(SplashActivity.this, MainActivity.class));
			finish();
		}
	};

}
