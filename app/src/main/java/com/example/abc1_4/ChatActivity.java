package com.example.abc1_4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abc1_4.Classes.ChatHistory;
import com.example.abc1_4.Classes.ChatRoom;
import com.example.abc1_4.Classes.User;
import com.example.abc1_4.Classes.UserMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {
    private int countOfMessages;
    private String NameOfRoom;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference refDataBase;
    private String DATABASE_URL;
    private static ChatRoom thisChatRoom;
    private ValueEventListener postListener2;
    private String MyUsername;
    private boolean chatLoaded;
    private boolean roomIsDeleting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        countOfMessages = 20000;
        chatLoaded = false;
        roomIsDeleting = false;

        Bundle arguments = getIntent().getExtras();
        NameOfRoom = arguments.get("roomName").toString();
        TextView nameOfRoomLabel = findViewById(R.id.nameOfRoom);
        nameOfRoomLabel.setText(NameOfRoom);

        mAuth = FirebaseAuth.getInstance();
        MyUsername = mAuth.getCurrentUser().getDisplayName();
        if (MyUsername == null) MyUsername = mAuth.getCurrentUser().getEmail();

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        DATABASE_URL = "https://client-server-9bcdf.firebaseio.com";
        refDataBase = FirebaseDatabase.getInstance().getReference();

        thisChatRoom = new ChatRoom(NameOfRoom, false);
        thisChatRoom.setId(12);

        postListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                thisChatRoom = dataSnapshot.child("chat_rooms").child(NameOfRoom).getValue(ChatRoom.class);


                if (thisChatRoom == null){
                    //thisChatRoom = new ChatRoom(NameOfRoom, false);
                    Toast.makeText(ChatActivity.this, "Error: empty chat room info", Toast.LENGTH_SHORT).show();
                    refDataBase.removeEventListener(postListener2);
                    Intent intent = new Intent(ChatActivity.this,RoomsListActivity.class);
                    startActivity(intent);
                }
                else{
                    Button deleteRoomButton = findViewById(R.id.deleteRoomButton_chat);
                    if (MyUsername.equals(thisChatRoom.getOwner())){
                        deleteRoomButton.setVisibility(Button.VISIBLE);
                    }
                    else{
                        deleteRoomButton.setVisibility(Button.GONE);
                    }

                    RemoveAllMessages();
                    int size = thisChatRoom.getChatHistory().userMessagesHistory.size();
                    int i;
                    for (i = 0; i < size; i++){
                        if (thisChatRoom.getChatHistory() == null) break;
                        if (thisChatRoom.getChatHistory().userMessagesHistory == null) break;
                        if (thisChatRoom.getChatHistory().userMessagesHistory.get(i) != null){
                            CreateMessage(thisChatRoom.getChatHistory().userMessagesHistory.get(i).getMessage(),
                                    thisChatRoom.getChatHistory().userMessagesHistory.get(i).getAuthor(),
                                    thisChatRoom.getChatHistory().userMessagesHistory.get(i).getAuthor().equals(MyUsername), countOfMessages);
                            countOfMessages+=4;
                        }
                    }
                }
                //TextView textview1 = findViewById(R.id.DataContainerText);
                //textview1.setText(u.username);
                if (!chatLoaded){
                    SendSystemMessage("Пользователь " + MyUsername + " присоединился к комнате");
                    countOfMessages+=4;
                }
                chatLoaded = true;
                ScrollView scrollView = findViewById(R.id.scrollSpace_chatRoom);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Toast.makeText(RoomsListActivity.this, "Changes cancelled. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        refDataBase.addValueEventListener(postListener2);

    }

    @Override
    public void onStart(){
        super.onStart();

        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    public void onStop(){
        if (!roomIsDeleting){
            SendSystemMessage("Пользователь " + MyUsername + " покинул комнату");
            countOfMessages+=4;
        }
        refDataBase.removeEventListener(postListener2);
        super.onStop();
    }

    public void onBackButton_chatRoom(View view){
        refDataBase.removeEventListener(postListener2);
        Intent intent = new Intent(ChatActivity.this,RoomsListActivity.class);
        startActivity(intent);
    }

    public void onSendMessageButton_chatRoom(View view){
        EditText editText = findViewById(R.id.messageEdit);

        if (editText.getText() != null){
            if (!editText.getText().toString().equals("")){
                SendMessage(editText.getText().toString(), MyUsername);
                editText.setText("");
                hideKeyboard();
            }
        }
    }

    private void SendMessage(String message, String name){
        UserMessage newMessage = new UserMessage(message,name);
        if (thisChatRoom != null){
            thisChatRoom.getChatHistory().Add(newMessage);
            refDataBase.child("chat_rooms").child(NameOfRoom).setValue(thisChatRoom);
        }
    }

    private void SendSystemMessage(String message){
        SendMessage(message, "System");
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void CreateMessage(String text, String Name, boolean itsMyMessage, int numberOfMessage){

        if (Name.equals("System")){
            CreateSystemMessage(text, numberOfMessage);
            return;
        }

        //инициализация рамки сообщения
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin1 = 0;
        float rightMargin1 = 0;
        if (itsMyMessage){
            leftMargin1 = (float) 30*dp;
            rightMargin1 = (float) 0*dp;
        }
        else{
            leftMargin1 = (float) 0*dp;
            rightMargin1 = (float) 30*dp;
        }
        float topMargin1 = (float) 0*dp;
        float bottomMargin1 = (float) 0*dp;

        LinearLayout messageFrame = new LinearLayout(getApplicationContext());
        messageFrame.setId(numberOfMessage);
        LinearLayout.LayoutParams lpParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lpParams1.setMargins((int)leftMargin1,(int)topMargin1,(int)rightMargin1,(int)bottomMargin1);
        messageFrame.setLayoutParams(lpParams1);
        messageFrame.setOrientation(LinearLayout.VERTICAL);
        if(itsMyMessage){
            messageFrame.setGravity(Gravity.RIGHT);
        }
        else{
            messageFrame.setGravity(Gravity.LEFT);
        }

        //инициализация тела сообщения
        float leftMargin = (float) 5*dp;
        float rightMargin = (float) 5*dp;
        float topMargin = (float) 5*dp;
        float bottomMargin = (float) 5*dp;
        LinearLayout messageBody;
        if (itsMyMessage){
            messageBody = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_message_body, null);
        }
        else{
            messageBody = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.opponent_message_body, null);
        }
        messageBody.setId(numberOfMessage+1);
        LinearLayout.LayoutParams lpParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lpParams2.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        messageBody.setLayoutParams(lpParams2);

        //инициализация текста сообщения
        TextView MessageText = new TextView(getApplicationContext());
        initTextLabel_forChatRoom(text,MessageText);
        MessageText.setId(numberOfMessage+2);

        //инициализация заголовка сообщения (имени отправителя)
        TextView MessageTitle = new TextView(getApplicationContext());
        initTextTitle_forChatRoom(Name,MessageTitle);
        MessageTitle.setId(numberOfMessage+3);
        if (itsMyMessage){
            MessageTitle.setGravity(Gravity.RIGHT);
        }
        else{
            MessageTitle.setGravity(Gravity.LEFT);
        }

        LinearLayout mainChatSpace = findViewById(R.id.chatSpace);
        messageBody.addView(MessageText);
        messageFrame.addView(MessageTitle);
        messageFrame.addView(messageBody);
        mainChatSpace.addView(messageFrame);
    }

    private void CreateSystemMessage(String text, int numberOfMessage){

        //инициализация рамки сообщения
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin1 = 0;
        float rightMargin1 = 0;
        leftMargin1 = (float) 0*dp;
        rightMargin1 = (float) 30*dp;
        float topMargin1 = (float) 0*dp;
        float bottomMargin1 = (float) 0*dp;

        LinearLayout messageFrame = new LinearLayout(getApplicationContext());
        messageFrame.setId(numberOfMessage);
        LinearLayout.LayoutParams lpParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lpParams1.setMargins((int)leftMargin1,(int)topMargin1,(int)rightMargin1,(int)bottomMargin1);
        messageFrame.setLayoutParams(lpParams1);
        messageFrame.setOrientation(LinearLayout.VERTICAL);
        messageFrame.setGravity(Gravity.LEFT);


        //инициализация тела сообщения
        float leftMargin = (float) 5*dp;
        float rightMargin = (float) 5*dp;
        float topMargin = (float) 5*dp;
        float bottomMargin = (float) 5*dp;

        LinearLayout messageBody;
        messageBody = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.system_message_body, null);
        messageBody.setId(numberOfMessage+1);
        LinearLayout.LayoutParams lpParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lpParams2.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        messageBody.setLayoutParams(lpParams2);

        //инициализация текста сообщения
        TextView MessageText = new TextView(getApplicationContext());
        initTextLabel_forChatRoom(text,MessageText);
        MessageText.setId(numberOfMessage+2);

        //инициализация заголовка сообщения (имени отправителя)
        TextView MessageTitle = new TextView(getApplicationContext());
        initTextTitle_forChatRoom("System",MessageTitle);
        MessageTitle.setId(numberOfMessage+3);
        MessageTitle.setGravity(Gravity.LEFT);

        LinearLayout mainChatSpace = findViewById(R.id.chatSpace);
        messageBody.addView(MessageText);
        messageFrame.addView(MessageTitle);
        messageFrame.addView(messageBody);
        mainChatSpace.addView(messageFrame);
    }

    private void RemoveAllMessages(){
        LinearLayout mainChatSpace = findViewById(R.id.chatSpace);
        mainChatSpace.removeAllViewsInLayout();
        countOfMessages = 20000;
    }

    private void initTextLabel_forChatRoom(String Text, TextView NewTextLabel){
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin = (float) 20*dp;
        float rightMargin = (float) 20*dp;
        float topMargin = (float) 3*dp;
        float bottomMargin = (float) 3*dp;

        LinearLayout.LayoutParams lpParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT );
        lpParams.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        NewTextLabel.setTextSize(10*dp);
        NewTextLabel.setText(Text);
        NewTextLabel.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
        NewTextLabel.setLayoutParams(lpParams);
    }

    private void initTextTitle_forChatRoom(String Text, TextView NewTextLabel){
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin = (float) 20*dp;
        float rightMargin = (float) 20*dp;
        float topMargin = (float) 0*dp;
        float bottomMargin = (float) 0*dp;

        LinearLayout.LayoutParams lpParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT );
        lpParams.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        NewTextLabel.setTextSize(8*dp);
        NewTextLabel.setText(Text);
        NewTextLabel.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
        NewTextLabel.setLayoutParams(lpParams);
    }

    public void onDeleteRoomButtonClick(View view){
        Context context = ChatActivity.this;
        String title = "Удалить комнату";
        String message = "Вы уверены, что хотите удалить комнату?";
        String button1String = "Удалить";
        String button2String = "Отмена";

        AlertDialog.Builder ad;

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);  // заголовок
        ad.setMessage(message); // сообщение

        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                //удаление комнаты
                deleteRoom(NameOfRoom);
                refDataBase.removeEventListener(postListener2);
                Intent intent = new Intent(ChatActivity.this,RoomsListActivity.class);
                startActivity(intent);
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
               //отмена
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                //отмена
            }
        });
        ad.show();
    }

    private void deleteRoom(String name){
        roomIsDeleting = true;
        Bundle arguments = getIntent().getExtras();
        int num = (int) arguments.get("number");
        refDataBase.child("chat_room_headers").child("HeadersList").child(Integer.toString(num)).removeValue();
        refDataBase.child("chat_rooms").child(name).removeValue();
    }

    private String EncryptInfo(String info, byte[] key){
        // Set up secret key spec for 128-bit AES encryption and decryption
        SecretKeySpec sks = null;
        try {
            //генерация случайного ключа
            //SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            //sr.setSeed(seed.getBytes());
            //KeyGenerator kg = KeyGenerator.getInstance("AES");
            //kg.init(128, sr);
            //sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
            sks = new SecretKeySpec(key,"AES");
        } catch (Exception e) {
            Log.e("Crypto", "AES secret key spec error");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c;
            c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(info.getBytes());
        } catch (Exception e) {
            Log.e("Crypto", "AES encryption error:" + e.getMessage());
        }
        String a = info;
        if (encodedBytes != null){
            a = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
        }
        return a;
    }

    private String DecryptInfo(byte[] encrypted, byte[] key){
        SecretKeySpec sks = new SecretKeySpec(key,"AES");
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(encrypted);
        } catch (Exception e) {
            Log.e("Crypto", "AES decryption error");
        }
        String a = "";
        if (decodedBytes != null){
            a = Base64.encodeToString(decodedBytes, Base64.DEFAULT);
        }
        return a;
    }
}
