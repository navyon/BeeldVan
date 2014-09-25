package com.ngage.beeldvan.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.BvDH.CityTalk.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngage.beeldvan.model.Locations;
import com.ngage.beeldvan.utilities.Utilities;

public class MessageFragment extends Fragment
{
    Intent intent;
    private EditText txtView_msg;
    private TextView txtView_maxLines;
    private TextView txtView_msgTip;
    private TextView txtView_continue;
    private ImageView aspectv;
    private ImageView progress2;
    String msg = null;
    float textsize;
    RelativeLayout msgRL;
    String imagePath;
    Bundle extras;
    Bitmap photo;
    Animation slideUpIn;
    Animation slideDownIn;
    Fragment fragment = null;
    Utilities utils;

    Locations screen;

	public MessageFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        msgRL = (RelativeLayout) rootView.findViewById(R.id.msgRL);

        // build alert dialog for max line check
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Er passen maar 4 regels tekst op het scherm");
        Bundle bundle = this.getArguments();
        utils = new Utilities(getActivity());

        //get selected screen
        screen = utils.getSelectedLocation(getActivity());

        if (bundle!= null)
        {
               imagePath = bundle.getString("imagePath");

        }
        txtView_msg = (EditText) rootView.findViewById(R.id.txtView_msg);
//				txtView_maxLines = (TextView) findViewById(R.id.txtView_maxLine);
//				txtView_msgTip = (TextView) findViewById(R.id.txtView_msgTip);
        aspectv = (ImageView) rootView.findViewById(R.id.aspectv);
        progress2 = (ImageView) rootView.findViewById(R.id.progress_2Img);
        setTextSizes(txtView_msg);

        slideUpIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_dialog);
        slideDownIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);

        builder.setMessage("Er passen maximaal 4 regels op het scherm!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });
        final AlertDialog alert = builder.create();

        Typeface fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface fontLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        Typeface fontHelv = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaBold.ttf");
        txtView_msg.setTypeface(fontHelv);
//				txtView_maxLines.setTypeface(fontRegular);
//				txtView_msgTip.setTypeface(fontLight);

        txtView_continue = (TextView) rootView.findViewById(R.id.txtpreview); //text above button
        final Button btnPrev = (Button) rootView.findViewById(R.id.btnpreview);
        btnPrev.setTypeface(fontLight);
        final Button btnhidekeyb = (Button) rootView.findViewById(R.id.btnhidekey);
        final RelativeLayout layhidekeyb = (RelativeLayout) rootView.findViewById(R.id.layouthidekey);
        bundle = this.getArguments();
        if (bundle!=null)
        {
            msg = bundle.getString("msg");
            txtView_msg.setText(msg);
        }
        btnPrev.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = txtView_msg.getText().toString();

                if (!msg.isEmpty())
                {
                    FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                    fragment = new PreviewFragment();
                    ft1.addToBackStack(null);

                    ft1.replace(R.id.frame_container, fragment);
                    if (photo == null)
                        extras = new Bundle();
                    else
                        extras = getArguments();
                    extras.putString("msg", msg);
                    extras.putString("imagePath", imagePath);
                    fragment.setArguments(extras);
                    ft1.commit();

                }

                else
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Het toevoegen van een bericht is verplicht.", Toast.LENGTH_LONG).show();
                }

            }
        });

        // some online hack that should limit the Edit box, dont think its any better than the first enter hack lets see
        txtView_msg.addTextChangedListener(new TextWatcher()
        {

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
            {
            }

            public void afterTextChanged(Editable arg0)
            {
                int lineCount = txtView_msg.getLineCount();
                if (lineCount > 4)
                {
                    txtView_msg.setText(txtView_msg.getText().delete(txtView_msg.length() - 1, txtView_msg.length()));
                    txtView_msg.setSelection(txtView_msg.length());
                    alert.show();
                }
            }
        });

        btnhidekeyb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                txtView_msg.clearFocus();
                btnPrev.startAnimation(slideUpIn);
                txtView_continue.startAnimation(slideUpIn);
                btnPrev.setVisibility(View.VISIBLE);
                txtView_continue.setVisibility(View.VISIBLE);
                progress2.startAnimation(slideDownIn);
                progress2.setVisibility(View.VISIBLE);

            }
        });


        txtView_msg.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    layhidekeyb.setVisibility(View.VISIBLE);
                    btnPrev.setVisibility(View.GONE);
                    txtView_continue.setVisibility(View.GONE);
                    progress2.setVisibility(View.GONE);



                }
                else
                    layhidekeyb.setVisibility(View.GONE);
//                                    btnPrev.setVisibility(View.VISIBLE);
//                                    txtView_continue.setVisibility(View.VISIBLE);

            }
        });
        return rootView;
    }

    void setTextSizes(EditText txt)
    {

        float width = utils.getScreenWidth(getActivity());
        System.out.println("width = "+width);
        // force aspect ratio for txtView
        int height = utils.getPreviewHeight(width,screen);
        Bitmap.Config conf = Bitmap.Config.ALPHA_8;
        Bitmap bmp = Bitmap.createBitmap((int)width, height, conf);// create transparent bitmap
        aspectv.setImageBitmap(bmp);

        textsize = utils.getFontSize(width,screen);
        int margin = utils.getMarginSize(width, screen);

        // set sizes
        txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        txt.setPadding(margin, margin, margin, margin);

    }



}
