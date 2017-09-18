package com.example.eurka.comp90018;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserPageActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        final Bundle bundle = this.getIntent().getExtras();
        username = bundle.getString("username");

        Button startTrackBtn = (Button) findViewById(R.id.button);
        Button historyBtn = (Button)findViewById(R.id.button2);


        //start tracking activity
        startTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent();
                intent1.setClass(UserPageActivity.this, TrackingActivity.class);
//                Bundle bundle1 = new Bundle();
//                bundle1.putString("username",username);
//                intent1.putExtras(bundle1);
                intent1.putExtras(bundle);
                startActivity(intent1);

            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent();
                intent2.setClass(UserPageActivity.this, HisrotyRecordActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("username",username);
                intent2.putExtras(bundle2);
                startActivity(intent2);
            }
        });

    }
}
