package com.dlog.molla;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

/**
 * 채팅방 대화 내용을 구성하기 위해 MMS 데이터베이스에서 내용을 읽어오는 기능을 하는 클래스
 * For example:
 * <pre>
 *       MMSReader mmsReader = new MMSReader();
 *       try {
 *           mmsReader.gatherMessages(this, targetNumSet,myNumber, calendar);
 *       } catch (Exception e) {
 *           e.toString();
 *       }
 *
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class MMSReader {
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
     * 대화 내용을 구성하는 데이터 저장
     */
    private ArrayList<ChattingRoomItem> mChatItemList;
    /**
     * 사용자의 핸드폰 번호 저장
     */
    private String mMyNumber;

    /**
     * 플래그가 붙은 메시지를 구별하기 위한 변수
     */
    private boolean isTarget = false;

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
     * MMS데이터베이스에서 targetNUmSet에 들어있는 핸드폰번호와 주고받은 MMS를 calendar에 들어있는 날짜로부터 읽어온다
     * @param context context
     * @param targetNumSet 찾을 핸드폰 번호가 들어있는 HashSet
     * @param myNumber 사용자 본인의 핸드폰 번호
     * @param calendar 찾을 날짜
     * @throws IOException 입출력 처리 실패에 의해 발생
     */

    public void gatherMessages(Context context, HashSet targetNumSet, String myNumber, Calendar calendar) throws IOException {//컨텐츠 제공자에 접근해 디바이스 저장소에 있는 문자 메시지를 읽어온다.
        this.mMyNumber = myNumber;
        mChatItemList = new ArrayList<>();
        String[] projection = {"_id","ct_t","date","msg_box"};
        mContentResolver = context.getContentResolver();
        Cursor cursor = mContentResolver.query(mUri,projection,null,null,"date DESC");
        setidList();
        if(cursor.moveToFirst()) {
            do {
                String msg_box = cursor.getString(cursor.getColumnIndexOrThrow("msg_box"));
                String mmsId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                Date mmsDate = new Date(Long.valueOf(date) * 1000);
                if(mmsDate.before(calendar.getTime()))
                    continue;
                isTarget = getPhoneNumber(mmsId,targetNumSet);
                if(isTarget) {//휴대폰 번호가 찾고자 하는 번호와 일치하면 데이터 꺼낸다. (getPhoneNumber에서 일치 확인하고 isTarget을 true로 )
                    //Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    //Date mmsDate = new Date(Long.valueOf(date) * 1000);
                    String selection = "mid=" + mmsId;
                    Uri partUri = Uri.parse("content://mms/part");

                    Cursor partCursor = mContentResolver.query(partUri, null, selection, null, null);
                    if (partCursor.moveToFirst()) {
                        ChattingRoomItem chattingRoomItem = new ChattingRoomItem();
                        do {
                            String type = partCursor.getString(partCursor.getColumnIndexOrThrow("ct"));

                            if (type.matches("^image.*")) {
                                String partId = partCursor.getString(partCursor.getColumnIndexOrThrow("_id"));
                                byte[] imgBytes = getMMSImage(partId);
                                chattingRoomItem.getImgByteList().add(imgBytes);
                            }
                            if (type.equals("text/plain")) {
                                String body = getMmsBody(partCursor);
                                chattingRoomItem.setmContent(body);
                            }

                        } while (partCursor.moveToNext());
                        partCursor.close();
                        if( !chattingRoomItem.getImgByteList().isEmpty() || chattingRoomItem.getmContent() != null) {
                            chattingRoomItem.setmDate(mmsDate);
                            chattingRoomItem.setmViewType(Integer.parseInt(msg_box));
                            mChatItemList.add(chattingRoomItem);
                        }
                    }
                    isTarget = false;
                }
            }while (cursor.moveToNext());
            cursor.close();
        }
    }

    /**
     * 이미지가 들어있는 MMS에서 이미지를 바이트 배열로 반환
     * @param id 이미지가 들어있는 MMS의 id
     * @return 이미지 바이트 배열
     */

    private byte[] getMMSImage(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        //Bitmap bitmap = null;
        byte[] bytes = null;
        try {
            is = mContentResolver.openInputStream(partURI);
            bytes = IOUtils.toByteArray(is);
            //bitmap = BitmapFactory.decodeStream(is);
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
     * targetNumSet에 들어있는 핸드폰 번호와 일치하거나 나 자신과의 대화방일 경우 true를 반환 아니면 false를 반환
     * @param id MMS id
     * @param targetNumSet 찾을 핸드폰 번호가 들어있는 HashSet
     * @return
     */

    private boolean getPhoneNumber(String id, HashSet targetNumSet){
        String selection = "msg_id="+id;
        Uri phoneNumberUri = Uri.parse("content://mms/"+id+"/addr");
        Cursor cursor = mContentResolver.query(phoneNumberUri,null,selection,null,null);
        HashSet addrSet = new HashSet();
        String phoneNumber = null;
        int myNumCount = 0; // myNumCount가 2이면 내 번호가 수신자, 발신자에 포함된것 . 즉 내가 나에게 보낸경우
        if(cursor.moveToFirst()){
            do{
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                if(address != null){
                    phoneNumber = address.replace("-","");
                    if(phoneNumber.startsWith("+82")){
                        phoneNumber = phoneNumber.replace("+82","0");
                    }
                    if(!phoneNumber.equals(mMyNumber)) {
                        addrSet.add(phoneNumber);
                    }else{
                        myNumCount++;
                    }
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        if(myNumCount > 1){
            addrSet.add(mMyNumber);
        }
        else if(myNumCount == 1 && addrSet.size() == 0 && targetNumSet.contains(mMyNumber) ){//이경우 나 자신과의 개인 대화방
            addrSet.add(mMyNumber);
            return true;
        }
        if(targetNumSet.equals(addrSet)){
            return true;
        }
        return false;
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
     * 대화 내용을 담은 리스트를 반환
     * @return 대화 내용이 담긴 chatItemList
     */

    public ArrayList<ChattingRoomItem> getmChatItemList() {
        return mChatItemList;
    }
}
