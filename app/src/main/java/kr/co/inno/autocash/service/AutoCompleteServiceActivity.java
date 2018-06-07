package kr.co.inno.autocash.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import kr.co.inno.autocash.Autoapp_DBopenHelper;
import kr.co.inno.autocash.PersistentCookieStore;
import kr.co.inno.autocash.cms.AppData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AutoCompleteServiceActivity extends Service
{
    public static Context context;

    private String loginID = "";
    private String memtype = "";
    private String google_id = "";
    private String uid = "";
    private String device = "";
    private boolean isLogin = false;
    public static int isApp = 0;

    String ev_type, ev_app_pkg;
    private boolean mLockListView;
    public IBinder onBind(Intent paramIntent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();

        context = this;
        Log.d("AutoComplete", "AutoServiceActivity : Service is Created");
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        int i = super.onStartCommand(intent, flags, startId);

        user_info();


//        if ( isNetWork() == 1 || isNetWork() == 2 ) {
            try {
                if ( intent.getStringExtra("ev_type") != null && !intent.getStringExtra("ev_type").equals("") ) {
                    ev_type = intent.getStringExtra("ev_type");
                    ev_app_pkg = intent.getStringExtra("ev_app_pkg");
                    Log.d("AutoComplete", "AutoServiceActivity : AutoCashCompleteService Excute");
                    Log.d("AutoComplete", "AutoServiceActivity : AutoCashCompleteService ev_type : " + ev_type);
                    Log.d("AutoComplete", "AutoServiceActivity : AutoCashCompleteService ev_app_pkg : " + ev_app_pkg);
                    if (ev_type.equals("packageADD") ) {
                    } else {
                        datacheck_async = new Datacheck_Async();
                        datacheck_async.execute();
                    }
                }
            } catch (NullPointerException e) {
                Log.d("AutoComplete", "AutoServiceActivity : AutoCashCompleteService " + e.toString());
            }
//        }
        return i;
    }

    // 데이터 가져오기
    public static Autoapp_DBopenHelper autoapp_mydb;
    private Datacheck_Async datacheck_async = null;
    public class Datacheck_Async extends AsyncTask<String, Integer, String>{
		AppData app_data;
		ArrayList<AppData> list;
		ArrayList<AppData> menuItems = new ArrayList<AppData>();
		int aai_seq;
		String aai_title;
		String aai_link_url;
		String aai_status;
		public Datacheck_Async(){
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
						for (int i = 0; i < list.size(); i++) {
                            if(list.get(i).aai_link_url.equals(ev_app_pkg)){
                                autoapp_mydb = new Autoapp_DBopenHelper(context);
                                Cursor cursor = autoapp_mydb.getReadableDatabase().rawQuery(
                                        "select * from auto_app_list where aai_seq = '"+list.get(i).aai_seq+"'", null);
                                if(null != cursor && cursor.moveToFirst()){
                                	aai_seq = cursor.getInt(cursor.getColumnIndex("aai_seq"));
                                }else{
                                    aai_seq = -1;
                                }
                                if(aai_seq == -1){
                                    ContentValues cv = new ContentValues();
                                    cv.put("aai_seq", list.get(i).aai_seq);
                                    cv.put("aai_title", list.get(i).aai_title);
                                    cv.put("aai_link_url", list.get(i).aai_link_url);
                                    cv.put("aai_status", list.get(i).aai_status);
                                    autoapp_mydb.getWritableDatabase().insert("auto_app_list", null, cv);
                                }
                                return;
                            }
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

    // 데이터 가져오기
    private void user_info(){
        SharedPreferences prefs = getSharedPreferences("kr.co.byapps", MODE_PRIVATE);
        loginID = prefs.getString("loginID", "");
        memtype = prefs.getString("memType", "");
        google_id = prefs.getString("google_id", "");
        uid = prefs.getString("uid", "");
        boolean chkLogin = (loginID.equals("")||memtype.equals("")) ? false:true;
        if ( isLogin != chkLogin ) {
            isLogin = chkLogin;
        }
    }

    private int isNetWork(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        /*boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();*/
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if ((isWifiAvailable && isWifiConnect)) {
            Log.d("AutoComplete", "AutoServiceActivity : isWifi : true");
            return 1;
        }
        /*else  if ((isMobileAvailable && isMobileConnect)) {
            Log.d("AutoComplete", "AutoServiceActivity : isMobile : true");
            return 2;
        }*/
        else{
            Log.d("AutoComplete", "AutoServiceActivity : isWifi : false");
            return 3;
        }
    }


    private void send_get_data(final String op, String url, RequestBody formBody, boolean isShow){
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);

        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String res = response.body().string();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            get_data_response(op, res);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void get_data_response(String op, String data){
        if(op.equals("complete_send")) {
            try {
                JSONObject jsonObj = new JSONObject(data);
                JSONArray responsJson = jsonObj.getJSONArray("result");
                JSONObject c = responsJson.getJSONObject(0);
                Log.d("msg", "msg : " + c.getString("msg"));
                if(c.getString("code").equals("1")){
                    String msg = c.getString("msg");
                    String result = c.getString("ev_result");
                    String result2 = c.getString("ev_result2");
                    String adid = c.getString("adid");
                    String app_id = c.getString("app_id");
                    String ev_idx = c.getString("ev_idx");
                    String uid = c.getString("uid");
                    String gid = c.getString("gid");
                    String ev_type_auto = c.getString("ev_type_auto");
                    String ev_app_pkg = c.getString("ev_app_pkg");
                    int cf_6 = Integer.parseInt(c.getString("cf_6"));

                    Log.d("AutoComplete", "AutoServiceActivity : Complete - msg : " + msg);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - result : " + result);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - result2 : " + result2);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - adid : " + adid);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - app_id : " + app_id);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - ev_idx : " + ev_idx);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - uid : " + uid);
                    Log.d("AutoComplete", "AutoServiceActivity : omplete - gid : " + gid);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - ev_type_auto : " + ev_type_auto);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - ev_app_pkg : " + ev_app_pkg);
                    Log.d("AutoComplete", "AutoServiceActivity : Complete - cf_6 : " + cf_6);
                    if ( ev_type_auto.equals("CPE") ) {
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - ev_type_auto : " + ev_type_auto);
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - ev_app_pkg : " + ev_app_pkg);

                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(ev_app_pkg);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - cf_6_1 : " + cf_6);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - cf_6_2 : " + cf_6);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - cf_6_3 : " + cf_6);
                        try {
                            Thread.sleep(1000 * cf_6);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("AutoComplete", "AutoServiceActivity : Complete - cf_6_4 : " + cf_6);
                        Log.d("AutoComplete", "CAutoServiceActivity : omplete - ev_app_pkg : " + ev_app_pkg);
                        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                        am.killBackgroundProcesses(ev_app_pkg);
                    }

                } else {
                    Log.d("AutoComplete", "AutoServiceActivity : msg : no action");
                }
            } catch (JSONException e) {
                //CommonLib.show_msg(context, getString(R.string.network_error));
            }
        } else if(op.equals("fail_send")) {
            try {
                JSONObject jsonObj = new JSONObject(data);
                JSONArray responsJson = jsonObj.getJSONArray("result");
                JSONObject c = responsJson.getJSONObject(0);
                Log.d("AutoComplete", "AutoServiceActivity : msg : " + c.getString("msg"));
                if(c.getString("code").equals("1")){
                    String msg = c.getString("msg");

                    Log.d("AutoComplete", "AutoServiceActivity : fail_send : " + msg);

                    /*if ( isApp == 1 ) {
                        ((PartActivity)PartActivity.context).load_refresh();
                        isApp = 0;
                    }*/

                } else {
                    Log.d("AutoComplete", "AutoServiceActivity : msg : no action");
                }
            } catch (JSONException e) {
                //CommonLib.show_msg(context, getString(R.string.network_error));
            }
        }
    }
}