package com.nmq.minhquan.multichoicequiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bhargavms.dotloader.DotLoader;
import com.nmq.minhquan.multichoicequiz.Adapter.ChatMessageAdapter;
import com.nmq.minhquan.multichoicequiz.Common.Common;
import com.nmq.minhquan.multichoicequiz.Holder.QBChatMessagesHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener{

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton;
    EditText edtContent;
    ChatMessageAdapter adapter;

    // Update Online User
    ImageView img_online_count, dialog_avatar;
    TextView txt_online_count;

    // Variables for Edit/Delete message
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    // Update dialog
    Toolbar toolbar;
    DotLoader dotLoader;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP){
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat_group_edit_name:
                editNameGroup();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
        }
        return true;
    }

    /*
    * In this function, we will create alert dialog
    * when user input new group name and update
    * group after user press OK
    * */
    private void editNameGroup(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName = (EditText)view.findViewById(R.id.edt_new_group_name);
        // Set Dialog Message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    // When user click OK, we will get name from EditText and update our
                    // Chat Dialog's name
                        qbChatDialog.setName(newName.getText().toString());
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ChatMessageActivity.this, "Group name edited", Toast.LENGTH_SHORT).show();
                                toolbar.setTitle(qbChatDialog.getName());
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /*
    * In this method, we will get all user not join
    * Chat Group and show as list
    * */
    private void addUser(){
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void removeUser(){
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Get index context menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId()){
            case R.id.chat_message_update_message:
                updateMessage();
                break;
            case R.id.chat_message_delete_message:
                deleteMessage();
                break;
        }
        return true;
    }

    private void updateMessage(){
        // Set message for edit text
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);
        edtContent.setText(editMessage.getBody());
        isEditMode = true;// Set Edit mode to true
    }

    private void deleteMessage(){
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId()).get(contextMenuIndexClicked);
        QBRestChatService.deleteMessage(editMessage.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessage();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initViews();
        initChatDialogs();
        // Load all messages of this dialog
        retrieveMessage();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtContent.getText().toString().isEmpty()){
                    if(!isEditMode){
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setBody(edtContent.getText().toString());
                        chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                        chatMessage.setSaveToHistory(true);
                        try {
                            qbChatDialog.sendMessage(chatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        // Fix Private chat don't show message
                        if(qbChatDialog.getType() == QBDialogType.PRIVATE){
                            // Save message to cache and refresh list view
                            QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                            ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(chatMessage.getDialogId());

                            adapter = new ChatMessageAdapter(getBaseContext(), messages);
                            lstChatMessages.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        // Remove text from editText
                        edtContent.setText("");
                        edtContent.setFocusable(true);
                    }else{
                        QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                        messageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();
                        QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                                .performAsync(new QBEntityCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                        // Refresh data
                                        retrieveMessage();
                                        isEditMode = false;
                                        edtContent.setText("");
                                        edtContent.setFocusable(true);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
        });
    }

    private void retrieveMessage(){
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);
        if(qbChatDialog != null){
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    // Save list messages to cache and refresh list
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialogs(){
        // Parse ChatDialog is sent from ChatDialogsActivity
        // and initialize for chat
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        if(!qbChatDialog.getPhoto().equals("null")){
            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto()))
                    .performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            String fileURL = qbFile.getPublicUrl();
                            Picasso.with(getBaseContext())
                                    .load(fileURL)
                                    .resize(50, 50)
                                    .centerCrop()
                                    .into(dialog_avatar);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e("ERROR_IMAGE", ""+e.getMessage());
                        }
                    });
        }

        qbChatDialog.initForChat(QBChatService.getInstance());
        // Register Listener Incoming Message to process incoming message
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });
        // Add typing listener
        registerTypingForChatDialog(qbChatDialog);

        // Add join group to enable group chat
        if(qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP){
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", ""+ e.getMessage());
                }
            });
        }

        final QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if(dialogId == qbChatDialog.getDialogId()){
                    QBRestChatService.getChatDialogById(dialogId)
                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    // Get Online User
                                    try {
                                        Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                        TextDrawable.IBuilder builder = TextDrawable.builder()
                                                .beginConfig()
                                                .withBorder(4)
                                                .endConfig()
                                                .round();
                                        TextDrawable online = builder.build("", Color.RED);
                                        img_online_count.setImageDrawable(online);
                                        txt_online_count.setText(String.format("%d/%d online", onlineList.size(),
                                                qbChatDialog.getOccupants().size()));
                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });


                }
            }
        };
        qbChatDialog.addParticipantListener(participantListener);
        qbChatDialog.addMessageListener(this);
        // Set title for toolbar
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);
    }


    private void registerTypingForChatDialog(QBChatDialog qbChatDialog){
        QBChatDialogTypingListener typingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String s, Integer integer) {
                if(dotLoader.getVisibility() != View.VISIBLE){
                    dotLoader.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void processUserStopTyping(String s, Integer integer) {
                if(dotLoader.getVisibility() != View.INVISIBLE){
                    dotLoader.setVisibility(View.INVISIBLE);
                }
            }
        };
        qbChatDialog.addIsTypingListener(typingListener);
    }


    private void initViews(){
        lstChatMessages = (ListView)findViewById(R.id.list_of_message);
        submitButton = (ImageButton)findViewById(R.id.send_button);
        edtContent = (EditText)findViewById(R.id.edt_content);
        edtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    qbChatDialog.sendIsTypingNotification();// Gui thong bao den all user
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    qbChatDialog.sendStopTypingNotification();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });

        dotLoader = (DotLoader)findViewById(R.id.dot_loader);
        img_online_count = (ImageView)findViewById(R.id.img_online_count);
        txt_online_count = (TextView)findViewById(R.id.txt_online_count);

        // Dialog Avatar
        dialog_avatar = (ImageView)findViewById(R.id.dialog_avatar);
        dialog_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectImage = new Intent();
                selectImage.setType("image/*");
                selectImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(selectImage, "Select Picture"), Common.SELECT_PICTURE);
            }
        });
        // Add context menu
        registerForContextMenu(lstChatMessages);

        // Add Toolbar
        toolbar = (Toolbar)findViewById(R.id.chat_message_toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == Common.SELECT_PICTURE){
            Uri selectedImageUri = data.getData();
            try
            {
                InputStream in = getContentResolver().openInputStream(selectedImageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(in);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bos.toByteArray());
                fileOutputStream.flush();
                fileOutputStream.close();
                int imageSizeKb = (int)file.length() / 1024;
                if(imageSizeKb >= (1024 * 100)){
                    Toast.makeText(this, "Error size", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Upload file
                QBContent.uploadFileTask(file, true, null)
                        .performAsync(new QBEntityCallback<QBFile>() {
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle) {
                                qbChatDialog.setPhoto(qbFile.getId().toString());
                                // Update Chat Dialog
                                QBRequestUpdateBuilder requestUpdateBuilder = new QBRequestUpdateBuilder();
                                QBRestChatService.updateGroupChatDialog(qbChatDialog, requestUpdateBuilder)
                                        .performAsync(new QBEntityCallback<QBChatDialog>() {
                                            @Override
                                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                                dialog_avatar.setImageBitmap(bitmap);
                                            }

                                            @Override
                                            public void onError(QBResponseException e) {
                                                Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        // Save message to cache and refresh list view
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR", ""+ e.getMessage());
    }
}
