package com.example.busyatra_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User_Account_Reg_Activity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseReference;

    String num;
    String uid;
    UserInfo DDUserInfo;

    EditText et1, et2, et3;

    Button b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account_reg);

        firebaseDatabase = FirebaseDatabase.getInstance();

        et1 =  findViewById(R.id.reg_name);
        et2 =  findViewById(R.id.reg_age);
        et3 =  findViewById(R.id.reg_city);
        b1  =  findViewById(R.id.reg_submit_bt);


        try {
            Bundle rs = getIntent().getExtras();
            uid = rs.getString("uid");
            num = rs.getString("num");
            String ref = "UserData/" + uid;

            DDUserInfo = new UserInfo();
            databaseReference = firebaseDatabase.getReference(ref);

        } catch (Exception e) {
            Toast.makeText(User_Account_Reg_Activity.this, "UID receiver Error", Toast.LENGTH_SHORT).show();
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et1.getText().toString();
                String age = et2.getText().toString();
                String city = et3.getText().toString();

                if (name.equals("") || age.equals("") || city.equals("")) {
                    Toast.makeText(User_Account_Reg_Activity.this, "Please Fill all the fields", Toast.LENGTH_SHORT).show();
                } else {

                    String t = "DRIVER";
                    addDatatoFirebase(name, age, city, t);
                }
            }
        });
    }

    private void addDatatoFirebase(String name, String Age, String City, String type) {

        DDUserInfo.setUserName(name);
        DDUserInfo.setUserAge(Age);
        DDUserInfo.setUserCity(City);
        DDUserInfo.setUserType(type);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    databaseReference.setValue(DDUserInfo);

                    Toast.makeText(User_Account_Reg_Activity.this, "Registration Completed", Toast.LENGTH_SHORT).show();
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("UserData", 0);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putString("UNAME" ,DDUserInfo.getUserName());
                    editor.putString("Unum" ,num);
                    editor.apply();
                    Intent i = new Intent(User_Account_Reg_Activity.this, Load_Activity.class);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(User_Account_Reg_Activity.this, "User Exists", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(User_Account_Reg_Activity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}