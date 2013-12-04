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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.android.gms.ads.*;

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
                            korbitTextView.setText(String.format("$ %.2f", finalprice / usdkrw));
                            if(Locale.getDefault().getCountry().equals("KR"))
                            {
                                korbitTextView2.setText(String.format("(%d 원)", (int) finalprice));
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
            korbitTextView2.setText("");
        }
        mtgoxTextView.setText("Loading...");
    }

    private void retrieveData() {
        resetData();
        CoinBaseTask task = new CoinBaseTask();
        task.execute(coinbaseTextView);
        korbitView.loadUrl("https://www.korbit.co.kr");

        BTCTask btctask = new BTCTask();
        btctask.execute(btcTextView);

        MtGoxTask mttask = new MtGoxTask();
        mttask.execute(mtgoxTextView);

        timeTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        retrieveData();
        final ImageButton im = (ImageButton) findViewById(R.id.refresh);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveData();
            }
        });
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

    private WebView korbitView;
    private TextView btcTextView;
    private AdView adView;

    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    public void onDestroy() {
        adView.destroy();
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
