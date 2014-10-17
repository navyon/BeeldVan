package com.ngagemedia.beeldvan.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngagemedia.beeldvan.R;
import com.ngagemedia.beeldvan.lazyloader.LazyImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 9/19/2014.
 */
public class CardArrayAdapter  extends ArrayAdapter<Card> implements Filterable{
    private static final String TAG = "CardArrayAdapter";
    private List<Card> cardList = new ArrayList<Card>();
    private LazyImageLoader imageLoader;


    static class CardViewHolder {
        TextView line1;
        TextView line2;
        TextView date;
        ImageView userImage;
    }

    public CardArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public void add(Card object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public Card getItem(int index) {
        return this.cardList.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final CardViewHolder viewHolder;
        imageLoader = new LazyImageLoader(getContext());
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_card, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.line1 = (TextView) row.findViewById(R.id.line1);
            viewHolder.line2 = (TextView) row.findViewById(R.id.line2);
            viewHolder.date = (TextView) row.findViewById(R.id.date);
            viewHolder.userImage = (ImageView) row.findViewById(R.id.userImage);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }
        int loader = R.drawable.loader;
        Card card = getItem(position);
        viewHolder.line1.setText(card.getLine1());
        viewHolder.line2.setText(card.getLine2());
        viewHolder.date.setText(card.getDate());
        viewHolder.line2.setMovementMethod(LinkMovementMethod.getInstance());
//        imageLoader.DisplayImage(card.getUrl(), loader, viewHolder.userImage);
        imageLoader.DisplayImage(card.getUrl(),viewHolder.userImage,48,48);
        imageLoader.setOnImageLoadListener(new LazyImageLoader.IImageLoadListener() {
            @Override
            public void onImageLoad() {
                // ready
                Log.d("imageLoader", "Tweet image loaded");
            }
        });
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                cardList = (ArrayList<Card>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new Filter.FilterResults();
                ArrayList<Card> FilteredTweets = new ArrayList<Card>();

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < cardList.size(); i++) {
                    Card tweets = cardList.get(i);
                    if (tweets.getLine2().toLowerCase().contains(constraint.toString())){
                        FilteredTweets.add(tweets);
                    }
                }

                results.count = FilteredTweets.size();
                results.values = FilteredTweets;
                Log.e("VALUES", results.values.toString());

                return results;
            }
        };

        return filter;
    }
}

