package com.dlog.molla;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <pre>
 *     SMS 데이터베이스에 접근하여 SMS 메시지를 읽는 클래스
 *     생성자에서 데이터베이스에서 읽을 column과 해당 column target이 되는 value를 받는다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see com.dlog.molla.MMSReader
 */
public class SMSReader {
    /**
     * 채팅방의 대화리스트에 해당하는 {@link ChattingRoomItem}의 집합
     */
    private HashSet<ChattingRoomItem> mchatItemList;
    /**
     * 채팅방 목록에 해당하는 {@link ChatListItem}의 리스트
     */
    private ArrayList<ChatListItem> mchatRoomList;
    /**
     * <pre>
     *     SMS 데이터베이스에서 threadId를 읽고 여기에 저장한다.
     *     {@link SMSReader#mchatRoomList}에서 그룹대화방을 찾을 때 사용한다.
     *
     * </pre>
     */
    private HashSet<String> mThreadIdSet = new HashSet<>();
    /**
     * SMSReader를 생성한 context
     */
    private Context mContext;
    /**
     * SMSReader가 읽어야할 SMS 데이터베이스의 {@link SMSReader#mtargetCol}의 value 값
     */
    private String mTargetStr;
    /**
     * SMSReader가 읽어야할 SMS 데이터베이스의 column
     */
    private String mtargetCol;
    /**
     * 그룹대화방의 메시지일 경우 같은 메시지가 id값만 다르게 중복해서 읽어진다. 이 경우 for문을 건너뛰게 하기 위해 만들었다.
     */
    private String prebody="";
    /**
     * 번호로 threadId를 찾는 작업을 수행할 때 threadId를 저장한다
     */
    private String mThreadId;
    /**
     * 읽어야할 SMS 메시지의 날짜를 저장한다
     */
    private Calendar mCalendar;

    public SMSReader(Context context, String targetCol, String targetStr, Calendar calendar){
        this.mContext = context;
        this.mTargetStr = targetStr;
        this.mtargetCol = targetCol;
        this.mCalendar = calendar;
    }

    public SMSReader(Context context, String targetCol, String targetStr){
        this.mContext = context;
        this.mTargetStr = targetStr;
        this.mtargetCol = targetCol;
        this.mCalendar = getPreviousMonth();
    }

    /**
     * 현재 시간으로부터 7달 전의 시간 대를 가진 calendar 객체를 반환하는 메서드
     * @return Calender
     */
    public Calendar getPreviousMonth() {
        Long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar;
    }

    /**
     * <pre>
     *      SMS 데이터베이스에 접근해서 데이터를 읽어오는 메서드
     *      ContentResolver에 query를 날려서 읽어온다.
     *      {@link SMSReader#mtargetCol}필드에서 {@link SMSReader#mTargetStr} 값에 해당하는 데이터들만 읽어온다.
     *
     *      {@link SMSReader#mtargetCol}이 body 인 경우 : 채팅방 목록을 만드는 경우
     *      이 경우 {@link SMSReader#mTargetStr}은 이 메시지가 암호화된 메시지라는 것을 알려주는 flag 값이다.
     *      {@link SMSReader#mchatRoomList}에 채팅방 목록이 저장된다. 채팅방은 상대방 번호 집합, 가장 최근 메시지 날짜, thread_id로 이루어져있다.
     *      메시지가 어떤 대화방의 메시지인지 찾기 위한 방법으로 {@link SMSReader#mThreadIdSet}을 사용한다.
     *      {@link SMSReader#mchatRoomList}에서 같은 thread_id를 가진 채팅방을 찾으면 해당 메시지의 전화번호를 해당 채팅방의 전화번호 집합에 추가한다.
     *      전화번호 집합은 HashSet이므로 중복이 허용되지 않는다. 따라서 이미 전화번호 집합에 있는 번호일지라도 중복되지 않는다.
     *
     *      {@link SMSReader#mtargetCol}이 thread_id 인 경우 : 채팅방을 만든는 경우
     *      thread_id는 채팅방 고유 번호이므로 thread_id를 가지는 모든 메시지를 읽어와서 {@link SMSReader#mchatItemList}에 저장한다.
     *
     *      {@link SMSReader#mtargetCol}이 address 인 경우 : 특정 번호와의 대화방의 thread_id를 찾는 경우
     *      {@link SMSReader#mThreadId}에 thread_id를 찾아서 저장한다.
     *
     *
     * </pre>
     *
     */
    public void readSMSMessage() {
        Uri allMessage = Uri.parse("content://sms/");//all 전체, sent 발신함, failed 실패, conversations 대화
        ContentResolver cr = mContext.getContentResolver();
        mchatItemList = new HashSet<>();
        mchatRoomList = new ArrayList<>();

        Log.d("tar", mTargetStr);

        Cursor c = cr.query(allMessage,
                null,
                //"address=?",new String[]{targetNum},
                mtargetCol +" LIKE ? ", new String[]{mTargetStr},
                null);

        while(c.moveToNext()) {
            String date = c.getString(c.getColumnIndexOrThrow("date"));//해당 메시지 날짜
            Date smsDate = new Date(Long.valueOf(date));
            if(smsDate.before(mCalendar.getTime()))
                continue;
            switch (mtargetCol){
                case "body" : {
                    String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));//대화방의 고유 id
                    String from_address = c.getString(c.getColumnIndexOrThrow("address"));//메시지 상대방 전화번호
                    if (mThreadIdSet.contains(thread_id)){//그룹대화방을 만들어야함.
                        Iterator it = mchatRoomList.iterator();
                        while (it.hasNext()){//같은 대화방을 찾아야함.
                            ChatListItem chatListItem = (ChatListItem)it.next();
                            if (chatListItem.getmThreadId().equals(thread_id)) {//같은 대화방
                                HashSet hashSet = chatListItem.getmNumber();
                                hashSet.add(from_address);
                                chatListItem.setmNumber(hashSet);
                                break;
                            }
                        }
                    }
                    else{
                        mThreadIdSet.add(thread_id);
                        HashSet<String> hashSet = new HashSet<>();
                        hashSet.add(from_address);
                        mchatRoomList.add(new ChatListItem(hashSet,smsDate,thread_id));
                    }
                    break;
                }
                case "thread_id" : {
                    //String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));//대화방의 고유 id
                    String message = c.getString(c.getColumnIndexOrThrow("body"));//메시지 본문
                    if(message.equals(prebody)){//중복제거
                        continue;
                    }
                    else {
                        String from_address = c.getString(c.getColumnIndexOrThrow("address"));//메시지 상대방 전화번호

                        if (from_address.startsWith("+82")) {
                            from_address = from_address.replace("+82", "0");
                        }
                        //String date = c.getString(c.getColumnIndexOrThrow("date"));//해당 메시지 날짜
                        //Date smsDate = new Date(Long.valueOf(date));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));//type 2가 수신자(나) 1이 발신자(상대방)
                        if (from_address.length() == 11) {
                            mchatItemList.add(new ChattingRoomItem(message, smsDate, Integer.parseInt(type)));
                        }
                        prebody = message;
                    }
                }
                case "address" : {//번호로 thread_id 찾기  (개인 메시지일 경우)
                    mThreadId = c.getString(c.getColumnIndexOrThrow("thread_id"));//대화방의 고유 id
                }
            }

        }
        c.close();
    }

    public HashSet<ChattingRoomItem> getMchatItemList() {
        return mchatItemList;
    }

    public ArrayList<ChatListItem> getNumberList() {
        return mchatRoomList;
    }

    public String getmThreadId(){
        return mThreadId;
    }
}