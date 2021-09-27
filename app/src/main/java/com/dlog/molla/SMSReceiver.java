package com.dlog.molla;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.List;

/**
 * <pre>
 *     새 SMS 메시지 도착을 수신하는 BroadcastReceiver
 *     BroadcastReceiver를 상속한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see BroadcastReceiver
 * @see com.dlog.molla.MMSReceiver
 */
public class SMSReceiver extends BroadcastReceiver {
    /**
     * SMSReceiver가 등록될 Context
     */
    private Context mContext;
    /**
     * SMSReceiver가 등록될 Context의 이름
     */
    private String mContextName ;
    public SMSReceiver(String contextName){
        this.mContextName = contextName;
    }

    /**
     * <pre>
     *     sms 메시지를 수신하면 호출된다.
     *     Runnable을 이용하여 백그라운드에서 SMS 데이터베이스에 접근한다.
     *     handler는 6000 milliseconds 대기하다가 백그라운 작업을 수행한다.
     *     (메시지가 실제 데이터베이스에 저장될 때 까지 약간의 시간이 소요된다. 일정한 시간이 아니기때문에 실험상 가장 적절한 시간이 6000 milliseconds 였다.)
     * </pre>
     * @param context SMSReceiver가 등록된 context
     * @param intent 시스템에서 보낸 Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMSReceiver","onReceive start");
        mContext= context;

        Runnable runn = new Runnable()
        {
            @Override
            public void run()
            {
                parseSMS();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runn, 6000); // 시간이 너무 짧으면 못 가져오는게 있더라
    }

    /**
     * SMS 데이터베이스에서 가장 최근의 메시지를 읽어온다.
     */
    private void parseSMS()
    {
        Log.d("SNSReceiver","parseSMS start");
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = contentResolver.query(uri, null, null, null, "_id desc limit 1");

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));//메시지 본문
        String thread_id = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
        String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));//메시지 상대방 전화번호
        cursor.close();

        Log.d("SMSReceiver parseSMS", "| " + sender +  " | " + msg);
        sendToActivity(sender,msg,thread_id);


    }

    /**
     * 새 메시지 데이터를 context에 전달한다.
     * @param sender 메시지 발신자
     * @param contents 메시지 내용
     * @param thread_id 채팅방 thread_id
     */
    private void sendToActivity(String sender, String contents,String thread_id){
        String currentClassName = getClassName();
        Log.d("SMSReciever",mContextName);
        if(contents != null) {
            switch (mContextName) {
                case "ChattingRoomActivity": {
                    if (currentClassName.equals("com.dlog.molla." + mContextName) && contents.matches("^.*?molla!.*")) {
                        Intent intent = new Intent(mContext, ChattingRoomActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Sender", sender);
                        intent.putExtra("Contents", contents);
                        boolean flag = true;
                        intent.putExtra("ReceiverFlag", flag);
                        mContext.startActivity(intent);
                    }
                    break;
                }
                case "MainActivity": {
                    if (contents.matches("^.*?molla!.*")) {
                        FragmentChatList fragmentChatList = (FragmentChatList) MainActivity.getmFragmentChatList();
                        fragmentChatList.getNewMsg(sender, thread_id, currentClassName);

                    }
                    break;
                }
            }
        }

    }

    /**
     * 현재 시점에서 화면의 맨 앞에서 running 되고 있는 Activity의 class 이름을 반환하는 메서드
     * @return 가장 맨 앞에 runnig되고 있는 Activity class name
     */
    private String getClassName(){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        Log.d("TAG","SMSReceiver runnig class name " + runningTaskInfos.get(0).topActivity.getClassName());
        return runningTaskInfos.get(0).topActivity.getClassName();
    }


}
