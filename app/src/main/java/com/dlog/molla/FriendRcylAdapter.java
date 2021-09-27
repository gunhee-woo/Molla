package com.dlog.molla;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * <pre>
 *     {@link FragmentFriendList}의 친구 목록 리사이클러 뷰의 Adapter 클래스
 *     검색기능 위해 Filterable을 implements한다.
 * </pre>
 * @author 최정헌
 * @version 1.0.0 20/04/13
 * @see Filterable
 * @see RecyclerView.Adapter
 */
public class FriendRcylAdapter extends RecyclerView.Adapter<FriendRcylAdapter.ViewHolder> implements Filterable {
    /**
     * 화면에 보여지는 User 리스트
     */
    private ArrayList<User> mUserList = null;
    /**
     * 전체 User 리스트
     */
    private ArrayList<User> mUnFilterUserList = null;
    /**
     * CheckBox에 선택된 User 갯수
     */
    private int mSelectCount = 0;
    /**
     * 이 어뎁터가 생성된 context
     */
    private Context mContext;


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txt_nickName;
        TextView txt_phonNum;
        CheckBox checkBox;
        ViewHolder(View itemView){
            super(itemView);
            txt_nickName = itemView.findViewById(R.id.friend_list_name);
            txt_phonNum = itemView.findViewById(R.id.friend_list_num);
            checkBox = itemView.findViewById(R.id.chckBx_friend_list);

        }

    }
    FriendRcylAdapter(Context context, ArrayList<User> list){
        mUserList = list;
        mUnFilterUserList = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public FriendRcylAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rycl_friend_item_view,parent,false);
        FriendRcylAdapter.ViewHolder vh = new FriendRcylAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRcylAdapter.ViewHolder holder, final int position) {
        String nickName = mUserList.get(position).mNickName;
        String phonNum = mUserList.get(position).mPhoneNum;
        holder.txt_nickName.setText(nickName);
        holder.txt_phonNum.setText(phonNum);
        if(mUserList.get(position).isCheck){
            holder.checkBox.setChecked(true);
        }
        else{
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()){
                    for(int i = 0 ; i < mUnFilterUserList.size() ; i++){
                        if(mUserList.get(position).equals(mUnFilterUserList.get(i))){
                            mUnFilterUserList.get(i).isCheck = true;
                            mSelectCount++;
                            /*
                            //전체 리스트에서 자리 변경
                            for(int j = 0 ; j < mUnFilterUserList.size() ; j++){
                                if(!mUnFilterUserList.get(j).isCheck){
                                    User empty = mUnFilterUserList.get(i);
                                    mUnFilterUserList.remove(i);
                                    mUnFilterUserList.add(j,empty);
                                    break;
                                }
                            }*/
                            break;
                        }
                    }
                }
                else{
                    for(int i = 0 ; i < mUnFilterUserList.size() ; i++){
                        if(mUserList.get(position).equals(mUnFilterUserList.get(i))){
                            mUnFilterUserList.get(i).isCheck = false;
                            mSelectCount--;
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    /**
     * 유저가 입력한 text와 일치하는 번호를 가진 User만 리스트에 띄우도록 하는 메서드
     * @return 필터링된 User 리스트
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()) {//전체 리스트 띄우기
                    //체크된 유저들을 상단으로
                    int uncheck_idx = 0;

                    for(int i = 0 ; i < mUnFilterUserList.size() ; i++){
                        if(mUnFilterUserList.get(i).isCheck){
                            //전체 리스트에서 자리 변경
                            User empty = mUnFilterUserList.get(i);
                            mUnFilterUserList.remove(i);
                            mUnFilterUserList.add(uncheck_idx,empty);
                            uncheck_idx++;
                        }
                    }
                    mUserList = mUnFilterUserList;
                    Log.d("TAG","리스트 자리변경 selectCount : " + mSelectCount);

                } else {
                    ArrayList<User> filteringList = new ArrayList<>();
                    for(int i = 0 ; i < mUnFilterUserList.size() ; i++) {
                        String name = mUnFilterUserList.get(i).mNickName;
                        if(name.toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(mUnFilterUserList.get(i));
                        }
                    }
                    mUserList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mUserList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mUserList = (ArrayList<User>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * CheckBox에 선택된 User들을 {@link UserDataBase}에서 삭제하는 메서드
     */
    public void onItRemove(){
        User[] deleteUserArr = new User[mSelectCount];
        int idx= 0;
        for(int i = 0; i < mUnFilterUserList.size() ; i++){
            if(mUnFilterUserList.get(i).isCheck){
                deleteUserArr[idx] = mUnFilterUserList.get(i);
                idx++;
                //리사이클러 뷰 리스트에서 제거
                mUnFilterUserList.remove(i);
                i--;
            }
        }
        notifyDataSetChanged();
        mSelectCount = 0;

        //Db에서 삭제
        new FragmentFriendList.UserDbAsyncTask((Activity) mContext, deleteUserArr,4).execute();
    }

    /**
     * <pre>
     *     화면에 보여지고 있는 User 리스트({@link FriendRcylAdapter#mUserList})의 아이템 모두를 선택하는 메서드
     *     {@link FriendRcylAdapter#mUnFilterUserList}에도 선택된 것들이 반영된다.
     * </pre>
     *
     */
    public void allSelect(){
        //지금 화면에 보여지고 있는 리스트를 모두 선택한다  (unfilterlist에도 반영해야함)
        //화면에 보여지고 있는 리스트 : mUserList
        for(int i = 0 ; i < mUserList.size() ; i++ ){
            if(!mUserList.get(i).isCheck) {
                mUserList.get(i).isCheck = true;
                mSelectCount++;
            }
        }
        notifyDataSetChanged();
        Log.d("TAG","allSelect() selectCount : " + mSelectCount);
    }

    /**
     * 화면에 보여지고 있는 User 리스트({@link FriendRcylAdapter#mUserList})의 아이템 모두를 선택해제 시키는 메서드
     * {@link FriendRcylAdapter#mUnFilterUserList}에도 선택해제 된 것들이 반영된다.
     */
    public void allDeSelect(){
        //지금 화면에 보여지고 있는 리스트를 모두 선택해제 한다.
        for(int i = 0 ; i < mUserList.size() ; i++ ){
            if(mUserList.get(i).isCheck) {
                mUserList.get(i).isCheck = false;
                mSelectCount--;
            }
        }
        notifyDataSetChanged();
        Log.d("TAG","allDeSelect() selectCount : " + mSelectCount);
    }

    /**
     * 화면에 보여지고 있는 User 리스트({@link FriendRcylAdapter#mUserList}) 중에서 선택된 User들만 가져오는 메서드
     * @return 선택된 유저 리스트
     */
    public ArrayList<User> getCheckUserList(){
        ArrayList<User> checkUserList = new ArrayList<>();
        for(int i = 0 ; i < mUnFilterUserList.size() ; i++ ){
            if(mUnFilterUserList.get(i).isCheck) {
                checkUserList.add(mUnFilterUserList.get(i));
            }
        }
        return checkUserList;
    }

    public int getmSelectCount() {
        return mSelectCount;
    }
}
