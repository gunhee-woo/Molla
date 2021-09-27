package com.dlog.molla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * OpenSourceActivity에서 오픈소스 라이브러리들을 보여주기 위해 RecyclerView에 사용하는 Adapter 클래스
 *
 * For example:
 * <pre>
 *      recyclerView.setAdapter(new RcylOpenSourceAdapter(openSourceInfoArrayList));
 * </pre>
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class RcylOpenSourceAdapter extends RecyclerView.Adapter<RcylOpenSourceAdapter.ViewHolder> {
    /**
     * 오픈소스 라이브러리 정보를 담고있는 리스트
     */
    private ArrayList<OpenSourceInfo> mOpensourceList;
    /**
     * RcylOpenSourceAdapter 생성자
     * @param opensourceList 오픈소스 라이브러리 정보를 담고있는 리스트
     */
    public RcylOpenSourceAdapter(ArrayList<OpenSourceInfo> opensourceList){
        this.mOpensourceList = opensourceList;
    }
    /**
     * RcylOpenSourceAdapter ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        /**
         * 오픈소스 라이브러리 개발자 이름을 담고 있는 TextView
         */
        TextView txt_name ;
        /**
         * 오픈소스 라이브러리 깃허브 주소를 담고 있는 TextView
         */
        TextView txt_link ;
        /**
         * 오픈소스 라이브러리 저작권을 담고 있는 TextView
         */
        TextView txt_copyright ;
        /**
         * 오픈소스 라이브러리 라이센스를 담고 있는 TextView
         */
        TextView txt_license ;

        /**
         * RcylOpenSourceAdapter ViewHolder 클래스 생성자
         */

        ViewHolder(View itemView){
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_opensource_name);
            txt_link = itemView.findViewById(R.id.txt_opensource_link);
            txt_copyright = itemView.findViewById(R.id.txt_opensource_copyright);
            txt_license = itemView.findViewById(R.id.txt_opensource_license);
        }
    }

    /**
     * RcylOpenSourceAdapter ViewHolder onCreateViewHolder
     */

    @NonNull
    @Override
    public RcylOpenSourceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rcyl_opensource_item_view,parent,false);
        RcylOpenSourceAdapter.ViewHolder vh = new RcylOpenSourceAdapter.ViewHolder(view);
        return vh;
    }

    /**
     * RcylOpenSourceAdapter ViewHolder onBindViewHolder
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_name.setText(mOpensourceList.get(position).getmName());
        holder.txt_copyright.setText(mOpensourceList.get(position).getmCopyright());
        holder.txt_license.setText(mOpensourceList.get(position).getmLicense());
        holder.txt_link.setText(mOpensourceList.get(position).getmWebAddress());
    }

    /**
     * 오픈소스 라이브러리 정보를 담고있는 리스트의 크기를 반환
     * @return 오픈소스 라이브러리 정보를 담고있는 리스트의 크기
     */

    @Override
    public int getItemCount() {
        return mOpensourceList.size();
    }
}
