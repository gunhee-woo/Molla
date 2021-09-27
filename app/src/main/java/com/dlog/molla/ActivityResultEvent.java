package com.dlog.molla;


import android.content.Intent;

/**
 * <pre>
 *     프래그먼트에서 부모 액티비티의 onActivityResult() 이벤트를 수신하기 위한 ActivityResultEvent class
 *     Bus Class를 활용한 otto library를 참고했다.
 *     Bus Class는 {@link BusProvider}를 참조.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see <a href="https://github.com/square/otto">square otto</a>
 */
public class ActivityResultEvent {
    /**
     * 부모 액티비티의 onActivityResult() 메서드에서 받은 requestCode
     */
    private int mRequestCode;
    /**
     * 부모 액티비티의 onActivityResult() 메서드에서 받은 resultCode
     */
    private int mResultCode;
    /**
     * 부모 액티비티의 onActivityResult() 메서드에서 받은 Intent
     */
    private Intent mData;

    /**
     * <pre>
     *     생성자. 부모액티비티의 onActivityResult()에서 받은 requestCode, resultCode, intent를 파라미터로 받는다.
     * </pre>
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param intent intent
     */
    public ActivityResultEvent(int requestCode, int resultCode, Intent intent){
        this.mRequestCode = requestCode;
        this.mResultCode = resultCode;
        this.mData = intent;
    }


    public int getmRequestCode() {
        return mRequestCode;
    }

    public int getmResultCode() {
        return mResultCode;
    }

    public Intent getmData() {
        return mData;
    }

    public void setmData(Intent mData) {
        this.mData = mData;
    }

    public void setmRequestCode(int mRequestCode) {
        this.mRequestCode = mRequestCode;
    }

    public void setmResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
    }
}
