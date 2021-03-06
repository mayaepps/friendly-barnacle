package com.example.localink.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.localink.Models.LocalInkUser;
import com.example.localink.Utils.GeocoderUtils;
import com.example.localink.databinding.ActivityRegisterBookstoreBinding;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterBookstoreActivity extends AppCompatActivity {

    ActivityRegisterBookstoreBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // view binding
        binding = ActivityRegisterBookstoreBinding.inflate(getLayoutInflater());;
        View view = binding.getRoot();
        setContentView(view);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {

        ParseUser user = new ParseUser();
        final LocalInkUser newUser = new LocalInkUser(user);

        //Set required fields
        user.setUsername(binding.etUsername.getText().toString());
        user.setPassword(binding.etPassword.getText().toString());

        // Set new user's custom values from views
        newUser.setName(binding.etName.getText().toString());
        newUser.setAddress(binding.etStreetAddress.getText().toString() + ", "
                + binding.etCity.getText().toString() + ", " + binding.etState.getText().toString() + " "
                + binding.etZipCode.getText().toString());
        newUser.setIsBookstore(true);
        newUser.setWebsite(binding.etWebsite.getText().toString());
        ParseGeoPoint point = GeocoderUtils.getGeoLocationFromAddress(this, newUser.getAddress());
        if (point != null) {
            newUser.setGeoLocation(point);
        } else {
            return;
        }

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    // Sign up didn't succeed
                    Toast.makeText(RegisterBookstoreActivity.this, "Could not sign up new user: "
                            + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // Sign up successful, go to login activity
                    Intent i = new Intent(RegisterBookstoreActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}