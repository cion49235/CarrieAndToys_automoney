package kr.co.inno.autocash.service.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import kr.co.inno.autocash.RestartReceiver;

public class AutoReceiveActivity {

    private static AutoReceiveActivity instance;
    private static AlarmManager am;
    private static Intent intent;
    private static PendingIntent sender;
    private static long interval = 1000 * 20;

    private static final String LOG_NAME = AutoReceiveActivity.class.getSimpleName();

    private AutoReceiveActivity() {}
    public static synchronized AutoReceiveActivity getInstance() {
        if (instance == null) {
            instance = new AutoReceiveActivity();
        }
        return instance;
    }

    public void onCreate() {
        instance.onCreate();
    }

    public static void startCall(Context context) {
        Log.d(LOG_NAME, "AutoServiceActivity :  startCall");
        //Calendar calendar = Calendar.getInstance();
        intent = new Intent(context, RestartReceiver.class);
        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, sender);
    }
}
