package com.dlog.molla;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 온보딩 화면 첫 페이지
 * @author 최정헌
 * @version 1.0.0 20/04/20
 */
public class FragmentOnBoardingOne extends Fragment {
    private View view;
    private int imgRid;
    public FragmentOnBoardingOne(int imgRid){
        this.imgRid = imgRid;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmnet_onboarding_one,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.img_onboarding_one);
        imageView.setImageResource(imgRid);
    }


}
