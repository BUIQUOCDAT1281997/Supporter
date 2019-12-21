package com.example.supporter.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.supporter.Activity.ChatActivity;
import com.example.supporter.Other.Post;
import com.example.supporter.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> listData;
    private Context mContext;

    public PostAdapter(List<Post> listData, Context mContext) {
        this.listData = listData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_posts, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        final Post post = listData.get(position);
        if (!post.getImageURL().equals("default")) {
            Glide.with(mContext).load(post.getImageURL()).placeholder(R.drawable.ic_user).into(holder.imageUser);
        } else
            holder.imageUser.setImageResource(R.drawable.ic_user);

        if (!post.getPicturePost().equals("default")) {
            holder.imagePost.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(post.getPicturePost()).placeholder(R.drawable.ic_insert_photo_black_24dp).into(holder.imagePost);
        }else {
            holder.imagePost.setVisibility(View.GONE);
        }

        holder.tvUserName.setText(post.getUserName());
        holder.tvTime.setText(post.getTime());
        holder.tvContent.setText(post.getContentPost());

        holder.btSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userID", post.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (listData.size()>100) return 100;
        else return listData.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageUser;
        TextView tvUserName , tvTime , tvContent;
        ImageView imagePost;
        Button btSupport;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);

            imageUser = itemView.findViewById(R.id.item_news_avatar_user);
            tvUserName = itemView.findViewById(R.id.item_news_username);
            tvTime = itemView.findViewById(R.id.item_news_current_time);
            tvContent = itemView.findViewById(R.id.item_news_text_posts);
            imagePost = itemView.findViewById(R.id.item_news_image_posts);
            btSupport = itemView.findViewById(R.id.item_news_botton_support);
        }
    }
}
