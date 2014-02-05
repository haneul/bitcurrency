package com.haneul.bitcurrency;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.haneul.bitcurrency.util.CurrencyChange;
import com.mocoplex.adlib.AdlibActivity;
import com.mocoplex.adlib.AdlibAdViewContainer;
import com.mocoplex.adlib.AdlibConfig;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewMainActivity extends AdlibActivity
{

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
                CurrencyChange c = CurrencyChange.getCurrency(Market.CURRENCY.USD, Market.CURRENCY.KRW);
                double usdkrw = c.getVal();

                Element el_finalprice = el_usdkrw.child(1);
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(el_finalprice.text().replace(",", ""));

                if(m.find())
                {
                    final double finalprice = Double.parseDouble(m.group());
                    korbitMarket.pushNewData(finalprice);

                    //NumberFormat kr = NumberFormat.getCurrencyInstance(Locale.KOREA);
                    //korbitMarket.additional = "("+kr.format(finalprice)+")";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            korbitMarket.doneUpdate();
                        }
                    });
                }

            }
        }
    }


    protected void initAds()
    {
        // AdlibActivity 를 상속받은 액티비티이거나,
        // 일반 Activity 에서는 AdlibManager 를 동적으로 생성한 후 아래 코드가 실행되어야 합니다. (AdlibTestProjectActivity4.java)

        // Manifest 에서 <uses-permission android:name="android.permission.GET_TASKS" /> 부분 권한 추가를 확인해주세요.

        // 광고 스케줄링 설정을 위해 아래 내용을 프로그램 실행시 한번만 실행합니다. (처음 실행되는 activity에서 한번만 호출해주세요.)
        // 광고 subview 의 패키지 경로를 설정합니다. (실제로 작성된 패키지 경로로 수정해주세요.)

        // 쓰지 않을 광고플랫폼은 삭제해주세요.
        AdlibConfig.getInstance().bindPlatform("ADAM","com.haneul.bitcurrency.ads.SubAdlibAdViewAdam");
        AdlibConfig.getInstance().bindPlatform("ADMOB","com.haneul.bitcurrency.ads.SubAdlibAdViewAdmob");
        /*AdlibConfig.getInstance().bindPlatform("CAULY","test.adlib.project.ads.SubAdlibAdViewCauly");
        AdlibConfig.getInstance().bindPlatform("TAD","test.adlib.project.ads.SubAdlibAdViewTAD");
        AdlibConfig.getInstance().bindPlatform("NAVER","test.adlib.project.ads.SubAdlibAdViewNaverAdPost");
        AdlibConfig.getInstance().bindPlatform("SHALLWEAD","test.adlib.project.ads.SubAdlibAdViewShallWeAd");
        AdlibConfig.getInstance().bindPlatform("INMOBI","test.adlib.project.ads.SubAdlibAdViewInmobi");
        AdlibConfig.getInstance().bindPlatform("MMEDIA","test.adlib.project.ads.SubAdlibAdViewMMedia");
        AdlibConfig.getInstance().bindPlatform("MOBCLIX","test.adlib.project.ads.SubAdlibAdViewMobclix");
        AdlibConfig.getInstance().bindPlatform("ADMOBECPM","test.adlib.project.ads.SubAdlibAdViewAdmobECPM");
        AdlibConfig.getInstance().bindPlatform("UPLUSAD","test.adlib.project.ads.SubAdlibAdViewUPlusAD");
        AdlibConfig.getInstance().bindPlatform("MEZZO","test.adlib.project.ads.SubAdlibAdViewMezzo");
        AdlibConfig.getInstance().bindPlatform("AMAZON","test.adlib.project.ads.SubAdlibAdViewAmazon");
        AdlibConfig.getInstance().bindPlatform("ADHUB","test.adlib.project.ads.SubAdlibAdViewAdHub");*/
        // 쓰지 않을 플랫폼은 JAR 파일 및 test.adlib.project.ads 경로에서 삭제하면 최종 바이너리 크기를 줄일 수 있습니다.

        // SMART* dialog 노출 시점 선택시 / setAdlibKey 키가 호출되는 activity 가 시작 activity 이며 해당 activity가 종료되면 app 종료로 인식합니다.
        // adlibr.com 에서 발급받은 api 키를 입력합니다.
        // https://sec.adlibr.com/admin/dashboard.jsp
        // ADLIB - API - KEY 설정
        AdlibConfig.getInstance().setAdlibKey("52c58c61e4b0f34da5056016");  //  <-- 테스트 키 입니다.

    }

    AdlibAdViewContainer avc;
    Market korbitMarket;

    private void createAdlibAd()
    {
        avc = new AdlibAdViewContainer(this);
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.addView(avc);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        avc.setLayoutParams(params);
        bindAdsContainer(avc);
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

    private AdView adView;
    private net.daum.adam.publisher.AdView adamView;
    private int update_rate_mins = 5;

    private void initMarkets()
    {
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
        markets.clear();
        if(sharedprefs.getBoolean("bitstamp_btcusd", true))
        {
            final Market m = new Market("BitStamp", "BTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new BitStamp().execute(m);
                }};
            markets.add(m);
        }
        if(sharedprefs.getBoolean("coinbase_btcusd", true))
        {
            final Market m = new Market("CoinBase", "BTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new CoinBaseTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("mtgox_btcusd", true))
        {
            final Market m = new Market("MtGox", "BTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new MtGoxTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("btce_btcusd", false))
        {
            final Market m = new Market("BTC-e", "BTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new BTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("kraken_btcusd", false))
        {
            final Market m = new Market("Kraken", "BTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new KrakenTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("btcchina_btccyn", true))
        {
            final Market m = new Market("BTCChina", "BTC/CNY", Market.CURRENCY.CNY);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new BTCChinaTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("huobi_btccyn", true))
        {
            final Market m = new Market("Huobi", "BTC/CNY", Market.CURRENCY.CNY);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new HuobiTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("korbit_btckrw", true))
        {
            final Market m = new Market(getResources().getString(R.string.korbit), "BTC/KRW", Market.CURRENCY.KRW);
            m.getNewData = new Runnable() {
                @Override
                public void run() {

                    korbitView.loadUrl("https://www.korbit.co.kr");
                }};
            korbitMarket = m;
            markets.add(m);
        }

        if(sharedprefs.getBoolean("ddengle_btckrw", true))
        {
            final Market m = new Market(getResources().getString(R.string.ddengle), "BTC/KRW", Market.CURRENCY.KRW);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new DdengleBTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("xcoin_btckrw", true))
        {
            final Market m = new Market("XCoin", "BTC/KRW", Market.CURRENCY.KRW);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new XCoinBTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("btce_ltcusd", true))
        {
            final Market m = new Market("BTC-e", "LTC/USD");
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new LTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("ddengle_ltckrw", true))
        {
            final Market m = new Market(getResources().getString(R.string.ddengle), "LTC/KRW", Market.CURRENCY.KRW);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new DdengleLTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("btce_btcltc", true))
        {
            final Market m = new Market("BTC-e", "BTC/LTC", Market.CURRENCY.LTC);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new BTCeBTCLTCTask().execute(m);
                }};
            markets.add(m);
        }

        if(sharedprefs.getBoolean("btce_ltcusd", true))
        {
            final Market m = new Market("Kraken", "XBT/LTC", Market.CURRENCY.LTC);
            m.getNewData = new Runnable() {
                @Override
                public void run() {
                    new KrakenBTCLTCTask().execute(m);
                }};
            markets.add(m);
        }

        MarketAdapter adapter = new MarketAdapter(this, markets);
        marketView.setAdapter(adapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newactivity_main);
        timeTextView = (TextView) findViewById(R.id.updated_time);
        marketView = (ListView) findViewById(R.id.list_view);

        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor ed = sharedprefs.edit();
        ed.putInt("prefV", 1);
        ed.commit();

        Market.SetDisplayCurrency(sharedprefs.getString("display_currency", "usd"));

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

        initMarkets();

        initAds();
        createAdlibAd();
        //SharedPreferences shareprefs = PreferenceManager.getDefaultSharedPreferences(this);
        update_rate_mins = Integer.parseInt(sharedprefs.getString("refresh_preference","5"));
        //if(localeKR) createAdamAd();
        //else createGoogleAd();
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
        timeTextView.setText(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String disp_cur = sharedprefs.getString("display_currency", "usd");
        if(!Market.GetDisplayCurrencyName().equals(disp_cur))
        {
            Market.SetDisplayCurrency(disp_cur);
            initMarkets();
        }

        update_rate_mins = Integer.parseInt(sharedprefs.getString("refresh_preference","5"));
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
        if(update_rate_mins != -1) {
            timer = new Timer();
            timer.scheduleAtFixedRate(r, 0, update_rate_mins * 60 * 1000);
        }

        //if(!localeKR) adView.resume();
        //else adamView.resume();
    }

    @Override
    public void onPause() {

//        if(!localeKR) {
//            adView.pause();
//        }
//        else
//        {
//            adamView.pause();
//        }
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
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
    private List<Market> markets = new ArrayList<Market>();
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
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_refresh:
                retrieveData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
