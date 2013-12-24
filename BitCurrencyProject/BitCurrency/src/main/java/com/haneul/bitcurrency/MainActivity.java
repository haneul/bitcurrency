package com.haneul.bitcurrency;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.android.gms.ads.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdView;

//import net.daum.adam.publisher.*;


public class MainActivity extends Activity {
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
                    korbitTextView.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if(Locale.getDefault().getCountry().equals("KR"))
                            {
                                korbitTextView.setText(String.format("$ %.2f", finalprice / usdkrw));
                                korbitTextView2.setText(String.format("(%d 원)", (int) finalprice));
                            }
                            else
                            {
                                korbitTextView.setText(String.format("$ %.2f", finalprice / usdkrw));
                            }
                        }
                    });


                }

            }
        }
    }

    private void resetData() {
        coinbaseTextView.setText("Loading...");
        btcTextView.setText("Loading...");
        korbitTextView.setText("Loading...");
        if(Locale.getDefault().getCountry().equals("KR"))
        {
            krakenTextView2.setVisibility(View.VISIBLE);
            korbitTextView2.setText("");
            krakenTextView2.setText("");
        }
        mtgoxTextView.setText("Loading...");
        krakenTextView.setText("Loading...");
    }

    private boolean localeKR = false;

    public void retrieveData(boolean manual) {
        resetData();
        CoinBaseTask task = new CoinBaseTask();
        //task.execute(coinbaseTextView);
        korbitView.loadUrl("https://www.korbit.co.kr");


        //KrakenTask ktask = new KrakenTask();
        //ktask.execute(krakenTextView, krakenTextView2);

        timeTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
    }

    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        localeKR = Locale.getDefault().getCountry().equals("KR");
        coinbaseTextView = (TextView) findViewById(R.id.textCoinBaseCur);
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

        korbitTextView = (TextView) findViewById(R.id.textKorbitCur);
        korbitTextView2 = (TextView) findViewById(R.id.textKorbitCur2);
        timeTextView = (TextView) findViewById(R.id.updated_time);
        btcTextView = (TextView) findViewById(R.id.textBTCCur);
        mtgoxTextView = (TextView) findViewById(R.id.textMTGoxCur);
        krakenTextView = (TextView) findViewById(R.id.textKrakenCur);
        krakenTextView2 = (TextView) findViewById(R.id.textKrakenCur2);

        final ImageButton im = (ImageButton) findViewById(R.id.refresh);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveData(true);
            }
        });

        if(localeKR) createAdamAd();
        else createGoogleAd();
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

    private TextView timeTextView;
    private TextView coinbaseTextView;
    private TextView korbitTextView;
    private TextView korbitTextView2;
    private TextView mtgoxTextView;
    private TextView krakenTextView;
    private TextView krakenTextView2;

    private WebView korbitView;
    private TextView btcTextView;
    private AdView adView;
    private net.daum.adam.publisher.AdView adamView;

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
        super.onPause();
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
                        retrieveData(false);
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(r, 0, 5*60*1000);
        if(!localeKR) adView.resume();
        else adamView.resume();
    }

    @Override
    public void onDestroy() {
        if(adView != null) adView.destroy();
        if(adamView != null) adamView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
