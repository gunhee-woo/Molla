package com.dlog.molla;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * 이 앱에서 사용하고 있는 오픈소스 라이브러리의 내용을 보여주는 액티비티
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class OpenSourceActivity extends AppCompatActivity {
    /**
     * 홈 버튼 클릭 이벤트를 수신하는 Broadcast Receiver
     */
    private HomeKeyReceiver homeKeyReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source);
        homeKeyReceiver = new HomeKeyReceiver();
        setRcyl();
    }

    /**
     * openSourceInfoArrayList에 오픈소스 라이브러리 정보를 탑재하고 리사이클러뷰로 뿌려 액티비에 보여줌
     */

    private void setRcyl(){
        /**
         * 오픈소스 라이브러리 정보를 담고있는 리스트
         */
        ArrayList<OpenSourceInfo> mOpenSourceInfoArrayList = new ArrayList<>();
        mOpenSourceInfoArrayList.add(new OpenSourceInfo("Otto - An event bus by Square",
                "Copyright 2012 Square Inc, 2010 Google Inc.","Apache License 2.0",
                "https://github.com/square/otto"));
        mOpenSourceInfoArrayList.add(new OpenSourceInfo("lottie - android","Copyright 2018 Airbnb Inc","Apache License 2.0","https://www.github.com/airbnb/lottie-android"));
        mOpenSourceInfoArrayList.add(new OpenSourceInfo("biometrics","Copyright 2012 the MITRE Corporation","Apache License 2.0","https://www.github.com/biometrics"));
        mOpenSourceInfoArrayList.add(new OpenSourceInfo("klinker41","Copyright 2017 Jacob Klinker","Apache License 2.0","https://www.github.com/klinker41/android-smsmms"));
        mOpenSourceInfoArrayList.add(new OpenSourceInfo("PageIndicatorView","Copyright 2017 Roman Danylyk","Apache License 2.0","https://github.com/romandanylyk/PageIndicatorView"));


        RecyclerView recyclerView = findViewById(R.id.rcyl_opensource);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(new RcylOpenSourceAdapter(mOpenSourceInfoArrayList));
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
