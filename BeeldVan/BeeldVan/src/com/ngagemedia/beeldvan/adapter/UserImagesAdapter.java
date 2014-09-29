package com.ngagemedia.beeldvan.adapter;

import java.util.ArrayList;
import java.util.List;

import com.ngagemedia.beeldvan.R;
import com.ngagemedia.beeldvan.lazyloader.LazyImageLaoder;
import com.ngagemedia.beeldvan.model.NavImagesInfo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class UserImagesAdapter extends BaseAdapter
{
	List<NavImagesInfo> navImagesInfoList = new ArrayList<NavImagesInfo>();
	Context context;
	LazyImageLaoder imageLoader;
	public UserImagesAdapter(Context context, List<NavImagesInfo> navImagesInfoList)
	{
		this.context = context;
		this.navImagesInfoList = navImagesInfoList;
		imageLoader=new LazyImageLaoder(context);
	}

	@Override
	public int getCount()
	{
		return navImagesInfoList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return navImagesInfoList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		UserImagesHolder holder = new UserImagesHolder();

		if (convertView == null)
		{
			LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.userimage_griditem, null);
			holder.userImg = (ImageView) convertView.findViewById(R.id.userImg);
			holder.userImgIsOnline = (ImageView) convertView.findViewById(R.id.userImgIsOnline);
			convertView.setTag(holder);
		}
		else
		{
			holder = (UserImagesHolder) convertView.getTag();
		}
		
		String imageUrl="";
		imageLoader.DisplayImage(imageUrl, holder.userImg, 150, 150);
		
		if(navImagesInfoList.get(position).isOnline())
		{
			holder.userImgIsOnline.setBackgroundResource(R.drawable.onlineimg);
		}
		else
		{
			holder.userImgIsOnline.setBackgroundResource(R.drawable.offlineimg);
		}
		
		

		return convertView;
	}

	class UserImagesHolder
	{
		ImageView userImg;
		ImageView userImgIsOnline;
	}

}
