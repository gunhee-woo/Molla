package com.dlog.molla;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * <pre>
 *     친구 목록 화면에 해당하는 Fragment
 *     부모 액티비티인 {@link MainActivity}의 onActivityResult()를 수신하기 위해 {@link BaseFragment}를 상속한다.
 *     검색 기능을 위해 TextWatcher를 implments 한다
 * </pre>
 *
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class FragmentFriendList extends BaseFragment implements TextWatcher {
    /**
     * 주소록 데이터베이스에서 가져온 User 리스트를 저장한다
     */
    private ArrayList<User> mUserAdressBook ; // 친구 리스트(주소록)
    /**
     * {@link UserDbAsyncTask}에 보낼 친구삭제 Request code
     */
    private final int REQUEST_DELET = 5;
    /**
     * {@link UserDbAsyncTask}에 보낼 친구추가 Request code
     */
    private final int REQUEST_ADD = 99;
    /**
     * 메시지 전송화면으로 이동하는 버튼
     */
    private ImageView btn_send ;
    /**
     * 친구 리스트를 화면에 보여주기 위한 리사이클러 뷰
     */
    private RecyclerView rcyl_friend;
    /**
     * 친구 검색 기능을 위한 Edit Text
     */
    private EditText edt_search;
    /**
     * 친구 리스트에서 각 아이템을 선택하기 위한 CheckBox
     */
    private CheckBox allSelectcheckBox;
    /**
     * 클릭하면 친구 추가 화면으로 이동하는 ImageView
     */
    private ImageView img_friend_add;
    /**
     * 클릭하면 친구 삭제 화면으로 이동하는 ImageView
     */
    private ImageView img_friend_delete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list,container,false);
        btn_send = v.findViewById(R.id.btn_friend_send);
        rcyl_friend = v.findViewById(R.id.rcyl_friend_list);
        edt_search = v.findViewById(R.id.edt_search);
        edt_search.addTextChangedListener(this);
        allSelectcheckBox = v.findViewById(R.id.chckBx_select_all);
        img_friend_add = v.findViewById(R.id.img_friendlist_add);
        img_friend_delete = v.findViewById(R.id.img_friendlist_delete);
        CheckAppFirstExecute();
        //callPermission();//퍼미션 요청하고 친구리스트 받기
        setBtnClick();//버튼들 클릭 이벤트 처리
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 이 fragment의 버튼 클릭 이벤트를 정의하는 메서드
     */
    private void setBtnClick(){//이 액티비티의 버튼 클릭 리스너 새팅 함수
        allSelectcheckBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(allSelectcheckBox.isChecked()){
                    ((FriendRcylAdapter)rcyl_friend.getAdapter()).allSelect();
                }
                else{
                    ((FriendRcylAdapter)rcyl_friend.getAdapter()).allDeSelect();
                }
            }
        });
        img_friend_delete.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //삭제 팝업 띄우고
                String deleteCount = String.valueOf((((FriendRcylAdapter)rcyl_friend.getAdapter()).getmSelectCount()));
                if(deleteCount.equals("0")){
                    Toast.makeText(getContext(),"선택된 연락처가 없습니다.",Toast.LENGTH_LONG).show();
                }
                else {
                    Intent deletePopupIntent = new Intent(getContext(), PopupActivity.class);
                    deletePopupIntent.putExtra("TaskNum", 0);
                    deletePopupIntent.putExtra("PopupTitle", "정말 삭제하시겠습니까?");
                    deletePopupIntent.putExtra("PopupInfo", deleteCount + "개의 " + "연락처가 삭제됩니다.");
                    startActivityForResult(deletePopupIntent, REQUEST_DELET);
                }
            }
        });
        img_friend_add.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getContext(), FriendAddActivity.class);
                startActivityForResult(addIntent,REQUEST_ADD);
            }
        });


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getActivity(), SendActivity.class);
                ArrayList<User> sendUserList = new ArrayList<>();
                sendUserList = ((FriendRcylAdapter)rcyl_friend.getAdapter()).getCheckUserList();
                sendIntent.putExtra("UserList",sendUserList);
                startActivity(sendIntent);
            }
        });

    }

    /**
     * <pre>
     *     주소록 데이터베이스에서 데이터를 가져오는 메서드
     *     주소록에서 읽은 데이터를 {@link User}로 가공하여 앱 내부 데이터베이스 {@link UserDataBase}에 저장한다.
     * </pre>
     * @return {@link User} 리스트
     */
    private ArrayList<User> getUserList(){//주소록 DB에서 데이터 긁어오는 함수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
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
                    User user = new User(cursor.getString(0),num);//name , num
                    hashlist.add(user);
                }while (cursor.moveToNext());
            }
            ArrayList<User> userList = new ArrayList<>(hashlist);
            return userList;
        }
        return new ArrayList<User>();

    }

    /**
     * <pre>
     *     이 fragment가 최초 실행되었을 때만 주소록에 접근해서 데이터를 읽어와야 한다. (혹은 Setting화면에서 유저가 주소록 데이터를 가져오길 원할 경우)
     *     SharedPreferences에 최초실행인지를 나타내는 boolean 값으로 판별.
     *     최초 실행시 주소록 데이터베이스를 읽어온다.
     *     최초 실행이 아닐시 {@link UserDataBase}에서 데이터를 가져온다.
     * </pre>
     *
     */
    public void CheckAppFirstExecute() {
        SharedPreferences pref = getContext().getSharedPreferences("IsFirst" , Activity.MODE_PRIVATE);
        boolean isFirst = pref.getBoolean("isFirst", false);
        if(!isFirst){ //최초 실행시 true 저장
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();
            mUserAdressBook = getUserList();
            addAdressBookToDb();
        } else {
            loadUserList();
        }
    }

    //주소록 접근 퍼미션 요청
    private void callPermission() { // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            mUserAdressBook = getUserList();
            addAdressBookToDb();
        } else {//이미 퍼미션 허가가 되어있다 > 디비에 다 저장되어있으니까 꺼내라
            //DB에서 긁어와서 리사이클러뷰 생성
            loadUserList();
        }
    }
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// Permission is granted . 즉 최초진입
                //주소록가서 긁어와서 db에저장하고 리사이클러뷰 생성
                mUserAdressBook = getUserList();
                addAdressBookToDb();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_LONG).show();
                finish();
            } }
    }*/

    /**
     * 최초실행시 가져온 주소록 리스트를 {@link UserDataBase}에 저장하는 메서드
     */
    private void addAdressBookToDb(){//주소록 리스트를 DB에 저장하는 함수.
        mUserAdressBook = getUserList();
        new UserDbAsyncTask(getActivity(), mUserAdressBook,1).execute();
    }
