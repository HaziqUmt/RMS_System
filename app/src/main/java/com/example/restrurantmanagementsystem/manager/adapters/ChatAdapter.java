package com.example.restrurantmanagementsystem.manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (message.getType() == ChatMessage.TYPE_USER) {
            holder.llUser.setVisibility(View.VISIBLE);
            holder.llAi.setVisibility(View.GONE);
            holder.tvUser.setText(message.getText());
        } else {
            holder.llAi.setVisibility(View.VISIBLE);
            holder.llUser.setVisibility(View.GONE);
            holder.tvAi.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llUser, llAi;
        TextView tvUser, tvAi;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            llUser = itemView.findViewById(R.id.llUserMessage);
            llAi = itemView.findViewById(R.id.llAiMessage);
            tvUser = itemView.findViewById(R.id.tvUserText);
            tvAi = itemView.findViewById(R.id.tvAiText);
        }
    }
}
