package org.BvDH.CityTalk.lazyloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.BvDH.CityTalk.R;
import org.BvDH.CityTalk.utilities.GetResizedImage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class LazyImageLaoder
{

	public interface IImageLoadListener
	{
		public void onImageLoad();
	}

	public void setOnImageLoadListener(IImageLoadListener imageLoadListener)
	{
		this.imageLoadListener = imageLoadListener;
	}

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	IImageLoadListener imageLoadListener = null;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	public LazyImageLaoder(Context context)
	{
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	final int stub_id = R.drawable.ic_photos;

	@SuppressWarnings("deprecation")
	public void DisplayImage(String url, ImageView imageView, int width, int height)
	{
		// fileCache.clear();

		// Utilities.printLog("LazyImageLoader DispalyImage", "url is: "+url);
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
		{

			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			imageView.setBackgroundDrawable(drawable);// (bitmap);
			// imageView.setImageDrawable(drawable);
			if (imageLoadListener != null)
			{
				// Log.e("ImageLoader", "Image Loaded from cache");
				imageLoadListener.onImageLoad();
			}
		}
		else
		{
			queuePhoto(url, imageView, width, height);
			imageView.setBackgroundResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView, int widht, int height)
	{
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p, widht, height));
	}

	private Bitmap getBitmap(String url, int width, int height)
	{
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = new GetResizedImage().decodeSampledBitmapFromFile(f, width, height);
		if (b != null)
			return b;

		// from web
		try
		{
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			CopyStream(is, os);
			os.close();
			bitmap = new GetResizedImage().decodeSampledBitmapFromFile(f, width, height);
			return bitmap;
		}
		catch (Exception ex)
		{
			// ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	// @SuppressWarnings("unused")
	// private Bitmap decodeFile1(File f, int size)
	// {
	// try
	// {
	// // decode image size
	// BitmapFactory.Options o = new BitmapFactory.Options();
	// o.inJustDecodeBounds = true;
	// BitmapFactory.decodeStream(new FileInputStream(f), null, o);
	//
	// BitmapFactory.Options o2 = new BitmapFactory.Options();
	//
	// // Find the correct scale value. It should be the power of 2.
	// if (size != Globals.REQUIRED_SIZE_NORMAL)
	// {
	// int width_tmp = o.outWidth, height_tmp = o.outHeight;
	// int scale = 1;
	// while (true)
	// {
	// if (width_tmp / 2 < size || height_tmp / 2 < size)
	// break;
	// width_tmp /= 2;
	// height_tmp /= 2;
	// scale *= 2;
	// }
	// // decode with inSampleSize
	//
	// o2.inSampleSize = scale;
	// }
	// return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	// }
	// catch (FileNotFoundException e)
	// {
	// }
	// return null;
	// }

	// Task for the queue
	private class PhotoToLoad
	{
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i)
		{
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable
	{
		PhotoToLoad photoToLoad;
		int width, height;

		PhotosLoader(PhotoToLoad photoToLoad, int width, int height)
		{
			this.photoToLoad = photoToLoad;
			this.width = width;
			this.height = height;
		}

		@Override
		public void run()
		{
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url, width, height);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
			{
				return;
			}
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad)
	{
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable
	{
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p)
		{
			bitmap = b;
			photoToLoad = p;
		}

		@SuppressWarnings("deprecation")
		public void run()
		{
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
			{
				BitmapDrawable drawable = new BitmapDrawable(bitmap);
				photoToLoad.imageView.setBackgroundDrawable(drawable);
				// photoToLoad.imageView.setImageDrawable(drawable);
				// photoToLoad.imageView.setImageBitmap(bitmap);
				if (imageLoadListener != null)
				{
					// Log.e("ImageLoader", "Image is Loaded check");
					imageLoadListener.onImageLoad();

				}
				else
				{
					// Log.e("ImageLoader", "Listner is not set");
				}
			}
			else
			{
				photoToLoad.imageView.setBackgroundResource(stub_id);
			}
		}
	}

	public void clearCache()
	{
		memoryCache.clear();
		fileCache.clear();
	}

	public void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size = 1024;
		try
		{
			byte[] bytes = new byte[buffer_size];
			for (;;)
			{
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch (Exception ex)
		{
		}
	}
}
