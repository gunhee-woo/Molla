package com.dlog.molla;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;

import org.apache.commons.codec.DecoderException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <pre>
 *     채팅방 화면에 해당하는 Activity
 *     메시지를 보낼 때 text를 검사하기 위해 TextWatcher를 implements한다.
 *     화면 캡처를 막기위해
 *     {@code
 *     getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
 *     }
 *     를 사용한다.
 *     SMS, MMS 메시지를 수신하기 위해 BroadCastReceiver를 상속한 {@link SMSReceiver}, {@link MMSReceiver}를 등록한다.
 * </pre>
 */
public class ChattingRoomActivity extends AppCompatActivity implements TextWatcher {
    /**
     * SMS를 READ하는 것 과 관련된 permission들을 요청하는 데 사용하는 request code
     */
    private static final int PERMISSIONS_REQUEST_READ_SMS = 100;
    /**
     * 디바이스에 등록된 휴대폰 번호를 가져오는 데 필요한 permission을 요청하는 request code
     */
    private static final int PERMISSIONS_REQUEST_READ_MYNUMBER = 99;

    private static final int PICK_FROM_GALLERY = 1;

    private static final int PICK_FROM_CAMERA = 2;
    /**
     * 채팅방 암호 타이머의 시간정보를 저장
     */
    private Long mTimer_time;
    /**
     * 채팅방 암호 타이머
     */
    private CountDownTimer mCountDownTimer;
    /**
     * 채팅 대화 리스트에 해당한다.
     */
    private ArrayList<ChattingRoomItem> mMmessageList;
    /**
     * 채팅 대화 리스트를 화면에 보여주기 위한 리사이클러 뷰에 해당한다.
     */
    private RecyclerView mRecyclerView;
    /**
     * 암호화된 메시지 리스트에 해당한다,
     */
    private ArrayList<ChattingRoomItem> mDefaultMessage;
    /**
     * 보낼 메시지를 작성하는 {@link ClearEditText}
     */
    private ClearEditText mChatMessage;
    /**
     * {@link ChattingRoomActivity#mRecyclerView}의 어뎁터
     */
    private RcylChatAdapter mRcylChatAdapter;
    /**
     * 사용자가 채팅방에 진입할 때 입력한 password
     */
    private String mIntentPassword = "";
    /**
     * 사용자가 채팅방 안에서 입력한 password
     */
    private String mRoomPassword = "";
    /**
     * 메시지 링크를 타고 앱으로 진입했는 지, 그냥 진입했는 지 나타내는 boolean 값
     */
    private boolean isLinkToApp = false;
    /**
     * 타이머가 돌아가고 있는 상태인지를 나타내는 boolean 값
     */
    private boolean isTicking = false;
    /**
     * 더보기 버튼, 사용자가 설정한 기간보다 오래된 메시지를 더 보고 싶을 때 사용된다.
     */
    private Button mUpdateChatMessageButton;
    /**
     * 사용자가 설정한 기간보다 오래된 메시지를 더 보고 싶을 때 calendar에 해당하는 날짜까지 데이터를 가져온다,
     */
    private Calendar mCalendar;
    /**
     * 본인을 제외한 채팅방에 참여한 사람들의 전화번호 집합.
     */
    private HashSet mTargetNumSet;
    /**
     * 디바이스에 등록된 휴대전화 번호
     */
    private String mMyNumber;
    /**
     * 이 채팅방의 thread_id 값
     */
    private String mThread_id;
    /**
     * 채팅방의 이름
     */
    private String mStrName;
    /**
     * 채팅방 타이머의 시간 간격 MMSS 형식 (분분초초) , 기본값은 0030
     */
    private String mTimer_interval = "0100";
    /**
     * 새 메시지가 도착했을 때 나타나는 팝업 레이아웃
     */
    private ConstraintLayout mLayout_new_msg;
    /**
     * 이 Activitiy에서 MMS 메시지를 수신하기 위해 등록할 MMSReceiver
     */
    private BroadcastReceiver mMMSReceiver = null;
    /**
     * 이 Activitiy에서 SMS 메시지를 수신하기 위해 등록할 SMSReceiver
     */
    private BroadcastReceiver mSMSReceiver = null;

    /**
     * 홈 버튼 클릭 이벤트를 수신하는 Broadcast Receiver
     */
    private HomeKeyReceiver homeKeyReceiver;

    /**
     * 핸드폰 갤러리나 카메라에서 이미지를 얻어오는 기능을 하는 FloatingActionButton
     */
    private FloatingActionButton mAdd_multimedia_flt_button;
    private FloatingActionButton mImage_flt_button;
    private FloatingActionButton mCamera_flt_button;

