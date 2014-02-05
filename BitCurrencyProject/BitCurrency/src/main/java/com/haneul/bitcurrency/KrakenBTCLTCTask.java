package com.haneul.bitcurrency;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.widget.TextView;

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

/**
 * Created by syhan on 2013. 12. 3..
 */
public class KrakenBTCLTCTask extends AsyncTask<Market, Void, Double> {

    private double readObject(JsonReader reader) throws IOException {
        double ret = 0.0;
        reader.beginObject();
        while(reader.hasNext())
        {
            if(reader.nextName().equals("c")) {
                reader.beginArray();
                while(reader.hasNext()) {
                    if(ret == 0) ret = reader.nextDouble();
                    else reader.skipValue();
                }
                reader.endArray();
            }
            else reader.skipValue();
        }
        reader.endObject();
        return ret;
    }

    private double xxbtxltc;

    private double read_btc_to_usd(InputStream in) {
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader.beginObject();

                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("result"))
                    {

                        reader.beginObject();
                        while(reader.hasNext()) {
                            name = reader.nextName();
                            if(name.equals("XXBTXLTC")) {
                                xxbtxltc = readObject(reader);
                            }
                            else reader.skipValue();
                        }
                        break;
                    }
                    else reader.skipValue();
                }
            } catch (IOException e) {}
            finally
            {
                reader.close();
                return xxbtxltc;
            }
        }
        catch(UnsupportedEncodingException e)
        {}
        catch(IOException e)
        {}
        return xxbtxltc;
    }
    @Override
    protected Double doInBackground(Market... params) {
        target = params[0];
        //resultView2 = params[1];
        double ret = 0;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://api.kraken.com/0/public/Ticker?pair=XXBTXLTC");
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
