package com.ngagemedia.beeldvan.fragments;

import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngagemedia.beeldvan.model.ImageLoader;
import com.ngagemedia.beeldvan.model.Locations;

public class GenInfoFragment extends Fragment
{
    Locations screen;
    ImageView cityImage;
    TextView cityInfo;
    TextView cityTitle;
    String baseUrl = "http://beeldvan.nu/";
    String cityName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        int loader = R.drawable.loader;
        View rootView = inflater.inflate(R.layout.fragment_gen_info, container, false);
        cityImage = (ImageView) rootView.findViewById(R.id.cityimageView);
        cityInfo = (TextView) rootView.findViewById(R.id.txtCityInfo);
        cityTitle =(TextView) rootView.findViewById(R.id.txtcityTitle);



        if(screen!=null) {

            MainActivity.main_include_layout.setVisibility(View.GONE);
            MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);
        }
        return rootView;
    }

}

