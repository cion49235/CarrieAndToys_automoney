package kr.co.inno.autocash.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;

import kr.co.inno.autocash.RestartLoginReceiver;

@SuppressLint({"InflateParams"})
public class AutoLoginServiceActivity extends Service {
    public static Context context;
    private static WebView webview = null;
    private String loginID = "";
    private static String authuser = "";
    private static String googlePasswd = "";

    private Intent intent;
    private AlarmManager am;
    private PendingIntent sender;
    private long interval = 1000 * 60;

    public void onCreate() {
        super.onCreate();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = this;
        user_info();

        startCall(true);
    }

    private void user_info(){
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        authuser = prefs.getString("authuser", "");
        loginID = prefs.getString("loginID", "");
        googlePasswd = prefs.getString("googlePasswd", "");

        Log.d("Cashmine", "Cashmine AutoLoginActivity authuser : " + authuser);
        Log.d("Cashmine", "Cashmine AutoLoginActivity loginID : " + loginID);
        Log.d("Cashmine", "Cashmine AutoLoginActivity googlePasswd : " + googlePasswd);
    }

    // 서비스가 호출될때마다 매번 실행
    public int onStartCommand(Intent intent, int flags, int startId) {

        int i = super.onStartCommand(intent, flags, startId);

        user_info();
        if ( authuser.equals("1") ) {
            sendURL();
        }

        startForeground(0,new Notification());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startCall(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startCall(true);
        }

        return START_STICKY;
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    public void sendURL()
    {
        if (webview == null) {
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
        webview.loadUrl("https://accounts.google.com/ServiceLogin?service=googleplay&passive=0&authuser=0");
    }

    private class ProxyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
            if( url.startsWith("https://www.google.com/favicon.ico") ) {
                Log.d("Cashmine", "Cashmine AutoLoginServiceActivity url : " + url);
                if ( authuser.equals("1") && !googlePasswd.equals("") ) {
                    super.onLoadResource(view, url);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("Cashmine", "Cashmine AutoLoginActivity URL3_LOGIN : 2");
                    String pwd = googlePasswd;
                    //String js = "javascript:document.getElementById('Passwd').value = '" + pwd + "';";
                    String js = "javascript:document.getElementsByName('password')[0].value = '" + pwd + "';";
                    if ( Build.VERSION.SDK_INT >= 19 ) {
                        view.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                            }
                        });
                    } else {
                        view.loadUrl(js);
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("Cashmine", "Cashmine AutoLoginActivity URL3_LOGIN : 3");
                    //webview.loadUrl("javascript:document.getElementById('gaia_loginform').submit();");
                    webview.loadUrl("javascript:document.getElementsByClassName('RveJvd snByac')[0].click()");
                    close_autolayout();
                }
            }
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    public void startCall(Boolean isOn) {
        Calendar calendar = Calendar.getInstance();
        intent = new Intent(context, RestartLoginReceiver.class);
        intent.setAction(RestartLoginReceiver.ACTION_RESTART_SERVICE);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if ( isOn ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, sender);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, sender);
            } else {
                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
            }
        } else {
            am.cancel(sender);
        }
    }

    private void close_autolayout(){
        authuser = "0";
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("authuser", "0");
        editor.commit();
    }

    // 서비스가 종료될때 실행
    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
