package carrie.toy.friends.automoney;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import carrie.toy.friends.automoney.util.PreferenceUtil;
import carrie.toy.friends.automoney.R;


public class IntroActivity extends AppCompatActivity {
    Context context;
    public Handler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);
        context = this;
        adstatus_async = new Adstatus_Async();
        adstatus_async.execute();
        handler = new Handler();
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null){
            handler.removeCallbacks(runnable);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(PreferenceUtil.getStringSharedData(context, PreferenceUtil.PREF_INTRO_STATUS, "Y").equals("Y")){
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                //fade_animation
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCN106RQxroojNzkEMuRC0BA"));
                startActivity(intent);
            }
        }
    };

    private Adstatus_Async adstatus_async = null;
    public class Adstatus_Async extends AsyncTask<String, Integer, String> {
        int ad_id;
        String ad_status;
        String download_status;
        String intro_status;
        String main_status;
        String ad_time;
        String channel;
        String channel2;
        String search_status;
        HttpURLConnection localHttpURLConnection;
        public Adstatus_Async(){
        }
        @Override
        protected String doInBackground(String... params) {
            String sTag;
            try{
                String str = "http://cion49235.cafe24.com/cion49235/carrieandtoys2_automoney/ad_status.php";
                localHttpURLConnection = (HttpURLConnection)new URL(str).openConnection();
                localHttpURLConnection.setFollowRedirects(true);
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
                        }else if(sTag.equals("intro_status")){
                            intro_status = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_INTRO_STATUS, intro_status);
                            Log.i("dsu", "intro_status : " + intro_status);
                        }else if(sTag.equals("main_status")){
                            main_status = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_MAIN_STATUS, main_status);
                            Log.i("dsu", "main_status : " + main_status);
                        }else if(sTag.equals("ad_time")){
                            ad_time = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_AD_TIME, ad_time);
                            Log.i("dsu", "ad_time : " + ad_time);
                        }else if(sTag.equals("channel")){
                            channel = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_CHANNEL, channel);
                            Log.i("dsu", "channel : " + channel);
                        }else if(sTag.equals("channel2")){
                            channel2 = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_CHANNEL2, channel2);
                            Log.i("dsu", "channel2 : " + channel2);
                        }else if(sTag.equals("search_status")){
                            search_status = xpp.nextText()+"";
                            PreferenceUtil.setStringSharedData(context, PreferenceUtil.PREF_SEARCH_STATUS, search_status);
                            Log.i("dsu", "search_status : " + search_status);
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
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(handler != null) handler.removeCallbacks(runnable);
        finish();
    }
}
