package com.dlog.molla;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     온보딩 화면의 activity
 *     뷰페이저를 이용했다.
 *     페이지를 넘길 때 마다 Indicator 표시를 위해 PageIndicatorView 라이브러리를 이용했다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/20
 * @see <a href="https://github.com/romandanylyk/PageIndicatorView">PageIndicatorView</a>
 */
public class OnBoardingActivity extends AppCompatActivity {
    /**
     * 뷰페이저 어뎁터
     */
    private OnBoardingAdapter onBoardingAdapter;

    /**
     * 페이지 인디케이터
     */
    private PageIndicatorView pageIndicatorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        if(GlobalApplication.prefs.getIsSeeOnBoarding().equals("1")){//이미 온보딩 화면을 봤음.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{//온보딩을 띄워야 함.
            setViewPager();
        }
    }

    /**
     * 온보딩 화면의 뷰페이저 세팅
     */
    private void setViewPager(){
        int[] imgRidArr = {R.drawable.onboarding_1,R.drawable.onboarding_2,R.drawable.onboarding_3,R.drawable.onboarding_4,R.drawable.onboarding_5,R.drawable.onboarding_6,
                R.drawable.onboarding_7,R.drawable.onboarding_8,R.drawable.onboarding_9,R.drawable.onboarding_10,R.drawable.onboarding_11,R.drawable.onboarding_12,R.drawable.onboarding_13,
                R.drawable.onboarding_14,R.drawable.onboarding_15};
        onBoardingAdapter = new OnBoardingAdapter(getSupportFragmentManager(),imgRidArr);
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        ViewPager vp = findViewById(R.id.layout_onboarding_view_pager);
        vp.setAdapter(onBoardingAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //do nothing
            }

            @Override
            public void onPageSelected(int position) {
                pageIndicatorView.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //do nothing
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG","Onboarding OnDestroy");
        GlobalApplication.prefs.saveIsSeeOnBoarding("1");//온보딩 봤음 상태로 변경
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(GlobalApplication.prefs.getIsSeeOnBoarding().equals("2")){//도움말에서 온보딩을 다시 보는 경우
            super.onBackPressed();
        }else {//앱 설치하고 맨 처음 온보딩 화면을 보는 경우
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
