package com.dlog.molla;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 친구 추가 화면에 해당하는 Activity
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class FriendAddActivity extends AppCompatActivity {
    /**
     * 클릭하면 새로운 친구가 데이터베이스에 저장되는 버튼
     */
    private Button mAdd_btn;
    /**
     * 새로운 친구의 이름을 입력받는 EditText
     */
    private EditText mEdt_name;
    /**
     * 새로운 친구의 번호를 입력받는 EditText
     */
    private EditText mEdt_phone;
    /**
     * <pre>
     *     {@link FriendAddActivity#mAdd_btn}의 setOnClickListener에서 호출할 UserDbAsycTask에 Activity를 넘겨주기 위해 만들었다.
     *     이 액티비티를 저장한다.
     * </pre>
     */
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        mAdd_btn = findViewById(R.id.btn_add);
        mEdt_name = findViewById(R.id.friend_add_name);
        mEdt_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    mEdt_name.setHint("");
                else
                    mEdt_name.setHint("이름을 입력하세요");
            }
        });
        mEdt_phone = findViewById(R.id.friend_add_phone);
        mEdt_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    mEdt_phone.setHint("");
                else
                    mEdt_phone.setHint("전화번호를 입력하세요");
            }
        });
        mActivity = this;


        setBtnListener();
    }

    /**
     * 이 Activity의 버튼 이벤트들을 정의하는 메서드
     */
    private void setBtnListener(){
        mAdd_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    int checkNumberException = Integer.parseInt(mEdt_phone.getText().toString().trim());//숫자 이외의 문자가 포함되면 NuberFormatException 발생.
                    if ((mEdt_name.length() != 0) && (mEdt_phone.length() != 0)) {
                        //DB에 User 추가
                        User user = new User(mEdt_name.getText().toString(), mEdt_phone.getText().toString());
                        new FragmentFriendList.UserDbAsyncTask(mActivity, user, 3).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "이름과 전화번호를 작성해주세요", Toast.LENGTH_LONG).show();
                    }
                }catch (NumberFormatException e){
                    Toast.makeText(getApplicationContext(), "전화번호는 숫자만 입력가능 합니다!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
