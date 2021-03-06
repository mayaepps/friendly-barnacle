package com.example.localink.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.localink.Models.LocalInkUser;
import com.example.localink.Utils.GeocoderUtils;
import com.example.localink.Utils.ImageUtils;
import com.example.localink.databinding.ActivityEditBookstoreProfileBinding;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class EditBookstoreProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditBookstoreActivity";
    ActivityEditBookstoreProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        binding = ActivityEditBookstoreProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);

        Intent i = getIntent();
        final LocalInkUser user = Parcels.unwrap(i.getParcelableExtra(ParseUser.class.getSimpleName()));

        binding.etStreetAddress.setText(user.getAddress());
        binding.etName.setText(user.getName());
        binding.etWebsite.setText(user.getWebsite());
        ParseFile profileImage = user.getProfileImage();
        if (profileImage != null) {
            Glide.with(this).load(profileImage.getUrl()).circleCrop().into(binding.ivProfileImage);
        }

        binding.fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToParse(user);

            }
        });

        binding.btnChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUtils.launchCamera(EditBookstoreProfileActivity.this, "bookstorePhoto.jpg");
            }
        });
    }

    private void saveToParse(LocalInkUser user) {
        // Get the new info from the views
        String name = binding.etName.getText().toString();
        String address = binding.etStreetAddress.getText().toString();
        String website = binding.etWebsite.getText().toString();

        user.setAddress(address);
        user.setName(name);
        user.setWebsite(website);
        if (ImageUtils.getPhotoFile() != null) {
            user.setProfileImage(new ParseFile(ImageUtils.getPhotoFile()));
        }
        ParseGeoPoint point = GeocoderUtils.getGeoLocationFromAddress(this, user.getAddress());
        if (point != null) {
            user.setGeoLocation(point);
        } else {
            return;
        }

        user.getUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error saving profile changes to Parse: " + e.getMessage(), e);
                } else {
                    finish();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When the camera activity comes back with the profile image
        if (requestCode == ImageUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(ImageUtils.getPhotoFile().getAbsolutePath());
                // TODO: compress/shrink the file so it will take less time loading to/from Parse
                // Load the taken image into a preview 
                binding.ivProfileImage.setImageBitmap(takenImage);

                Log.i(TAG, "Image successfully saved in the file provider");

            } else {
                // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}