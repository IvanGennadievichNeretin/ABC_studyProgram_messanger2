package com.example.abc1_4;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abc1_4.Classes.ListOf_RoomHeaders;
import com.example.abc1_4.Classes.RoomHeader;
import com.example.abc1_4.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomsListActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference refDataBase;
    private String DATABASE_URL;
    private int roomCount;
    private ValueEventListener postListener3;
    private static ListOf_RoomHeaders listOf_roomHeaders;
    private String MyUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_list);

        mAuth = FirebaseAuth.getInstance();
        DATABASE_URL = "https://client-server-9bcdf.firebaseio.com";
        refDataBase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        MyUsername = mAuth.getCurrentUser().getDisplayName();
        if (MyUsername == null) MyUsername = mAuth.getCurrentUser().getEmail();
        //listOf_roomHeaders = new ListOf_RoomHeaders();

        postListener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                listOf_roomHeaders = dataSnapshot.child("chat_room_headers").getValue(ListOf_RoomHeaders.class);

                if (listOf_roomHeaders == null){
                    listOf_roomHeaders = new ListOf_RoomHeaders();
                }
                else{
                    RemoveAllRooms();
                    int size = listOf_roomHeaders.GetSize();
                    int i;
                    for (i = 1; i < size; i++){
                        if (listOf_roomHeaders.getHeader(i) != null) {
                            String nameRoom = listOf_roomHeaders.getHeader(i).getRoomName();
                            String passworded = "Открыто";
                            if (listOf_roomHeaders.getHeader(i).isPassword()) passworded = "Защищено";
                            drawRoom(nameRoom, passworded, roomCount);
                            roomCount+=4;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(RoomsListActivity.this, "Changes cancelled. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        refDataBase.addValueEventListener(postListener3);

    }

    @Override
    public void onStart(){
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        roomCount = 10000;
    }

    @Override
    public void onStop(){
        refDataBase.removeEventListener(postListener3);
        super.onStop();
    }

    public void onBackButton_RoomList(View view){
        refDataBase.removeEventListener(postListener3);
        Intent intent = new Intent(RoomsListActivity.this,ContentActivity.class);
        startActivity(intent);
    }

    public void onCreateRoomListButton(View view){

        //создание диалогового окна с созданием комнаты
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.create_room_dialog,
                (ViewGroup)findViewById(R.id.create_Room_Dialog));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        final CheckBox addPasswordCheckbox = layout.findViewById(R.id.add_password_checkbox);
        addPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                PasswordCheckboxChanged(layout);
            }
        });

        final EditText roomNameEdit = layout.findViewById(R.id.name_of_room_edit);
        final EditText roomPasswordEdit = layout.findViewById(R.id.password_of_room_edit);

        builder.setMessage(R.string.create_room)
                .setPositiveButton(R.string.creating_room_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Пользователь создает комнату
                        String roomName = roomNameEdit.getText().toString();
                        String roomPassword = roomPasswordEdit.getText().toString();
                        boolean RoomIsPassworded = addPasswordCheckbox.isChecked();
                        createRoom(roomName, RoomIsPassworded, roomPassword);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.show();

    }


    private void PasswordCheckboxChanged(View layout){
        EditText passwordEdit = layout.findViewById(R.id.password_of_room_edit);
        CheckBox addPasswordCheckbox = layout.findViewById(R.id.add_password_checkbox);
        if (addPasswordCheckbox.isChecked()){
            passwordEdit.setVisibility(EditText.VISIBLE);
        }
        else{
            passwordEdit.setVisibility(EditText.GONE);
        }
    }

    private void createRoom(String name, boolean isPassworded, String password){

        if (!nameIsCorrect(name)){
            if (name.equals("")){
                incorrectNewRoomMessage("Имя не должно быть пустым");
                return;
            }
            if (isPassworded){
                if (!passwordIsCorrect(password)){
                    incorrectNewRoomMessage("Пароль не должен содержать пробел");
                    return;
                }
            }
            incorrectNewRoomMessage("Имя комнаты не должно содержать специальные символы: пробел,"+
                    " !, '', #, $, %, &, ', (, ), *, +, ,, -, ., /, :, ;, <, =, >, ?, @, [, ], ^, _, `;"+
                    " Имя комнаты должно быть не короче 3 символов");
            return;
        }

        if (listOf_roomHeaders != null){
            //проверка на уже существующие имена
            int size = listOf_roomHeaders.GetSize();
            int i; int count = 1;
            for (i = 0; i < size; i++){
                if (listOf_roomHeaders.getHeader(i) != null){
                    if ((name + " *" + count).equals(listOf_roomHeaders.getHeader(i).getRoomName())) {
                        count++;
                        i = 0;
                    }
                }
            }
            name = name + " *" + count;
            RoomHeader newRoom = new RoomHeader(name,isPassworded);
            if (newRoom.isPassword()) newRoom.setPasswordValue(password);
            listOf_roomHeaders.add(newRoom);
            refDataBase.child("chat_room_headers").setValue(listOf_roomHeaders);
            refDataBase.child("chat_rooms").child(name).child("owner").setValue(MyUsername);
        }
    }

    private void drawRoom(final String name, String info1, int numberOfRoom){
        //главная область, в которую будут добавляться карточки комнат
        final LinearLayout mainLayout = findViewById(R.id.mainRoomListLayout);

        //инициализация параметров стилей текстовых полей
        TextView newTitle = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.room_title_roomname_text_style, null);
        TextView newInfo1 = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.room_title_roominfo_text_style, null);
        initTextLabel(name, newTitle);
        initTextLabel(info1, newInfo1);
        newTitle.setId(numberOfRoom+1);
        newInfo1.setId(numberOfRoom+2);

        //инициализация поля карточки комнаты
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin = (float) 30*dp;
        float rightMargin = (float) 30*dp;
        float topMargin = (float) 5*dp;
        float bottomMargin = (float) 5*dp;

        LinearLayout newLayout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.room_title, null);
        newLayout.setId(numberOfRoom);
        LinearLayout.LayoutParams lpParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lpParams.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        newLayout.setLayoutParams(lpParams);

        View.OnClickListener newListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RoomHeader roomHeader = listOf_roomHeaders.findHeaderByName(name);
                if (roomHeader != null){
                    if (roomHeader.isPassword()){
                        //ввод пароля
                        enterPassword(roomHeader, name);
                    }
                    else{
                        //переход в комнату
                        refDataBase.removeEventListener(postListener3);
                        //TextView text = findViewById((int)view.getId() + 1);
                        int num = listOf_roomHeaders.findNumberByName(name);
                        Intent intent = new Intent(RoomsListActivity.this,ChatActivity.class);
                        intent.putExtra("roomName",name);
                        intent.putExtra("number",num);
                        startActivity(intent);
                    }
                }
            }
        };

        //инициализация слушателей поля карточки
        newLayout.setOnClickListener(newListener);

        //добавление текстовых полей в поле карточки
        newLayout.addView(newTitle);
        newLayout.addView(newInfo1);
        //добавление карточки комнаты в главное поле
        mainLayout.addView(newLayout);
    }

    private void RemoveAllRooms(){
        LinearLayout mainRoomSpace = findViewById(R.id.mainRoomListLayout);
        mainRoomSpace.removeAllViewsInLayout();
        roomCount = 10000;
    }

    private void initTextLabel(String Text, TextView NewTextLabel){
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;
        float leftMargin = (float) 30*dp;
        float rightMargin = (float) 30*dp;
        float topMargin = (float) 0*dp;
        float bottomMargin = (float) 0*dp;

        LinearLayout.LayoutParams lpParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT );
        lpParams.setMargins((int)leftMargin,(int)topMargin,(int)rightMargin,(int)bottomMargin);
        NewTextLabel.setText(Text);
        NewTextLabel.setLayoutParams(lpParams);
    }

    private void enterPassword(final RoomHeader roomHeader, final String name){
        //создание диалогового окна с вводом пароля
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.room_enter_password_dialog,
                (ViewGroup)findViewById(R.id.enterPassword_Room_Dialog));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        final EditText roomPasswordEdit = layout.findViewById(R.id.enter_password_edit);

        builder.setMessage(R.string.create_room)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Пользователь ввел пароль и вошел
                        String enteredPassword = roomPasswordEdit.getText().toString();
                        boolean enteredPassword_isCorrect = roomHeader.enteredPasswordIsCorrect(enteredPassword);
                        if (enteredPassword_isCorrect){
                            //вход в комнату
                            refDataBase.removeEventListener(postListener3);
                            //TextView text = findViewById((int)view.getId() + 1);
                            int num = listOf_roomHeaders.findNumberByName(name);
                            Intent intent = new Intent(RoomsListActivity.this,ChatActivity.class);
                            intent.putExtra("roomName",name);
                            intent.putExtra("number",num);
                            startActivity(intent);
                        }
                        else{
                            //пароль некорректен
                            incorrectPasswordMessage();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.show();
    }

    private void incorrectPasswordMessage(){
        //создание диалогового окна с сообщением об ошибке в пароле
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomsListActivity.this);
        builder.setTitle("Ошибка")
                .setMessage("Неверный пароль.")
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void incorrectNewRoomMessage(String text){
        //создание диалогового окна с сообщением об ошибке в пароле
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomsListActivity.this);
        builder.setTitle("Ошибка")
                .setMessage(text)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean passwordIsCorrect(String password){
        boolean check = true;
        int i;
        //if ((password.length() >= 6)){
            for (i = 0; i < password.length(); i++){
                //отказ, если содержится символ не латинского алфавита или не цифра
                if ((password.codePointAt(i) < 33)
                )
                {
                    check = false;
                }
            }
        //}
        //else {check = false;}
        return check;
    }

    private boolean nameIsCorrect(String name){
        boolean check = true;
        int i;
        if ((name.length() >= 3)){
            for (i = 0; i < name.length(); i++){
                //отказ, если содержится специальный символ
                if ((name.codePointAt(i) < 48)&&(name.codePointAt(i) != 32)
                        ||((name.codePointAt(i) >= 58)&&(name.codePointAt(i) <= 64))
                        ||((name.codePointAt(i) >= 91)&&(name.codePointAt(i) <= 96))
                )
                {
                    check = false;
                }
            }
        }
        else {check = false;}

        return check;
    }
}