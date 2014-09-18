package org.BvDH.CityTalk.fragments;

import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import org.BvDH.CityTalk.MainActivity;
import org.BvDH.CityTalk.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.BvDH.CityTalk.model.ImageLoader;
import org.BvDH.CityTalk.model.Locations;

import java.util.List;

public class InfoFragment extends Fragment
{
    int childPosition = 0;
    int groupPosition = 0;
    ImageView cityImage;
    TextView cityInfo;
    TextView cityTitle;
    String baseUrl = "http://beeldvan.nu/";
	public InfoFragment(int childposition, int groupposition)
	{
        childPosition =  childposition;
        groupPosition = groupposition;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        int loader = R.drawable.loader;
		View rootView = inflater.inflate(R.layout.fragment_city_info, container, false);
        cityImage = (ImageView) rootView.findViewById(R.id.cityimageView);
        cityInfo = (TextView) rootView.findViewById(R.id.txtCityInfo);
        cityTitle =(TextView) rootView.findViewById(R.id.txtcityTitle);


        List<Locations> l = MainActivity.mList.get(groupPosition).getLocations();
        String cityName = MainActivity.mList.get(groupPosition).getName();

        if(l!=null) {
            String info = l.get(0).getText();
            String infoImage =  l.get(0).getInfoImageLocation();
            String image_url = baseUrl+infoImage;
            // ImageLoader class instance


            ImageLoader imgLoader = new ImageLoader(getActivity().getApplicationContext());
            imgLoader.DisplayImage(image_url, loader, cityImage);
            if(info!=null)
            cityInfo.setText(Html.fromHtml(info));
            cityTitle.setText(cityName+" "+l.get(0).getName());
            MainActivity.main_include_layout.setVisibility(View.GONE);
            MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);
        }
        return rootView;
	}

}
