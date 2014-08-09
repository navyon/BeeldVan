package org.BvDH.CityTalk;

import java.util.Locale;

import org.BvDH.CityTalk.utilities.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SplashActivity1 extends Activity {
	// Set the display time, in milliseconds (or extract it out as a
	// configurable parameter)
	private TextView Welcome;
	// private TextView IntroText;
	private Button SplBtn;
	private Button SplInfBtn;
	private CheckBox SplChk;
	// private RadioGroup LangSelectGroup;
	// private RadioButton LangSelectBtn;

	private AlertDialog.Builder infobuilder;
	Utilities utils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_new);
		utils = new Utilities(this);
		Typeface fontRegular =utils.loadTypeFace(0);
		Typeface fontLight = utils.loadTypeFace(1);
		Welcome = (TextView) findViewById(R.id.welkom_text);
		// IntroText = (TextView) findViewById(R.id.introText);
		Welcome.setTypeface(fontRegular);
		// IntroText.setTypeface(fontLight);
		SplBtn = (Button) findViewById(R.id.SplBtn);
		SplBtn.setTypeface(fontLight);
		SplInfBtn = (Button) findViewById(R.id.SplInfoBtn);
		SplInfBtn.setTypeface(fontLight);
		SplChk = (CheckBox) findViewById(R.id.splashCheck);
		// LangSelectGroup = (RadioGroup) findViewById(R.id.LangSelect);
		infobuilder = new AlertDialog.Builder(this);
		loadLocale();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Obtain the sharedPreference, default to true if not available
		String  isSplashEnabled = utils.getSharedPrefValue("isSplashEnabled");

		if (!isSplashEnabled.equals("")) {
			// if the splash is not enabled, then finish the activity
			// immediately and go to main.
			finish();
			Intent mainIntent = new Intent(SplashActivity1.this,
					MainActivity.class);
			startActivity(mainIntent);
		}

		else {

			// info alert builder

			infobuilder
					.setMessage(R.string.SplashTextInfo)
					.setTitle(R.string.SplashTextInfoBtn)
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							});

			if (!utils.isNetWorkConnected()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.InternetCheck)
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// finish();
									}
								});
				final AlertDialog alert = builder.create();
				alert.show();
			}

			SplInfBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog info = infobuilder.create();
					info.show();
				}
			});

			/*
			 * LangSelectGroup.setOnCheckedChangeListener(new
			 * RadioGroup.OnCheckedChangeListener() {
			 * 
			 * @Override public void onCheckedChanged(RadioGroup group, int
			 * checkedId) { LangSelectBtn = (RadioButton)
			 * findViewById(checkedId); int index =
			 * LangSelectGroup.indexOfChild(LangSelectBtn); String
			 * languageToLoad = "en"; if(LangSelectBtn ==
			 * findViewById(R.id.nl)){ changeLang("nl"); } else if(LangSelectBtn
			 * == findViewById(R.id.en)){ changeLang("en"); } if(index == 0){
			 * languageToLoad = "nl"; System.out.println(languageToLoad); } else
			 * if(index == 1){ languageToLoad = "en";
			 * System.out.println(languageToLoad); } changeLang(languageToLoad);
			 * } });
			 */

			SplBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					utils.saveValueToSharedPrefs("isSplashEnabled", "");
					
					finish();
					Intent mainIntent = new Intent(SplashActivity1.this,
							MainActivity.class);
					startActivity(mainIntent);
				}
			});
		}

	}

	public void changeLang(String lang) {
		if (lang.equalsIgnoreCase(""))
			return;
		Locale myLocale = new Locale(lang);
		saveLocale(lang);
		Locale.setDefault(myLocale);
		android.content.res.Configuration config = new android.content.res.Configuration();
		config.locale = myLocale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		updateTexts();
	}

	public void saveLocale(String lang) {
		String langPref = "Language";
		utils.saveValueToSharedPrefs(langPref, lang);
	}

	private void updateTexts() {
		Welcome.setText(R.string.SplashTextWelcome);
		SplChk.setText(R.string.SplashTextCheck);
		SplInfBtn.setText(R.string.SplashTextInfoBtn);
		// IntroText.setText(R.string.SplashTextIntro);
		infobuilder.setMessage(R.string.SplashTextInfo).setTitle(
				R.string.SplashTextInfoBtn);
	}

	public void loadLocale() {
		String langPref = "Language";
		String language = utils.getSharedPrefValue(langPref);
		changeLang(language);
	}
}