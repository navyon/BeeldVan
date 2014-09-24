package org.BvDH.CityTalk.fragments;

import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import org.BvDH.CityTalk.BaseActivity;
import org.BvDH.CityTalk.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.BvDH.CityTalk.model.ImageLoader;
import org.BvDH.CityTalk.model.Locations;
import org.jsoup.Connection;

import java.util.List;

public class InfoFragment extends BaseActivity
{
    int childPosition = 0;
    int groupPosition = 0;
    ImageView cityImage;
    TextView cityInfo;
    TextView cityTitle;
    String baseUrl = "http://beeldvan.nu/";

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        int loader = R.drawable.loader;
        setContentView(R.layout.fragment_city_info);
        cityImage = (ImageView) findViewById(R.id.cityimageView);
        cityInfo = (TextView) findViewById(R.id.txtCityInfo);
        cityTitle =(TextView) findViewById(R.id.txtcityTitle);


        List<Locations> l = BaseActivity.mList.get(groupPosition).getLocations();
        String cityName = BaseActivity.mList.get(groupPosition).getName();

        if(l!=null) {
            String info = l.get(0).getText();
            String infoImage =  l.get(0).getInfoImageLocation();
            String image_url = baseUrl+infoImage;
            // ImageLoader class instance


            ImageLoader imgLoader = new ImageLoader(this);
            imgLoader.DisplayImage(image_url, loader, cityImage);
            if(info!=null)
            cityInfo.setText(Html.fromHtml(info));
            cityTitle.setText(cityName+" "+l.get(0).getName());
//            BaseActivity.main_include_layout.setVisibility(View.GONE);
            toggle();
        }
	}

}
