package com.dlog.molla;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.util.concurrent.Executor;

/**
 * <pre>
 *     로그인 화면에 해당하는 Activity
 * </pre>
 * @version 1.0.0 20/04/13
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * 사용자가 password를 입력하는 EditText
     */
    private EditText mEdt_password;

    /**
     * 이 Activity를 저장한다.
     */
    private Activity mActivity;

    private Handler mHandler = new Handler();

    private Executor mExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
            mHandler.post(command);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /**
         * 비밀번호를 검사하고 메인으로 이동하는 버튼
         */
        Button mBtn_ok = findViewById(R.id.btn_login_ok);
        mEdt_password = findViewById(R.id.edt_login_password);
        TextView mLogin_txt = findViewById(R.id.txt_login);
        mActivity = this;
        /**
         * 사용자의 지문을 입력받는 화면으로 넘어가는 버튼
         */
        Button mBiometric_login_button = findViewById(R.id.biometric_login);

        final String savedPassword = GlobalApplication.prefs.getPasswordPreferences();

        if(savedPassword.equals("")){//최초 로그인 > 비밀번호 설정
            mLogin_txt.setText("비밀번호 만들기");
            mBtn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mEdt_password.length() < 4){
                        Toast.makeText(getApplicationContext(),"4자리 이상 입력해주세요",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        GlobalApplication.prefs.savePreferences(hashingPassword(mEdt_password.getText().toString()));
                        Intent permission_intent = new Intent(getApplicationContext(), PermissionActivity.class);
                        startActivity(permission_intent);
                        mActivity.finish();
                    }
                }
            });
        }
        else{//이미 로그인했었음 > 비밀번호 검사
            mBtn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(hashingPassword(mEdt_password.getText().toString()).equals(savedPassword)){
                        Intent permission_intent = new Intent(getApplicationContext(), PermissionActivity.class);
                        startActivity(permission_intent);
                        mActivity.finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        mBiometric_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkUseDevice())
                    showBiometricPrompt();
                else
                    Toast.makeText(getApplicationContext(), "생체인식을 지원하지 않는 기기입니다", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 지문인식 화면으로 이동하고 지문인증을 진행하는 메서드
     */
    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("생체인식 로그인")
                        .setNegativeButtonText("취소")
                        .setConfirmationRequired(false)
                        .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                mExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "인증 실패: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                BiometricPrompt.CryptoObject authenticatedCryptoObject =
                        result.getCryptoObject();
                Toast.makeText(getApplicationContext(),
                        "인증 성공", Toast.LENGTH_SHORT)
                        .show();
                Intent permission_intent = new Intent(getApplicationContext(), PermissionActivity.class);
                startActivity(permission_intent);
                mActivity.finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "인증 실패",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * 디바이스에 지문인식 기능이 있는 지 확인하는 메서드
     */
    public boolean checkUseDevice() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("TAG","No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("TAG","Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("TAG","The user hasn't associated any biometric credentials " +
                        "with their account.");
                return false;
        }
        return false;
    }

    /**
     * SHA-1 암호화를 이용하여 비밀번호를 해싱하여 반환
     * @param password 사용자가 입력한 비밀번호
     * @return 해싱된 비밀번호
     */

    public static String hashingPassword(String password) { // SHA-1 apache commons codec library 사용
        return new String(Hex.encodeHex(DigestUtils.sha1(password)));
    }
}
