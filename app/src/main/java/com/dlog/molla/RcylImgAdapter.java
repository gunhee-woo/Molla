package com.dlog.molla;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RcylImgAdapter extends RecyclerView.Adapter<RcylImgAdapter.ViewHolder> {

    private ArrayList<byte[]> mImgList;
    private ConstraintLayout mLayout_img_expand;

    public RcylImgAdapter(ArrayList<byte[]> imgList, ConstraintLayout layout_img_expand){
        this.mImgList = imgList;
        this.mLayout_img_expand = layout_img_expand;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgView;


        ViewHolder(View itemView){
            super(itemView);
            imgView = itemView.findViewById(R.id.img_chat_content);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rcyl_img_item_view,parent,false);
        RcylImgAdapter.ViewHolder vh = new RcylImgAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        byte[] img_bytes = mImgList.get(position);
        Bitmap bitmap = null;
        if(img_bytes != null){
            bitmap = BitmapFactory.decodeByteArray(img_bytes,0,img_bytes.length);
            if(bitmap == null){
                holder.imgView.setImageResource(R.drawable.lock);
            }else{
                holder.imgView.setImageBitmap(bitmap);
            }
            holder.imgView.setVisibility(View.VISIBLE);
        }
        else{
            holder.imgView.setVisibility(View.GONE);
        }

        final Bitmap finalBitmap = bitmap;
        holder.imgView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(finalBitmap == null) {
                    holder.imgView.setEnabled(false);
                } else {
                    mLayout_img_expand.setVisibility(View.VISIBLE);
                    ImageView imageView = mLayout_img_expand.findViewById(R.id.img_expand);
                    imageView.setImageDrawable(holder.imgView.getDrawable());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mImgList.size();
    }


}
