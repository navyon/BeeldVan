package com.ngagemedia.beeldvan.fragments;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.widget.TextView;

import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FacebookFragment extends Fragment {
    Handler handler;

    public FacebookFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_thanks, container, false);

        getActivity().setTitle("Bedankt!");

        //open facebook app, otherwise link to web
        rootView.findViewById(R.id.btnlike).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isAppInstalled("com.facebook.katana")) {
                    String uri = "fb://page/1530754327148297";
                    Intent fbapp = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(fbapp);
                } else {
                    Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/BeeldVan"));
                    startActivity(open);
                }
            }
        });
        handler = new Handler();
        //open drawer after delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //show sliding menu
                MainActivity.mDrawerLayout.openDrawer(MainActivity.mDrawerList);
            }
        }, 3000);
        return rootView;
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public void onStop() {
        super.onStop();
        //on stop clear the backstack
        FragmentManager fm = getFragmentManager();
        fm.popBackStack("main", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);
    }


    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        MainActivity.mDrawerLayout.closeDrawer(MainActivity.mDrawerList);
    }
}
