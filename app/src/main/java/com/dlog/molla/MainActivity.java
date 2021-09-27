package com.dlog.molla;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * <pre>
 *     메인화면에 해당하는 Activity
 *     이 Activity에는 3개의 Fragment가 등록된다.
 *     {@link FragmentFriendList}, {@link FragmentChatList}, {@link FragmentSetting}
 *     가장 처음에 화면에 보여지게 되는 fragment는 {@link FragmentChatList}이다.
 *     이 액티비티는 fragment 전환과 새 메시지가 왔을 때 Event를 수신,
 *     onActivityResult()로 받는 Event를 {@link BusProvider}를 이용하여 전달하는 역할을 한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class MainActivity extends AppCompatActivity {
    /**
     * {@link FragmentFriendList}로 fragment를 교체하는 버튼
     */
    private Button mBtn_friend_list;
    /**
     * {@link FragmentChatList}로 fragment를 교체하는 버튼
     */
    private Button mBtn_chat_list;
    /**
     * {@link FragmentSetting}로 fragment를 교체하는 버튼
     */
    private Button mBtn_setting;

    /**
     * {@link FragmentFriendList}의 인스턴스
     */
    private FragmentFriendList mFragmentFriendList;
    /**
     * {@link FragmentChatList}의 인스턴스
     */
    private static FragmentChatList mFragmentChatList;
    /**
     * {@link FragmentSetting}의 인스턴스
     */
    private FragmentSetting mFragmentSetting;
    /**
     * 이 액티비티의 fragment들을 관리하는 FragmentManager
     */
    private FragmentManager mFragmentManager;
    /**
     * 새 mms 메시지를 수신하는 Broadcast Receiver
     */
    private MMSReceiver mMMSReceiver;
    /**
     * 새 sms 메시지를 수신하는 Broadcast Receiver
     */
    private SMSReceiver mSMSReceiver;
    /**
     * 홈 버튼 클릭 이벤트를 수신하는 Broadcast Receiver
     */
    private HomeKeyReceiver homeKeyReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Broadcast Receiver 등록
        try {
            registMmsReceiver();
            registSmsReceiver();
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mBtn_friend_list = findViewById(R.id.btn_friend_list_go);
        mBtn_chat_list = findViewById(R.id.btn_chat_list);
        mBtn_setting = findViewById(R.id.btn_setting);
        mFragmentManager = getSupportFragmentManager();
        homeKeyReceiver = new HomeKeyReceiver();
        //버튼들 리스너 정의
        setBtnListener();
        //초기에 시작할 프래그먼트 실행
        setInitFragment();

    }

    /**
     * 초기 fragment를 {@link FragmentChatList}으로 설정하는 메서드
     */
    private void setInitFragment(){
        mFragmentChatList = new FragmentChatList();
        mFragmentManager.beginTransaction().replace(R.id.framelayout_main, mFragmentChatList).commit();
    }

    /**
     * 이 액티비티의 버튼들의 클릭 Listener를 정의하는 메서드
     */
    private void setBtnListener(){
        mBtn_friend_list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mFragmentFriendList == null) {
                    mFragmentFriendList = new FragmentFriendList();
                    mFragmentManager.beginTransaction().add(R.id.framelayout_main, mFragmentFriendList).commit();
                }
                changFragment(mFragmentFriendList, mFragmentChatList, mFragmentSetting);
                mBtn_friend_list.setBackground(getResources().getDrawable(R.drawable.person_one_black));
                mBtn_chat_list.setBackground(getResources().getDrawable(R.drawable.main_bottom_list));
                mBtn_setting.setBackground(getResources().getDrawable(R.drawable.main_bottom_setting));
            }
        });
        mBtn_chat_list.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mFragmentChatList == null) {
                    mFragmentChatList = new FragmentChatList();
                    mFragmentManager.beginTransaction().add(R.id.framelayout_main, mFragmentChatList).commit();
                }
                changFragment(mFragmentChatList, mFragmentFriendList, mFragmentSetting);
                mBtn_friend_list.setBackground(getResources().getDrawable(R.drawable.person_one));
                mBtn_chat_list.setBackground(getResources().getDrawable(R.drawable.main_bottom_list_black));
                mBtn_setting.setBackground(getResources().getDrawable(R.drawable.main_bottom_setting));
            }
        });
        mBtn_setting.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mFragmentSetting == null) {
                    mFragmentSetting = new FragmentSetting();
                    mFragmentManager.beginTransaction().add(R.id.framelayout_main, mFragmentSetting).commit();
                }
                changFragment(mFragmentSetting, mFragmentFriendList, mFragmentChatList);
                mBtn_friend_list.setBackground(getResources().getDrawable(R.drawable.person_one));
                mBtn_chat_list.setBackground(getResources().getDrawable(R.drawable.main_bottom_list));
                mBtn_setting.setBackground(getResources().getDrawable(R.drawable.main_bottom_setting_black));
            }
        });
    }

    /**
     * 화면에 보여질 fragment를 교체시키는 메서드
     * @param showFragment 화면에 보여질 fragment
     * @param anotherFragment1 다른 fragment1
     * @param anotherFragment2 다른 fragment2
     */
    private void changFragment(Fragment showFragment, Fragment anotherFragment1 , Fragment anotherFragment2){

        if(showFragment != null){
            mFragmentManager.beginTransaction().show(showFragment).commit();
        }
        if(anotherFragment1 != null){
            mFragmentManager.beginTransaction().hide(anotherFragment1).commit();
        }
        if(anotherFragment2 != null){
            mFragmentManager.beginTransaction().hide(anotherFragment2).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 98:
            case 1: case 10: case 11:
            case 3: {//친구삭제 //인증완료  //비번변경취소/ok  fragment로 전달
                BusProvider.getInstance().post(new ActivityResultEvent(requestCode,resultCode,data));
                break;
            }
            case 0:
            case 2: {//친구삭제 no//인증 취소
                //do nothing
                break;
            }

        }
    }

    /**
     * MMSReceiver를 등록하는 메서드
     * @throws IntentFilter.MalformedMimeTypeException
     */
    private void registMmsReceiver() throws IntentFilter.MalformedMimeTypeException {
        mMMSReceiver = new MMSReceiver("MainActivity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
        intentFilter.addDataType("application/vnd.wap.mms-message");
        Log.d("TAG", "regist Receiver");
        this.registerReceiver(mMMSReceiver,intentFilter, Manifest.permission.BROADCAST_WAP_PUSH,null);
    }

    /**
     * SMSReceiver를 등록하는 메서드
     */
    private void registSmsReceiver(){
        mSMSReceiver = new SMSReceiver("MainActivity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        Log.d("TAG","regist SMSReceiver");
        this.registerReceiver(mSMSReceiver,intentFilter,Manifest.permission.BROADCAST_SMS,null);

    }
    /**
     * Homekey Receiver를 등록하는 메서드
     */
    private void registHomeKeyReceiver(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(homeKeyReceiver,intentFilter);
    }

    /**
     * SMSReceiver를 등록해제 하는 메서드
     */
    private void unregistSMSReceiver(){
        this.unregisterReceiver(mSMSReceiver);
    }

    /**
     * MMSReceiver를 등록해제 하는 메서드
     */
    private void unregistMmsReceiver(){
        this.unregisterReceiver(mMMSReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMMSReceiver != null) {
            unregistMmsReceiver();
        }
        if(mSMSReceiver != null) {
            unregistSMSReceiver();
        }
        Log.d("TAG","Main onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG","Main onPause");
        if(homeKeyReceiver != null){
            unregistHomeKeyReceiver();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registHomeKeyReceiver();
    }

    /**
     * HonmeKeyReceiver를 등록해제 하는 메서드
     */
    private void unregistHomeKeyReceiver(){
        this.unregisterReceiver(homeKeyReceiver);
    }

    /**
     * {@link MainActivity#mFragmentChatList}를 반환하는 메서드
     * @return {@link MainActivity#mFragmentChatList}
     */
    public static Fragment getmFragmentChatList(){
        return mFragmentChatList;
    }


}
