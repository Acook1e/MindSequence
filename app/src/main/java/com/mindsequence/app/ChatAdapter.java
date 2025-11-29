package com.mindsequence.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> mMessageList;

    // 构造方法
    public ChatAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    // 根据消息类型返回布局类型
    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getType();
    }

    // 创建ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            // 加载用户消息布局
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            // 加载AI消息布局
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    // 绑定数据
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvContent.setText(message.getContent());
            ((UserViewHolder) holder).tvTime.setText(message.getTimestamp());
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).tvContent.setText(message.getContent());
            ((AIViewHolder) holder).tvTime.setText(message.getTimestamp());
        }
    }

    // 获取消息数量
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // AI消息ViewHolder
    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvTime;
        ImageView ivAvatar;

        public AIViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_ai_content);
            tvTime = itemView.findViewById(R.id.tv_ai_time);
            ivAvatar = itemView.findViewById(R.id.iv_ai_avatar);
        }
    }

    // 用户消息ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvTime;
        ImageView ivAvatar;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_user_content);
            tvTime = itemView.findViewById(R.id.tv_user_time);
            ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
        }
    }
}