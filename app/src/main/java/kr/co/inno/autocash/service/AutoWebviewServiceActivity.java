package kr.co.inno.autocash.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint({"InflateParams"})
public class AutoWebviewServiceActivity extends Service
{
    public static Context context;

    private int callingCount = 0;
    public static String device_name = "";
    private static WebView webview = null;
    private static String ev_url;
    private static String ev_app_pkg;
    private static int chkCount = 0;
    private static int openCount = 0;
    private static int loopCount = 0;

    public void onCreate() {
        super.onCreate();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = this;

        chkCount = 0;
        openCount = 0;
        loopCount = 0;
        ev_app_pkg = "";
    }

    // 서비스가 호출될때마다 매번 실행(onResume()과 비슷)
    public int onStartCommand(Intent intent, int flags, int startId) {

        int i = super.onStartCommand(intent, flags, startId);

        try {
            ev_app_pkg = intent.getStringExtra("ev_app_pkg");
            sendURL(ev_app_pkg);
        } catch ( NullPointerException e ) {
            Log.d("Cashmine", "Cashmine AutoWebviewServiceActivity NullPointerException : " + e.toString());
        }
        return i;
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    public void sendURL(String ev_app_pkg)
    {
        if (webview == null)
        {
            webview = new WebView(context);
        }
        webview.clearHistory();
        webview.clearCache(true);
        WebSettings set = webview.getSettings();
        webview.clearCache(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setJavaScriptEnabled(true);
        webview.setWebViewClient(new ProxyWebViewClient());
        webview.setWebChromeClient(new ProxyWebChromeClient());
        webview.loadUrl("https://play.google.com/store/apps/details?id=" + ev_app_pkg);
    }

    private class ProxyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 재로그인 체크 : 2016-12-18
            Log.d("Cashmine", "Cashmine AutoWebviewServiceActivity URL1_LOGIN CHECK : authuser 1");
            if( url.startsWith("https://accounts.google.com/ServiceLogin") ) {
                Log.d("Cashmine", "Cashmine AutoWebviewServiceActivity URL1_LOGIN CHECK : authuser 2");
                SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("authuser", "1");
                editor.commit();
                Log.d("Cashmine", "Cashmine AutoWebviewServiceActivity URL1_LOGIN CHECK : authuser 3");
            }
            if( url.startsWith("https://play.google.com") ) {
                return true;
            } else {
                view.loadUrl(url);
                return true;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub
            ev_url = url;
            Log.i("Cashmine", "onLoadResource===>" + url);
            if ( device_name != null && !device_name.equals("") ) {
                //webview.loadUrl("javascript:$('div.device-selector').trigger('click');");
                //webview.loadUrl("javascript:$('span.device-title:contains(\"" + device_name + "\")').trigger('click');");
                if( url.startsWith("https://play.google.com/store/getdevicepermissions?authuser=0") ) {
                    //Log.d("AutoCash", "AutoLayoutActivity1 URL : " + url);
                } else if( url.startsWith("https://play.google.com/store/xhr/ructx?authuser=0") ) {
                    /*
                    SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("authuser", "1");
                    editor.commit();
                    */
                } else {
                    webview.loadUrl("javascript:$('button[data-uitype=221].price.buy.id-track-click.id-track-impression').trigger('click');");
                    webview.loadUrl("javascript:$('#purchase-ok-button').trigger('click');");
                }
            } else {
                if( url.startsWith("https://play.google.com/store/getdevicepermissions?authuser=0") ) {
                    //Log.d("AutoCash", "AutoLayoutActivity1 URL : " + url);
                } else if( url.startsWith("https://play.google.com/store/xhr/ructx?authuser=0") ) {
                    /*
                    SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("authuser", "1");
                    editor.commit();
                    */
                } else {
                    webview.loadUrl("javascript:$('button[data-uitype=221].price.buy.id-track-click.id-track-impression').trigger('click');");
                    webview.loadUrl("javascript:$('#purchase-ok-button').trigger('click');");
                }
            }
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private static class ProxyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress){
        }
    }

    // AsyncTask클래스는 항상 Subclassing 해서 사용 해야 함.
    // 사용 자료형은
    // background 작업에 사용할 data의 자료형: String 형
    // background 작업 진행 표시를 위해 사용할 인자: Integer형
    // background 작업의 결과를 표현할 자료형: Long
    private static class doInstall extends AsyncTask<String, Integer, Long> {
        // 이곳에 포함된 code는 AsyncTask가 execute 되자 마자 UI 스레드에서 실행됨.
        // 작업 시작을 UI에 표현하거나
        // background 작업을 위한 ProgressBar를 보여 주는 등의 코드를 작성.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // UI 스레드에서 AsynchTask객체.execute(...) 명령으로 실행되는 callback
        @Override
        protected Long doInBackground(String... strData) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    /*
                    Log.d("URL3 : ", "URL3_2 : " + ev_url);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    webview.loadUrl("javascript:$('button[data-uitype=221].price.buy.id-track-click.id-track-impression').trigger('click');");
                    */
                }
            });
            return null;
        }

        // onInBackground(...)에서 publishProgress(...)를 사용하면
        // 자동 호출되는 callback으로
        // 이곳에서 ProgressBar를 증가 시키고, text 정보를 update하는 등의
        // background 작업 진행 상황을 UI에 표현함.
        // (예제에서는 UI스레드의 ProgressBar를 update 함)
        @Override
        protected void onProgressUpdate(Integer... progress) {
            //progressBar.setProgress(progress[0]);
        }

        // onInBackground(...)가 완료되면 자동으로 실행되는 callback
        // 이곳에서 onInBackground가 리턴한 정보를 UI위젯에 표시 하는 등의 작업을 수행함.
        // (예제에서는 작업에 걸린 총 시간을 UI위젯 중 TextView에 표시함)
        @Override
        protected void onPostExecute(Long result) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    /*
                    for (int i = 0; i < 400000; i++) {
                        Log.d("AutoCash", "AutoWebviewServiceActivity URL3_1 : " + i);
                        if ( device_name != null && !device_name.equals("") ) {
                            if ( i == 250000 ) {
                                Log.d("AutoCash", "AutoWebviewServiceActivity URL3_1 : " + i);
                                webview.loadUrl("javascript:$('div.device-selector').trigger('click');");
                            }
                            if ( i == 300000 ) {
                                Log.d("AutoCash", "AutoWebviewServiceActivity URL3_2 : " + i);
                                webview.loadUrl("javascript:$('span.device-title:contains(\"" + device_name + "\")').trigger('click');");
                            }
                            if ( i == 350000 ) {
                                Log.d("AutoCash", "AutoWebviewServiceActivity URL3_3 : " + i);
                                webview.loadUrl("javascript:$('button[data-uitype=221].price.buy.id-track-click.id-track-impression').trigger('click');");
                            }
                            if ( i > 380000 && i % 50 == 0 ) {
                                for (int j = 0; j < 1000; j++) {
                                    Log.d("URL3 : ", "URL3_6 : " + j);
                                }
                                webview.loadUrl("javascript:$('#purchase-ok-button').trigger('click');");
                            }
                        } else {
                            if ( i == 330000 ) {
                                Log.d("URL3 : ", "URL4_1 : " + i);
                                webview.loadUrl("javascript:$('button[data-uitype=221].price.buy.id-track-click.id-track-impression').trigger('click');");
                            }
                            if ( i > 380000 && i % 50 == 0 ) {
                                for (int j = 0; j < 1000; j++) {
                                    Log.d("URL3 : ", "URL3_6 : " + j);
                                }
                                webview.loadUrl("javascript:$('#purchase-ok-button').trigger('click');");
                            }
                        }
                    }
                    */
                    /*
                    if ( device_name != null && !device_name.equals("") ) {
                        Log.d("URL3 : ", "URL3_device_name : " + device_name);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        webview.loadUrl("javascript:$('div.device-selector').trigger('click');");
                        webview.loadUrl("javascript:$('span.device-title:contains(\"" + device_name + "\")').trigger('click');");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        webview.loadUrl("javascript:$('#purchase-ok-button').trigger('click');");
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    */
                }
            });
        }

        // AsyncTask.cancel(boolean) 메소드가 true 인자로
        // 실행되면 호출되는 콜백.
        // background 작업이 취소될때 꼭 해야될 작업은  여기에 구현.
        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
        }
    }

    // 서비스가 종료될때 실행
    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
