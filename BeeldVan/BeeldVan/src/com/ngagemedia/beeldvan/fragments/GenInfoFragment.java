package com.ngagemedia.beeldvan.fragments;

import android.text.Html;
import android.text.method.LinkMovementMethod;
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
    TextView appInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        getActivity().setTitle("Ngage Media");
        View rootView = inflater.inflate(R.layout.fragment_gen_info, container, false);
        appInfo = (TextView) rootView.findViewById(R.id.appInfo);

        MainActivity.main_include_layout.setVisibility(View.GONE);
        MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);

        String info = getString(R.string.app_info);
        System.out.println(info);
        appInfo.setText(Html.fromHtml(getString(R.string.app_info)));
        appInfo.setMovementMethod(LinkMovementMethod.getInstance());
        return rootView;
    }

}

