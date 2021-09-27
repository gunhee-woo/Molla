package com.dlog.molla;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * <pre>
 *     설정 화면에 해당하는 Fragment
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class FragmentSetting extends Fragment {
    /**
     * 비밀번호 변경 화면으로 이동할 때 , Intent에 전달하는 request code
     */
    private final int REQUEST_CHECK_PASSWORD = 5;
    /**
     * 주소록에서 데이터를 가져올 때 permission을 요청할 때 전달하는 request code
     */
    private final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static String MESSAGE_DATE_PREF = "MESSAGE_DATE_PREF";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOpenSourceRcyl(view);
        setLayoutListener(view);

    }
    private void setOpenSourceRcyl(View view){

    }

    /**
     * 설정 화면의 각 레이아웃들을 클릭했을 때 처리할 이벤트들을 정의하는 메서드
     * @param view
     */
    private void setLayoutListener(View view){
        ConstraintLayout layout_password = view.findViewById(R.id.layout_setting_password);
        ConstraintLayout layout_timer = view.findViewById(R.id.layout_setting_timer);
        ConstraintLayout layout_address = view.findViewById(R.id.layout_setting_address);
        ConstraintLayout layout_opensource = view.findViewById(R.id.layout_setting_opensource);
        ConstraintLayout layout_help = view.findViewById(R.id.layout_setting_help);

        layout_password.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //비번변경 팝업띄우기

                // 비밀번호 확인 팝업
                Intent checkPasswordIntent = new Intent(getContext(), PopupActivity.class);
                checkPasswordIntent.putExtra("TaskNum", REQUEST_CHECK_PASSWORD);
                checkPasswordIntent.putExtra("PopupTitle", "비밀번호 확인");
                checkPasswordIntent.putExtra("PopupInfo", "현재 비밀번호를 입력해주세요");
                startActivity(checkPasswordIntent);

            }
        });

        layout_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"30초", "1분", "3분"};
                int setTimeIntervalValueIndex;
                if(GlobalApplication.prefs.getTimerIntervalPreferences().equals("0030"))
                    setTimeIntervalValueIndex = 0;
                else if(GlobalApplication.prefs.getTimerIntervalPreferences().equals("0100"))
                    setTimeIntervalValueIndex = 1;
                else
                    setTimeIntervalValueIndex = 2;
                AlertDialog.Builder dayDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                dayDialog.setTitle("타이머 시간 간격을 설정해주세요.")
                        .setSingleChoiceItems(items, setTimeIntervalValueIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedItem = items[which];
                                switch (selectedItem){
                                    case "30초":{
                                        GlobalApplication.prefs.saveTimerIntervalPreferences("0030");
                                        break;
                                    }
                                    case "1분":{
                                        GlobalApplication.prefs.saveTimerIntervalPreferences("0100");
                                        break;
                                    }
                                    case "3분":{
                                        GlobalApplication.prefs.saveTimerIntervalPreferences("0300");
                                        break;
                                    }
                                }
                            }
                        })
                        .setNeutralButton("선택", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which >= 0)
                                    Toast.makeText(getContext(),
                                            items[which], Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });
        layout_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        });
        layout_opensource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getContext(), OpenSourceActivity.class);
                getContext().startActivity(intent1);
            }
        });
        layout_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalApplication.prefs.saveIsSeeOnBoarding("2");//다시 온보딩을 보지 않은 상태로 되돌리고
                Intent intent = new Intent(getContext(),OnBoardingActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{//주소록 퍼미션
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    addAdressBookToDb();
                }
                else{
                    Toast.makeText(getContext(),"sorry, we need permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 주소록에서 가져온 데이터들을 {@link User} 클래스로 가공하여 {@link UserDataBase}에 저장하는 메서드
     */
    private void addAdressBookToDb(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = getContext().getContentResolver().query(uri,projection,null,selectionArgs,sortOrder);
        LinkedHashSet<User> hashlist = new LinkedHashSet<>();
        if(cursor.moveToFirst()){
            do{
                String num = cursor.getString(1).replace("-","");
                User user = new User(cursor.getString(0),num);
                hashlist.add(user);
            }while (cursor.moveToNext());
        }
        ArrayList<User> userList = new ArrayList<>(hashlist);
        new FragmentFriendList.UserDbAsyncTask(getActivity(), userList,5).execute();
    }
    /**
     * 부모 액티비티인 {@link MainActivity}에서 onActivityResult()를 수신하기 위한 Subscribe 메서드
     * @param activityResultEvent {@link ActivityResultEvent}
     */
    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        onActivityResult(activityResultEvent.getmRequestCode(),activityResultEvent.getmResultCode(),activityResultEvent.getmData());
    }
    /**
     * 부모 액티비티인 {@link MainActivity}에서 onActivityResult()를 수신하는 메서드
     * @param requestCode {@link MainActivity}에서 onActivityResult()의 requestCode
     * @param resultCode {@link MainActivity}에서 onActivityResult()의 resultcode
     * @param data {@link MainActivity}에서 onActivityResult()의 Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 10){//비번 변경 no
            Toast.makeText(getContext(), "비밀번호 변경이 취소되었습니다", Toast.LENGTH_SHORT).show();
        }
        if(resultCode == 11){//비번 변경 ok
            Toast.makeText(getContext(), "비밀번호 변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
        }
    }
}
