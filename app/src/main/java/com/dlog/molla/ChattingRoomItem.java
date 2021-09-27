package com.dlog.molla;

import java.sql.Date;
import java.util.ArrayList;

/**
 * <pre>
 *     채팅방에서 메시지 리스트의 한 아이템에 해당하는 클래스
 *     sort를 위한 Comparable를 implements한다.
 * </pre>
 * @author 최정헌
 * @see Comparable
 */
public class ChattingRoomItem implements Comparable<ChattingRoomItem>{
    /**
     * 메시지 text를 저장하는 String
     */
    private String mContent;
    /**
     * <pre>
     *     상대방이 보낸 메시지인지 내가 보낸 메시지인지를 나타낸다 .
     *     1은 상대방이 보낸 메시지이고 화면 왼쪽에 정렬된다.
     *     2는 내가 보낸 메시지이고 화면 오른쪽에 정렬된다.
     * </pre>
     */
    private int mViewType;//2가 오른쪽 즉, 나  1이 왼쪽 즉 상대방
    /**
     * 메시지가 발신/수신된 날짜 정보
     */
    private Date mDate;
    /**
     * 메시지에 이미지가 첨부되어있다면 해당 이미지의 byte array를 저장한다. 첨부되어있지 않다면 null.
     */
    private ArrayList<byte[]> mImgByteList;

    /**
     * <pre>
     *     이미지가 첨부되어있지 않을 때 사용하는 생성자
     *     메시지 text 정보, 날짜, {@link ChattingRoomItem#mViewType}을 파라미터로 받는다.
     * </pre>
     * @param mContent {@link ChattingRoomItem#mContent}
     * @param mDate {@link ChattingRoomItem#mDate}
     * @param mViewType {@link ChattingRoomItem#mViewType}
     */
    public ChattingRoomItem(String mContent, Date mDate, int mViewType) {
        this.mContent = mContent;
        this.mViewType = mViewType;
        this.mDate = mDate;
    }

    public ChattingRoomItem(){
        mImgByteList = new ArrayList<>();
    }

    /**
     * 객체의 깊은 복사 생성자.
     * @param chattingRoomItem 복사할 ChattingRoomItem 객체
     */
    public ChattingRoomItem(ChattingRoomItem chattingRoomItem){
        this.mContent = chattingRoomItem.getmContent();
        this.mDate = chattingRoomItem.getmDate();
        this.mImgByteList = chattingRoomItem.getImgByteList();
        this.mViewType = chattingRoomItem.getmViewType();
        this.mImgByteList = chattingRoomItem.getImgByteList();
    }

    /**
     * <pre>
     *     이미지가 첨부된 경우 사용하는 생성자
     *     메시지 text 정보, 날짜,  {@link ChattingRoomItem#mViewType}, 해당 이미지의 Bytes array를 파라미터로 받는다.
     * </pre>
     * @param mContent {@link ChattingRoomItem#mContent}
     * @param mDate {@link ChattingRoomItem#mDate}
     * @param mViewType {@link ChattingRoomItem#mViewType}
     * @param mImgByteList {@link ChattingRoomItem#mImgByteList}
     */
    public ChattingRoomItem(String mContent, Date mDate, int mViewType, ArrayList<byte[]> mImgByteList) {
        this.mContent = mContent;
        this.mViewType = mViewType;
        this.mDate = mDate;
        this.mImgByteList = mImgByteList;
    }

    public String getmContent() {
        return mContent;
    }


    public int getmViewType() {
        return mViewType;
    }

    public Date getmDate(){ return mDate;}

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }


    public void setmViewType(int mViewType) {
        this.mViewType = mViewType;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public  ArrayList<byte[]> getImgByteList() {
        return mImgByteList;
    }

    public void setImgByteList( ArrayList<byte[]> imgByteList) {
        this.mImgByteList = imgByteList;
    }

    /**
     * <pre>
     *      클래스를 비교하는 메서드인 compareTo 메서드를 정의한다.
     *      {@link ChattingRoomItem#mDate}를 기준으로, 즉 날짜를 기준으로 비교한다.
     *      Collections.sort() 메서드를 사용할 때 이 메서드가 사용된다.
     * </pre>
     * 클래스를 비교하는 메서드인 compareTo 메서드를 정의한다.
     * {@link ChattingRoomItem#mDate}를 기준으로, 즉 날짜를 기준으로 비교한다.
     * Collections.sort() 메서드를 사용할 때 이 메서드가 사용된다.
     * @param o
     * @return 비교의 결과값
     */
    @Override
    public int compareTo(ChattingRoomItem o) {
        if(this.mDate.before(o.mDate)){
            return -1;
        }else if(this.mDate.after(o.mDate)){
            return 1;
        }else{

            return 0;
        }
    }
}

