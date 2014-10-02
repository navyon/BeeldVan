package com.ngagemedia.beeldvan.views;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.ngagemedia.customwheel.ArrayWheelAdapter;
import com.ngagemedia.customwheel.NumericWheelAdapter;
import com.ngagemedia.customwheel.WheelView;

public class MyDatePicker
{

	private Context mContex;
	public static WheelView month, year, day;
	private int NoOfYear = 40;
	// String months[] = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	String monthsShort[] = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	int curYear, mYear;

	public MyDatePicker( Context context, Calendar calendar, View parent )
		{

			mContex = context;
			LinearLayout lytmain = (LinearLayout) parent;
			new LinearLayout(mContex);
			lytmain.setOrientation(LinearLayout.VERTICAL);
			LinearLayout lytdate = new LinearLayout(mContex);

			Button btnset = new Button(mContex);
			Button btncancel = new Button(mContex);

			btnset.setText("Set");
			btncancel.setText("Cancel");

			month = new WheelView(mContex);
			year = new WheelView(mContex);
			day = new WheelView(mContex);

			lytdate.addView(day, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.1f));
			lytdate.addView(month, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
//			lytdate.addView(year, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.9f));

			lytmain.addView(lytdate);

			int curMonth = calendar.get(Calendar.MONTH);
			month.setViewAdapter(new DateArrayAdapter(context, monthsShort, curMonth));
			month.setCurrentItem(curMonth);

			Calendar cal = Calendar.getInstance();
			// year
			curYear = calendar.get(Calendar.YEAR);
			mYear = cal.get(Calendar.YEAR);

			year.setViewAdapter(new DateNumericAdapter(context, mYear - NoOfYear, mYear + NoOfYear, NoOfYear));
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
			day.setViewAdapter(new DateNumericAdapter(mContex, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH)));
			int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
			day.setCurrentItem(curDay - 1, true);
			calendar.set(Calendar.DAY_OF_MONTH, curDay);
			return calendar;

		}

	public String getDate()
		{
			String format = "%02d/%02d/%4d";
			return String.format(format, day.getCurrentItem() + 1, month.getCurrentItem() + 1, mYear - NoOfYear + year.getCurrentItem());
		}

	private class DateNumericAdapter extends NumericWheelAdapter
	{
		int currentItem;
		int currentValue;

		public DateNumericAdapter( Context context, int minValue, int maxValue, int current )
			{
				super(context, minValue, maxValue);
				this.currentValue = current;
				setTextSize(20);
			}

		@Override
		protected void configureTextView(TextView view)
			{
				super.configureTextView(view);
				if (currentItem == currentValue)
					{
                        view.setTextColor(0xFFFFFFFF);
					}
                else {
                    view.setTextColor(0x50FFFFFF);
                }
                view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
			}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent)
			{
				currentItem = index;
				return super.getItem(index, cachedView, parent);
			}
	}

	private class DateArrayAdapter extends ArrayWheelAdapter<String>
	{
		int currentItem;
		int currentValue;

		public DateArrayAdapter( Context context, String[] items, int current )
			{
				super(context, items);
				this.currentValue = current;
				setTextSize(20);
			}

		@Override
		protected void configureTextView(TextView view)
			{
				super.configureTextView(view);
				if (currentItem == currentValue)
					{
						view.setTextColor(0xFFFFFFFF);
					}
                else {
                    view.setTextColor(0x50FFFFFF);
                }
                view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
			}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent)
			{
				currentItem = index;
				return super.getItem(index, cachedView, parent);
			}
	}

	public interface DatePickerListner
	{
		public void OnDoneButton(Dialog datedialog, Calendar c);

		public void OnCancelButton(Dialog datedialog);
	}
}
