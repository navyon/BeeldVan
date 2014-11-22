package com.ngagemedia.beeldvan.fragments;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GenInfoFragment extends Fragment
{
    TextView appInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        getActivity().setTitle("Ngage Media");
        View rootView = inflater.inflate(R.layout.fragment_gen_info, container, false);
        appInfo = (TextView) rootView.findViewById(R.id.appInfo);

        MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);

        String info = getString(R.string.app_info);
        Log.d("info", info);
        appInfo.setText(Html.fromHtml(getString(R.string.app_info)));
        appInfo.setMovementMethod(LinkMovementMethod.getInstance());
        return rootView;
    }
}

