package com.dlog.molla;

import android.app.Application;

/**
 * <pre>
 *     Application 전체에서 사용될 인스턴스들을 가지고 있는 클래스
 *     어느 클래스에서나 접근가능하다.
 *     Application이 실행되면 가장먼저 GlobalApplication의 onCreate()이 실행된다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class GlobalApplication extends Application {
    /**
     * 이 Application에 사용되는 SharedPreferneces들을 관리하는 {@link SharedPreferencesActivity} 클래스의 인스턴스
     */
    public static SharedPreferencesActivity prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new SharedPreferencesActivity(getApplicationContext());
        FontOverride.setDefaultFont(this, "DEFAULT", "godom.ttf");
        FontOverride.setDefaultFont(this, "MONOSPACE", "godom.ttf");
    }

}
