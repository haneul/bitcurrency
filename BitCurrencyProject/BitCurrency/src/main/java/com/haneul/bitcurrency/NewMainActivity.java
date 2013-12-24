package com.haneul.bitcurrency;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewMainActivity extends Activity {

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        public void processHTML(String html)
        {
            // process the html as needed by the app
            Document doc = Jsoup.parse(html);
            String title = doc.title();
            if(title.contains("Korbit"))
            {
                Element el_usdkrw = doc.body().getElementsByClass("usdkrw").first();
                final double usdkrw = Float.parseFloat(el_usdkrw.attr("data-usdkrw"));
                Element el_finalprice = el_usdkrw.child(1);
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(el_finalprice.text().replace(",", ""));

                if(m.find())
                {
                    final double finalprice = Double.parseDouble(m.group());
                    markets[5].pushNewData(finalprice / usdkrw);
                    if(Locale.getDefault().getCountry().equals("KR"))
                    {
                       markets[5].additional = String.format("(%d 원)", (int) finalprice);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            markets[5].doneUpdate();
                        }
                    });
                }

            }
        }
    }

    private void createAdamAd()
    {
        adamView = new net.daum.adam.publisher.AdView(this);
        adamView.setClientId("5b71Z05T142cabbaa7f");
        adamView.setVisibility(View.VISIBLE);
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.addView(adamView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        adamView.setLayoutParams(params);
    }

    private void createGoogleAd()
    {
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-1119778000865171/3071782262");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        adView.setLayoutParams(params);
        adView.loadAd(adRequest);
    }

    private boolean localeKR = false;
    private AdView adView;
    private net.daum.adam.publisher.AdView adamView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newactivity_main);
        localeKR = Locale.getDefault().getCountry().equals("KR");
        timeTextView = (TextView) findViewById(R.id.updated_time);
        marketView = (ListView) findViewById(R.id.list_view);
        markets = new Market[] {
                new Market("CoinBase"), new Market("MtGox"), new Market("BTC-e"), new Market("Kraken"),
                new Market("BTCChina"), new Market(getResources().getString(R.string.korbit)),
        };
        markets[0].getNewData =  new Runnable() {
        @Override
        public void run() {
            new CoinBaseTask().execute(markets[0]);
        }};

        markets[1].getNewData = new Runnable() {
            @Override
            public void run() {
                new MtGoxTask().execute(markets[1]);
            }};

        markets[2].getNewData =  new Runnable() {
            @Override
            public void run() {
                new BTCTask().execute(markets[2]);
            }};

        markets[3].getNewData =  new Runnable() {
            @Override
            public void run() {
                new KrakenTask().execute(markets[3]);
            }};

        markets[4].getNewData =  new Runnable() {
            @Override
            public void run() {
                new BTCChinaTask().execute(markets[4]);
            }};

        markets[5].getNewData = new Runnable() {
            @Override
            public void run() {
                korbitView.loadUrl("https://www.korbit.co.kr");
            }};

        korbitView = (WebView) findViewById(R.id.korbitView);

        korbitView.getSettings().setJavaScriptEnabled(true);
        korbitView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        korbitView.setWebViewClient(new WebViewClient(){
            boolean loadingFinished = false;
            boolean redirect = false;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                korbitView.loadUrl(urlNewString);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!redirect){
                    loadingFinished = true;
                }

                if(loadingFinished && !redirect){
                    korbitView.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByTagName('html')[0].innerHTML);");
                } else{
                    redirect = false;
                }

            }
        });


        MarketAdapter adapter = new MarketAdapter(this, markets);
        marketView.setAdapter(adapter);

        final ImageButton im = (ImageButton) findViewById(R.id.refresh);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveData();
            }
        });

        if(localeKR) createAdamAd();
        else createGoogleAd();
    }

    private WebView korbitView;

    private Timer timer;
    private TextView timeTextView;
    private void retrieveData()
    {
        for(Market m:markets)
        {
            m.getNewData();
        }
        timeTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = new Timer();
        TimerTask r = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        retrieveData();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(r, 0, 5 * 60 * 1000);
        if(!localeKR) adView.resume();
        else adamView.resume();
    }

    @Override
    public void onPause() {

        if(!localeKR) {
            adView.pause();
        }
        else
        {
            adamView.pause();
        }

        timer.cancel();
        timer.purge();
        timer = null;
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
    private Market [] markets;
    private ListView marketView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
