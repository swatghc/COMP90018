package com.example.eurka.comp90018;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText accountEditText;
    EditText passwordEditText;
    Button loginButton;
    Button registerButton;
    CheckBox savePasswordCheckBox;
    private final static String TAG="MainActivity";
    SharedPreferences sp;
    String accountStr;
    String passwordStr;
    private DatabaseAdapter mDbHelper;
    String emergencycontact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button bindings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        accountEditText = (EditText)findViewById(R.id.mainAccount);
        passwordEditText = (EditText)findViewById(R.id.mainPassword);
        loginButton = (Button)findViewById(R.id.main_bt1);
        registerButton = (Button)findViewById(R.id.main_btn2);
        savePasswordCheckBox = (CheckBox)findViewById(R.id.savePasswordCB);
        sp = this.getSharedPreferences("passwordFile",MODE_PRIVATE);
        savePasswordCheckBox.setChecked(true);
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //get emergency contact from registerActivity


//        Intent intent1 = this.getIntent();
//        Bundle bundle = this.getIntent().getExtras();
//        emergencycontact = bundle.getString("emergencycontact");






        //login
        loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                accountStr = accountEditText.getText().toString();
                passwordStr = passwordEditText.getText().toString();

                if((accountStr == null||accountStr.equalsIgnoreCase("")) || (passwordStr == null||passwordStr.equalsIgnoreCase(""))){
                    Toast.makeText(MainActivity.this, "must input account name and password.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Cursor cursor = mDbHelper.getDiary(accountStr);
                    Log.i("hello",cursor.toString()+" "+accountStr);

                    if(!cursor.moveToFirst()){
                        Log.i("hello","!!!!!!!");
                        Toast.makeText(MainActivity.this, "account doesn't exist.",
                                Toast.LENGTH_SHORT).show();
                    }else if (!passwordStr.equals(cursor.getString(2))) {
                        Toast.makeText(MainActivity.this, "Wrong password.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (savePasswordCheckBox.isChecked()) {
                            //once login successful,then can save password
                            sp.edit().putString(accountStr, passwordStr).commit();
                        }
                        Toast.makeText(MainActivity.this, "Login successful！",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
//                        Intent.putExtra("username",accountStr);
                        intent.setClass(MainActivity.this, UserPageActivity.class);
//                        intent.setClass(MainActivity.this, TrackingActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("username",accountStr);
                        //bundle1.putString("emergencycontact",emergencycontact);
                        intent.putExtras(bundle1);
                        startActivity(intent);
                    }
                }

            }
        });

        //开启数据库
        mDbHelper = new DatabaseAdapter(this);
        mDbHelper.open();


        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent,1);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    //this is a comment to test the githubLLLLLLLL

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        emergencycontact = data.getExtras().getString("emergencycontact");//得到新Activity 关闭后返回的数据
//        Log.i(TAG, emergencycontact);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
