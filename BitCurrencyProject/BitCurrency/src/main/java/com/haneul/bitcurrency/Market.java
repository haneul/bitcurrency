package com.haneul.bitcurrency;

import android.widget.ArrayAdapter;

import com.haneul.bitcurrency.util.CurrencyChange;

import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by syhan on 2013. 12. 19..
 */
public class Market {
    public enum CURRENCY {
        USD("usd", Locale.US), KRW("krw", Locale.KOREA), CNY("cny", Locale.CHINA), EUR("eur", Locale.GERMANY), JPY("jpy", Locale.JAPAN), BTC("btc", null), LTC("ltc", null);
        private String name;
        private Locale locale;
        private CURRENCY(String name, Locale locale)
        {
            this.name = name;
            this.locale = locale;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public boolean equals(String name)
        {
            return this.name.equals(name);
        }

        private static Hashtable<String, CURRENCY> byname = new Hashtable<String, CURRENCY>();
        static {
            byname.put("usd", USD);
            byname.put("krw", KRW);
            byname.put("cny", CNY);
            byname.put("eur", EUR);
            byname.put("jpy", JPY);
            byname.put("btc", BTC);
            byname.put("ltc", LTC);
        }
        public static CURRENCY GetCurrencyByName(String name)
        {
            return byname.get(name);
        }
    };

    public CURRENCY targetCur = CURRENCY.USD;
    public String name;
    public String valueData;
    public double value;
    private Vector<Double> prevs;
    public Runnable getNewData;
    private ArrayAdapter<Market> myAdapter;
    public String additional = null;
    public String currency = "BTC/USD";
    private static CURRENCY displayCur = CURRENCY.USD;
    public static void SetDisplayCurrency(String name)
    {
        displayCur = CURRENCY.GetCurrencyByName(name);
    }

    public static String GetDisplayCurrencyName()
    {
        return displayCur.name();
    }

    public void init(String name, String currency, CURRENCY targetCur)
    {
        this.name = name;
        this.valueData = "Loading...";
        this.currency = currency;
        prevs = new Vector<Double>();
        this.targetCur = targetCur;
    }

    public Market(String name, String currency)
    {
        init(name, currency, CURRENCY.USD);
    }

    public Market(String name, String currency, CURRENCY targetCur)
    {
        init(name, currency, targetCur);
    }

    public void getNewData()
    {
        if(getNewData != null) getNewData.run();
    }

    public void pushNewData(double value)
    {
        if(targetCur != CURRENCY.BTC && targetCur != CURRENCY.LTC)
        {
            if(displayCur != targetCur)
            {
                CurrencyChange change = CurrencyChange.getCurrency(targetCur, displayCur);
                change.update();
                NumberFormat nf = NumberFormat.getCurrencyInstance(targetCur.getLocale());
                additional = "("+nf.format(value)+")";
                value *= change.getVal();
            }
        }
        if(this.value != 0) {
            prevs.add(this.value);
            if(prevs.size() > 3)
            {
                prevs.remove(0);
            }
        }
        this.value = value;
    }

    public String getValueString()
    {
       if(targetCur == CURRENCY.LTC)
            return String.format("%.2f LTC", value);
       NumberFormat nf = NumberFormat.getCurrencyInstance(displayCur.getLocale());
       return nf.format(value);
    }

    public String getRecentData()
    {
        String ret = "";
        int count = 0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(displayCur.getLocale());
        for(int i=0;i<prevs.size();i++)
        {
            Double d = prevs.elementAt(prevs.size()-i-1);
            if(targetCur != CURRENCY.BTC && targetCur != CURRENCY.LTC)
                ret +=  nf.format(d);
            else
                ret += String.format("%.2f", d);
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
