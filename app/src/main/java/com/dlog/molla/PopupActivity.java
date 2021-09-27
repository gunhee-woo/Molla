package com.dlog.molla;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;

import static com.dlog.molla.LoginActivity.hashingPassword;

/**
 * 이 앱에서 사용하는 팝업 액티비티
 *
 * For example:
 * <pre>
 *       Intent intent = new Intent(context, PopupActivity.class);
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class PopupActivity extends AppCompatActivity {
    /**
     * PopupActivity의 제목
     */
    private TextView mTxt_title;
    /**
     * PopupActivity의 내용
     */
    private TextView mTxt_info;
    /**
     * 확인버튼
     */
    private Button mBtn_ok;
    /**
     * 취소버튼
     */
    private Button mBtn_no;
    /**
     * 비밀번호 입력받는 EditText
     */
    private EditText mEdt_password;
    /**
     * taskNum 0 : 삭제  ,  1 : 채팅방 인증  , 2, 3: 채팅방 디코딩, 4: 비번변경
     */
    private int mTaskNum;
    /**
     * 수신자 핸드폰 번호가 담겨있는 HashSet
     */
    private HashSet mNumSet;
    /**
     * thread_id
     */
    private String thread_id;
    /**
     * 홈 버튼 클릭 이벤트를 수신하는 Broadcast Receiver
     */
    private HomeKeyReceiver homeKeyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//타이틀바없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_popup);
        homeKeyReceiver = new HomeKeyReceiver();

        /**
         * PopupActivity의 제목
         */
        mTxt_title = findViewById(R.id.txt_popup_title);
        mTxt_info = findViewById(R.id.txt_popup_info);
        mBtn_ok = findViewById(R.id.btn_popup_ok);
        mBtn_no = findViewById(R.id.btn_popup_no);
        mEdt_password = findViewById(R.id.edt_popup);

        String str_title = getIntent().getStringExtra("PopupTitle");
        String str_info = getIntent().getStringExtra("PopupInfo");
        mTaskNum = getIntent().getIntExtra("TaskNum",0);

        mTxt_title.setText(str_title);
        mTxt_info.setText(str_info);

        if(mTaskNum == 3 || mTaskNum == 0) {
            mEdt_password.setVisibility(View.GONE);
        } else {
            mEdt_password.setVisibility(View.VISIBLE);
        }
        mNumSet = (HashSet)getIntent().getSerializableExtra("Number");
        thread_id = getIntent().getStringExtra("ThreadId");

        setBtnEvent();

    }

    /**
     * 버튼 클릭 이벤트
     */

    private void setBtnEvent(){
        mBtn_no.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (mTaskNum){
                    case 0 : {
                        //삭제 취소
                        setResult(0);
                        finish();
                        break;
                    }
                    case 1 : {//인증 취소
                        setResult(2);
                        finish();
                        break;
                    }
                    case 2:
                    case 3: {
                        finish();
                        break;
                    }
                    case 4:
                    case 5:{
                        //비번 변경 취소
                        setResult(10);
                        finish();
                        break;
                    }
                }
            }
        });
        mBtn_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (mTaskNum){
                    case 0 : {
                        //삭제 ok
                        setResult(1);
                        finish();
                        break;
                    }
                    case 1 : {
                        Intent intent = new Intent(getApplicationContext(), ChattingRoomActivity.class);
                        intent.putExtra("Number", mNumSet);
                        intent.putExtra("ThreadId",thread_id);
                        String str = mEdt_password.getText().toString();
                        intent.putExtra("IntentPassword", str);
                        String name = getIntent().getStringExtra("Name");
                        intent.putExtra("Name",name);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case 2: {
                        Intent intent = new Intent();
                        String s = mEdt_password.getText().toString();
                        intent.putExtra("RoomPassword", s);
                        setResult(3, intent);
                        finish();
                        break;
                    }
                    case 3: {
                        Intent intent = new Intent();
                        String s = mTxt_info.getText().toString();
                        intent.putExtra("RoomPassword", s);
                        setResult(3, intent);
                        finish();
                        break;
                    }
                    case 4: {//비번 변경 ok
                        //shared preferences 변경,
                        if (mEdt_password.length() < 4) {
                            Toast.makeText(getApplicationContext(), "4자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            GlobalApplication.prefs.savePreferences(hashingPassword(mEdt_password.getText().toString()));
                            Toast.makeText(getApplicationContext(), "비밀번호 변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        break;
                    }
                    case 5: { // 비밀번호 변경시 기존 비밀번호 check
                        if (mEdt_password.length() < 4) {
                            Toast.makeText(getApplicationContext(), "4자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            if(GlobalApplication.prefs.getPasswordPreferences().equals(hashingPassword(mEdt_password.getText().toString()))) {
                                Toast.makeText(getApplicationContext(), "비밀번호가 확인되었습니다", Toast.LENGTH_SHORT).show();
                                Intent intent = getIntent();
                                intent.putExtra("TaskNum", 4);
                                intent.putExtra("PopupTitle","비밀번호 변경");
                                intent.putExtra("PopupInfo", "새 비밀번호를 4자리 이상 입력하세요.");
                                finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }

                }

            }
        });

    }

    /**
     * 화면 터치 이벤트 바깥레이어 클릭시 팝업이 닫히지 않게 함
     * @param event event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        registHomeKeyReceiver();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(homeKeyReceiver != null){
            unregistHomeKeyReceiver();
        }
    }
    /**
     * Homekey Receiver를 등록하는 메서드
     */
    private void registHomeKeyReceiver(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(homeKeyReceiver,intentFilter);
    }
    /**
     * HonmeKeyReceiver를 등록해제 하는 메서드
     */
    private void unregistHomeKeyReceiver(){
        this.unregisterReceiver(homeKeyReceiver);
    }

}
