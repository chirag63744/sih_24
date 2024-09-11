package com.example.busyatra_user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Welcome_Activity extends AppCompatActivity {
    Button b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        b1 = findViewById(R.id.wlc_bt);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getApplicationContext().getSharedPreferences("UserData", 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("UNAME" ,"Logged");
                editor.apply();
                Intent i = new Intent(Welcome_Activity.this, Load_Activity.class);
                startActivity(i);
            }
        });
    }
}