package com.dlog.molla;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <pre>
 *     이 Application에서 사용할 SharedPreferneces를 관리하는 클래스
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class SharedPreferencesActivity {
    /**
     * 로그인 password
     */
    private SharedPreferences prefs;
    /**
     * 채팅방 타이머 시간 간격. 형식은 MMSS (분분초초)
     */
    private SharedPreferences timerPrefs;
    /**
     * 온보딩 화면을 봤는 지 확인하기 위한 값. 1이면 확인/ 0이면 확인하지 않음/  2이면 확인 했지만 설정에서 도움말을 클릭해서 다시보기를 한 경우
     */
    private SharedPreferences isSeeOnBoarding;

    public SharedPreferencesActivity(Context context){
        prefs = context.getSharedPreferences("login_password",Context.MODE_PRIVATE);
        timerPrefs = context.getSharedPreferences("timer_interval",Context.MODE_PRIVATE);
        isSeeOnBoarding = context.getSharedPreferences("isSeeOnBoarding",Context.MODE_PRIVATE);
    }

    public String getPasswordPreferences(){
        return prefs.getString("login_password","");
    }
    public String getTimerIntervalPreferences(){ return  timerPrefs.getString("timer_interval","0030");}
    public String getIsSeeOnBoarding(){ return isSeeOnBoarding.getString("isSeeOnBoarding","0");}

    /**
     * 파라미터로 받은 value 값을 {@link SharedPreferencesActivity#prefs}에 저장한다.
     * @param value
     */
    public void savePreferences(String value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("login_password",value);
        editor.commit();
    }

    /**
     * 파라미터로 받은 value 값을 {@link SharedPreferencesActivity#timerPrefs}에 저장한다.
     * @param value
     */
    public void saveTimerIntervalPreferences(String value){
        SharedPreferences.Editor editor = timerPrefs.edit();
        editor.putString("timer_interval",value);
        editor.commit();
    }

    /**
     * 파라미터로 받은 value 값을 {@link SharedPreferencesActivity#isSeeOnBoarding}에 저장한다.
     * @param value
     */
    public void saveIsSeeOnBoarding(String value){
        SharedPreferences.Editor editor = isSeeOnBoarding.edit();
        editor.putString("isSeeOnBoarding",value);
        editor.commit();
    }

    public void clearSharedPreference() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
