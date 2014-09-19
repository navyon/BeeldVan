package org.BvDH.CityTalk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import org.BvDH.CityTalk.model.Card;
import org.BvDH.CityTalk.model.CardArrayAdapter;

/**
 * Created by admin on 9/19/2014.
 */
public class TwitterListActivity extends Activity {

    private static final String TAG = "TwitterListActivity";
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        listView = (ListView) findViewById(R.id.card_listView);

        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));

        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);

        for (int i = 0; i < 10; i++) {
            Card card = new Card("Card " + (i+1) + " Line 1", "Card " + (i+1) + " Line 2");
            cardArrayAdapter.add(card);
        }
        listView.setAdapter(cardArrayAdapter);
    }
}

