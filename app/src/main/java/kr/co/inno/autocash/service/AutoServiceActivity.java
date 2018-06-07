package kr.co.inno.autocash.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import carrie.toy.friends.automoney.player.BasePlayer;
import kr.co.inno.autocash.Autoapp_DBopenHelper;
import kr.co.inno.autocash.RestartReceiver;
import kr.co.inno.autocash.cms.AppData;
import carrie.toy.friends.automoney.App;
import carrie.toy.friends.automoney.util.PreferenceUtil;

@SuppressLint({"InflateParams"})
public class AutoServiceActivity extends Service
{
    public static Context context;

    private int callingCount = 0;
    private String loginID = "";
    private String memtype = "";
    private String google_id = "";
    private String trhead_google_id = "";
    private String uid = "";
    private String device = "";
    private String authuser = "";
    private String mb_google_id = "";
    private boolean isLogin = false;
    private String currentHour;

    private Intent intent;
    private AlarmManager am;
    private PendingIntent sender;
    private long interval = 1000 * 10;
    private InterstitialAd mInterstitialAd;
    private AudioManager audiomanager;
    HttpURLConnection localHttpURLConnection = null;
    public void onCreate() {
        super.onCreate();
		context = this;
        startCall(true);
        mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-4092414235173954/8663840734");
        if(PreferenceUtil.getStringSharedData(context, PreferenceUtil.PREF_AD_STATUS, "N").equals("Y")){
                addInterstitialView();
        }
		audiomanager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Log.d("AutoCash", "AutoServiceActivity : Service is Created");
    }
    // 서비스가 호출될때마다 매번 실행(onResume()과 비슷)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(0,new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("AutoCash", "AutoServiceActivity Version : " + Build.VERSION.SDK_INT);
            Log.d("AutoCash", "AutoServiceActivity Version M : " + Build.VERSION_CODES.M);
            startCall(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d("AutoCash", "AutoServiceActivity Version : " + Build.VERSION.SDK_INT);
            Log.d("AutoCash", "AutoServiceActivity Version KITKAT : " + Build.VERSION_CODES.KITKAT);
            startCall(true);
        }

        /*new Thread() {
            public void run() {
                try {
                    getIdThread();
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO Auto-generated catch block
                    Log.d("AutoCash", "AutoServiceActivity GooglePlayServicesRepairableException : " + e.toString());
                }
            }
        }.start();*/

        Log.d("AutoCash", "AutoServiceActivity Service is onStartCommand : " + callingCount);
        user_info();
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        String loginID = prefs.getString("loginID", "");

