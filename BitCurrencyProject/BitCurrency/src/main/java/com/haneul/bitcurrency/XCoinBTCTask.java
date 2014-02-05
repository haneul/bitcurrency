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
public class XCoinBTCTask extends AsyncTask<Market, Void, Double> {

    private double read_btc_to_usd(InputStream in) {
        double ret = 0;
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader.beginArray();
                reader.beginObject();

                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("krwAmt"))
                    {
                        ret = reader.nextDouble();
                        break;
                    }
                    else reader.skipValue();
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

    private double usdkrw = 0;


    @Override
    protected Double doInBackground(Market... params) {
        CurrencyChange c = CurrencyChange.getCurrency(Market.CURRENCY.USD, Market.CURRENCY.KRW);
        usdkrw = c.getVal();

        target = params[0];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://www.xcoin.co.kr/json/contractListJson.action");
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
        target.doneUpdate();
    }

    private Market target;
}
