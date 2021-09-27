package com.dlog.molla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <pre>
 *     채팅방 서랍장의 대화상대 목록 리사이클러 뷰의 Adapter 클래스.
 *     이름만 받아서 리스트에 뿌려준다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 */
public class ChatPeopleRcylAdapter extends RecyclerView.Adapter<ChatPeopleRcylAdapter.ViewHolder>  {
    /**
     * 채팅방에 참여한 상대방의 이름 목록. 친구추가가 되어 있지 않다면 번호로 표시된다.
     */
    private String[] mNameArr;

    /**
     * 생성자. 채팅방에 참여한 상대방의 이름을 String array 형태로 받는다.
     * @param nameArr
     */
    public ChatPeopleRcylAdapter(String[] nameArr ){
        this.mNameArr = nameArr;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rcyl_chat_setting_people_item,parent,false);
        ChatPeopleRcylAdapter.ViewHolder vh = new ChatPeopleRcylAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_name.setText(mNameArr[position]);
    }

    @Override
    public int getItemCount() {
        return mNameArr.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txt_name;
        ViewHolder(View itemView){
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_chat_person_name);
        }

    }
}
