package com.ngagemedia.beeldvan.views;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.ngagemedia.customwheel.ArrayWheelAdapter;
import com.ngagemedia.customwheel.NumericWheelAdapter;
import com.ngagemedia.customwheel.OnWheelChangedListener;
import com.ngagemedia.customwheel.WheelView;

public class MyDatePicker
{

	private Context mContex;
	public static WheelView month, year, day;
	private int NoOfYear = 40;
	// String months[] = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	String monthsShort[] = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	int curYear, mYear;
	final int DEFAULT_COLOR = Color.GRAY;
	final int SELECTED_COLOR = Color.WHITE;

	public MyDatePicker( Context context, Calendar calendar, View parent )
		{

			mContex = context;
			LinearLayout lytmain = (LinearLayout) parent;
			new LinearLayout(mContex);
			lytmain.setOrientation(LinearLayout.VERTICAL);
			LinearLayout lytdate = new LinearLayout(mContex);


			month = new WheelView(mContex);
			year = new WheelView(mContex);
			day = new WheelView(mContex);

			lytdate.addView(day, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.1f));
			lytdate.addView(month, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
			// lytdate.addView(year, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.9f));

			lytmain.addView(lytdate);

			int curMonth = calendar.get(Calendar.MONTH);
			DateArrayAdapter monthAdapter = new DateArrayAdapter(context, monthsShort, curMonth);
			month.setViewAdapter(monthAdapter);
			month.addChangingListener(monthAdapter);
			month.setCurrentItem(curMonth);

			Calendar cal = Calendar.getInstance();
			// year
			curYear = calendar.get(Calendar.YEAR);
			mYear = cal.get(Calendar.YEAR);

			DateNumericAdapter yearAdapter = new DateNumericAdapter(context, mYear - NoOfYear, mYear + NoOfYear, NoOfYear);
			year.setViewAdapter(yearAdapter);
			year.addChangingListener(yearAdapter);
			year.setCurrentItem(curYear - (mYear - NoOfYear));

			// day
			updateDays(year, month, day);
			day.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH) - 1);

		}

	Calendar updateDays(WheelView year, WheelView month, WheelView day)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + (year.getCurrentItem() - NoOfYear));
			calendar.set(Calendar.MONTH, month.getCurrentItem());

			int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			DateNumericAdapter dayAdapter = new DateNumericAdapter(mContex, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1);
			day.setViewAdapter(dayAdapter);
			day.addChangingListener(dayAdapter);
			int curDay = Math.min(maxDays, day.getCurrentItem());
			day.setCurrentItem(curDay, true);
			calendar.set(Calendar.DAY_OF_MONTH, curDay);
			// SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
			// sdf.format(calendar.getTime());
			return calendar;

		}

	public String getDate()
		{
			String format = "%02d/%02d/%4d";
			return String.format(format, day.getCurrentItem() + 1, month.getCurrentItem() + 1, mYear - NoOfYear + year.getCurrentItem());
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
		public void onChanged(WheelView wheel, TextView oldTextView, TextView newTextView)
			{

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
	}

	private class DateArrayAdapter extends ArrayWheelAdapter<String> implements OnWheelChangedListener
	{
		TextView selTv;

		public DateArrayAdapter( Context context, String[] items, int current )
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
                //make sure there' no non-existent dates
                updateDays(year, month, day);
                selTv = ((TextView) wheel.getItemView(newValue));
				notifyDataChangedEvent();
			}

		@Override
		public void onChanged(WheelView wheel, TextView oldTextView, TextView newTextView)
			{

			}
	}

	public interface DatePickerListner
	{
		public void OnDoneButton(Dialog datedialog, Calendar c);

		public void OnCancelButton(Dialog datedialog);
	}
}
