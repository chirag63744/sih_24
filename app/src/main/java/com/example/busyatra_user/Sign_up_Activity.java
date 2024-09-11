package com.example.busyatra_user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Sign_up_Activity extends AppCompatActivity {

    Button b1;
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        b1 = (Button) findViewById(R.id.sign_up_bt);
        et = (EditText) findViewById(R.id.phone_num_et2);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Sign_up_Activity.this, Sign_up_OTP_Activity.class);
                    i.putExtra("number", et.getText().toString());
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(Sign_up_Activity.this, "Next Intent Problem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}