/*
    private void addReceiverToFriendList() {
        receiverList = new ArrayList<User>();
        HashMap<Integer, User> receiveMap = (HashMap<Integer, User>) getIntent().getExtras().get("receiverList");
        for(Map.Entry<Integer, User> entry : receiveMap.entrySet()) {
            receiverList.add(entry.getValue());
        }
        new UserDbAsyncTask(this, receiverList, 5);
    }*/

    /**
     * {@link UserDataBase}에서 모든 {@link User}를 가져와서 리사이클러 뷰로 화면에 뿌려주는 작업을 수행하는 메서드
     */
    private void loadUserList(){
        new UserDbAsyncTask(getActivity(), (User) null,2).execute();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            ((FriendRcylAdapter) rcyl_friend.getAdapter()).getFilter().filter(s);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * {@link UserDataBase}와 관련된 백그라운드 작업을 수행하는 AsyncTask 클래스
     * , taskCode 별로 수행할 작업들을 구별한다.
     */
    public static class UserDbAsyncTask  extends AsyncTask<Void, Void, Integer> {//UserList DB관련 백그라운드 작업  taskCode별로 백그라운드 작업다르게 처리

        //Prevent leak
        private WeakReference<Activity> weakActivity;
        private ArrayList<User> userArrayList;
        private User[] userArr;
        public User user;
        private int taskCode = 0;

        public UserDbAsyncTask(Activity activity, ArrayList<User> userArrayList, int taskCode) {
            weakActivity = new WeakReference<>(activity);
            this.userArrayList = userArrayList;
            this.taskCode = taskCode;

        }
        public UserDbAsyncTask(Activity activity, User user, int taskCode) {
            weakActivity = new WeakReference<>(activity);
            this.taskCode = taskCode;
            this.user = user;
        }
        public UserDbAsyncTask(Activity activity, User[] userArr, int taskCode){
            weakActivity = new WeakReference<>(activity);
            this.userArr = userArr;
            this.taskCode = taskCode;
        }


        /**
         * <pre>
         *     taskCode : 1 일 경우
         *     유저리스트를 받아서 {@link UserDataBase}에 저장한다
         *     taskCode : 2 일 경우
         *     {@link UserDataBase}에서 모든 유저리스트를 가져온다
         *     taskCode : 3 일 경우
         *     {@link User} 한 명을 추가한다.
         *     taskCode : 4 일 경우
         *     유저 배열을 받아서 {@link UserDataBase}에서 삭제시킨다.
         *     taskCode : 5 일 경우
         *     도움말 > 주소록 동기화를 클릭한 경우 . 유저리스트를 받아서 {@link UserDataBase}에 저장하고 {@link FragmentFriendList#rcyl_friend}가 null이아니면 업데이트한다.
         * </pre>
         * @param params
         * @return UserListSize
         */
        @Override
        protected Integer doInBackground(Void... params) {
            switch (taskCode){
                case 5 :
                case 1 : {//유저리스트를 받아서 DB에 저장
                    UserDataBase db = Room.databaseBuilder( weakActivity.get(),
                            UserDataBase.class, "Users").build();
                    User[] users = new User[userArrayList.size()];
                    for(int i = 0 ; i < userArrayList.size() ; i ++){
                        users[i] = userArrayList.get(i);
                    }
                    db.userDao().insertUsers(users);
                    break;
                }
                case 2 : {//DB의 모든 유저리스트 가져오기
                    UserDataBase db = Room.databaseBuilder( weakActivity.get(),
                            UserDataBase.class, "Users").build();
                    userArrayList = (ArrayList<User>) db.userDao().getAllPerson();
                    break;
                }
                case 3 : {//유저 한명 추가
                    UserDataBase db = Room.databaseBuilder( weakActivity.get(),
                            UserDataBase.class, "Users").build();
                    db.userDao().insertUsers(user);
                    return 0;
                }
                case 4 : {//유저 arr db에서  삭제
                    UserDataBase db = Room.databaseBuilder( weakActivity.get(),
                            UserDataBase.class, "Users").build();
                    db.userDao().deleteUsers(userArr);
                    return 0;
                }
            }
            return userArrayList.size();
        }

        @Override
        protected void onPostExecute(Integer userArrayListSize) {
            Activity activity = weakActivity.get();
            if(activity != null) {
                switch (taskCode){
                    case 1 :{
                        if( (userArrayList != null) && !userArrayList.isEmpty()){
                            RecyclerView recyclerView = activity.findViewById(R.id.rcyl_friend_list);
                            recyclerView.setLayoutManager(new LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false));
                            FriendRcylAdapter adapter = new FriendRcylAdapter   (activity,userArrayList);
                            recyclerView.setAdapter(adapter);
                            Toast.makeText(activity,"연락처 리스트를 업데이트 했습니다.",Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    case 2 :{
                        if( (userArrayList != null)){
                            RecyclerView recyclerView = activity.findViewById(R.id.rcyl_friend_list);
                            recyclerView.setLayoutManager(new LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false));
                            FriendRcylAdapter adapter = new FriendRcylAdapter(activity,userArrayList);
                            recyclerView.setAdapter(adapter);
                            break;
                        }
                    }
                    case 3 :{
                        activity.setResult(98);
                        activity.finish();
                        break;
                    }
                    case 5:{
                        RecyclerView recyclerView = activity.findViewById(R.id.rcyl_friend_list);
                        if(recyclerView != null){
                            new UserDbAsyncTask(activity,(User)null,2).execute();
                        }
                        Toast.makeText(activity,"연락처 리스트를 업데이트 했습니다.",Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                return;
            }
        }
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
        if (requestCode == REQUEST_ADD) {//친구 추가
            switch (resultCode) {
                case 98: {
                    new UserDbAsyncTask(getActivity(), (User) null, 2).execute();
                    break;
                }
            }
        }
        if(resultCode == 0){//친구 삭제 no
            //do nothing
        }
        if(resultCode == 1){//친구 삭제 ok
            //리사이클러뷰와 DB에서 삭제
            ((FriendRcylAdapter)rcyl_friend.getAdapter()).onItRemove();
            Toast.makeText(getContext(),"삭제가 완료 되었습니다.",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        edt_search.removeTextChangedListener(this);
    }
}
