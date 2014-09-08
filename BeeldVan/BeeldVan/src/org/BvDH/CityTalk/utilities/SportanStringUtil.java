package org.BvDH.CityTalk.utilities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.util.Log;

public class SportanStringUtil {
	public static synchronized String ConvertStreamToString(InputStream is) {
		try {
			String total = IOUtils.toString(is);
			return total;
		} catch (IOException e) {
			Log.e("SamyStringUtil", "error ConvertStreamToString()", e);
			return null;
		}
	}
	
	public static String StripJSONPCallback(String raw) {
		raw = raw.replace("\\/", "/");//("\\/", "/");
		return raw;
	}

}
