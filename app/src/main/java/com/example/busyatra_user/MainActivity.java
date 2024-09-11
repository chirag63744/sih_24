package com.example.busyatra_user;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.FirebaseApp;


// implements onClickListener for the onclick behaviour of button
public class MainActivity extends AppCompatActivity{
    Button b1, b2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.button);
       // b2 = (Button) findViewById(R.id.button2);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("UserData", 0);
        String name = settings.getString("UNAME", "");

        if(name.equals("")) {
            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
        }else {
            Intent i = new Intent(MainActivity.this, Home_Activity.class);
            startActivity(i);
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Welcome_Activity.class);
                startActivity(i);
            }
        });

        /*b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Sign_up_Activity.class);
                startActivity(i);
            }
        });*/


    }
}