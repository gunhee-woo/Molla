package com.dlog.molla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * ChattingRoomActivity에서 채팅화면을 보여주기 위해 RecyclerView에 사용하는 Adapter 클래스
 *
 * For example:
 * <pre>
 *       RcylChatAdapter rcylChatAdapter = new RcylChatAdapter();
 *       recyclerView.setAdapter(new RcylChatAdapter(messageList,leftName));
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */


public class RcylChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /**
     * RecyclerView에 넣을 대화 내용을 담고있는 리스트
     */
    private ArrayList<ChattingRoomItem> myDataList = null;
    /**
     * 채팅화면에서 왼쪽(상대방)의 이름
     */
    private String mLeftName;

    /**
     * 이미지를 클릭했을 때 확대한 이미지가 보여질 레이아웃
     */
    private ConstraintLayout mLayout_img_expand;

    private Context mContext;
    /**
     * RcylChatAdapter 생성자
     * @param dataList RecyclerView에 넣을 대화 내용을 담고있는 리스트
     * @param LeftName 채팅화면에서 왼쪽(상대방)의 이름
     * @param layout_img_expand 확대 이미지가 보여지는 레이아웃
     */

    RcylChatAdapter(ArrayList<ChattingRoomItem> dataList, String LeftName, ConstraintLayout layout_img_expand, Context context)
    {
        myDataList = dataList;
        this.mLeftName = LeftName;
        this.mLayout_img_expand = layout_img_expand;
        this.mContext = context;
    }

    /**
     * RcylChatAdapter 생성자
     */

    RcylChatAdapter() { }

    /**
     * RcylChatAdapter onCreateViewHolder
     * @param parent parent
     * @param viewType viewType
     * @return
     */

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(viewType == Code.ViewType.LEFT_CONTENT)
        {
            view = inflater.inflate(R.layout.left_content, parent, false);
            return new LeftViewHolder(view);
        }
        else
        {
            view = inflater.inflate(R.layout.right_content, parent, false);
            return new RightViewHolder(view);
        }
    }

    /**
     * RcylChatAdapter onBindViewHolder
     * @param viewHolder viewHolder
     * @param position 각 홀더의 위치
     */

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position)
    {

        if(viewHolder instanceof LeftViewHolder)
        {
            ChattingRoomItem chattingRoomItem = myDataList.get(position);

            ((LeftViewHolder) viewHolder).name.setText(mLeftName);
            ((LeftViewHolder) viewHolder).content.setText(chattingRoomItem.getmContent());
            ((LeftViewHolder) viewHolder).date.setText(dateToString(chattingRoomItem.getmDate()));
            ((LeftViewHolder) viewHolder).rcyl_img_left.setVisibility(View.GONE);
            if(chattingRoomItem.getImgByteList() != null && !chattingRoomItem.getImgByteList().isEmpty()) {
                RcylImgAdapter rcylImgAdapter = new RcylImgAdapter(chattingRoomItem.getImgByteList(), mLayout_img_expand);
                ((LeftViewHolder) viewHolder).rcyl_img_left.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                ((LeftViewHolder) viewHolder).rcyl_img_left.setAdapter(rcylImgAdapter);
                ((LeftViewHolder) viewHolder).rcyl_img_left.setVisibility(View.VISIBLE);
            }



        }
        else
        {
            ChattingRoomItem chattingRoomItem = myDataList.get(position);

            ((RightViewHolder) viewHolder).content.setText(chattingRoomItem.getmContent());
            ((RightViewHolder) viewHolder).date.setText(dateToString(chattingRoomItem.getmDate()));
            ((RightViewHolder) viewHolder).rcyl_img_right.setVisibility(View.GONE);
            if(chattingRoomItem.getImgByteList() != null && !chattingRoomItem.getImgByteList().isEmpty()) {
                RcylImgAdapter rcylImgAdapter = new RcylImgAdapter(chattingRoomItem.getImgByteList(), mLayout_img_expand);
                ((RightViewHolder) viewHolder).rcyl_img_right.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                ((RightViewHolder) viewHolder).rcyl_img_right.setAdapter(rcylImgAdapter);
                ((RightViewHolder) viewHolder).rcyl_img_right.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * myDataList 의 크기를 반환
     * @return myDataList 의 크기
     */

    @Override
    public int getItemCount()
    {
        return myDataList.size();
    }

    /**
     * position에 위치해 있는 홀더의 viewType을 반환
     * @param position 홀더의 위치
     * @return position에 위치해 있는 홀더의 viewType
     */

    @Override
    public int getItemViewType(int position) {
        return myDataList.get(position).getmViewType();
    }

    /**
     * 왼쪽(상대방)의 대화내용 viewHolder 클래스
     */

    public class LeftViewHolder extends RecyclerView.ViewHolder{
        /**
         * 왼쪽(상대방)의 대화내용
         */
        TextView content;
        /**
         * 왼쪽(상대방)의 이름
         */
        TextView name;
        /**
         * 왼쪽(상대방)이 메시지를 보낸 날짜
         */
        TextView date;
        /**
         * 왼쪽(상대방)이 보내는 이미지를 나타내기 위한 ImageView
         */
        RecyclerView rcyl_img_left;

        LeftViewHolder(View itemView)
        {
            super(itemView);
            content = itemView.findViewById(R.id.txt_chat_content_left);
            name = itemView.findViewById(R.id.txt_chat_name_left);
            date = itemView.findViewById(R.id.txt_chat_date_left);
            rcyl_img_left = itemView.findViewById(R.id.rcyl_chat_img_left);
        }
    }

    /**
     * 오른쪽(나)의 대화내용 viewHolder 클래스
     */

    public class RightViewHolder extends RecyclerView.ViewHolder{
        /**
         * 오른쪽(나)의 대화내용
         */
        TextView content;
        /**
         * 오른쪽(나)이 메시지를 보낸 날짜
         */
        TextView date;
        /**
         * 오른쪽(나)이 보낸 이미지를 나타내기 위한 ImageView
         */
        RecyclerView rcyl_img_right;

        RightViewHolder(View itemView)
        {
            super(itemView);
            date = itemView.findViewById(R.id.txt_chat_date_right);
            content = itemView.findViewById(R.id.txt_chat_content_right);
            rcyl_img_right = itemView.findViewById(R.id.rcyl_chat_img_right);
        }
    }

    /**
     * date를 yyyy-MM-dd hh:mm 형식의 String 으로 반환
     * @param date 날짜
     * @return String 으로 바뀐 date
     */

    private String dateToString(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String messageTime = dateFormat.format(date);
        return messageTime;
    }

    /**
     * 추가된 데이터가 있을 경우 리사이클러뷰에 업데이트된 내용을 보여주는 함수
     * @param messageList
     */

    public void updateDataList(ArrayList<ChattingRoomItem> messageList) {
        if (messageList != null && messageList.size() > 0) {
            myDataList.clear();
            myDataList.addAll(messageList);
            notifyDataSetChanged();
        }
    }

}