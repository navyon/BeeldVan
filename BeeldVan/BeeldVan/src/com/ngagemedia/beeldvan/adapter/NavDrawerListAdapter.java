package com.ngagemedia.beeldvan.adapter;

import java.util.HashMap;
import java.util.List;

import android.graphics.Typeface;
import android.widget.BaseExpandableListAdapter;
import com.ngagemedia.beeldvan.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseExpandableListAdapter
{

    private Context _context;
    ImageView childIcon;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    Integer[] slideImages = { R.drawable.slide_newmessage,
            R.drawable.slide_info, R.drawable.slide_news };

    public NavDrawerListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.drawer_list_item, null);
        }
        childIcon = (ImageView)convertView.findViewById(R.id.icon);
        if(childPosition==0)
        childIcon.setImageResource(slideImages[0]);
        if(childPosition==1)
            childIcon.setImageResource(slideImages[1]);
        if(childPosition==2)
            childIcon.setImageResource(slideImages[2]);
        if(childPosition==3)
            childIcon.setImageResource(0);
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ImageView image = null;
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        image = (ImageView) convertView.findViewById(R.id.expandableIcon);
        if(groupPosition != -1){
            int imageResourceId = isExpanded ? R.drawable.arrow_up_float : R.drawable.arrow_down_float;
            image.setImageResource(imageResourceId);

            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.INVISIBLE);
        }
        if(groupPosition == _listDataHeader.size()-1){
            image.setVisibility(View.INVISIBLE);
        }


        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
