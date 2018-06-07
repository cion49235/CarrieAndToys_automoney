package kr.co.inno.autocash;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MyJavaScriptInterface {     
	private WebView mAppView;
	private Activity mContext;
	private final Handler handler = new Handler();
	private String total_html = "";
	
	public MyJavaScriptInterface(Activity activity, WebView view) {
		mAppView = view;       
		mContext = activity;    
	}
	
	@JavascriptInterface
	public void toastLong (final String message) {
		handler.post(new Runnable() {
            public void run() {
            	Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
		});
	} 
	
	@JavascriptInterface
	public void toastShort (final String message) {
		handler.post(new Runnable() {
            public void run() {
            	// Show toast for a short time       
        		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			}
		});		
	}


	@JavascriptInterface
	public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
		System.out.println(html);
		((AutoLayoutGoogleActivity) AutoLayoutGoogleActivity.context).send_html(html);
	}

	@JavascriptInterface
	public void setHtml(String passwd) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
		((AutoLayoutGoogleActivity) AutoLayoutGoogleActivity.context).set_html(passwd);
	}

	@JavascriptInterface
	public void getDeviceHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
		//System.out.println(html);
		((AutoLayoutGoogleDeviceActivity)AutoLayoutGoogleDeviceActivity.context).send_html(html);
	}
}