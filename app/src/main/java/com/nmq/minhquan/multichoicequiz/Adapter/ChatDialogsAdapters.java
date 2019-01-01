package com.nmq.minhquan.multichoicequiz.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.nmq.minhquan.multichoicequiz.Holder.QBUnreadMessagesHolder;
import com.nmq.minhquan.multichoicequiz.R;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatDialogsAdapters extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogsAdapters(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int i) {
        return qbChatDialogs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView = view;
        if(convertView == null){
            LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_chat_dialog, null);
            TextView txtTitle, txtMessage;
            final ImageView imageView, image_unread;
            txtTitle = (TextView) convertView.findViewById(R.id.list_chat_dialog_title);
            txtMessage = (TextView) convertView.findViewById(R.id.list_chat_dialog_message);
            imageView = (ImageView) convertView.findViewById(R.id.image_chatDialog);
            image_unread = (ImageView) convertView.findViewById(R.id.image_unread);
            txtMessage.setText(qbChatDialogs.get(i).getLastMessage());
            txtTitle.setText(qbChatDialogs.get(i).getName());
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            if(qbChatDialogs.get(i).getPhoto().equals("null")){
                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig().withBorder(4)
                        .endConfig()
                        .round();
                // Get first character from Chat Dialog Title for create Chat Dialog Image
                TextDrawable drawable = builder.build(txtTitle.getText().toString().substring(0, 1)
                        .toUpperCase(), randomColor);
                imageView.setImageDrawable(drawable);
            }else{
                // Download bitmap from Server and set for Dialog
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(i).getPhoto()))
                        .performAsync(new QBEntityCallback<QBFile>() {
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle) {
                                String fileURL = qbFile.getPublicUrl();
                                Picasso.with(context)
                                        .load(fileURL)
                                        .resize(50, 50)
                                        .centerCrop()
                                        .into(imageView);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.e("ERROR_IMAGE", "" + e.getMessage());
                            }
                        });
            }

            // Set message unread count
            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig().withBorder(4)
                    .endConfig()
                    .round();

            int unread_count = QBUnreadMessagesHolder.getInstance().getBundle().getInt(qbChatDialogs.get(i).getDialogId());
            if(unread_count > 0){
                TextDrawable unread_drawable = unreadBuilder.build("" + unread_count, Color.RED);
                image_unread.setImageDrawable(unread_drawable);
                }

        }
        return convertView;
    }
}
