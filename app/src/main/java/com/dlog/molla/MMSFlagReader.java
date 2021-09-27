package com.dlog.molla;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

/**
 * 채팅방을 구성하기 위해 MMS 데이터베이스에서 플래그가 붙은 MMS메시지만 쿼리하여 가져오는 기능을하는 클래스
 * For example:
 * <pre>
 *     MMSFlagReader mmsFlagReader = new MMSFlagReader(myNumber);
 *     mmsFlagReader.gatherMessages(context);
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class MMSFlagReader {
    /**
     * MMS id 가 담긴 리스트
     */
    private ArrayList<String> mIdList = new ArrayList<>();
    /**
     * MMS데이터베이스에 접근하기 위한 Uri
     */
    private Uri mUri = Uri.parse("content://mms/");
    /**
     * 컨텐츠 제공자
     */
    private ContentResolver mContentResolver;
    /**
     * 채팅방을 생성하기 위한 내용이 담긴 리스트
     */
    private ArrayList<ChatListItem> mChatRoomList;
    /**
     * 플래그가 붙은 메시지를 구별하기 위한 변수
     */
    private boolean isTarget = false;
    //private boolean isPwdTarget = false;
    /**
     * 사용자 본인의 핸드폰 번호
     */
    private String mMyNumber = "";

    /**
     * MMSFlagReader 생성자
     * @param myNumber 사용자 본인의 핸드폰 번호
     */

    public MMSFlagReader(String myNumber){
        this.mMyNumber = myNumber;
    }

    /**
     * MMS 데이터베이스에서 _id column을 프로젝션하여 날짜별 내림차순으로 MMS id 데이터를 갖고와 idList에 저장
     */

    private void setidList(){
        String[] projection = {"_id"};
        Cursor cursor = mContentResolver.query(mUri,projection,null,null,"date DESC");
        if(cursor.moveToFirst()){
            do{
                String mmsid = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                mIdList.add(mmsid);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 지금시간부터 일주일 전의 시간을 반환
     * @return 일주일 전의 시간
     */

    public java.util.Date getPreviousMonth() {
        Long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        //calendar.add(Calendar.HOUR, -2);
        return calendar.getTime();
    }

    /**
     * contentResolver에 접근해 MMS 데이터베이스에서 플래그가 붙은 MMS 내용을 chatRoomList에 저장
     * @param context context
     * @throws IOException 입출력 처리 실패에 의해 발생
     */

    public void gatherMessages(Context context) throws IOException {
        mChatRoomList = new ArrayList<>();
        String[] projection = {"_id","ct_t","date","msg_box","thread_id"};
        mContentResolver = context.getContentResolver();
        Cursor cursor = mContentResolver.query(mUri,projection,null,null,"date DESC");
        setidList();
        if(cursor.moveToFirst()){
            do {
                String mmsId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                String threadId = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
                Date mmsDate = new Date(Long.valueOf(date) * 1000);
                if(mmsDate.before(getPreviousMonth()))
                    continue;
                String selection = "mid=" + mmsId;
                Uri partUri = Uri.parse("content://mms/part");
                Cursor partCursor = mContentResolver.query(partUri, null, selection, null, null);

                String body="";
                if (partCursor.moveToFirst()) {
                    do {
                        String type = partCursor.getString(partCursor.getColumnIndexOrThrow("ct"));
                        if (type.equals("text/plain")) {
                            body = getMmsBody(partCursor);
                            if(body.matches("^.*?molla!.*")){
                                isTarget = true;
                            }

                        }
                    } while (partCursor.moveToNext());
                    partCursor.close();
                }
                if(isTarget) {
                    HashSet numSet = getPhoneNumber(mmsId);
                    if (!mChatRoomList.contains(numSet)) {
                        if(numSet.size() == 0){//단체문자에 받는 사람에 자기 자신이 포함된 경우,  자기 자신과의 개인 대화방에 numSet이 0이됨
                            numSet.add(mMyNumber);
                        }
                        mChatRoomList.add(new ChatListItem(numSet, mmsDate,threadId));
                    }
                    /*
                    Iterator it = numSet.iterator();
                    while (it.hasNext()){
                        String phoneNum = (String) it.next();
                        if(phoneNum.length() == 11) {
                            if (!numberList.contains(phoneNum)) {
                                numberList.add(new ChatListItem(phoneNum, mmsDate));
                            }
                        }
                    }*/
                }
                isTarget = false;
            }while (cursor.moveToNext());
            cursor.close();
        }
    }

    /**
     * MMS데이터베이스에서 id에 일치하는 핸드폰 번호를 HashSet으로 반환
     * @param id MMS데이터베이스에 저장된 메시지 id
     * @return 핸드폰번호가 저장된 HashSet
     */

    private HashSet getPhoneNumber(String id){
        String selection = "msg_id="+id;
        Uri phoneNumberUri = Uri.parse("content://mms/"+id+"/addr");
        Cursor cursor = mContentResolver.query(phoneNumberUri,null,selection,null,null);
        String phoneNumber = null;
        HashSet numSet = new HashSet();
        int myNumCount = 0; // myNumCount가 2이면 내번호가 두번 나온것. 받는사람에도 있고 보낸 사람에도 잇다는 것. 내가 나에게 보낸것.
        if(cursor.moveToFirst()){
            do {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                if (address != null) {
                    phoneNumber = address.replace("-", "");
                    if (phoneNumber.startsWith("+82")) {
                        phoneNumber = phoneNumber.replace("+82", "0");
                    }
                }
                if (!phoneNumber.equals(mMyNumber)) {
                    numSet.add(phoneNumber);
                }
                else{
                    myNumCount++;
                }

            }while (cursor.moveToNext());
        }
        cursor.close();
        if(myNumCount >1){
            numSet.add(mMyNumber);
        }
        return numSet;
    }

    /**
     * MMS데이터베이스에서 MMS id에 맞는 메시지 본문을 파일이 있으면  getMessageText 함수로 읽어서 반환
     * 파일이 없으면 text로 쿼리를 날려 본문을 읽어서 반환
     * @param partCurosr MMS id에 맞는 부분 커서
     * @return MMS id에 맞는 메시지 본문
     * @throws IOException 입출력 처리 실패에 의해 발생
     */

    private String getMmsBody(Cursor partCurosr) throws IOException {
        String partId = partCurosr.getString(partCurosr.getColumnIndexOrThrow("_id"));
        String data = partCurosr.getString(partCurosr.getColumnIndexOrThrow("_data"));
        if(data != null){
            return getMessageText(mContentResolver, partId);
        }else{
            return partCurosr.getString(partCurosr.getColumnIndexOrThrow("text"));
        }
    }

    /**
     * MMS데이터베이스에서 MMS id에 맞는 메시지 본문을 읽어서 반환
     * @param contentResolver 컨텐츠 제공자
     * @param id MMS id
     * @return 메시지 본문 반환
     * @throws IOException 입출력 처리 실패에 의해 발생
     */

    private String getMessageText(ContentResolver contentResolver, String id) throws IOException {
        Uri partUri = Uri.parse("content://mms/part/"+id);
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = contentResolver.openInputStream(partUri);
        if(inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String temp = bufferedReader.readLine();
            while(temp != null){
                stringBuilder.append(temp);
                temp = bufferedReader.readLine();
            }
            inputStream.close();
        }
        return stringBuilder.toString();
    }

    /**
     * 플래그가 붙은 MMS 내용이 담긴 chatRoomList를 반환
     * @return 플래그가 붙은 MMS 내용이 담긴 chatRoomList
     */

    public ArrayList<ChatListItem> getmChatRoomList() {
        return mChatRoomList;
    }
}
