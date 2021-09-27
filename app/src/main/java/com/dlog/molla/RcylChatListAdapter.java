package com.dlog.molla;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * FragmentChatList에서 채팅방들을 보여주기 위해 RecyclerView에 사용하는 Adapter 클래스
 *
 * For example:
 * <pre>
 *       RcylChatListAdapter rcylChatListAdapter = new RcylChatListAdapter();
 *       recyclerView.setAdapter(new RcylChatListAdapter(getContext(),msgList));
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class RcylChatListAdapter extends RecyclerView.Adapter<RcylChatListAdapter.ViewHolder> implements Filterable {
    /**
     * 필터링된 메시지를 담은 리스트
     */
    private ArrayList<ChatListItem> mMsgList;
    /**
     * 필터링되지 않은 원본 메시지를 담은 리스트
     */
    private ArrayList<ChatListItem> mUnFiliterMsgList;
    /**
     * context
     */
    private Context mContext;

    /**
     * 메시지를 필터링하는 함수
     * @return FilterResults
     */

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()) {//전체 리스트 띄우기
                    mMsgList = mUnFiliterMsgList;
                } else {
                    ArrayList<ChatListItem> filteringList = new ArrayList<>();
                    for(int i = 0; i < mUnFiliterMsgList.size() ; i++) {
                        ChatListItem chatListItem = mUnFiliterMsgList.get(i);
                        String name = chatListItem.getmName();
                        HashSet numSet = chatListItem.getmNumber();
                        Iterator it = numSet.iterator();
                        boolean isNumContain = false;
                        while (it.hasNext()){
                            String num = (String)it.next();
                            isNumContain = num.toLowerCase().contains(charString.toLowerCase());
                            if(isNumContain){
                                break;
                            }
                        }

                        if(name == null || name.equals("")){
                            if(isNumContain){
                                filteringList.add(chatListItem);
                            }
                        }
                        else{
                            if(name.toLowerCase().contains(charString.toLowerCase()) || isNumContain) {
                                filteringList.add(chatListItem);
                            }
                        }
                    }
                    mMsgList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mMsgList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mMsgList = (ArrayList<ChatListItem>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * RcylChatListAdapter의 viewHolder 클래스
     */

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txt_nickName;
        ImageView img_face;
        ConstraintLayout layout;

        /**
         * RcylChatListAdapter의 viewHolder 생성자
         * @param itemView itemView
         */
        ViewHolder(View itemView){
            super(itemView);
            txt_nickName = itemView.findViewById(R.id.txt_chat_list_name);
            img_face = itemView.findViewById(R.id.img_chat_list_face);
            layout = itemView.findViewById(R.id.layout_chat_list);
        }
    }

    /**
     * RcylChatListAdapter 생성자
     * @param context context
     * @param msgList 필터링된 메시지를 담고 있는 리스트
     */

    RcylChatListAdapter(Context context, ArrayList<ChatListItem> msgList){
        this.mContext = context;
        this.mMsgList = msgList;
        this.mUnFiliterMsgList = msgList;


    }

    /**
     * RcylChatListAdapter onCreateViewHolder
     * @param parent parent
     * @param viewType viewType
     * @return RcylChatListAdapter.ViewHolder
     */

    @NonNull
    @Override
    public RcylChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rcyl_chat_list_item_view,parent,false);
        RcylChatListAdapter.ViewHolder vh = new RcylChatListAdapter.ViewHolder(view);
        return vh;
    }

    /**
     * RcylChatListAdapter onBindViewHolder
     * @param holder holder
     * @param position 각 홀더의 위치
     */

    @Override
    public void onBindViewHolder(@NonNull final RcylChatListAdapter.ViewHolder holder, final int position) {
        final ChatListItem chatListItem = mMsgList.get(position);
        String name = chatListItem.getmName();
        holder.txt_nickName.setText(name);

        Log.d("TAG", "onBindViewHolder name" + name);
        Log.d("TAG", "onBindViewHolder msgList get " + position + " " +chatListItem.getmNumber());

        if(chatListItem.getmNumber().size()>1){
            if(chatListItem.isHaveNewMsg()){
                //holder.img_face.setImageResource(R.drawable.ic_people_black);
                holder.layout.setBackgroundResource(R.drawable.friend_list_card_new);
            }
            else{
                holder.img_face.setImageResource(R.drawable.person_two);
                holder.layout.setBackgroundResource(R.drawable.friend_list_card);
            }
        }
        else{
            if(chatListItem.isHaveNewMsg()){
                //holder.img_face.setImageResource(R.drawable.ic_person_black);
                holder.layout.setBackgroundResource(R.drawable.friend_list_card_new);
            }else {
                holder.img_face.setImageResource(R.drawable.person_one);
                holder.layout.setBackgroundResource(R.drawable.friend_list_card);
            }
        }

        holder.layout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                notifyDataSetChanged();
                Intent intent = new Intent(mContext, PopupActivity.class);
                intent.putExtra("Name",chatListItem.getmName());
                intent.putExtra("Number", chatListItem.getmNumber());
                intent.putExtra("ThreadId",chatListItem.getmThreadId());
                intent.putExtra("PopupTitle","대화방 인증");
                intent.putExtra("PopupInfo", "비밀번호를 입력해주세요.");
                intent.putExtra("TaskNum", 1);
                ((Activity) mContext).startActivity(intent);
                chatListItem.setHaveNewMsg(false);
            }
        });
    }

    /**
     * 필터링된 메시지 리스트의 크기를 반환
     * @return 필터링된 메시지 리스트의 크기
     */

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

    /**
     * 새로운 메시지가 도착했을때 새로운 메시지의 핸드폰 번호로 구성하고 있는 채팅방을 최상단으로 올려주는 기능을 하는 함수
     * @param numSet 채팅방을 구성하고 있는 핸드폰 번호를 담고 있는 HashSet
     * @param currentClassName 현재 사용자가 보고있는 액티비티
     */

    public void newMsgArrived(HashSet numSet,String currentClassName){
        //일치하는 hashSet을 가진 놈 찾기 unfilterlist에서 찾기
        for(int i = 0; i< mUnFiliterMsgList.size() ; i++){
            ChatListItem chatListItem = mUnFiliterMsgList.get(i);
            if(chatListItem.getmNumber().equals(numSet)){
                ChatListItem empty = chatListItem;
                mUnFiliterMsgList.remove(i);
                if(currentClassName.equals("com.dlog.molla.MainActivity")) {
                    empty.setHaveNewMsg(true);
                }
                mUnFiliterMsgList.add(0,empty);
                notifyDataSetChanged();
                break;
            }
        }
        //맨 앞으로 보내기
        //notifydatasetchanged
        //ishavenewmsg true로 set하기
        //이미지 변경

    }



}