    /**
     * FloatingActionButton이 활성화되었는지 check
     */
    private Boolean isFabOpen = false;
    /**
     * FloatingActionButton 애니메이션
     */
    private Animation mBtnOpen, mBtnClose;
    /**
     * 갤러리에서 가져온 이미지 Uri를 담는 변수, 리스트
     */
    private Uri mPhotoURI;
    private ArrayList<Uri> mPhotoUri;

    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들을 담고 있는 HashMap
     */
    private HashMap<Integer, Bitmap> mReveiveImageListData;
    /**
     * 메시지에 이미지를 삽입하여 같이 보낼때 그 이미지들의 수를 계산하는 변수
     */
    private int mReceiveImageViewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chatting_room);
        boolean taskFlag = getIntent().getBooleanExtra("ReceiverFlag",false);
        if(taskFlag){//브로드 캐스트 리시버에서 보낸 이 flag는 항상 true이다.  onCreate에서 taskFlag가 true라는 것은 이 액티비티가 종료되었는데 리시버에서 인탠트를 보낸것.
            finish();
        }
        else {
            try {//mms sms 브로드캐스트 리시버 등록    onDestroy에서 해제함
                registMmsReceiver();
                registSmsReceiver();
            } catch (IntentFilter.MalformedMimeTypeException e) {
                e.printStackTrace();
            }
            setTimer_time();
            setBtnClick();
            homeKeyReceiver = new HomeKeyReceiver();
            mDefaultMessage = new ArrayList<ChattingRoomItem>();
            mChatMessage = findViewById(R.id.chat_message_text);
            mChatMessage.addTextChangedListener(this);
            mChatMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(isTicking)
                        mCountDownTimer.start();
                    return false;
                }
            });
            mMyNumber = getmMyNumber();

            mBtnOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
            mBtnClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
            mPhotoUri = new ArrayList<Uri>();
            mReveiveImageListData = new HashMap<Integer, Bitmap>();

            mUpdateChatMessageButton = findViewById(R.id.update_chat_message);
            mUpdateChatMessageButton.setVisibility(View.GONE);
            mUpdateChatMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(mCalendar.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, -2);
                    loadMessage(cal);
                    mCalendar = cal;
                    mDefaultMessage.clear();
                    for(int i = 0; i < mMmessageList.size(); i++)  {
                        ChattingRoomItem msgItme = mMmessageList.get(i);
                        ChattingRoomItem chattingRoomItem = new ChattingRoomItem(msgItme);
                        ArrayList<byte[]> arrayList = new ArrayList<>();
                        if(msgItme.getImgByteList() != null && !msgItme.getImgByteList().isEmpty()) {
                            arrayList.addAll(msgItme.getImgByteList());
                            chattingRoomItem.setImgByteList(arrayList);
                        }
                        mDefaultMessage.add(chattingRoomItem);
                    }
                    mRcylChatAdapter.updateDataList(mMmessageList);
                    mRecyclerView.scrollToPosition(1);
                    if(isTicking) {
                        mCountDownTimer.start();
                        decoded(mIntentPassword);
                    }
                    mUpdateChatMessageButton.setVisibility(View.GONE);
                }
            });
            mRcylChatAdapter = new RcylChatAdapter();

            String myNumber = getmMyNumber();
            String pwd = "";
            if (myNumber == null) {
                Toast.makeText(this, "기기에 등록된 전화번호가 없습니다!", Toast.LENGTH_LONG).show();
                //finish();
            } else {
                // 앱에서 채팅방을 타고 들어온 경우
                try {
                    mThread_id = getIntent().getStringExtra("ThreadId");
                    mTargetNumSet = (HashSet)getIntent().getSerializableExtra("Number");
                    //timer_time = getIntent().getLongExtra("Time", 0);
                    mIntentPassword = getIntent().getStringExtra("IntentPassword");
                    mRoomPassword = mIntentPassword;
                    mStrName = getIntent().getStringExtra("Name");
                    setRoomDrawer(mStrName);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                new MsgDbAsyncTask(getApplicationContext()).execute();
            }
        }
    }

    /**
     * 백그라운드 작업으로 SMS , MMS 데이터베이스에 접근하여 채팅방의 대화 데이터들을 가져오는 AsyncTask 클래스
     */
    public class MsgDbAsyncTask  extends AsyncTask<Void, Void, ArrayList<ChattingRoomItem>> {
        /**
         * 이 클래스를 생성한 context
         */
        private Context context;
        /**
         * 데이터베이스에서 데이터들을 가져올 동안 사용자에게 보여줄 Lottie animatinon view
         */
        private LottieAnimationView lottieAnimationView;

        public MsgDbAsyncTask(Context context) {
            this.context = context;
        }

        /**
         * 백그라운드 작업을 하기 전에 Lottie animation을 실행한다
         */
        @Override
        protected void onPreExecute() {
            lottieAnimationView = findViewById(R.id.lottie_chat_room_loading);
            lottieAnimationView.setAnimation("loading_anim.json");
            lottieAnimationView.loop(true);
            lottieAnimationView.playAnimation();
        }

        /**
         * 관련된 permissions 요청 작업을 하고 , SMS MMS 데이터베이스에서 채팅방 메시지 정보들을 가져온다.
         * @param params Void
         * @return {@link ChattingRoomActivity#mMmessageList}
         */
        @Override
        protected ArrayList<ChattingRoomItem> doInBackground(Void... params) {
            try {
                callPermission();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mMmessageList;
        }

        /**
         * <pre>
         *     lottie animaition을 종료한다.
         *     파라미터로 넘겨받은 {@link ChattingRoomActivity#mMmessageList}와 {@link ChattingRoomActivity#mRcylChatAdapter}를 연결하고
         *     {@link ChattingRoomActivity#mRecyclerView}에 어뎁터를 연결한다.
         * </pre>
         *
         * @param chattingRoomItems
         */
        @Override
        protected void onPostExecute(ArrayList<ChattingRoomItem> chattingRoomItems) {
            lottieAnimationView.cancelAnimation();
            lottieAnimationView.setVisibility(View.GONE);

            if(mDefaultMessage.isEmpty()) {
                for(int i = 0; i < mMmessageList.size(); i++)  {
                    ChattingRoomItem msgItme = mMmessageList.get(i);
                    ChattingRoomItem chattingRoomItem = new ChattingRoomItem(msgItme);
                    ArrayList<byte[]> arrayList = new ArrayList<>();
                    if(msgItme.getImgByteList() != null && !msgItme.getImgByteList().isEmpty()) {
                        arrayList.addAll(msgItme.getImgByteList());
                        chattingRoomItem.setImgByteList(arrayList);
                    }

                    mDefaultMessage.add(chattingRoomItem);

                }
            }
            if(mMmessageList != null) {
                mRecyclerView = findViewById(R.id.rcyl_chat_view);
                if(mTargetNumSet.size()>1){
                    String leftName = "";
                    mRecyclerView.setAdapter(new RcylChatAdapter(mMmessageList,leftName, (ConstraintLayout)findViewById(R.id.layout_img_expand),context));  // Adapter 등록
                }
                else{
                    Iterator it = mTargetNumSet.iterator();
                    String leftName = (String)it.next();
                    mRecyclerView.setAdapter(new RcylChatAdapter(mMmessageList,leftName, (ConstraintLayout)findViewById(R.id.layout_img_expand),context));  // Adapter 등록
                }
                mRcylChatAdapter = (RcylChatAdapter) mRecyclerView.getAdapter();
                LinearLayoutManager manager
                        = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(manager); // LayoutManager 등록
                mRecyclerView.addOnScrollListener(onScrollListener);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                    }
                });


                // 앱에서 채팅방을 타고 들어왔을경우
                if(!isLinkToApp) {
                    if (mTimer_time != 0) {
                        try {
                            setTimer(mIntentPassword);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * <pre>
     *     {@link ChattingRoomActivity#mRecyclerView}의 스크롤 이벤트들을 수신한다.
     *     가장 상단까지 스크롤 했을 때 , 더보기 버튼을 띄운다.
     *     가장 하단까지 스크롤 했을 때, 새 메시지 도착 팝업창을 보이지 않게 한다.
     *     스크롤이 되고 있는 중에는 더보기 버튼을 사라지게 한다.
     * </pre>
     *
     */
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        /**
         * <pre>
         *     가장 위 까지 스크롤 했을 때 더보기 버튼인 {@link ChattingRoomActivity#mUpdateChatMessageButton}을 보이게한다.
         *     가장 아래 까지 스크롤 했을 때 새메시지 도착 팝업 레이아웃인 {@link ChattingRoomActivity#mLayout_new_msg}를 보이지 않게 한다.
         *     그 외의 경우에 더보기 버튼을 사라지게 한다.
         * </pre>
         * @param recyclerView
         * @param newState
         */
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (!recyclerView.canScrollVertically(-1)) {
                mUpdateChatMessageButton.setVisibility(View.VISIBLE);
            }else if(!recyclerView.canScrollVertically(1)){
                findViewById(R.id.layout_new_msg).setVisibility(View.INVISIBLE);
            }
            else {
                mUpdateChatMessageButton.setVisibility(View.GONE);
            }
        }
    };



    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!s.equals("") && isTicking) {
            mCountDownTimer.start();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * {@link ChattingRoomActivity#mMmessageList}의 암호화된 content들을 복호화하는 메서드
     * @param pwd 사용자가 입력한 password
     */
    private void decoded(String pwd) {
        // 기존에 있던 메시지 디코딩
        boolean isDecoded = false;
        MessageEncryption messageEncryption;
        try {
            messageEncryption = new MessageEncryption(pwd);
        } catch (Exception e) {
            return;
        }
        for(int i = 0; i < mMmessageList.size(); i++) {
            ChattingRoomItem chattingRoomItem = mMmessageList.get(i);
            String content = chattingRoomItem.getmContent();
            ArrayList<byte[]> image_bytes_list = chattingRoomItem.getImgByteList();
            if (image_bytes_list != null && !image_bytes_list.isEmpty()){
                for(int j = 0 ; j <image_bytes_list.size() ; j++) {
                    byte[] imgBytes = image_bytes_list.get(j);
                    mMmessageList.get(i).getImgByteList().set(j,messageEncryption.AES_Decode(imgBytes));
                }
            }
            String encryptedMessage = "";
            try {
                encryptedMessage = content.split("\\?")[1];
            } catch (Exception e) {
                continue;
            }
            String decryptedMessage = messageEncryption.AES_Decode(encryptedMessage);
            if(!decryptedMessage.equals(encryptedMessage)) {
                isDecoded = true;
                mMmessageList.get(i).setmContent(decryptedMessage.split(":")[1]);
            }
        }
        if(!isDecoded)
            Toast.makeText(getApplicationContext(), "복호화된 메시지가 없습니다", Toast.LENGTH_SHORT).show();
        updateChattingRoomRecyclerView();
    }

    /**
     * {@link ChattingRoomActivity#mMmessageList}의 content들을 암호화하는 메서드
     */
    private void encoded() {
        for(int i = 0; i < mMmessageList.size(); i++) {
            ChattingRoomItem chatEncodeMessageItem = mDefaultMessage.get(i);
            ChattingRoomItem encodedChattingRoomItem = new ChattingRoomItem(chatEncodeMessageItem);
            ArrayList<byte[]> arrayList = new ArrayList<>();
            String message = chatEncodeMessageItem.getmContent();
            encodedChattingRoomItem.setmContent(message);
            if(chatEncodeMessageItem.getImgByteList() != null && !chatEncodeMessageItem.getImgByteList().isEmpty()) {
                arrayList.addAll(chatEncodeMessageItem.getImgByteList());
                encodedChattingRoomItem.setImgByteList(arrayList);
            }
            mMmessageList.set(i, encodedChattingRoomItem);
        }
        updateChattingRoomRecyclerView();
        //defaultMessage.clear();
    }

    /**
     * 채팅방 화면에 필요한 데이터들을 initailize하는 메서드
     */
    public void initializeData() {
        Long currentTime = System.currentTimeMillis();
        Date currentDate = new Date(currentTime);
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(currentDate);
        mCalendar.add(Calendar.DATE, -1);
        loadMessage(mCalendar);
    }

    /**
     * MMS , SMS 데이터베이스에 접근해서 데이터들을 받아오는 메서드
     * SMS는 thread_id로 쿼리를 날려서 채팅방 데이터들을 받아온다
     * MMS는 채팅방에 참여한 전화번호들과 매칭되는 메시지 데이터들을 받아온다.
     * @param calendar
     */
    public void loadMessage(Calendar calendar) {
        HashSet<ChattingRoomItem> smsSet = new HashSet<>();
        ArrayList<ChattingRoomItem> mmsList = new ArrayList<>();
        if(mTargetNumSet != null) {
            Iterator it = mTargetNumSet.iterator();
            /*
            while (it.hasNext()){
                String targetNum = (String)it.next();
                targetNum = targetNum.replaceAll("-", "");
            }*/

            //thread_id 가 null 이라면? << 메시지를 내가 보냈을 때
            if(mThread_id == null){
                if(mTargetNumSet.size() == 1) {//단체 메시지는 MMS로 보내진다. 개인 메시지중 sms로 보내진것만 신경쓰면 된다.
                    SMSReader threadIdFinder = new SMSReader(this, "address", (String) it.next(), calendar);
                    threadIdFinder.readSMSMessage();
                    mThread_id = threadIdFinder.getmThreadId();
                    if(mThread_id != null) {//thread_id가 null이면 targetNumset과의 sms 대화가 없음
                        SMSReader smsReader = new SMSReader(this, "thread_id", mThread_id, calendar);
                        smsReader.readSMSMessage();
                        smsSet = smsReader.getMchatItemList();
                    }
                }
            }
            else {
                SMSReader smsReader = new SMSReader(this, "thread_id", mThread_id, calendar);
                smsReader.readSMSMessage();
                smsSet = smsReader.getMchatItemList();
            }

            MMSReader mmsReader = new MMSReader();
            try {
                mmsReader.gatherMessages(this, mTargetNumSet, mMyNumber, calendar);
                mmsList = mmsReader.getmChatItemList();
            } catch (Exception e) {
                e.toString();
            }
        }

        //dataList = smsList + mmsList  >> 날짜 순으로 정렬
        ArrayList<ChattingRoomItem> smsList = new ArrayList<>(smsSet);
        mMmessageList = addSmsMmsList(smsList, mmsList);
    }

    /**
     * 복호화, 암호화 작업 후, 리사이클러 뷰를 update시킬 때 처리할 작업들을 모아 놓은 메서드
     */
    public void updateChattingRoomRecyclerView() {

        mRcylChatAdapter.notifyDataSetChanged();
        /*
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        });*/
    }

    /**
     * SMS READ permission과 관련된 permissions들을 요청하는 메서드
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    private void callPermission() { // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, PERMISSIONS_REQUEST_READ_SMS);
        } else {
            initializeData();
        }
    }

    /**
     * Sms List와 Mms List를 하나로 합치는 메서드
     * 합친 후에 날짜 순서로 sort 한다.
     * @param smsList
     * @param mmsList
     * @return
     */
    private ArrayList<ChattingRoomItem> addSmsMmsList(ArrayList<ChattingRoomItem> smsList, ArrayList<ChattingRoomItem> mmsList) {

        smsList.addAll(mmsList);
        Collections.sort(smsList);
        return smsList;

    }

    /**
     * permission을 요청하고 그 결과를 리턴 받는 메서드 , permission이 승인되면 다음 작업을 수행한다.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// Permission is granted
                try {
                    initializeData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_MYNUMBER){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
            }
            else{
                Toast.makeText(this,"sorry , we need permissions.",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * <pre>
     *     타이머를 리셋 시키고 파라미터로 받은 password로 복호화 작업을 하는 메서드
     *     유저가 비밀번호를 입력하거나, 새로운 메시지가 도착했을 때 호출된다.
     * </pre>
     *
     * @param pwd 사용자가 입력한 비밀번호
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws DecoderException
     */
    private void setTimer(String pwd) throws UnsupportedEncodingException {
        final TextView txt_timer = findViewById(R.id.txt_chat_room_timer);
        final ImageView img_timer = findViewById(R.id.img_chat_lock);
        mCountDownTimer = new CountDownTimer(mTimer_time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Long getMin = millisUntilFinished - (millisUntilFinished / (60 * 60 * 1000));
                String min = String.valueOf(getMin / (60 * 1000));

                String second = String.valueOf((getMin % (60 * 1000)) / 1000);

                if (min.length() == 1) {
                    min = "0" + min;
                }
                if (second.length() == 1) {
                    second = "0" + second;
                }
                if (txt_timer != null) {
                    txt_timer.setText(min + ":" + second);
                }
                isTicking = true;

            }

            @Override
            public void onFinish() {
                try {
                    encoded();
                } catch (Exception e) {
                    e.toString();
                }
                isTicking = false;
                txt_timer.setText("보안을 위해 암호화되었습니다.");
                img_timer.setImageResource(R.drawable.lock);

            }
        }.start();
        decoded(pwd);
        img_timer.setImageResource(R.drawable.lock_open);
    }

    /**
     * <pre>
     *     {@link ChattingRoomActivity#mChatMessage}에 입력된 내용으로 메시지를 보낼 때 사용하는 메서드
     *     메시지를 보내기전에 필요한 데이터들을 생성하는 메서드
     *     메시지를 받는 대상은 {@link ChattingRoomActivity#mTargetNumSet}이 된다.
     *     보낼 메시지 내용이 없다면 내용을 입력해달라는 Toast를 띄운다.
     * </pre>
     *
     *
     */

    public void sendEncryptionMessage() {
        String text = mChatMessage.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(getApplicationContext(), "내용을 입력해주세요", Toast.LENGTH_LONG).show();
            return;
        }
        String password = mIntentPassword;
        MessageEncryption messageEncryption = null;
        try {
            messageEncryption = new MessageEncryption(password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "암호화에 실패하였습니다", Toast.LENGTH_LONG).show();
        }
        ArrayList<Bitmap> images = new ArrayList<>();
        for(HashMap.Entry<Integer, Bitmap> entry : mReveiveImageListData.entrySet()) {
            Bitmap resized = Bitmap.createScaledBitmap(entry.getValue(), 1080, 720, true);
            images.add(resized);
        }
        String message = "message:" + text;
        String encryptedMessage = "http://www.dlogsoft.com/link.html?" + messageEncryption.AES_Encode(message) + "?molla!";
        try {
            sendMessage(encryptedMessage, mTargetNumSet, images.toArray(new Bitmap[images.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "메시지 전송에 실패하였습니다", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * <pre>
     *     메시지 전송을 요청하는 메서드
     *     보내고 난 후 UI 작업도 여기서 수행한다.
     * </pre>
     * @param encryptedMessage 유저가 입력한 암호화된 메시지
     * @param receiverNumbers  메시지를 받는 사람들의 전화번호 집합
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    public void sendMessage(String encryptedMessage, HashSet receiverNumbers, Bitmap[] images) throws NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        String[] receivers = new String[receiverNumbers.size()];
        receiverNumbers.toArray(receivers);
        String originMessage = mChatMessage.getText().toString();
        mChatMessage.setText(null);
        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        Transaction transaction = new Transaction(this, settings);
        Message message;
        if(images == null || images.length == 0)
            message = new Message(encryptedMessage, receivers);
        else
            message = new Message(encryptedMessage, receivers, images);
        long id = android.os.Process.getThreadPriority(android.os.Process.myTid());
        transaction.sendNewMessage(message, id, mIntentPassword);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        ChattingRoomItem originChattingRoomItem;
        ChattingRoomItem encryptedChattingRoomItem;
        ArrayList<byte[]> imageByteArray = new ArrayList<>();
        for(int i = 0; i < images.length; i++) {
            imageByteArray.add(bitmapToByteArray(images[i]));
        }
        if(images == null || images.length == 0) {
            originChattingRoomItem = new ChattingRoomItem(originMessage, date, 2);
            encryptedChattingRoomItem = new ChattingRoomItem(encryptedMessage, date, 2);
        } else {
            originChattingRoomItem = new ChattingRoomItem(originMessage, date, 2, imageByteArray);
            encryptedChattingRoomItem = new ChattingRoomItem(encryptedMessage, date, 2, imageByteArray);
        }
        mMmessageList.add(originChattingRoomItem);
        mDefaultMessage.add(encryptedChattingRoomItem);
        mRcylChatAdapter.notifyItemInserted(mRcylChatAdapter.getItemCount() + 1);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        });
    }

    /**
     * 비트맵 이미지를 바이트 array로 변환하여 반환
     * @param image 비트맵 이미지
     * @return 이미지 바이트 array
     */

    public byte[] bitmapToByteArray(Bitmap image) {
        byte[] output = new byte[0];
        if (image == null) {
            com.klinker.android.logger.Log.v("Message", "image is null, returning byte array of size 0");
            return output;
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try {
                image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                output = stream.toByteArray();
            } finally {
                try {
                    stream.close();
                } catch (IOException var9) {
                }

            }

            return output;
        }
    }

    /**
     * ImageButton, Button등의 Click 이벤트들을 정의하는 메서드
     */
    private void setBtnClick() {
        final ImageView img_timer = findViewById(R.id.img_chat_lock);
        final ImageView sendMessageButton = findViewById(R.id.chatting_message_send_button);
        final ImageView img_back = findViewById(R.id.img_chat_back);
        final ConstraintLayout layout_setting_back = findViewById(R.id.layout_chat_room_setting_back);
        final ConstraintLayout layout_setting  = findViewById(R.id.layout_chat_room_setting);
        final ImageView img_setting = findViewById(R.id.img_chat_room_setting);
        final ConstraintLayout layout_img_expand = findViewById(R.id.layout_img_expand);
        mAdd_multimedia_flt_button = findViewById(R.id.chat_room_add_multimedia_flt_button);
        mImage_flt_button = findViewById(R.id.chat_room_image_flt_button);
        mCamera_flt_button = findViewById(R.id.chat_room_camera_flt_button);
        mLayout_new_msg = findViewById(R.id.layout_new_msg);

        layout_img_expand.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                layout_img_expand.setVisibility(View.GONE);
            }
        });
        mLayout_new_msg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mRecyclerView != null) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                            mLayout_new_msg.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        img_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //인증 popup
                Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                intent.putExtra("TaskNum", 2);
                intent.putExtra("PopupTitle", "대화방 인증");
                intent.putExtra("PopupInfo", "비밀번호를 입력해주세요.");
                intent.putExtra("Number", mTargetNumSet);
                startActivityForResult(intent, 5);
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEncryptionMessage();
            }
        });
        img_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        img_setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Animation ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.setting_slide);
                ani.setDuration(500);
                layout_setting_back.setVisibility(View.VISIBLE);
                layout_setting.startAnimation(ani);
                layout_setting.setVisibility(View.VISIBLE);
            }
        });
        layout_setting_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                layout_setting_back.setVisibility(View.INVISIBLE);
            }
        });
        layout_setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });
        mAdd_multimedia_flt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
            }
        });
        mImage_flt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                selectGallery();
            }
        });
        mCamera_flt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                takePhoto();
            }
        });
    }

    /**
     * FloatingActionButton 애니메이션 기능을 하는 함수
     */

    public void anim() {
        if (isFabOpen) {
            mAdd_multimedia_flt_button.setImageResource(R.drawable.ic_add_white_24dp);
            mImage_flt_button.setVisibility(View.GONE);
            mImage_flt_button.startAnimation(mBtnClose);
            mCamera_flt_button.setVisibility(View.GONE);
            mCamera_flt_button.startAnimation(mBtnClose);
            mImage_flt_button.setClickable(false);
            mCamera_flt_button.setClickable(false);
            isFabOpen = false;
        } else {
            mAdd_multimedia_flt_button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_close_white_24dp));
            mImage_flt_button.setVisibility(View.VISIBLE);
            mImage_flt_button.startAnimation(mBtnOpen);
            mCamera_flt_button.setVisibility(View.VISIBLE);
            mCamera_flt_button.startAnimation(mBtnOpen);
            mImage_flt_button.setClickable(true);
            mCamera_flt_button.setClickable(true);
            isFabOpen = true;
        }
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
     * 카메라로 사진을 찍고 그 사진을 내부저장소에 저장하는 기능을 하는 함수
     */

    public void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
        }
    }

    /**
     * 이 액티비티에서 onStartActivityForResult()로 실행시킨 액티비티에서 setResult()를 했을 때 호출되는 메서드,
     * resultCode별로 수행할 task를 관리
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 3: {
                // 채팅방에서 입력한 암호
                mRoomPassword = data.getStringExtra("RoomPassword");
                //changePw = true;

                if(mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                }

                try {
                    setTimer(mRoomPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 5: {
                String[] receiverNumbers = data.getStringArrayExtra("receiverNumbers");
                mTargetNumSet = new HashSet();
                for(String s : receiverNumbers)
                    mTargetNumSet.add(s);
                Log.d("gdsg", mTargetNumSet.toString());
                isLinkToApp = true;
                //intentPassword = data.getStringExtra("LinkPassword");
                new MsgDbAsyncTask(getApplicationContext()).execute();
                break;
            }
        }
        switch (requestCode){
            case PICK_FROM_GALLERY : {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v("알림","앨범에서 가져오기 에러");
                } finally {
                    break;
                }
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
                } finally {
                    break;
                }
            }
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
        final HorizontalScrollView mReceiverImageListScrollView = findViewById(R.id.chat_room_receiver_image_list_scrollview);
        final LinearLayout mReceiveImageLayout = findViewById(R.id.chat_room_receiver_image_layout);
        final View mReceiverImageListScrollViewTopLine = findViewById(R.id.chat_room_image_list_scrollview_top_line);
        Bitmap bmp = null;
        if(bitmap == null) {
            bmp = resizeImage(uri);
        } else {
            bmp = bitmap;
        }
        mReceiverImageListScrollView.setVisibility(View.VISIBLE);
        final ImageView receiveImageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 50);
        layoutParams.setMarginStart(5);
        receiveImageView.setLayoutParams(layoutParams);
        if(mReveiveImageListData.isEmpty()) {
            mReveiveImageListData.put(mReceiveImageViewCount, bmp);
            receiveImageView.setImageBitmap(bmp);
            receiveImageView.setId(mReceiveImageViewCount);
            mReceiveImageLayout.addView(receiveImageView, 300, 300);
            mReceiveImageViewCount++;
            mReceiverImageListScrollViewTopLine.setVisibility(View.VISIBLE);
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
            mReceiveImageLayout.addView(receiveImageView, 300, 300);
            mReceiveImageViewCount++;
            mReceiverImageListScrollViewTopLine.setVisibility(View.VISIBLE);
        }

        receiveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout parentLayout = (LinearLayout) v.getParent();
                parentLayout.removeView(v);
                mReveiveImageListData.remove(v.getId());
                mReceiveImageViewCount--;
                if(mReceiveImageViewCount == 0) {
                    mReceiverImageListScrollViewTopLine.setVisibility(View.GONE);
                    mReceiverImageListScrollView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 디바이스에 등록된 휴대폰 번호를 가져오는 메서드
     * @return 디바이스에 등록된 휴대폰 번호, 없으면 ""을 리턴.
     */
    private String getmMyNumber() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_SMS,Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_MYNUMBER);
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String myNumber = telephonyManager.getLine1Number();
        if(myNumber != null) {
            if (myNumber.startsWith("+82")) {
                myNumber = myNumber.replace("+82", "0");
            }
        }

        return myNumber;
    }

    /**
     * MMSReceiver를 등록하는 메서드
     * @throws IntentFilter.MalformedMimeTypeException
     */
    private void registMmsReceiver() throws IntentFilter.MalformedMimeTypeException {
        mMMSReceiver = new MMSReceiver("ChattingRoomActivity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
        intentFilter.addDataType("application/vnd.wap.mms-message");
        Log.d("TAG", "regist MMSReceiver");
        this.registerReceiver(mMMSReceiver,intentFilter,Manifest.permission.BROADCAST_WAP_PUSH,null);
    }
    /**
     * SMSReceiver를 등록하는 메서드
     * @throws IntentFilter.MalformedMimeTypeException
     */
    private void registSmsReceiver(){
        mSMSReceiver = new SMSReceiver("ChattingRoomActivity");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        Log.d("TAG","regist SMSReceiver");
        this.registerReceiver(mSMSReceiver,intentFilter,Manifest.permission.BROADCAST_SMS,null);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMMSReceiver != null) {
            unregistMmsReceiver();
        }
        if(mSMSReceiver != null){
            unregistSMSReceiver();
        }
        mChatMessage.removeTextChangedListener(this);

    }
    /**
     * MMSReceiver를 해제하는 메서드
     */
    private void unregistMmsReceiver(){
        Log.d("TAG", "unregist MMSReceiver");
        this.unregisterReceiver(mMMSReceiver);
    }

    /**
     * SMSReceiver를 해제하는 메서드
     */
    private void unregistSMSReceiver(){
        Log.d("TAG", "unregist SMSReceiver");
        this.unregisterReceiver(mSMSReceiver);
    }


    /**
     * 이 Activity에서 새로운 Intent를 받았을 때 호출 되는 메서드
     * MMSReceiver , SMSReceiver에서 Intent를 보낼 때 사용한다.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {//receiver에서 인텐트 보낼때 (새 메시지가 들어왔을 때)
        super.onNewIntent(intent);
        try {
            processIntent(intent);
        } catch (Exception e) {
            e.toString();
        }
    }

    /**
     * <pre>
     *     새로 받은 Intent에서 데이터를 읽는 메서드
     *     Intent에는 새로운 메시지 데이터가 들어있다. 메시지를 보낸 사람의 전화번호가 {@link ChattingRoomActivity#mTargetNumSet}과 일치하면
     *     {@link ChattingRoomActivity#mRecyclerView}에 새 메시지를 추가한다.
     *     그리고 {@link ChattingRoomActivity#mLayout_new_msg}를 띄운다.
     * </pre>
     *
     * @param intent
     * @throws UnsupportedEncodingException
     */
    private void processIntent(Intent intent) throws UnsupportedEncodingException {
        if (intent != null) {
            HashSet senderSet = new HashSet();
            senderSet.add(intent.getStringExtra("Sender"));
            if (mTargetNumSet.equals(senderSet)) {
                String contents = intent.getStringExtra("Contents");
                ArrayList<byte[]> image_bytes = (ArrayList<byte[]>) intent.getSerializableExtra("ImageBytes");
                if (mTimer_time != 0)
                    Log.d("yet", String.valueOf(mTimer_time));
                else
                    Log.d("end", String.valueOf(mTimer_time));
                ChattingRoomItem chattingRoomItem = new ChattingRoomItem(contents, new Date(System.currentTimeMillis()), 1,image_bytes);
                mMmessageList.add(chattingRoomItem);
                mRecyclerView.getAdapter().notifyDataSetChanged();

                if(mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                }

                setTimer(mRoomPassword);

                //하단에 새메시지가 도착했습니다 알림 창 띄움  << 바닥에 스크롤 닿으면 없어짐.
                mLayout_new_msg.setVisibility(View.VISIBLE);

            }
        }
    }

    /**
     * <pre>
     *     타이머의 시간 간격을 정의하는 메서드
     *     SharedPrefernces에 저장된 시간 간격 데이터를 받아와서 사용한다.
     *
     * </pre>
     */
    private void setTimer_time(){
        mTimer_interval = GlobalApplication.prefs.getTimerIntervalPreferences();
        String time = mTimer_interval;
        // 1000 1초
        // 60000 1분
        // 60000 * 3600 1시간
        String getMin = time.substring(0, 2);
        String getSecond = time.substring(2, 4);
        mTimer_time = Long.parseLong(getMin) * 60 * 1000 + Long.parseLong(getSecond) * 1000;
    }

    @Override
    public void onBackPressed() {
        ConstraintLayout layout_setting_back = findViewById(R.id.layout_chat_room_setting_back);
        ConstraintLayout layout_img_expand = findViewById(R.id.layout_img_expand);
        if(layout_setting_back.getVisibility() == View.VISIBLE){
            layout_setting_back.setVisibility(View.INVISIBLE);
        }
        else if(layout_img_expand.getVisibility() == View.VISIBLE){
            layout_img_expand.setVisibility(View.GONE);
        }
        else{
            super.onBackPressed();
            finish();
        }
    }

    /**
     * 채팅방 서럽장 화면을 setting하는 메서드,
     * 채팅방 제목에 대화 상대 정보가 들어있으므로 체팅방 제목을 파라미터로 받는다.
     * @param strName 채팅방 제목
     */
    private void setRoomDrawer(String strName){

        RecyclerView rcyl_people_list = findViewById(R.id.rcyl_chat_room_setting_people);
        String[] nameArr = strName.split("  ");
        ChatPeopleRcylAdapter chatPeopleRcylAdapter = new ChatPeopleRcylAdapter(nameArr);
        LinearLayoutManager manager
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rcyl_people_list.setLayoutManager(manager); // LayoutManager 등록
        rcyl_people_list.setAdapter(chatPeopleRcylAdapter);
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
