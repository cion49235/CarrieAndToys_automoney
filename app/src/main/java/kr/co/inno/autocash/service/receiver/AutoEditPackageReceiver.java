package kr.co.inno.autocash.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.inno.autocash.service.AutoCompleteServiceActivity;

public class AutoEditPackageReceiver extends BroadcastReceiver {
    private Intent intent;

    private void packageADD(Context context, String packagename)
    {
        /*
        if (SharedPreferenceUtils.getAllInstallKeyData(paramContext).contains(paramString))
        {
            ADInfoData localADInfoData = SharedPreferenceUtils.LoadInstallValueData(paramContext, paramString);
            SharedPreferenceUtils.SaveAppListData(paramContext, paramString, localADInfoData);
            if (localADInfoData.getAdType() == 11)
            {
                paramContext.startService(new Intent(paramContext, AutoCashCompleteService.class));
                SharedPreferenceUtils.RemoveInstallData(paramContext, paramString);
            }
            paramString = new Intent();
            paramString.putExtra("packagename", "");
            paramString.setAction(String.valueOf(234089));
            paramContext.sendBroadcast(paramString);
        }
        */
        if ( !packagename.equals("") ) {
            Log.d("AutoEditPackageReceiver", "packageADD : " + packagename);

            intent = new Intent(context, AutoCompleteServiceActivity.class);
            intent.putExtra("ev_type", "packageADD");
            intent.putExtra("ev_app_pkg", packagename);
            context.startService(intent);
        }
    }

    private void packageRemove(Context context, String packagename)
    {
        /*
        SharedPreferenceUtils.RemoveInstallData(paramContext, paramString);
        SharedPreferenceUtils.RemoveAppListData(paramContext, paramString);
        Intent localIntent = new Intent();
        localIntent.setAction(String.valueOf(492139));
        localIntent.putExtra("packagename", paramString);
        paramContext.sendBroadcast(localIntent);
        paramString = new Intent();
        paramString.putExtra("packagename", "");
        paramString.setAction(String.valueOf(234089));
        paramContext.sendBroadcast(paramString);
        */

        if ( !packagename.equals("") ) {
            Log.d("AutoEditPackageReceiver", "packageRemove : " + packagename);

            intent = new Intent(context, AutoCompleteServiceActivity.class);
            intent.putExtra("ev_type", "packageRemove");
            intent.putExtra("ev_app_pkg", packagename);
            context.startService(intent);
        }
    }

    public void onReceive(Context content, Intent intent)
    {
        if ((intent != null) && (intent.getAction() != null)) {
            if ( intent.getAction().equals("android.intent.action.PACKAGE_ADDED") ) {
                Log.d("AutoEditPackageReceiver", "onReceive PACKAGE_ADDED : " + intent.getAction() + " ? " + intent.getData().toString());
                packageADD(content, intent.getData().toString().replaceAll("package:", ""));
            } else if ( intent.getAction().equals("android.intent.action.PACKAGE_REMOVED") ) {
                Log.d("AutoEditPackageReceiver", "onReceive PACKAGE_REMOVED : " + intent.getAction() + " ? " + intent.getData().toString());
                packageRemove(content, intent.getData().toString().replaceAll("package:", ""));
            }
        }
    }
}