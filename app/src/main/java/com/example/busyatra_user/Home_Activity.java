package com.example.busyatra_user;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.busyatra_user.databinding.ActivityHomeBinding;

public class Home_Activity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            replaceFragment(new HomeFragment());

            binding.btm.setOnItemSelectedListener(item -> {

                switch (item.getItemId()) {
                    case R.id.home:
                        replaceFragment(new HomeFragment());
                        break;
                    case R.id.history:
                        replaceFragment(new HistoryFragment());
                        break;
                    case R.id.schedule:
                        replaceFragment(new ScheduleFragment());
                        break;
                    case R.id.payment:
                        replaceFragment(new PaymentFragment());
                        break;
                }

                return true;
            });
        } catch (Exception e) {
            Toast.makeText(Home_Activity.this,"Farmer Fragment Error",Toast.LENGTH_SHORT).show();
        }

    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fadein,
                        R.anim.fadeout

                )
                .replace(R.id.Frg_1, fragment)
                .addToBackStack(null)
                .commit();
    }
}