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
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by syhan on 2013. 12. 23..
 */
public class BTCChinaTask  extends AsyncTask<Market, Void, Double> {

    private double read_btc_to_usd(InputStream in) {
        double ret = 0;
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader.beginObject();

                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("ticker"))
                    {
                        reader.beginObject();
                        while(reader.hasNext()) {
                            if(reader.nextName().equals("last")) {
                                ret = reader.nextDouble();
                                break;
                            }
                            else reader.skipValue();
                        }

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

    static private double cnyusd = 0;
    static private Date cnyusdexpire;

    static private void updateCurrency()
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://www.google.com/finance/converter?a=1&from=CNY&to=USD");
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
                String resultStr = result.text();
                cnyusd = Float.parseFloat(resultStr.split(" ")[0]);
                cnyusdexpire = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
                instream.close();
                br.close();;
            }
        }  catch(ClientProtocolException p)
        {} catch(IOException e)
        {}

    }

    @Override
    protected Double doInBackground(Market... params) {
        Date now = new Date();

        if(cnyusd == 0 || cnyusdexpire.before(now))
        {
            updateCurrency();
        }


        target = params[0];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://data.btcchina.com/data/ticker");
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

        return ret;
    }

    protected void onPostExecute(Double result) {
        target.pushNewData(result*cnyusd);
        target.additional = String.format("(¥ %.2f)", result);
        target.doneUpdate();
    }

    private Market target;
}
