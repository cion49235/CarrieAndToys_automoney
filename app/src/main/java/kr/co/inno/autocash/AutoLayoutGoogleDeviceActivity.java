package kr.co.inno.autocash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import carrie.toy.friends.automoney.R;


/**
 * Created by byapps on 2016. 3. 16..
 */
public class AutoLayoutGoogleDeviceActivity extends Activity {

    public static Activity prevActivity;

    public static Context context;
    private Dialog dialog;
    WebView webview;

    private MyJavaScriptInterface myJavaScriptInterface;

    ProgressDialog dialogLoading;
    private int isStep = 0;
    private int isResult = 0;
    private int isSearch = 0; // 검색처리 완료일경우 처리

    private String loginID = "";
    private String memtype = "";
    private boolean isLogin = false;
    private String device = "";
    private String device_name = "";

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.5f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_devicelayout);
        context = this;
        prevActivity = this;

        dialog = new Dialog(context, R.style.NewDialog);
        View v = LayoutInflater.from(context).inflate(R.layout.progress_circle,null);
        dialog.setContentView(v);

        dialogLoading = ProgressDialog.show(AutoLayoutGoogleDeviceActivity.this, "", "구글 아이디 기기명 수집중입니다.", true);

        user_info();

        webview = (WebView)findViewById(R.id.autoWebView);
        WebSettings set = webview.getSettings();
        webview.clearHistory();
        webview.clearCache(true);
        webview.clearView();
        //webview.setBackgroundColor(Color.TRANSPARENT);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setJavaScriptEnabled(true);
        set.setDomStorageEnabled(true);
        webview.setWebViewClient(new ProxyWebViewClient());
        webview.setWebChromeClient(new ProxyWebChromeClient());
        myJavaScriptInterface = new MyJavaScriptInterface(AutoLayoutGoogleDeviceActivity.this, webview); //JavascriptInterface 객체화
        webview.addJavascriptInterface(myJavaScriptInterface, "Android"); //웹뷰에 JavascriptInterface를 연결
        webview.loadUrl("https://play.google.com/settings");
        //webview.loadUrl("http://www.naver.com");
        //webview.loadUrl("https://byapps.co.kr");
    }

    private void user_info(){
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        loginID = prefs.getString("loginID", "");
        memtype = prefs.getString("memType", "");
        device = Build.MODEL;

        boolean chkLogin = (loginID.equals("")||memtype.equals("")) ? false:true;

        if ( isLogin != chkLogin ) {
            isLogin = chkLogin;
        }
    }

    public void send_html(String html){
        // 구글 기기명 추출
        Log.d("Automoney", "AutoLayoutGoogleDeviceActivity isStep : " + isStep);
        boolean isDevice1 = html.contains(device);
        if ( isDevice1 == true ) {
            String[] arr1 = html.split("<tbody>");
            for ( int i = 0; i < arr1.length; i++ ) {
                boolean isDevice2 = arr1[i].toString().contains(device);
                if ( isDevice2 == true ) {
                    String[] arr2 = arr1[i].split("<tr");
                    for ( int j = 0; j < arr2.length; j++ ) {
                        boolean isDevice3 = arr2[j].toString().contains(device);
                        if ( isDevice3 == true && isSearch == 0 ) {
                            Log.d("Automoney", "AutoLayoutGoogleDeviceActivity send_html 1 : " + device);
                            Log.d("Automoney", "AutoLayoutGoogleDeviceActivity send_html 2 : " + arr2[j]);
                            String[] arr3 = arr2[j].split("data-nickname=\"");
                            String[] arr4 = arr3[1].split("\"");
                            Log.d("Automoney", "AutoLayoutGoogleDeviceActivity send_html 3 : " + arr4[0]);
                            if ( !arr4[0].equals("") ) {
                                device_name = arr4[0];
                                isSearch++;
                            }
                        }
                    }
                }
            }

            if ( !device_name.equals("") && isResult == 0 ) {
                SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("device_name", device_name);
                editor.commit();
                isResult++;
            }
        }
    }

    private class ProxyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Automoney", "AutoLayoutGoogleDeviceActivity URL1 : " + url);
            if( url.startsWith("market://") ) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } else if( url.startsWith("https://play.google.com") ) {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity URL : " + url);
                view.loadUrl(url);
                return true;
            } else {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity URL : " + url);
                view.loadUrl(url);
                return true;
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if ( isStep > 3 ) {
                isStep++;
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity isStep : " + isStep);
            }
            if ( isStep == 5 && isResult == 0 ) {
                isStep++;
            }
            if ( isStep == 3 ) {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 0 : " + url);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 0 : " + device_name);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 0 : " + isStep);

                isStep++;
                view.loadUrl("javascript:window.Android.getDeviceHtml(document.getElementsByTagName('html')[0].innerHTML);");
            }
            if ( isStep == 2 ) {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 1 : " + url);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 1 : " + device_name);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 1 : " + isStep);

                isStep++;
                view.loadUrl("javascript:window.Android.getDeviceHtml(document.getElementsByTagName('html')[0].innerHTML);");
            }
            if ( isStep == 1 ) {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 2 : " + url);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 2 : " + device_name);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 2 : " + isStep);

                isStep++;
                view.loadUrl("javascript:window.Android.getDeviceHtml(document.getElementsByTagName('html')[0].innerHTML);");
            }
            if ( isStep == 0 ) {
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 3 : " + url);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 3 : " + device_name);
                Log.d("Automoney", "AutoLayoutGoogleDeviceActivity 3 : " + isStep);

                isStep++;
                view.loadUrl("javascript:window.Android.getDeviceHtml(document.getElementsByTagName('html')[0].innerHTML);");
            }
            super.onPageFinished(view, url);
        }
    }

    private class ProxyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress){
            if (progress == 100) {
                try {
                    dialogLoading.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.d("Automoney", "AutoLayoutGoogleDeviceActivity IllegalArgumentException : " + e.toString());
                }
            } else {
                if(!dialogLoading.isShowing()) {
                    try {
                        dialogLoading.show();
                    } catch (WindowManager.BadTokenException e) {
                        Log.d("Automoney", "AutoLayoutGoogleDeviceActivity BadTokenException : " + e.toString());
                    }
                }
                if(dialogLoading.isShowing()) {
                   /* final ProgressWheel wheel = (ProgressWheel)dialog.findViewById(R.id.progressBar1);
                    wheel.setCallback(new ProgressWheel.ProgressCallback() {
                        @Override
                        public void onProgressUpdate(float progress) {
                            if(progress == 1.0f) {
                                try {
                                    dialog.dismiss();
                                } catch (IllegalArgumentException e) {
                                    Log.d("Automoney", "AutoLayoutGoogleDeviceActivity IllegalArgumentException : " + e.toString());
                                }
                                try {
                                    dialogLoading.dismiss();
                                } catch (IllegalArgumentException e) {
                                    Log.d("Automoney", "AutoLayoutGoogleDeviceActivity IllegalArgumentException : " + e.toString());
                                }
                            }
                        }
                    });
                    wheel.setProgress(1.0f);*/
                }
            }
        }
    }

    private void close_autolayout(){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        close_autolayout();
    }
}
