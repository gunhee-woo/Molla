package com.dlog.molla;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * 온 보딩 화면의 뷰페이저의 어뎁터
 * @author 최정헌
 * @version 1.0.0 20/04/20
 */
public class OnBoardingAdapter extends FragmentPagerAdapter {
    /**
     * 온보딩 이미지 리소스 id 배열
     */
    private int[] imgRidArr;
    public OnBoardingAdapter(FragmentManager fm , int[] imgRIdArr){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.imgRidArr = imgRIdArr;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 14 : {//마지막
                return new FragmentOnBoardingLast();
            }
            default:{
                return new FragmentOnBoardingOne(imgRidArr[position]);
            }
        }
    }


    @Override
    public int getCount() {
        return imgRidArr.length;
    }


}
