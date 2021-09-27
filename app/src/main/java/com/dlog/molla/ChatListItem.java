package com.dlog.molla;

import java.sql.Date;
import java.util.HashSet;

/**
 * <pre>
 *     채팅방 목록 리스트의 한 아이템에 해당하는 클래스
 *     sort를 위한 Comparable를 implements한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see Comparable
 */
public class ChatListItem implements Comparable<ChatListItem> {
    /**
     * 채팅방에 참여한 사람들의 전화번호들의 집합. 본인의 전화번호는 포함되지 않는다.
     */
    private HashSet mNumber;
    /**
     * <pre>
     *     채팅방의 제목에 해당하는 문자열.
     *     기본은 전화번호를 나열한 형태이지만 친구 데이터베이스에서 일치하는 번호가 있으면 이름으로 변경된다.
     * </pre>
     */
    private String mName;
    /**
     * <pre>
     *     채팅방의 마지막 메시지에 대한 날짜 정보
     * </pre>
     */
    private Date mDate;
    /**
     * 문자 데이터베이스에서 가져온 채팅방의 고유 Id 값인 thread_id 값
     */
    private String mThreadId;
    /**
     * 채팅방에 읽지 않은 새 문자 메시지가 있음을 나타내는 boolean 값
     */
    private boolean isHaveNewMsg = false;
    public ChatListItem(HashSet mNumber){
        this.mNumber = mNumber;
    }

    /**
     * 생성자. 전화번호 집합과 채팅방 제목을 파라미터로 받는다.
     * @param mNumber {@link ChatListItem#mNumber}
     * @param mName {@link ChatListItem#mName}
     */
    public ChatListItem(HashSet mNumber, String mName){
        this.mNumber = mNumber;
        this.mName = mName;
    }

    /**
     * 생성자. 전화번호 집합과 채팅방의 마지막 메시지에 대한 날짜 정보, 채팅방의 ThreadId 값을 파라미터로 받는다.
     * @param mNumber {@link ChatListItem#mNumber}
     * @param mDate {@link ChatListItem#mDate}
     * @param mThreadId {@link ChatListItem#mNumber}
     */
    public ChatListItem(HashSet mNumber, Date mDate, String mThreadId){
        this.mNumber = mNumber;
        this.mDate = mDate;
        this.mThreadId = mThreadId;
    }


    public HashSet getmNumber() {
        return mNumber;
    }

    public void setmNumber(HashSet mNumber) {
        this.mNumber = mNumber;
    }


    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmThreadId() {
        return mThreadId;
    }

    public void setmThreadId(String mThreadId) {
        this.mThreadId = mThreadId;
    }
    public boolean isHaveNewMsg(){return isHaveNewMsg;}
    public void setHaveNewMsg(boolean bool){this.isHaveNewMsg = bool;}

    /**
     * <pre>
     *     클래스를 비교하는 메서드인 compareTo 메서드를 정의한다.
     *     {@link ChatListItem#mDate}를 기준으로, 즉 날짜를 기준으로 비교한다.
     *     Collections.sort() 메서드를 사용할 때 이 메서드가 사용된다.
     * </pre>
     * @param o
     * @return 비교의 결과값
     */
    @Override
    public int compareTo(ChatListItem o) {
        if(this.mDate.before(o.mDate)){
            return 1;
        }else if(this.mDate.after(o.mDate)){
            return -1;
        }else{

            return 0;
        }
    }
}
