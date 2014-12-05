package com.ngagemedia.beeldvan.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.ngagemedia.beeldvan.MainActivity;
import com.ngagemedia.beeldvan.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ngagemedia.beeldvan.model.Locations;
import com.ngagemedia.beeldvan.utilities.Utilities;

public class MessageFragment extends Fragment implements Animation.AnimationListener
{
    private EditText txtView_msg;
    private ImageView progress2;
    private RelativeLayout continueRL;
    String msg = null;
    float textsize;
    RelativeLayout msgRL;
    String imagePath;
    Bundle extras;
    Bitmap photo;
    Animation slideUpIn;
    Animation slideDownIn;
    Animation slideUpOut;
    Animation slideDownOut;
    Fragment fragment = null;
    Utilities utils;
    private boolean hasPhoto;

    Locations screen;

	public MessageFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        getActivity().setTitle("Tekst");
        msgRL = (RelativeLayout) rootView.findViewById(R.id.msgRL);
        continueRL = (RelativeLayout) rootView.findViewById(R.id.msgContRL);
        // build alert dialog for max line check
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Er passen maar 4 regels tekst op het scherm");
        Bundle bundle;
        utils = new Utilities(getActivity());

        //get selected screen
        screen = utils.getSelectedLocation(getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        txtView_msg = (EditText) rootView.findViewById(R.id.txtView_msg);
        progress2 = (ImageView) rootView.findViewById(R.id.progress_2Img);
        setTextSizes(txtView_msg);

        slideUpIn = AnimationUtils.loadAnimation(getActivity(), R.anim.button_slide_in_bottom);
        slideDownIn = AnimationUtils.loadAnimation(getActivity(), R.anim.button_slide_in_top);
        slideUpOut = AnimationUtils.loadAnimation(getActivity(), R.anim.button_slide_out_top);
        slideDownOut = AnimationUtils.loadAnimation(getActivity(), R.anim.button_slide_out_bottom);

        slideUpIn.setAnimationListener(this);
        slideUpOut.setAnimationListener(this);
        slideDownIn.setAnimationListener(this);
        slideDownOut.setAnimationListener(this);
        continueRL.setVisibility(View.GONE);
        progress2.setVisibility(View.GONE);

        builder.setMessage("Er passen maximaal 4 regels op het scherm!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });
        final AlertDialog alert = builder.create();

        Typeface fontHelv = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaBold.ttf");
        txtView_msg.setTypeface(fontHelv);
        bundle = this.getArguments();
        if (bundle!=null)
        {
            msg = bundle.getString("msg");
            txtView_msg.setText(msg);
            imagePath = bundle.getString("imagePath");
            hasPhoto = bundle.getBoolean("hasphoto", false);
            Log.d("message hasPhoto", Boolean.toString(hasPhoto));
        }

        final Button btnPrev = (Button) rootView.findViewById(R.id.btnpreview);
        final Button btnhidekeyb = (Button) rootView.findViewById(R.id.btnhidekey);
        final RelativeLayout layhidekeyb = (RelativeLayout) rootView.findViewById(R.id.layouthidekey);

        btnPrev.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = txtView_msg.getText().toString();

                if (!msg.isEmpty())
                {
                    utils.setSelectedLocation(getActivity(), screen);
                    FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                    ft1.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                    fragment = new PreviewFragment();
                    ft1.addToBackStack(null);

                    ft1.replace(R.id.frame_container, fragment, "PreviewFragment");
                    if (photo == null)
                        extras = new Bundle();
                    else
                        extras = getArguments();
                    extras.putString("msg", msg);
                    extras.putString("imagePath", imagePath);
                    extras.putBoolean("hasphoto", hasPhoto);
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
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                txtView_msg.clearFocus();
                continueRL.startAnimation(slideUpIn);
                continueRL.setVisibility(View.VISIBLE);
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
                    Log.d("Message", "Keyboard should show");
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    layhidekeyb.setVisibility(View.VISIBLE);
                    continueRL.startAnimation(slideDownOut);
                    progress2.startAnimation(slideUpOut);
                }
                else
                    layhidekeyb.setVisibility(View.GONE);


            }
        });

        txtView_msg.requestFocus();
        return rootView;
    }

    void setTextSizes(EditText txt)
    {

        float width = utils.getScreenWidth(getActivity());
        Log.d("width", String.valueOf(width));
        // force aspect ratio for txtView
        int height = utils.getPreviewHeight(width, screen);
        txt.setHeight(height);
        textsize = utils.getFontSize(width,screen);
        int margin = utils.getMarginSize(width, screen);

        // set sizes
        txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        txt.setPadding(margin, margin, margin, margin);
    }


    @Override
    public void onAnimationStart(Animation animation) {
        if(animation == slideDownIn){
            continueRL.setVisibility(View.VISIBLE);
        }
        if(animation == slideUpIn){
            progress2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == slideDownOut){
            continueRL.setVisibility(View.GONE);
        }
        if(animation == slideUpOut){
            progress2.setVisibility(View.GONE);
        }


    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        if(animation == slideDownIn){
            continueRL.setVisibility(View.VISIBLE);
        }
        if(animation == slideUpIn){
            progress2.setVisibility(View.VISIBLE);
        }
    }
}
