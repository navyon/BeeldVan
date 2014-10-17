package com.ngagemedia.customwheel;

import android.widget.TextView;

public interface OnWheelChangedListener
{
	/**
	 * Callback method to be invoked when current item changed
	 * 
	 * @param wheel
	 *            the wheel view whose state has changed
	 * @param oldValue
	 *            the old value of current item
	 * @param newValue
	 *            the new value of current item
	 */
	void onChanged(WheelView wheel, int oldValue, int newValue);

	void onChanged(WheelView wheel, TextView oldTextView, TextView newTextView);

	public interface OnWheelChangedListenerTime
	{
		/**
		 * Callback method to be invoked when current item changed
		 * 
		 * @param wheel
		 *            the wheel view whose state has changed
		 * @param oldValue
		 *            the old value of current item
		 * @param newValue
		 *            the new value of current item
		 */
		void onChanged(WheelView wheel, int oldValue, int newValue);
	}
}
