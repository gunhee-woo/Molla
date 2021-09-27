package com.dlog.molla;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 수신자를 선택하고 새로운 메시지를 작성하고 보내는 기능을 하는 클래스
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 *
 */


public class SendActivity extends AppCompatActivity implements TextWatcher, ReceiverPhoneNumberAdapter.OnItemClickListener, View.OnClickListener {
    /**
     * 사용자 핸드폰의 갤러리에서 이미지를 갖고 오기 위한 요청코드
     */
    private static final int PICK_FROM_GALLERY = 1;
    /**
     * 사용자 핸드폰의 카메라를 사용할 수 있게 하기 위한 요청코드
     */
    private static final int PICK_FROM_CAMERA = 2;
    /**
     * 사용자 핸드폰의 갤러리에 접근하는 Uri
     */
    private Uri mPhotoURI;
    /**
     * 친구를 검색할 수 있는 EditText
     */
    private ClearEditText mReceiverClearEditText;
    /**
     * 메시지를 입력하는 EditText
     */
    private ClearEditText mMessageClearEditText;
    /**
     * 수신자를 추가하기 위해 친구목록으로 이동할 수 있는 버튼
     */
    private ImageView mAddReceiverButton;
    /**
     * receiverClearEditText에서 친구를 검색할때 친구목록을 보여주는 RecyclerView
     */
    private RecyclerView mRecyclerView;
    /**
     * recyclerView에 사용하는 Adapter
     */
    private ReceiverPhoneNumberAdapter mAdapter;
    /**
     * 필터링된 친구 목록을 담고 있는 리스트
     */
    private ArrayList<User> mPersonDataSet;
    /**
     * 필터링되지 않은 친구목록을 담고 있는 리스트
     */
    private ArrayList<User> mDefaultPersonDataSet;
    /**
     * 수신자 버튼을 담는 레이아웃
     */
    private LinearLayout mReceiverNameListLayout;
    /**
     * 수신자 목록을 담고 있는 리스트
     */
    private HashMap<Integer, User> mReceiverList;
    /**
     * 비밀번호 입력 팝업 액티비티와 연결하는 버튼
     */
    private ImageView mSendMessageButton;
    /**
     * receiverNameListLayout를 감싸고 있는 HorizontalScrollView
     */
    private HorizontalScrollView mReceiverNameListLayoutScrollView;
    /**
     * 갤러리나 카메라로 연결하는 버튼
     */
    private FloatingActionButton mAddMultiMediaButton;
    /**
     * 수신자 버튼의 숫를 계산하는 변수
     */
    private int mReceiverButtonCount;
    /**
     * addMultiMediaButton을 누르때 일어나는 애니메이션
     */
    private Animation mBtnOpen, mBtnClose;
    /**
     * addMultiMediaButton을 누르때 FloatingActionButton들을 조절하는 변수
     */
    private Boolean isFabOpen = false;
    /**
     * addMultiMediaButton을 누르때 생기는 버튼
     */
    private FloatingActionButton mImageFltButton, mCameraFltButton;
    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들을 담고 있는 레이아웃을 감사고 있는 HorizontalScrollView
     */
    private HorizontalScrollView mReceiverImageListScrollView;
    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들을 담고 있는 레이아웃
     */
    private LinearLayout mReceiveImageList;
    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들을 담고 있는 HashMap
     */
    private HashMap<Integer, Bitmap> mReveiveImageListData;
    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들의 수를 계산하는 변수
     */
    private int mReceiveImageViewCount;
    /**
     * 메시지를 보냈을 때 일어나는 애니메이션
     */
    private LottieAnimationView mLottieAnimationView;

    /**
     * 사진 Uri를 담는 변수
     */
    private ArrayList<Uri> mPhotoUri;

    /**
     * 홈 버튼 클릭 이벤트를 수신하는 Broadcast Receiver
     */
    private HomeKeyReceiver homeKeyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        homeKeyReceiver = new HomeKeyReceiver();

        new UserDbAsyncTask(this, mDefaultPersonDataSet).execute();

