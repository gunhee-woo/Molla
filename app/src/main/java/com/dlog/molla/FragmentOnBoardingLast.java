package com.dlog.molla;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * <pre>
 *     온보딩 화면 마지막 페이지
 *     다음 버튼을 누르면 로그인 화면으로 넘어간다.
 * </pre>
 *
 * @author 최정헌
 * @version 1.0.0 20/04/20
 */
public class FragmentOnBoardingLast extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_last,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btn_next = view.findViewById(R.id.btn_onboarding_next);
        if(GlobalApplication.prefs.getIsSeeOnBoarding().equals("2")){//설정 > 도움말 눌러서 온보딩을 다시보는 경우
            btn_next.setVisibility(View.GONE);
        }
        else{
            btn_next.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

    }
}
