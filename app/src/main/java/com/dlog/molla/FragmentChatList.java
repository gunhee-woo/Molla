package com.dlog.molla;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * <pre>
 *     채팅방 목록 화면에 해당하는 클래스
 *     부모 액티비티인 {@link MainActivity}의 onActivityResult()를 수신하기 위해 {@link BaseFragment}를 상속한다.
 *     검색 기능을 위해 TextWatcher를 implments 한다
 * </pre>
 *
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class FragmentChatList extends BaseFragment implements TextWatcher {
    /**
     * <pre>
     *     sms 데이터베이스에서 가져온 채팅방 정보를 저장한다.
     * </pre>
     */
    private ArrayList<ChatListItem> mSmsList;
    /**
     * <pre>
     *     mms 데이터베이스에서 가져온 채팅방 정보를 저장한다.
     * </pre>
     */
    private ArrayList<ChatListItem> mMmsList;
    /**
     * <pre>
     *     {@link FragmentChatList#mMsgList}의 각 아이템에서 번호 Set만 따로 뽑아서 저장한다.
     *     새 메시지가 들어왔을 때 , 빨리 대화방을 찾기 위해서 따로 관리한다.
     * </pre>
     */
    private HashSet<HashSet> mNumberList;
    /**
     * <pre>
     *     {@link FragmentChatList#mSmsList}와 {@link FragmentChatList#mMmsList}를 중복을 없애고 합친 후, 날짜 순서에 따라 정렬한 후
     *     여기에 저장된다.
     * </pre>
     */
    private ArrayList<ChatListItem> mMsgList;
    /**
     * <pre>
     *     Layout을 swipe했을 때 이벤트를 받아서 처리할 수 있는 SwiperRefreshLayout.
     *     채팅방 목록을 Swipe하면 SwipeRefreshLayout.OnRefreshListener()의 onRefresh()에서 이벤트를 받을 수 있다.
     * </pre>
     */
    private SwipeRefreshLayout mChattingRoomRefreshLayout;
    /**
     * <pre>
     *     sms, mms 데이터베이스에서 채팅방 리스트에 필요한 데이터를 가져오고, 채팅방을 만드는 동안 사용자에게 보여줄 애니메이션 뷰.
     * </pre>
     */
    private LottieAnimationView mLottieAnimationView;
    /**
     * <pre>
     *     채팅방 목록 리스트를 화면에 보여주기 위한 리사이클러 뷰
     * </pre>
     */
    private RecyclerView mRcyl_chat_list;
    /**
     * <pre>
     *     채팅방 검색을 위한 Edit Text 뷰
     * </pre>
     */
    private EditText mEdt_search;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chatlist,container,false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRcyl_chat_list = view.findViewById(R.id.rcyl_chatRoomList);
        mLottieAnimationView = view.findViewById(R.id.lottie_main_loading);
        mEdt_search = view.findViewById(R.id.edt_chatlist_search);
        mEdt_search.addTextChangedListener(this);
        mChattingRoomRefreshLayout = view.findViewById(R.id.chatting_room_refresh_layout);
        mChattingRoomRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new MainDbAsyncTask(getContext()).execute();
                mChattingRoomRefreshLayout.setRefreshing(false);

            }
        });

        new MainDbAsyncTask(getContext()).execute();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * <pre>
     *     {@link FragmentChatList#mEdt_search}의 text가 변경될 때 마다 호출된다.
     *     {@link FragmentChatList#mEdt_search}의 text 데이터, 시작 위치, 이전 위치, text 길이를 파라미터로 받는다.
     * </pre>
     *
     * @param s {@link FragmentChatList#mEdt_search}의 text 데이터
     * @param start 시작 위치
     * @param before 이전 위치
     * @param count text 길이
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try{
            ((RcylChatListAdapter) mRcyl_chat_list.getAdapter()).getFilter().filter(s);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * <pre>
     *     SMS, MMS 데이터베이스에 접근해서 채팅방 목록 리스트를 만드는 클래스
     *     백그라운드 작업을 위해 AsyncTask를 상속한다.
     * </pre>
     */
    public class MainDbAsyncTask  extends AsyncTask<Void, ChatListItem,  ArrayList<ChatListItem>> {
        /**
         * 이 클래스가 생성된 context
         */
        private Context context;

        public MainDbAsyncTask(Context context) {
            this.context = context;

        }

        /**
         * 백그라운드 작업이 시작되기 전 Lottie animaiton을 실행한다.
         */
        @Override
        protected void onPreExecute() {
            mLottieAnimationView.setAnimation("loading_anim.json");
            mLottieAnimationView.loop(true);
            mLottieAnimationView.playAnimation();
        }

        /**
         * <pre>
         *     {@link SMSReader},{@link MMSReader}를 이용하여 데이터베이스에 접근한다.
         *     메시지 text에 ?molla! 가 포함된 메시지만 뽑아와서 채팅방 목록을 만든다.
         * </pre>
         *
         * @param params Void
         * @return {@link FragmentChatList#mMsgList}를 반환
         */
        @Override
        protected  ArrayList<ChatListItem> doInBackground(Void... params) {

            try {
                SMSReader smsReader = new SMSReader(context, "body", "%?molla!%");
                smsReader.readSMSMessage();
                mSmsList = smsReader.getNumberList();

                String myNumber = getMyNumber();
                MMSFlagReader mmsFlagReader = new MMSFlagReader(myNumber);
                mmsFlagReader.gatherMessages(context);
                mMmsList = mmsFlagReader.getmChatRoomList();
                int mmsListSize = mMmsList.size();
                for (int i = 0; i < mmsListSize; i++) {
                    if (!mSmsList.contains(mMmsList.get(i).getmNumber())) {
                        mSmsList.add(mMmsList.get(i));
                    }
                }
                Collections.sort(mSmsList);
                mNumberList = new HashSet<>();
                mMsgList = new ArrayList<>();
                for (ChatListItem item : mSmsList) {
                    publishProgress(item);
                }
                return mMsgList;
            } catch (Exception e) {
                e.printStackTrace();
                return mMsgList;
            }

        }

        /**
         * <pre>
         *     {@link FragmentChatList#mSmsList}와 {@link FragmentChatList#mMmsList}를 합치고 나서 중복된 번호들의 집합(같은 대화방)들을 제거해준다.
         * </pre>
         * @param values ChatListItem
         */
        @Override
        protected void onProgressUpdate(ChatListItem... values) {
            ChatListItem chatListItem = values[0];
            HashSet numSet = chatListItem.getmNumber();
            if (!mNumberList.contains(numSet)) {
                mNumberList.add(numSet);
                mMsgList.add(chatListItem);
            }
        }

        /**
         * <pre>
         *     Lottie animation을 종료하고 {@link GetNameAsyncTask}를 실행한다.
         * </pre>
         *
         * @param chatListItems {@link FragmentChatList#mMsgList}
         */
        @Override
        protected void onPostExecute( ArrayList<ChatListItem> chatListItems) {
            mMsgList = chatListItems;
            mLottieAnimationView.cancelAnimation();
            mLottieAnimationView.setVisibility(View.GONE);

            new GetNameAsyncTask(getActivity(), mMsgList).execute();

        }
    }

    /**
     * <pre>
     *     채팅방 목록 리스트인 {@link FragmentChatList#mMsgList}의 전화번호들이 {@link UserDataBase}에 있다면 (친구 추가되어 있다면) , 채팅방 이름에서 해당 번호를 이름으로 바꾸어준다.
     * </pre>
     */
    public class GetNameAsyncTask extends AsyncTask<Void, Void, Void> {
        /**
         * 채팅방 목록 리스트
         */
        private ArrayList<ChatListItem> msgList;
        /**
         * 이 클래스가 생성된 activity를 WeakReference에 저장
         */
        private WeakReference<Activity> weakActivity;
        public GetNameAsyncTask(Activity activity,ArrayList<ChatListItem> msgList){
                this.weakActivity = new WeakReference<>(activity);
                this.msgList = msgList;
        }

        /**
         * <pre>
         *     {@link UserDataBase}에 접근하여 {@link GetNameAsyncTask#msgList}의 전화번호들 중 일치하는 것을 찾고
         *     {@link GetNameAsyncTask#msgList}의 name을 수정한다.
         * </pre>
         *
         * @param voids Void
         * @return null
         */
        @Override
        protected Void doInBackground(Void... voids) {
            if(msgList.isEmpty() || msgList == null)
                return null;
            for(int i = 0 ; i < msgList.size() ; i ++){
                HashSet numSet = msgList.get(i).getmNumber();
                UserDataBase db = Room.databaseBuilder( weakActivity.get(),
                        UserDataBase.class, "Users").build();
                Iterator it = numSet.iterator();
                String roomName = "";
                while (it.hasNext()){
                    String num = (String) it.next();
                    String name = db.userDao().getUserName(num);
                    if (name == null || name.equals("")) {
                        roomName = roomName + num +"  " +
                                "";
                    } else {
                        roomName = roomName + name  + "  ";

                    }
                }
                msgList.get(i).setmName(roomName);
            }
            return null;
        }

        /**
         * 채팅방 목록을 화면에 보여주기 위한 리사이클러 뷰에 어뎁터를 연결한다.
         * @param aVoid null
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            mRcyl_chat_list.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
            mRcyl_chat_list.setAdapter(new RcylChatListAdapter(getContext(),msgList));
        }

            final Migration MIGRATION_1_2 = new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    // Create the new table
                    database.execSQL(
                            "CREATE TABLE Users_new (mNickName Text, mPhoneNum Text NOT NULL, PRIMARY KEY (mPhoneNum))");
                    // Remove the old table
                    database.execSQL("DROP TABLE User");
                    // Change the table name to the correct one
                    database.execSQL("ALTER TABLE Users_new RENAME TO User");
                }
            };
    }

    /**
     * 부모 액티비티인 {@link MainActivity}에서 onActivityResult()를 수신하기 위한 Subscribe 메서드
     * @param activityResultEvent {@link ActivityResultEvent}
     */
    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        onActivityResult(activityResultEvent.getmRequestCode(),activityResultEvent.getmResultCode(),activityResultEvent.getmData());
    }

    /**
     * 부모 액티비티인 {@link MainActivity}에서 onActivityResult()를 수신하는 메서드
     * @param requestCode {@link MainActivity}에서 onActivityResult()의 requestCode
     * @param resultCode {@link MainActivity}에서 onActivityResult()의 resultcode
     * @param data {@link MainActivity}에서 onActivityResult()의 Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 2 : {
                //인증취소
                //do nothing
                break;
            }
        }
    }

    /**
     * 디바이스에 등록된 휴대폰 번호를 가져오는 메서드
     * @return 디바이스에 등록된 휴대폰 번호, 없으면 ""을 리턴.
     */
    private String getMyNumber() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(TELEPHONY_SERVICE);
            String myNumber = telephonyManager.getLine1Number();
            if(myNumber != null) {
                if (myNumber.startsWith("+82")) {
                    myNumber = myNumber.replace("+82", "0");
                }
            }
            return myNumber;
        }catch (SecurityException e){
            e.toString();
        }

        return "";
    }

    /**
     * {@link MMSReceiver}, {@link SMSReceiver}에서 새 메시지를 받았을 때 이 fragemnt로 메시지 정보를 전달하기 위한 메서드
     * @param num 보낸사람 번호
     * @param thread_id 보낸사람과 나의 채팅방의 thread_id
     * @param currentClassName
     */
    public void getNewMsg(String num,String thread_id,String currentClassName){
        HashSet hashSet = new HashSet();
        hashSet.add(num);
        if(!mNumberList.contains(hashSet)){//이경우 새로운 대화방이 생성되어야 함
            //todo : 번호, 친구이름 매칭?
            mNumberList.add(hashSet);
            ChatListItem chatListItem = new ChatListItem(hashSet,num);
            chatListItem.setmThreadId(thread_id);
            if(currentClassName.equals("com.dlog.molla.MainActivity")) {
                chatListItem.setHaveNewMsg(true);
            }
            mMsgList.add(0,chatListItem);
            mRcyl_chat_list.getAdapter().notifyDataSetChanged();
        }
        else{
            ((RcylChatListAdapter) mRcyl_chat_list.getAdapter()).newMsgArrived(hashSet,currentClassName);
        }
    }

    /**
     * {@link SendActivity}에서 메시지를 보냈을 때 이 fragment로 메시지 정보를 전달하기 위한 메서드
     * @param numSet 메시지 받는 사람들의 전화번호 집합
     * @param name 채팅방 이름
     */
    public void sendNewMsg(HashSet numSet,String name){
        if(!mNumberList.contains(numSet)){
            mNumberList.add(numSet);
            ChatListItem chatListItem = new ChatListItem(numSet,name);
            chatListItem.setHaveNewMsg(true);
            mMsgList.add(0,chatListItem);
            mRcyl_chat_list.getAdapter().notifyDataSetChanged();
        }
        else{
            ((RcylChatListAdapter) mRcyl_chat_list.getAdapter()).newMsgArrived(numSet,"com.dlog.molla.MainActivity");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEdt_search.removeTextChangedListener(this);
    }
}
