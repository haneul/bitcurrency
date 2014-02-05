package com.haneul.bitcurrency;

import android.os.AsyncTask;
import android.util.JsonReader;

import com.haneul.bitcurrency.util.CurrencyChange;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by syhan on 2013. 12. 23..
 */
public class HuobiTask extends AsyncTask<Market, Void, Double> {

    private double read_btc_to_usd(InputStream in) {
        double ret = 0;
        byte [] strip = new byte[12];
        try{
            in.read(strip, 0, 12);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader.beginObject();

                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("p_new"))
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
        return ret;
    }

    @Override
    protected Double doInBackground(Market... params) {
        target = params[0];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://detail.huobi.com/staticmarket/detail.html");
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

    protected void onPostExecute(Double result) {

//        NumberFormat cn = NumberFormat.getCurrencyInstance(Locale.CHINA);
//        target.additional = "("+cn.format(result)+")";
        target.doneUpdate();
    }

    private Market target;
}
