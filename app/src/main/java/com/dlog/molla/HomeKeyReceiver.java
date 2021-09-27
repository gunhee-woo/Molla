package com.dlog.molla;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.ActivityCompat;


/**
 * 홈 버튼 클릭 이벤트를 수신하는 BroadcastReceiver 클래스
 * @author 최정헌
 * @version 1.0.0 20/04/20
 */
public class HomeKeyReceiver extends BroadcastReceiver {
    private final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG","HOME KEY");
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
            String reason= intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if(reason !=null){
                if(reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)){
                    Log.d("TAG","HOME CLICK EVENT");
                    ActivityCompat.finishAffinity((Activity)context);
                }else if(reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)){
                    Log.d("TAG","HOME LONG PRESS EVENT");
                }
            }
        }
    }
}
