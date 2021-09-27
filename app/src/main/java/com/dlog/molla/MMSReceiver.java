package com.dlog.molla;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * BroadcastReceiver를 상속받아 디바이스에서 MMS를 받으면 수행하는 기능을 담은 클래스
 * For example:
 * <pre>
 *       MMSReceiver mmsReceiver = new MMSReceiver("MainActivity");
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class MMSReceiver extends BroadcastReceiver {

    /**
     * context
     */
    private Context mContext;
    /**
     * context class 이름
     */
    String mContextName;

    /**
     * MMSReceiver의 생성자
     * @param contextName context class 이름
     */
    public MMSReceiver(String contextName){
        this.mContextName = contextName;
    }

    /**
     * MMS를 받았을때 수행할 행동을 나타냄
     * @param $context context
     * @param $intent
     */
    @Override
    public void onReceive(Context $context, final Intent $intent)
    {

        Log.d("MMSReceiver","onReceive start");
        mContext = $context;

        Runnable runn = new Runnable()
        {
            @Override
            public void run()
            {
                parseMMS();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runn, 6000); // 시간이 너무 짧으면 못 가져오는게 있더라
    }

    /**
     * 방금 받은 MMS를 MMS데이터베이스에서 가져와 파싱을 함
     */

    private void parseMMS()
    {
        Log.d("MMSReceiver","parseMMS start");
        ContentResolver contentResolver = mContext.getContentResolver();
        final String[] projection = new String[] { "_id","thread_id" };
        Uri uri = Uri.parse("content://mms");
        Cursor cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1");

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex("_id"));
        String thread_id = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
        cursor.close();

        String sender = parseNumber(id);
        ChattingRoomItem chattingRoomItem = parseMessage(id);
        //byte[] image_bytes = parseImageBytes(id);

        /*
        TelephonyManager telephonyManager = (TelephonyManager) _context.getSystemService(TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(_context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(_context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(_context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            myNumber = telephonyManager.getLine1Number();
            if(myNumber != null) {
                if (myNumber.startsWith("+82")) {
                    myNumber = myNumber.replace("+82", "0");
                }
            }
        }*/

        Log.d("MMSReceiver parseMMS", "| " + sender +  " | " + chattingRoomItem.getmContent());
        sendToActivity(sender,chattingRoomItem.getmContent(),chattingRoomItem.getImgByteList(),thread_id);

    }

    /**
     * id를 사용하여 가장 최근에 받은 MMS의 발신자 번호를 가져와 반환
     * @param $id 가장 최근 MMS데이터베이스에 저장된 MMS의 MMS id
     * @return 가장 최근 받은 MMS 발신자의 핸드폰 번호
     */

    private String parseNumber(String $id)
    {
        Log.d("MMSReceiver","parsNumber start");
        String result = null;

        Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
        String[] projection = new String[] { "address" };
        String selection = "msg_id = ? and type = 137";// type=137은 발신자
        String[] selectionArgs = new String[] { $id };

        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, "_id asc limit 1");

        if (cursor.getCount() == 0)
        {
            cursor.close();
            return result;
        }

        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex("address"));
        cursor.close();

        return result;
    }

    /**
     * id를 사용하여 가장 최근에 받은 MMS의 메시지 내용을가져와 반환
     * @param $id 가장 최근 MMS데이터베이스에 저장된 MMS의 MMS id
     * @return 가장 최근 받은 MMS의 내용
     */

    private ChattingRoomItem parseMessage(String $id)
    {
        Log.d("MMSReceiver","parseMsg start");
        String result = null;

        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://mms/part"), new String[] { "mid", "_id", "ct", "_data", "text" }, null, null, null);

        Log.d("MMSReceiver parseMsg", "|mms 메시지 갯수 : " + cursor.getCount() + "|");
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        ChattingRoomItem chattingRoomItem = new ChattingRoomItem();
        while (!cursor.isAfterLast())
        {
            String mid = cursor.getString(cursor.getColumnIndex("mid"));
            if ($id.equals(mid))
            {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type))
                {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));

                    if (TextUtils.isEmpty(data))
                        result = cursor.getString(cursor.getColumnIndex("text"));
                    else
                        result = parseMessageWithPartId(partId);
                    chattingRoomItem.setmContent(result);
                }
                if(type.matches("^image.*")){
                    byte[] imgBytes = getMMSImage(partId);
                    chattingRoomItem.getImgByteList().add(imgBytes);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();

        return chattingRoomItem;
    }
    private byte[] getMMSImage(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = mContext.getContentResolver().openInputStream(partURI);
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bytes;
    }


    /**
     * <pre>
     *     가장 최근의 mms 메시지 id를 파라미터로 받고 해당 메시지에 첨부된 이미지를 읽어오는 메서드
     *     첨부된 이미지가 없다면 null을 반환한다.
     * </pre>
     * @param id 가장최근의 mms 메시지 id
     * @return 이미지 byte array , 첨부된 이미지가 없다면 null을 반환.
     */
    private byte[] parseImageBytes(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        ContentResolver contentResolver = mContext.getContentResolver();
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = contentResolver.openInputStream(partURI);
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bytes;
    }
    /**
     * 가장 최근 받은 MMS에 팡일이 들어있을 경우 메시지 내용을 읽어와 반환
     * @param $id 가장 최근 MMS데이터베이스에 저장된 MMS의 MMS part id
     * @return 가장 최근 받은 MMS의 내용
     */
    private String parseMessageWithPartId(String $id)
    {
        Uri partURI = Uri.parse("content://mms/part/" + $id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try
        {
            is = mContext.getContentResolver().openInputStream(partURI);
            if (is != null)
            {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (!TextUtils.isEmpty(temp))
                {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        return sb.toString();
    }

    /**
     * 현재 사용자가 있는 액티비티가 ChattingRoomActivity이면 ChattingRoomActivity에 최근에 받은 MMS내용을 전달
     * MainActivity이면 MainActivity에 플래그를 확인하고 플래그가 들어있으면 최근에 받은 MMS내용을 전달
     * @param sender 메시지를 보낸 사람
     * @param contents 메시지의 내용
     * @param thread_id 가장 최근에 받은 MMS의 thread_id
     */

    private void sendToActivity(String sender, String contents, ArrayList<byte[]> imgByteList, String thread_id){
        String currentClassName = getClassName();
        if(contents != null) {
            switch (mContextName) {
                case "ChattingRoomActivity": {
                    if (currentClassName.equals("com.dlog.molla." + mContextName) && contents.replace("\r\n", "").matches("^.*?molla!.*")) {
                        Intent intent = new Intent(mContext, ChattingRoomActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Sender", sender);
                        intent.putExtra("Contents", contents);
                        intent.putExtra("ImageBytes", imgByteList);
                        boolean flag = true;
                        intent.putExtra("ReceiverFlag", flag);
                        mContext.startActivity(intent);
                    }
                    break;
                }
                case "MainActivity": {
                    if (currentClassName.equals("com.dlog.molla." + mContextName) && contents.replace("\r\n", "").matches("^.*?molla!.*")) {
                        FragmentChatList fragmentChatList = (FragmentChatList) MainActivity.getmFragmentChatList();
                        fragmentChatList.getNewMsg(sender, thread_id, currentClassName);
                    /*
                    Intent intent = new Intent(_context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Sender", sender);
                    boolean flag = true;
                    intent.putExtra("ReceiverFlag", flag);//리시버에서 보내는 인텐트라는 뜻.
                    _context.startActivity(intent);*/
                    }
                    break;
                }
            }
        }
    }

    /**
     * 현재 사용자가 보고 있는 액티비티를 반환
     * @return 현재 사용자가 보고 있는 액티비티
     */

    private String getClassName(){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        Log.d("TAG","M " + runningTaskInfos.get(0).topActivity.getClassName());
        return runningTaskInfos.get(0).topActivity.getClassName();
    }
}
