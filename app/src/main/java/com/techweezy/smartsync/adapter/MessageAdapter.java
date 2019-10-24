package com.techweezy.smartsync.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.techweezy.smartsync.R;
import com.techweezy.smartsync.model.Message;

import java.util.List;

public class MessageAdapter extends
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List <Message> messageList ;
    Context mContext;

    public MessageAdapter (Context context, List<Message>messageList){
        this.mContext = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.sms_view_list,viewGroup, false);

        return new MessageViewHolder(mView);
    }

    @Override
    public void onBindViewHolder
            (@NonNull MessageViewHolder messageViewHolder, final int position) {
        messageViewHolder.MSG_SENDER.setText(messageList.get(position).getSender());
        messageViewHolder.MSG_BODY.setText(messageList.get(position).getMessage());
        messageViewHolder.TIMESTAMP.setText(messageList.get(position).getTimestamp());
        messageViewHolder.MSG_STATUS.setText(messageList.get(position).getSync_status());

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public  class MessageViewHolder extends
            RecyclerView.ViewHolder {
        TextView MSG_BODY, MSG_SENDER, TIMESTAMP, MSG_STATUS;
        ConstraintLayout mViewLayout;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            MSG_BODY = itemView.findViewById(R.id.message_message_bodyTV);
            MSG_SENDER = itemView.findViewById(R.id.message_senderTV);
            TIMESTAMP = itemView.findViewById(R.id.message_timestampTV);
            MSG_STATUS = itemView.findViewById(R.id.message_status_TV);

            mViewLayout = itemView.findViewById(R.id.message_view_layout);
        }

    }
}
