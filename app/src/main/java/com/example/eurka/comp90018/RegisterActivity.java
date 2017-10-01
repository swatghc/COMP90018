package com.example.eurka.comp90018;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText mUserText;
    private EditText mPasswordText;
    private EditText mConPasswordText;
    private EditText mEmeContact;
    private DatabaseAdapter databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseAdapter(this);
        databaseHelper.open();
        setContentView(R.layout.activity_register);

        mUserText = (EditText)findViewById(R.id.userNameAuto);
        mPasswordText = (EditText) findViewById(R.id.password);
        mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConPasswordText = (EditText) findViewById(R.id.conpasswordET);
        mConPasswordText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mEmeContact = (EditText)findViewById(R.id.emergencyContact);

        Button confirmButton = (Button) findViewById(R.id.regBT);
        Button cancelButton = (Button) findViewById(R.id.canBT);



        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserText.getText().toString();
                String password = mPasswordText.getText().toString();
                String conPassword = mConPasswordText.getText().toString();
                String emeContact = mEmeContact.getText().toString();

                if((username == null||username.equalsIgnoreCase("")) || (password == null||password.equalsIgnoreCase("")) || (conPassword == null||conPassword.equalsIgnoreCase(""))){
                    Toast.makeText(RegisterActivity.this,"must input account name and password!.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Cursor cursor = databaseHelper.getDiary(username);
                    if(cursor.moveToFirst()){
                        Toast.makeText(RegisterActivity.this,"account name already exists!",
                                Toast.LENGTH_SHORT).show();
                    }else if (!password.equals(conPassword)) {
                        Toast.makeText(RegisterActivity.this, "two passwords inconsistent",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        databaseHelper.createUser(username, password,emeContact);
                        Toast.makeText(RegisterActivity.this, "register success!",
                                Toast.LENGTH_SHORT).show();

//                        Intent intent = new Intent();
//                        intent.setClass(RegisterActivity.this, MainActivity.class);
//                        intent.putExtra("emergencycontact",emeContact);
//                        RegisterActivity.this.setResult(RESULT_OK,intent);
//                        RegisterActivity.this.finish();
                        Intent intent = new Intent();
                        intent.setClass(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });



    }
}
