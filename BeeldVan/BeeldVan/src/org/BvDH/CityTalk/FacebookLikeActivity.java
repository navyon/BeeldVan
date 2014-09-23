package org.BvDH.CityTalk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class FacebookLikeActivity extends BaseActivity
{

	TextView thankyou;
	TextView finalTip;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.likefacebook_layout);

		thankyou = (TextView) findViewById(R.id.thanks);
		finalTip = (TextView) findViewById(R.id.FinalTip);

        //TIMER to open slidingmenu ->notworking!
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                toggle();
//            }
//        }, 3000);

		findViewById(R.id.btnlike).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{

				if (isAppInstalled("com.facebook.katana"))
				{
					String uri = "fb://page/211994748854049";
					Intent fbapp = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					startActivity(fbapp);
				}
				else
				{
					Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/BeeldVanDenHaag"));
					startActivity(open);
				}
			}
		});
	}

	private boolean isAppInstalled(String packageName)
	{
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try
		{
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			installed = true;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			installed = false;
		}
		return installed;
	}

	// call onStop() to start initial activity
	protected void onStop()
	{
		super.onStop();

		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}


}