        // 현재 시간을 저장 한다.
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH");
        currentHour = sdfNow.format(date);
        auto_count++;
        Log.i("dsu", "auto_count : " + auto_count + "\nad_view : " + PreferenceUtil.getBooleanSharedData(context, PreferenceUtil.PREF_AD_VIEW, false) + "\nad_time : " + Integer.parseInt(PreferenceUtil.getStringSharedData(context, PreferenceUtil.PREF_AD_TIME, "250")));
        if(auto_count == Integer.parseInt(PreferenceUtil.getStringSharedData(context, PreferenceUtil.PREF_AD_TIME, "150"))){
            auto_count = 1;
                if(PreferenceUtil.getBooleanSharedData(context, PreferenceUtil.PREF_AD_VIEW, false) == true) {
                    if(PreferenceUtil.getStringSharedData(context, PreferenceUtil.PREF_AD_STATUS, "N").equals("Y")){
                        addInterstitialView();
                }
            }
        }
        callingCount++;
        return START_STICKY;
    }
    
    private void loadIntersitialAd(){
        AdRequest interstitialRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(interstitialRequest);
    }
    
    private void addInterstitialView() {
    	mInterstitialAd.loadAd(new AdRequest.Builder().build());
		mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            	loadIntersitialAd();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            	loadIntersitialAd();
            }

            @Override
            public void onAdOpened() {
//            	audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            	loadIntersitialAd();
            }
        });
        
        if (mInterstitialAd.isLoaded()) {
        	mInterstitialAd.show();
        }
    }
    
    private Adstatus_Async adstatus_async = null;
    public class Adstatus_Async extends AsyncTask<String, Integer, String> {
        int ad_id;
        String ad_status;
        String ad_time;
        String download_status;
        HttpURLConnection localHttpURLConnection = null;
        public Adstatus_Async(HttpURLConnection localHttpURLConnection){
            this.localHttpURLConnection = localHttpURLConnection;
        }
        @Override
        protected String doInBackground(String... params) {
            String sTag;
            try{
            	String str = "http://cion49235.cafe24.com/cion49235/carrieandplay_automoney/ad_status.php";
                localHttpURLConnection = (HttpURLConnection)new URL(str).openConnection();
                HttpURLConnection.setFollowRedirects(false);
                localHttpURLConnection.setConnectTimeout(15000);
                localHttpURLConnection.setReadTimeout(15000);
                localHttpURLConnection.setRequestMethod("GET");
                localHttpURLConnection.connect();
                InputStream inputStream = new URL(str).openStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(inputStream, "EUC-KR"); 
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    }else if (eventType == XmlPullParser.END_DOCUMENT) {
                    }else if (eventType == XmlPullParser.START_TAG){
                        sTag = xpp.getName();
                        if(sTag.equals("Ad")){
                            ad_id = Integer.parseInt(xpp.getAttributeValue(null, "ad_id") + "");
                        }else if(sTag.equals("ad_status")){
                            ad_status = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_AD_STATUS, ad_status);
                            Log.i("dsu", "ad_status : " + ad_status);
                        }else if(sTag.equals("download_status")){
                            download_status = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_DOWNLOAD_STATUS, download_status);
                            Log.i("dsu", "download_status : " + download_status);
                        }else if(sTag.equals("ad_time")){
                            ad_time = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_AD_TIME, ad_time);
                            Log.i("dsu", "ad_time : " + ad_time);
                        }
                    } else if (eventType == XmlPullParser.END_TAG){
                        sTag = xpp.getName();
                        if(sTag.equals("Finish")){

                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                    }
                    eventType = xpp.next();
                }
            }
            catch (SocketTimeoutException localSocketTimeoutException)
            {
            }
            catch (ClientProtocolException localClientProtocolException)
            {
            }
            catch (IOException localIOException)
            {
            }
            catch (Resources.NotFoundException localNotFoundException)
            {
            }
            catch (NullPointerException NullPointerException)
            {
            }
            catch (Exception e)
            {
            }
            return ad_status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String ad_status) {
            super.onPostExecute(ad_status);
            try{
                if(ad_status != null){
//                    if(ad_status.equals("Y")){
                        if(adstatus_async != null)
                            adstatus_async.cancel(true);
                        if(localHttpURLConnection != null)
                            localHttpURLConnection.disconnect();
                            localHttpURLConnection = null;
                        addInterstitialView();
//                    }
                }
            }catch(NullPointerException e){
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }


    int auto_count = 0;
    public void getIdThread() throws GooglePlayServicesRepairableException {
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
        } catch (IOException e) {
            Log.d("AutoCash", "AutoServiceActivity IOException : " + e.toString());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d("AutoCash", "AutoServiceActivity GooglePlayServicesNotAvailableException : " + e.toString());
        } catch (IllegalStateException e) {
            Log.d("AutoCash", "AutoServiceActivity IllegalStateException : " + e.toString());
        } catch (GooglePlayServicesRepairableException e) {
            Log.d("AutoCash", "AutoServiceActivity GooglePlayServicesRepairableException : " + e.toString());
        }

        trhead_google_id = adInfo.getId();
        final boolean isLAT = adInfo.isLimitAdTrackingEnabled();

        if ( trhead_google_id == null ) {
        } else if ( !trhead_google_id.equals(google_id) ) {
            SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("google_id", trhead_google_id);
            editor.commit();
        }

        String imei = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch(SecurityException e) {
            Log.d("AutoCash", "AutoServiceActivity SecurityException : " + e.toString());
        }

        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("uid", imei);
        editor.commit();
    }

    // 데이터 가져오기
    public String aai_seq = "empty";
    public static Autoapp_DBopenHelper autoapp_mydb;
    private void getData() {
        /** 통신처리 */
    	autoinstall_async = new Autoinstall_Async();
    	autoinstall_async.execute();
    }
    private Autoinstall_Async autoinstall_async = null;
    public class Autoinstall_Async extends AsyncTask<String, Integer, String>{
		AppData app_data;
		ArrayList<AppData> list;
		ArrayList<AppData> menuItems = new ArrayList<AppData>();
		int aai_seq;
		String aai_title;
		String aai_link_url;
		String aai_status;
		public Autoinstall_Async(){
		}
			@Override
			protected String doInBackground(String... params) {
				String sTag;
				try{
		           String str = "";
		           HttpURLConnection localHttpURLConnection = (HttpURLConnection)new URL(str).openConnection();
		           HttpURLConnection.setFollowRedirects(false);
		           localHttpURLConnection.setConnectTimeout(15000);
		           localHttpURLConnection.setReadTimeout(15000); 
		           localHttpURLConnection.setRequestMethod("GET");
		           localHttpURLConnection.connect();
		           InputStream inputStream = new URL(str).openStream(); //open Stream을 사용하여 InputStream을 생성합니다.
		           XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
		           XmlPullParser xpp = factory.newPullParser();
		           xpp.setInput(inputStream, "EUC-KR"); //euc-kr로 언어를 설정합니다. utf-8로 하니깐 깨지더군요
		           int eventType = xpp.getEventType();
		           while (eventType != XmlPullParser.END_DOCUMENT) {
			        	if (eventType == XmlPullParser.START_DOCUMENT) {
			        	}else if (eventType == XmlPullParser.END_DOCUMENT) {
			        	}else if (eventType == XmlPullParser.START_TAG){
			        		sTag = xpp.getName();
			        		if(sTag.equals("AppList")){
			        			app_data = new AppData();
			        			aai_seq = Integer.parseInt(xpp.getAttributeValue(null, "aai_seq") + "");
			            	}else if(sTag.equals("aai_title")){
			            		aai_title = xpp.nextText()+"";
			            	}else if(sTag.equals("aai_link_url")){
			            		aai_link_url = xpp.nextText()+"";
			            	}else if(sTag.equals("aai_status")){
			            		aai_status = xpp.nextText()+"";
			            	}
			        	} else if (eventType == XmlPullParser.END_TAG){
			            	sTag = xpp.getName();
			            	if(sTag.equals("AppList")){
			            		app_data.aai_seq = aai_seq;
			            		app_data.aai_title = aai_title;
			            		app_data.aai_link_url = aai_link_url;
			            		app_data.aai_status = aai_status;
			            		list.add(app_data);
			            	}
			            } else if (eventType == XmlPullParser.TEXT) {
			            }
			            eventType = xpp.next();
			        }
		         }
				 catch (SocketTimeoutException localSocketTimeoutException)
		         {
		         }
		         catch (ClientProtocolException localClientProtocolException)
		         {
		         }
		         catch (IOException localIOException)
		         {
		         }
		         catch (Resources.NotFoundException localNotFoundException)
		         {
		         }
		         catch (NullPointerException NullPointerException)
		         {
		         }
				 catch (Exception e) 
				 {
				 }
				 return aai_link_url;
			}
			
			@Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            list = new ArrayList<AppData>();
	        	list.clear();
	        }
			@Override
			protected void onPostExecute(String aai_link_url) {
				super.onPostExecute(aai_link_url);
				try{
					if(aai_link_url != null){
						if (event_count == list.size()) {
                            event_count = 0;
                        }
                        autoapp_mydb = new Autoapp_DBopenHelper(context);
                        Cursor cursor = autoapp_mydb.getReadableDatabase().rawQuery(
                                "select * from auto_app_list where aai_seq = '"+list.get(event_count).aai_seq+"'", null);
                        if(null != cursor && cursor.moveToFirst()){
                            aai_seq = cursor.getInt(cursor.getColumnIndex("aai_seq"));
                        }else{
                            aai_seq = -1;
                        }
                        if(aai_seq == -1){
                        	if(list.get(event_count).aai_status.equals("Y")) {
                        		do_autoring_service(list.get(event_count).aai_link_url);
                                event_count++;
                        	}else{
                        		event_count++;
                        	}
                        }else{
                            event_count++;
                        }
					}
				}catch(NullPointerException e){
				}finally {
					if(autoapp_mydb != null) autoapp_mydb.close();
				}
			}
			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
			}
		}

    private int isNetWork(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
//        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
//        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if ((isWifiAvailable && isWifiConnect)) {
            Log.d("AutoCash", "AutoServiceActivity isWifi : true");
            return 1;
        }
        /*else  if ((isMobileAvailable && isMobileConnect)) {
            Log.d("AutoCash", "AutoServiceActivity isMobile : true");
            return 2;
        }*/
        else{
            Log.d("AutoCash", "AutoServiceActivity isWifi : false");
            return 3;
        }
    }

    private void user_info(){
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        loginID = prefs.getString("loginID", "");
        memtype = prefs.getString("memType", "");
        google_id = prefs.getString("google_id", "");
        uid = prefs.getString("uid", "");
        authuser = prefs.getString("authuser", "");
        boolean chkLogin = (loginID.equals("")||memtype.equals("")) ? false:true;

        if ( isLogin != chkLogin ) {
            isLogin = chkLogin;
        }
    }

    int event_count = 0;
    private void do_autoring_service(String ev_app_pkg){
        boolean isInstalled = isAppInstalled(ev_app_pkg);
        if ( isInstalled == true ) {
            /*Intent intent = new Intent(context, AutoServiceActivity.class);
            context.stopService(intent);*/
        }else{
            Intent intent = new Intent(context, AutoWebviewServiceActivity.class);
            intent.putExtra("ev_app_pkg", ev_app_pkg);
            context.startService(intent);
        }
    }

    protected boolean isAppInstalled(String packageName) {
        Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void startCall(Boolean isOn) {
        Log.d("AutoCash", "AutoServiceActivity Service is AutoServiceActivity :  startCall");
        Calendar calendar = Calendar.getInstance();
        intent = new Intent(context, RestartReceiver.class);
        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
        if ( isOn ) {
            //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
            //am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, sender);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("AutoCash", "AutoServiceActivity startCall : Version M");
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, sender);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d("AutoCash", "AutoServiceActivity startCall : Version KITKAT");
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, sender);
            } else {
                Log.d("AutoCash", "AutoServiceActivity startCall : Version ETC");
                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
            }
        } else {
            Log.d("AutoCash", "AutoServiceActivity startCall : False");
            am.cancel(sender);
        }
    }

    // 서비스가 종료될때 실행
    public void onDestroy() {
        super.onDestroy();
        startCall(false);
        Log.d("AutoCash", "AutoServiceActivity Service is Destroied");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
    
	
}
