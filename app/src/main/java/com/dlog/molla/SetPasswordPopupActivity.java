package com.dlog.molla;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <pre>
 *     새 메시지를 작성할 때 사용자로부터 비밀번호를 입력받고 , 메시지 전송을 요청하는 Popup Activity
 *     이때 메시지 데이터 , 텍스트 혹은 이미지 데이터는 암호화되어서 보내진다.
 * </pre>
 * @author 우건희
 * @see <a href="https://github.com/klinker41/android_smsmms">klinker</a>
 * @see com.klinker.android.send_message.Message
 * @see com.klinker.android.send_message.Settings
 */
public class SetPasswordPopupActivity extends AppCompatActivity {
    /**
     * 팝업 ok 버튼
     */
    private Button mBtn_ok;
    /**
     * 팝업 취소 버튼
     */
    private Button mBtn_no;
    /**
     * password를 입력하는 edit text
     */
    private EditText mEdt_password;
    //MessageEncryption messageEncryption;
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
         * 팝업 제목
         */
        TextView mTxt_title = findViewById(R.id.txt_popup_title);
        /**
         * 팝업 정보
         */
        TextView mTxt_info = findViewById(R.id.txt_popup_info);
        mBtn_ok = findViewById(R.id.btn_popup_ok);
        mBtn_no = findViewById(R.id.btn_popup_no);
        mEdt_password = findViewById(R.id.edt_popup);
        mEdt_password.setVisibility(View.VISIBLE);

        mTxt_title.setText("비밀번호 설정");
        mTxt_info.setText("비밀번호를 입력해주세요");

        setBtnEvent();
    }

    /**
     * <pre>
     *     이 팝업창의 버튼들의 Event들을 정의하는 메서드
     *     {@link SetPasswordPopupActivity#mBtn_ok} 버튼 클릭 시 , 비밀번호를 입력받고 메시지를 암호화는 작업을 수행한다.
     *     {@link SetPasswordPopupActivity#sendMessage(String, String[], Bitmap[],String)} 메서드를 호출해서 메시지 전송을 요청한다.
     * </pre>
     *
     */
    private void setBtnEvent(){
        mBtn_no.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setResult(102);finish();
            }
        });
        mBtn_ok.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String password = mEdt_password.getText().toString();
                if(password.length() < 4) {
                    Toast.makeText(getApplicationContext(),"비밀번호는 4자리 이상 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        MessageEncryption messageEncryption = new MessageEncryption(password);
                        ArrayList<Bitmap> images = new ArrayList<>();
                        ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra("image");
                        for(int i = 0; i < imageUris.size(); i++) {
                            InputStream inputStream = getContentResolver().openInputStream(imageUris.get(i));
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 1080, 720, true);
                            images.add(resized);
                        }
                        String url = "http://www.dlogsoft.com/link.html";
                        String message = url + "?" + messageEncryption.AES_Encode(getIntent().getStringExtra("message")) + "?molla!";
                        String[] phoneNumbers = getIntent().getStringArrayExtra("phoneNumbers");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setAction(message);
                        String ac = intent.getAction();
                        sendMessage(ac, phoneNumbers, images.toArray(new Bitmap[images.size()]),password);
                        setResult(101);
                        finish();
                    } catch (Exception e) {
                        e.toString();
                    }
                }

            }

        });
    }



    /**
     * <pre>
     *     메시지 전송을 요청하는 메서드
     *     파라미터로 받은 데이터들을 {@link Message} 객체에 담는다.
     *     {@link Transaction#sendNewMessage(Message, long, String)}를 호출하여 메시지를 전송한다.
     * </pre>
     * @param msg 메시지 text
     * @param phoneNumbers 수신자 번호 Array
     * @param images 이미지 Array
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    public void sendMessage(String msg, String[] phoneNumbers, Bitmap[] images, String key) throws NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        Transaction transaction = new Transaction(this, settings);
        Message message = new Message(msg, phoneNumbers, images);
        long id = android.os.Process.getThreadPriority(android.os.Process.myTid());
        transaction.sendNewMessage(message, id,key);
    }

    public void sendMessage(String msg, String[] phoneNumbers,String key) throws NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        Transaction transaction = new Transaction(this, settings);
        Message message = new Message(msg, phoneNumbers);
        long id = android.os.Process.getThreadPriority(android.os.Process.myTid());
        transaction.sendNewMessage(message, id,key);
        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
    }

    /**
     * 팝업 창 뒤의 바깥 레이어 클릭시 팝업이 닫히지 않게 한다.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    //안드로이드 백버튼 막기
    @Override
    public void onBackPressed() {
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
