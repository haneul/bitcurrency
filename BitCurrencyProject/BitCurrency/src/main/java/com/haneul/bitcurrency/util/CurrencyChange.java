package com.haneul.bitcurrency.util;

import android.util.Log;
import android.util.Pair;

import com.haneul.bitcurrency.Market;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by syhan on 2014. 1. 26..
 */
public class CurrencyChange {
    Market.CURRENCY from;
    Market.CURRENCY to;
    double val;
    Date expire;
    boolean getReverse = false;
    private static final Hashtable<Pair<Market.CURRENCY, Market.CURRENCY>, CurrencyChange> instances = new Hashtable<Pair<Market.CURRENCY, Market.CURRENCY>, CurrencyChange>();

    public static CurrencyChange getCurrency(Market.CURRENCY from, Market.CURRENCY to)
    {
        CurrencyChange ret;
        synchronized (instances) {
            Pair<Market.CURRENCY, Market.CURRENCY> key = Pair.create(from, to);
            if(instances.contains(key)) {
                ret = instances.get(key);
            }
            else
            {
                ret = new CurrencyChange(from, to);
                instances.put(key, ret);
            }
        }
        ret.update();
        return ret;
    }

    public double getVal() { return val; }

    public CurrencyChange(Market.CURRENCY from, Market.CURRENCY to)
    {
        this.from = from;
        this.to = to;
    }

    public void getReverseVal() {
        CurrencyChange reverse = getCurrency(to, from);
        reverse.update();
        val = 1.0/reverse.getVal();
    }

    public boolean update()
    {
        Date now = new Date();
        if(val != 0 && now.before(expire))
        {
            return true;
        }
        if(!getReverse) {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("https://www.google.com/finance/converter?a=1&from="+from+"&to="+to);
            try{
                HttpResponse response = client.execute(httpget);
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(instream));
                    StringBuffer sb = new StringBuffer();
                    String aux;
                    while((aux = br.readLine()) != null)
                    {
                        sb.append(aux);
                    }
                    String html = sb.toString();
                    Document doc = Jsoup.parse(html);
                    Element result = doc.body().getElementsByClass("bld").first();
                    if(result != null) {
                        String resultStr = result.text();
                        val = Float.parseFloat(resultStr.split(" ")[0]);
                        Log.i("CurrencyChange", "from: " + from + " to: " + to + " " + val);
                        if(val<1 && val>0) {
                            getReverse = true;
                            getReverseVal();
                        }
                        expire = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
                    }
                    instream.close();
                    br.close();;
                }
            }  catch(ClientProtocolException p)
            { return false; } catch(IOException e)
            { return false;}
        }
        else {
            getReverseVal();
            expire = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
        }
        return true;
    }
}

