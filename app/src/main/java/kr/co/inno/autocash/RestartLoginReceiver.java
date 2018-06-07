package kr.co.inno.autocash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.inno.autocash.service.AutoLoginServiceActivity;

public class RestartLoginReceiver extends BroadcastReceiver {
    private static int callingLoginCount = 0;
    static public final String ACTION_RESTART_SERVICE = "RestartLoginReceiver.restart";    // 값은 맘대로

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AutoCash", "RestartLoginReceiver Service is : " + callingLoginCount);
        callingLoginCount++;
        if( intent.getAction().equals(ACTION_RESTART_SERVICE) || intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ){
            intent = new Intent(context, AutoLoginServiceActivity.class);
            //context.stopService(intent);
            context.startService(intent);
        }
    }
}