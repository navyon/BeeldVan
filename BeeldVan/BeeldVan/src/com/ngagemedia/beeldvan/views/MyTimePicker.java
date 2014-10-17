package com.ngagemedia.beeldvan.views;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.ngagemedia.customwheel.ArrayWheelAdapter;
import com.ngagemedia.customwheel.NumericWheelAdapter;
import com.ngagemedia.customwheel.OnWheelChangedListener;
import com.ngagemedia.customwheel.WheelView;

public class MyTimePicker
{

	private Context mContex;
	public static WheelView hour, min, amPm;
	final int DEFAULT_COLOR = Color.GRAY;
	final int SELECTED_COLOR = Color.WHITE;

	public MyTimePicker( Context context, Calendar calendar, View parent )
		{

			mContex = context;
			LinearLayout lytmain = (LinearLayout) parent;
			new LinearLayout(mContex);
			lytmain.setOrientation(LinearLayout.VERTICAL);
			LinearLayout lytdate = new LinearLayout(mContex);
			LinearLayout lytbutton = new LinearLayout(mContex);

			Button btnset = new Button(mContex);
			Button btncancel = new Button(mContex);

			btnset.setText("Set");
			btncancel.setText("Cancel");

			hour = new WheelView(mContex);
			min = new WheelView(mContex);
			// amPm = new WheelView(mContex);

			lytdate.addView(hour, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
			lytdate.addView(min, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
			// lytdate.addView(amPm, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

			lytmain.addView(lytdate);

			// year
			int curHour = calendar.get(Calendar.HOUR_OF_DAY);
			int curMin = calendar.get(Calendar.MINUTE);

			// int curAmPm = calendar.get(Calendar.AM_PM);
			// String[] amPmArr = new String[] {"AM", "PM"};
			DateNumericAdapter hourAdapter = new DateNumericAdapter(context, 0, 23, curHour);
			hour.setViewAdapter(hourAdapter);
			hour.addChangingListener(hourAdapter);

			DateNumericAdapter minAdapter = new DateNumericAdapter(context, 0, 59, curMin);
			min.setViewAdapter(minAdapter);
			min.addChangingListener(minAdapter);

			// hour.setViewAdapter(new DateNumericAdapter(context, 0, 23, curHour));
			// min.setViewAdapter(new DateNumericAdapter(context, 0, 59, curMin));
			// amPm.setViewAdapter(new TimeArrayAdapter(context, amPmArr, curAmPm));

			hour.setCurrentItem(curHour);
			min.setCurrentItem(curMin);
			// amPm.setCurrentItem(curAmPm);

			updateTime(hour, min);

		}

	Calendar updateTime(WheelView hour, WheelView min)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hour.getCurrentItem());
			calendar.set(Calendar.MINUTE, min.getCurrentItem());
			// calendar.set(Calendar.AM_PM, amPm.getCurrentItem());

			return calendar;

		}

	public String getTime()
		{
			String format = "%02d:%02d";
			return String.format(format, hour.getCurrentItem(), min.getCurrentItem());
		}

	private class DateNumericAdapter extends NumericWheelAdapter implements OnWheelChangedListener
	{
		TextView selTv;

		public DateNumericAdapter( Context context, int minValue, int maxValue, int current )
			{
				super(context, minValue, maxValue);
				setTextSize(20);
			}

		@Override
		protected void configureTextView(TextView view)
			{
				super.configureTextView(view);
				view.setTextColor(DEFAULT_COLOR);
				try
					{
						if (view.getId() == selTv.getId())
							view.setTextColor(SELECTED_COLOR);
					}
				catch (Exception e)
					{
						e.printStackTrace();
					}

				view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
			}

		@Override
		public View getItem(int index, int resId, View cachedView, ViewGroup parent)
			{
				return super.getItem(index, index, cachedView, parent);
			}

		@Override
		public void onChanged(WheelView wheel, int oldValue, int newValue)
			{
				selTv = ((TextView) wheel.getItemView(newValue));
				notifyDataChangedEvent();
			}

		@Override
		public void onChanged(WheelView wheel, TextView oldTextView, TextView newTextView)
			{

			}
	}

	private class TimeArrayAdapter extends ArrayWheelAdapter<String> implements OnWheelChangedListener
	{
		TextView selTv;

		public TimeArrayAdapter( Context context, String[] items, int current )
			{
				super(context, items);
				setTextSize(20);
			}

		@Override
		protected void configureTextView(TextView view)
			{
				super.configureTextView(view);
				view.setTextColor(DEFAULT_COLOR);
				try
					{
						if (view.getId() == selTv.getId())
							view.setTextColor(SELECTED_COLOR);
					}
				catch (Exception e)
					{
						e.printStackTrace();
					}

				view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
			}

		@Override
		public View getItem(int index, int resId, View cachedView, ViewGroup parent)
			{
				return super.getItem(index, index, cachedView, parent);
			}

		@Override
		public void onChanged(WheelView wheel, int oldValue, int newValue)
			{
				selTv = ((TextView) wheel.getItemView(newValue));
				notifyDataChangedEvent();
			}

		@Override
		public void onChanged(WheelView wheel, TextView oldTextView, TextView newTextView)
			{

			}
	}

}
