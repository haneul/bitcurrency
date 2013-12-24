package com.haneul.bitcurrency;

import android.widget.ArrayAdapter;

import java.util.Vector;

/**
 * Created by syhan on 2013. 12. 19..
 */
public class Market {
    public String name;
    public String valueData;
    public double value;
    private Vector<Double> prevs;
    public Runnable getNewData;
    private ArrayAdapter<Market> myAdapter;
    public String additional;

    public Market(String name)
    {
        this.name = name;
        this.valueData = "Loading...";
        prevs = new Vector<Double>();
    }

    public void getNewData()
    {
        if(getNewData != null) getNewData.run();
    }

    public void pushNewData(double value)
    {
        if(this.value != 0) {
            prevs.add(this.value);
            if(prevs.size() > 3)
            {
                prevs.remove(0);
            }
        }
        this.value = value;
    }

    public String getRecentData()
    {
        String ret = "";
        int count = 0;
        for(int i=0;i<prevs.size();i++)
        {
            Double d = prevs.elementAt(prevs.size()-i-1);
            ret +=  String.format("$ %.2f", d);
            ret += ", ";
            count ++;
            if(count == 3) break;
        }
        return ret;
    }

    public int updown()
    {
        if(prevs.size() == 0)
        {
            return R.drawable.ic_menu_forward;
        }
        Double lastEl = prevs.elementAt(prevs.size()-1);
        if(lastEl < this.value)
        {
            return R.drawable.up_arrow;
        }
        else if(lastEl > this.value) return R.drawable.down_arrow;
        return R.drawable.ic_menu_forward;
    }

    public void setAdapter(ArrayAdapter<Market> adapter)
    {
        myAdapter = adapter;
    }

    public void doneUpdate()
    {
        myAdapter.notifyDataSetChanged();
    }

}
