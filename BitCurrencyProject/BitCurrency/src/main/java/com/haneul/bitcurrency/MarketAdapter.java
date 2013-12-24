package com.haneul.bitcurrency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by syhan on 2013. 12. 19..
 */
public class MarketAdapter extends ArrayAdapter<Market> {
    private final Market[] markets;
    private final Context context;
    public MarketAdapter(Context context, Market[] markets)
    {
        super(context, R.layout.rowlayout, markets);
        this.context = context;
        this.markets = markets;
        for(Market m:markets)
        {
            m.setAdapter(this);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView valueView = (TextView) rowView.findViewById(R.id.value);
        TextView prevView = (TextView) rowView.findViewById(R.id.prevs);
        Market m = markets[position];
        textView.setText(m.name);
        valueView.setText(String.format("$ %.2f", m.value));
        prevView.setText(m.getRecentData());
        // Change the icon for Windows and iPhone
        ImageView updown = (ImageView) rowView.findViewById(R.id.updown);
        updown.setImageResource(m.updown());
        if(m.additional != null)
        {
            TextView additional = (TextView) rowView.findViewById(R.id.additional);
            additional.setVisibility(View.VISIBLE);
            additional.setText(m.additional);
        }

        return rowView;
    }
}
