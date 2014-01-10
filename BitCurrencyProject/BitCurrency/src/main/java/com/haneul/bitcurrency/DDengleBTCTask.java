package com.haneul.bitcurrency;

import android.os.AsyncTask;
import android.util.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by syhan on 2013. 12. 3..
 */
public class DDengleBTCTask extends AsyncTask<Market, Void, Double> {

    private double read_btc_to_krw(String html) {
        double ret = 0;
        Document doc = Jsoup.parse(html);

        return ret;
    }
    @Override
    protected Double doInBackground(Market... params) {
        target = params[0];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://www.ddengle.com/pricebtc");
        try{
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(instream));
                String html = "";
                String line;
                while((line=br.readLine()) != null)
                {
                    html += line;
                }
                read_btc_to_krw(html);
                instream.close();
            }
        }  catch(ClientProtocolException p)
        {} catch(IOException e)
        {}

        return ret;
    }

    protected void onPostExecute(Double result) {
        target.pushNewData(result);
        target.doneUpdate();
    }

    private Market target;

}
