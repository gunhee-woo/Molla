package com.dlog.molla;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * SendActivity에서 수신자의 핸드폰 번호를 보여주기 위해 RecyclerView에 사용하는 Adapter 클래스
 *
 * For example:
 * <pre>
 *       ReceiverPhoneNumberAdapter adapter = new ReceiverPhoneNumberAdapter(this, personDataSet);
 *       recyclerView.setAdapter(adapter);
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */


public class ReceiverPhoneNumberAdapter extends RecyclerView.Adapter<ReceiverPhoneNumberAdapter.PhoneNumberViewHolder> {
    /**
     * 수신자의 데이터가 들어있는 리스트
     */
    private ArrayList<User> mDataset;
    /**
     * itemClickListener
     */
    private static OnItemClickListener mListener;
    /**
     * itemClickListener interface
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    /**
     * context
     */
    Context context;

    /**
     * setOnItemClickListener
     * @param onItemClickListener itemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }

    /**
     * ReceiverPhoneNumber PhoneNumberViewHolder 클래스
     */

    public static class PhoneNumberViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        /**
         * RecyclerView의 각 Holder 내에 핸드폰번호를 담고 있는 TextView
         */
        protected TextView phone_number_tv;
        /**
         * RecyclerView의 각 Holder 내에 수신자 이름을 담고 있는 TextView
         */
        protected TextView name_tv;
        /**
         * view
         */
        protected View view;
        /**
         * ReceiverPhoneNumber PhoneNumberViewHolder 생성자
         */
        public PhoneNumberViewHolder(View v) {
            super(v);
            phone_number_tv = v.findViewById(R.id.phone_number_tv);
            name_tv = v.findViewById(R.id.name_tv);
            view = v;

            v.setClickable(true);
            v.setEnabled(true);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /**
     * ReceiverPhoneNumberAdapter 생성자
     * @param context context
     * @param personDataSet 수신자 번호가 담겨있는 리스트
     */

    public ReceiverPhoneNumberAdapter(Context context, ArrayList<User> personDataSet) {
        this.context = context;
        mDataset = personDataSet;
    }

    /**
     * PhoneNumberViewHolder onCreateViewHolder
     * @param parent parent
     * @param viewType viewType
     * @return PhonewNumberViewHolder
     */

    @Override
    public PhoneNumberViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        // create a new view
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        PhoneNumberViewHolder vh = new PhoneNumberViewHolder(linearLayout);
        return vh;
    }

    /**
     * onBindViewHolder
     * @param holder holder
     * @param position 각 홀더의 위치
     */

    @Override
    public void onBindViewHolder(PhoneNumberViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.name_tv.setText(mDataset.get(position).mNickName);
        holder.phone_number_tv.setText(mDataset.get(position).mPhoneNum);
        holder.view.setTag(position);
    }

    /**
     * mDataset 의 크기를 반환
     * @return mDataset 의 크기
     */

    @Override
    public int getItemCount() {
        return (null != mDataset ? mDataset.size() : 0);
    }

    /**
     * 필터링된 수신자 리스트를 mDataset에 저장하고 리사이클러뷰에 내용이 변화되었음을 알림
     * @param filteredList 필터링된 수신자 리스트
     */

    public void filterList(ArrayList<User> filteredList) {
        mDataset = filteredList;
        notifyDataSetChanged();
    }
}
