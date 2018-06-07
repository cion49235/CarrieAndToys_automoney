package kr.co.inno.autocash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.inno.autocash.service.AutoServiceActivity;

public class RestartReceiver extends BroadcastReceiver {
    private static int callingCount = 0;
    static public final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";    // 값은 맘대로

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AutoCash", "RestartReceiver Service is : " + callingCount);
        callingCount++;
        if( intent.getAction().equals(ACTION_RESTART_SERVICE) || intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ){
            intent = new Intent(context, AutoServiceActivity.class);
            //context.stopService(intent);
            context.startService(intent);
        }
    }
}