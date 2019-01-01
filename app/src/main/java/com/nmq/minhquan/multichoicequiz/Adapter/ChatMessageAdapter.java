package com.nmq.minhquan.multichoicequiz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.github.library.bubbleview.BubbleTextView;
import com.nmq.minhquan.multichoicequiz.Holder.QBUsersHolder;
import com.nmq.minhquan.multichoicequiz.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

public class ChatMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }


    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return qbChatMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView = view;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Kiem tra message neu duoc gui tu user hien tai thi dung layout
            // 'list_send_message'
            if(qbChatMessages.get(i).getSenderId().equals(QBChatService.getInstance().getUser().getId())){
                convertView = inflater.inflate(R.layout.list_send_message, null);
                BubbleTextView bubbleTextView = (BubbleTextView)convertView.findViewById(R.id.message_content);
                bubbleTextView.setText(qbChatMessages.get(i).getBody());
            }else{
                convertView = inflater.inflate(R.layout.list_recv_message, null);
                BubbleTextView bubbleTextView = (BubbleTextView)convertView.findViewById(R.id.message_content);
                bubbleTextView.setText(qbChatMessages.get(i).getBody());
                TextView txtName = (TextView)convertView.findViewById(R.id.message_user);
                txtName.setText(QBUsersHolder.getInstance().getUserById(qbChatMessages.get(i).getSenderId()).getFullName());
            }
        }
        return convertView;
    }
}
