package com.haneul.bitcurrency;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.InputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import android.widget.TextView;

/**
 * Created by syhan on 2013. 12. 2..
 */
public class CoinBaseTask extends AsyncTask<Market, Void, Double> {

    private double read_btc_to_usd(InputStream in) {
        double ret = 0;
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("btc_to_usd"))
                    {
                        ret = reader.nextDouble();
                        break;
                    }
                    reader.skipValue();
                }
            } catch (IOException e) {}
            finally
            {
                reader.close();
                return ret;
            }
        }
        catch(UnsupportedEncodingException e)
        {}
        catch(IOException e)
        {}
        return 0;
    }

    protected void onPostExecute(Double result) {

        target.doneUpdate();
        //resultView.setText(String.format("$ %.2f", result));
    }

    private Market target;

    @Override
    protected Double doInBackground(Market... params) {
        target = params[0];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://coinbase.com/api/v1/currencies/exchange_rates");
        try{
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                ret = read_btc_to_usd(instream);
                instream.close();
            }
        }  catch(ClientProtocolException p)
        {} catch(IOException e)
        {}
        target.pushNewData(ret);
        return ret;
    }
}
