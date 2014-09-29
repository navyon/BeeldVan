package com.ngage.beeldvan.fragments;

import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import com.ngage.beeldvan.MainActivity;
import org.BvDH.CityTalk.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngage.beeldvan.SplashActivity;
import com.ngage.beeldvan.model.CityData;
import com.ngage.beeldvan.model.ImageLoader;
import com.ngage.beeldvan.model.Locations;

import java.util.List;

public class InfoFragment extends Fragment
{
    Locations screen;
    ImageView cityImage;
    TextView cityInfo;
    TextView cityTitle;
    String baseUrl = "http://beeldvan.nu/";
    String cityName;
    public InfoFragment(String cName, Locations loc)
	{
        screen = loc;
        cityName = cName;

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        int loader = R.drawable.loader;
		View rootView = inflater.inflate(R.layout.fragment_city_info, container, false);
        cityImage = (ImageView) rootView.findViewById(R.id.cityimageView);
        cityInfo = (TextView) rootView.findViewById(R.id.txtCityInfo);
        cityTitle =(TextView) rootView.findViewById(R.id.txtcityTitle);



        if(screen!=null) {
            String info = screen.getText();
            String infoImage =  screen.getInfoImageLocation();

            String image_url = baseUrl+infoImage;
            // ImageLoader class instance


            ImageLoader imgLoader = new ImageLoader(getActivity().getApplicationContext());
            imgLoader.DisplayImage(image_url, loader, cityImage);
            if(info!=null)
            cityInfo.setText(Html.fromHtml(info));
            cityTitle.setText(cityName+" "+screen.getName());
            MainActivity.main_include_layout.setVisibility(View.GONE);
            MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);
        }
        return rootView;
	}

}
