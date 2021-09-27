package com.dlog.molla;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * <pre>
 *     부모 액티비티로부터 onActivityResult() 이벤트를 수신하기위해서 Bus Class에 Fragment를 등록해야한다.
 *     이 클래스는 Fragemnt를 상속받고 onViewCreated()에서 Bus에 등록된다.
 *     Fragemnt에서 onActivityResult() 이벤트를 수신하려면 이 클래스를 상속해야한다.
 *     {@link BaseFragment#stateRegister}가 false일 경우 Bus에 등록하고 true로 바꾼다.
 *     onDestroy()에서 Bus에서 등록을 해제하고 {@link BaseFragment#stateRegister}를 false로 바꾼다.
 *     Bus Class는 {@link BusProvider}를 참조.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see <a href="https://gun0912.tistory.com/4>otto</a>
 * @see <a href="https://github.com/square/otto">square otto</a>
 */
public class BaseFragment extends Fragment {
    /**
     * Bus 클래스에 Fragment가 등록되어 있는 지를 나타내는 boolean 값
     * true 라면 등록되어있는 상태
     */
    private boolean stateRegister = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!stateRegister){
            BusProvider.getInstance().register(this);
            stateRegister = true;
        }
    }

    /**
     * Bus 등록을 해제하고 {@link BaseFragment#stateRegister}를 false로 바꾼다.
     */
    @Override
    public void onDestroy() {
        if(stateRegister){
            BusProvider.getInstance().unregister(this);
            stateRegister = false;
        }

        super.onDestroy();

    }
}
