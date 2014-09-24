package org.BvDH.CityTalk.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.TextView;
import org.BvDH.CityTalk.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FacebookFragment extends Fragment
{
    TextView thankyou;
    TextView finalTip;

    public FacebookFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.fragment_community, container, false);
        thankyou = (TextView) rootView.findViewById(R.id.thanks);
        finalTip = (TextView) rootView.findViewById(R.id.FinalTip);

        rootView.findViewById(R.id.btnlike).setOnClickListener(new View.OnClickListener()
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
		return rootView;
	}
    private boolean isAppInstalled(String packageName)
    {
        PackageManager pm = getActivity().getPackageManager();
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
   public void onStop()
    {
        super.onStop();

        Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
