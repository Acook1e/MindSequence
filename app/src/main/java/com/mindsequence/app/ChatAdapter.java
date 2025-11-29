package com.mindsequence.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_message, parent, false);
            return new AIMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // 用户消息ViewHolder
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_user_content);
            tvTime = itemView.findViewById(R.id.tv_user_time);
        }

        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(message.getTimestamp());
        }
    }

    // AI消息ViewHolder
    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        AIMessageViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_ai_content);
            tvTime = itemView.findViewById(R.id.tv_ai_time);
        }

        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(message.getTimestamp());
        }
    }
}