        //메시지 보내고 나서 애니메이션
        final ConstraintLayout layout_lottie = findViewById(R.id.layout_send_anim);
        layout_lottie.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                layout_lottie.setVisibility(View.GONE);
                mLottieAnimationView.cancelAnimation();
            }
        });
        mLottieAnimationView = findViewById(R.id.send_lottie_view);
        mLottieAnimationView.setAnimation("msgsend.json");
        mLottieAnimationView.loop(true);


        mReceiverList = new HashMap<Integer, User>();
        mReceiverNameListLayoutScrollView = findViewById(R.id.receiver_name_list_scrollview);
        mReceiverNameListLayout = findViewById(R.id.receiver_name_list);
        mReceiverButtonCount = 0;

        mReceiverClearEditText = findViewById(R.id.clear_edit_text);
        mReceiverClearEditText.setHint("수신자 검색");
        mReceiverClearEditText.setHintTextColor(Color.WHITE);
        mReceiverClearEditText.addTextChangedListener(this);
        mRecyclerView = findViewById(R.id.phone_number_list);

        mAddReceiverButton = findViewById(R.id.add_receiver_button);
        mAddReceiverButton.setOnClickListener(this);

        mPersonDataSet = new ArrayList<User>();

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ReceiverPhoneNumberAdapter(this, mPersonDataSet);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        mReceiverImageListScrollView = findViewById(R.id.receiver_image_list_scrollview);
        mReceiveImageList = findViewById(R.id.receiver_image_list);
        mReveiveImageListData = new HashMap<Integer, Bitmap>();
        mReceiveImageViewCount = 0;

        mMessageClearEditText = findViewById(R.id.message_text);
        mSendMessageButton = findViewById(R.id.message_send_button);

        mPhotoUri = new ArrayList<Uri>();

        //FriendList에서 데이터 받는 부분
        ArrayList<User> sendUserList = new ArrayList<>();
        try {
            sendUserList = (ArrayList<User>) getIntent().getExtras().get("UserList");
            if (!sendUserList.isEmpty()) {
                for (int i = 0; i < sendUserList.size(); i++) {
                    mReceiverList.put(i, sendUserList.get(i));
                    createReceiverButton(sendUserList.get(i));
                }
            }
        } catch (Exception e) {
        }

        mSendMessageButton.setOnClickListener(this);

        mBtnOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        mBtnClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        mAddMultiMediaButton = findViewById(R.id.add_multimedia_flt_button);
        mImageFltButton = findViewById(R.id.image_flt_button);
        mCameraFltButton = findViewById(R.id.camera_flt_button);
        mAddMultiMediaButton.setOnClickListener(this);
        mImageFltButton.setOnClickListener(this);
        mCameraFltButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_multimedia_flt_button:
                anim();
                break;
            case R.id.image_flt_button:
                anim();
                selectGallery();
                break;
            case R.id.camera_flt_button:
                anim();
                takePhoto();
                break;
            case R.id.add_receiver_button:
                onBackPressed();
                break;
            case R.id.message_send_button:
                sendEncryptionMessage();
                break;
        }
    }

    /**
     * 사용자가 입력한 멧시지와 수신자 전화번호 자신의 잔화번호, 이미지를 같이 SetPasswordPopupActivity로 보내는 역할을 하는 함수
     */

    public void sendEncryptionMessage() {
        String text = mMessageClearEditText.getText().toString();
        if(mReceiverList.isEmpty() || mReceiverList == null) {
            Toast.makeText(getApplicationContext(), "수신자를 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.isEmpty()) {
            Toast.makeText(getApplicationContext(), "내용을 입력해주세요", Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<Bitmap> images = new ArrayList<>();
        for(HashMap.Entry<Integer, Bitmap> entry : mReveiveImageListData.entrySet()) {
            images.add(entry.getValue());
        }
        String message = "message:" + text;
        ArrayList<String> phoneNumbers = new ArrayList<>();
        for(HashMap.Entry<Integer, User> entry : mReceiverList.entrySet()) {
            phoneNumbers.add(entry.getValue().mPhoneNum);
        }
        Intent intent = new Intent(SendActivity.this, SetPasswordPopupActivity.class);
        intent.putExtra("message", message);
        intent.putExtra("phoneNumbers", phoneNumbers.toArray(new String[phoneNumbers.size()]));
        intent.putExtra("image", mPhotoUri);
        startActivityForResult(intent,101);
    }

    /**
     * FloatingActionButton 에서 일어나는 애니메이션을 조절하는 함수
     */

    public void anim() {
        if (isFabOpen) {
            mAddMultiMediaButton.setImageResource(R.drawable.ic_add_white_24dp);
            mImageFltButton.startAnimation(mBtnClose);
            mCameraFltButton.startAnimation(mBtnClose);
            mImageFltButton.setClickable(false);
            mCameraFltButton.setClickable(false);
            isFabOpen = false;
        } else {
            mAddMultiMediaButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_close_white_24dp));
            mImageFltButton.startAnimation(mBtnOpen);
            mCameraFltButton.startAnimation(mBtnOpen);
            mImageFltButton.setClickable(true);
            mCameraFltButton.setClickable(true);
            isFabOpen = true;
        }
    }

    /**
     * 카메라로 사진을 찍고 그 사진을 내부저장소에 저장하는 기능을 하는 함수
     */

    public void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
        }
    }

    /**
     * 사진을 찍고 얻은 비트맵으로 Uri를 얻는 기능을 하는 함수
     * @param context context
     * @param inImage 사진
     * @return 사진 Uri
     */

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * 사용자 핸드폰 갤러리에서 사진을 선택하는 기능을 하는 함수
     */

    public void selectGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_GALLERY);
    }

    /**
     * 메시지를 보내고 난 후, 갤러리에서 이미지를 선택하고 난 후, 카메라로 사진을 찍고 난 후 다시 액티비티로 돌아왓을때 수행하는 함수
     * @param requestCode 요청코드
     * @param resultCode 결과코드
     * @param data Intent에 담긴 데이터
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 101){
            //메시지 보내고 돌아온 후
            findViewById(R.id.layout_send_anim).setVisibility(View.VISIBLE);
            mLottieAnimationView.playAnimation();
            mMessageClearEditText.setText(null);
            HashSet<String> numSet = new HashSet();
            String name = "";
            for (int i = 0; i < mReceiverList.size() ; i++){
                User user = mReceiverList.get(i);
                numSet.add(user.mPhoneNum);
                name = name + "  " + user.mNickName;
            }
            FragmentChatList fragmentChatList = (FragmentChatList) MainActivity.getmFragmentChatList();
            fragmentChatList.sendNewMsg(numSet,name);

        }
        if(resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
                case PICK_FROM_GALLERY : {
                    if(data.getData()!= null){
                        try{
                            mPhotoURI = data.getData();
                            createReceiveImageView(mPhotoURI, null);
                            mPhotoUri.add(mPhotoURI);
                        }catch (Exception e){
                            e.printStackTrace();
                            Log.v("알림","앨범에서 가져오기 에러");
                        }
                    }
                    break;
                }
                case PICK_FROM_CAMERA: {
                    try{
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Uri uri = getImageUri(getApplicationContext(), imageBitmap);
                        mPhotoUri.add(uri);
                        createReceiveImageView(null, imageBitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
        }
    }

    /**
     * 사진의 크기를 재조정하고 비트맵으로 반환
     * @param uri 이미지 Uri
     * @return 이미지 비트맵
     * @throws FileNotFoundException 이미지 파일을 찾지 못했을 때 발생
     */

    public Bitmap resizeImage(Uri uri) throws FileNotFoundException {
        InputStream inputStream = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    /**
     * 수신자에게 보낼 이미지를 담고있는 레이아웃에 이미지를 넣고 빼는 역할을 하는 함수
     * @param uri 이미지 Uri
     * @throws IOException 입출력 처리 실패에 의해 발생
     */

    public void createReceiveImageView(Uri uri, Bitmap bitmap) throws IOException {
        Bitmap bmp = null;
        if(bitmap == null) {
            bmp = resizeImage(uri);
        } else {
            bmp = bitmap;
        }
        mReceiverImageListScrollView.setVisibility(View.VISIBLE);
        final ImageView receiveImageView = new ImageView(this);
        receiveImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(mReveiveImageListData.isEmpty()) {
            mReveiveImageListData.put(mReceiveImageViewCount, bmp);
            receiveImageView.setImageBitmap(bmp);
            receiveImageView.setId(mReceiveImageViewCount);
            mReceiveImageList.addView(receiveImageView, 300, 300);
            mReceiveImageViewCount++;
        } else {
            for(HashMap.Entry<Integer, Bitmap> entry : mReveiveImageListData.entrySet()) {
                if(bmp.sameAs(entry.getValue())) {
                    Toast.makeText(getApplicationContext(), "이미 추가된 이미지입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mReveiveImageListData.put(mReceiveImageViewCount, bmp);
            receiveImageView.setImageBitmap(bmp);
            receiveImageView.setId(mReceiveImageViewCount);
            mReceiveImageList.addView(receiveImageView, 300, 300);
            mReceiveImageViewCount++;
        }

        receiveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout parentLayout = (LinearLayout) v.getParent();
                parentLayout.removeView(v);
                mReveiveImageListData.remove(v.getId());
                mReceiveImageViewCount--;
                if(mReceiveImageViewCount == 0)
                    mReceiverImageListScrollView.setVisibility(View.GONE);

            }
        });
    }

    /**
     * 친구목록 검색할때 검색한 결과를 필터링하여 보여주는 기능을 하는 함수들
     * @param charSequence receiverClearEditText에서 사용자가 입력하는 문자들
     * @param i
     * @param i1
     * @param i2
     */

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            filter(charSequence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    /**
     * receiverNameListLayout에 수신자 버튼을 추가하고 빼는 기능을 하는 함수
     * @param receiver 수신자
     */

    public void createReceiverButton(User receiver) {
        mReceiverNameListLayoutScrollView.setVisibility(View.VISIBLE);
        final Button receiverButton = new Button(this);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.setMarginStart(10);
        receiverButton.setLayoutParams(mLayoutParams);
        receiverButton.setBackground(ContextCompat.getDrawable(this, R.drawable.receiver_button));
        receiverButton.setText(receiver.mNickName);
        receiverButton.setId(mReceiverButtonCount);
        mReceiverNameListLayout.addView(receiverButton);
        mReceiverButtonCount++;

        receiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout parentLayout = (LinearLayout) v.getParent();
                parentLayout.removeView(v);
                mReceiverList.remove(v.getId());
                mReceiverButtonCount--;
                if(mReceiverButtonCount == 0)
                    mReceiverNameListLayoutScrollView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 필터링된 친구목록을 보여주는 Recyclerview 에서 각 홀더를 클릭했을때 수신자 리스트에 친구를 추가하는 기능을 하는 함수
     * @param position 사용자가 클릭한 홀더의 위치
     */

    @Override
    public void onItemClick(int position) {
        User receiver = mPersonDataSet.get(position);
        if(mReceiverList.isEmpty()) {
            mReceiverList.put(mReceiverButtonCount, receiver);
            createReceiverButton(receiver);
        } else {
            for(HashMap.Entry<Integer, User> entry : mReceiverList.entrySet()) {
                if(receiver.mNickName.equals(entry.getValue().mNickName)) {
                    Toast.makeText(getApplicationContext(), "받는 사람에 이미 추가하였습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mReceiverList.put(mReceiverButtonCount, receiver);
            createReceiverButton(receiver);
        }

    }

    /**
     * 사용자가 글자를 입력할때마다 그 글자로 친구목록을 필터링하여 보여주는 기능을 하는 함수
     * @param charSequence 사용자가 입력한 글자
     */

    public void filter(CharSequence charSequence) {
        String charString = charSequence.toString();
        ArrayList<User> filteredList = new ArrayList<>();
        if(charString.isEmpty()) {
            mAdapter.filterList(filteredList);
            mPersonDataSet = filteredList;
            if(mReceiverButtonCount == 0)
                mReceiverNameListLayoutScrollView.setVisibility(View.GONE);
        } else {
            for (User person : mDefaultPersonDataSet) {
                if(person.mNickName.toLowerCase().contains(charString.toLowerCase())
                        || person.mPhoneNum.contains(charString)) {
                    filteredList.add(person);
                }
            }
            if(filteredList.isEmpty() || filteredList == null) {
                return;
            }
            mAdapter.filterList(filteredList);
            mPersonDataSet = filteredList;
        }

    }

    /**
     * User 데이터베이스에서 친구목록을 가져오는 백그라운드 작업을 하는 클래스
     */

    public class UserDbAsyncTask  extends AsyncTask<Void, Void, ArrayList<User>> {//UserList DB관련 백그라운드 작업  taskCode별로 백그라운드 작업다르게 처리
        //Prevent leak
        private WeakReference<Activity> weakActivity;
        public ArrayList<User> userArrayList;

        public UserDbAsyncTask(Activity activity, ArrayList<User> userArrayList) {
            weakActivity = new WeakReference<>(activity);
            Log.d("test", "test");
            this.userArrayList = userArrayList;
        }

        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            UserDataBase db = Room.databaseBuilder(weakActivity.get(),
                    UserDataBase.class, "Users").build();
            userArrayList = (ArrayList<User>) db.userDao().getAllPerson();
            Log.d("test", "dddd");
            return userArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            Activity activity = weakActivity.get();
            if(activity != null) {
                mDefaultPersonDataSet = users;
                Log.d("dd", "aaa");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registHomeKeyReceiver();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(homeKeyReceiver != null){
            unregistHomeKeyReceiver();
        }
    }
    /**
     * Homekey Receiver를 등록하는 메서드
     */
    private void registHomeKeyReceiver(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(homeKeyReceiver,intentFilter);
    }
    /**
     * HonmeKeyReceiver를 등록해제 하는 메서드
     */
    private void unregistHomeKeyReceiver(){
        this.unregisterReceiver(homeKeyReceiver);
    }
}