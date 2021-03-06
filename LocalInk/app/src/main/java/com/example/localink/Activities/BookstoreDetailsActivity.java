package com.example.localink.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.localink.Adapters.BooksAdapter;
import com.example.localink.Models.Book;
import com.example.localink.Models.LocalInkUser;
import com.example.localink.R;
import com.example.localink.Utils.MapsUtils;
import com.example.localink.databinding.ActivityBookstoreDetailsBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BookstoreDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookstoreDetailsActivit";
    ActivityBookstoreDetailsBinding binding;
    private LocalInkUser bookstore;
    private List<Book> storeBooks;
    private BooksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        binding = ActivityBookstoreDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);


        // Get the bookstore (ParseUser) to be detailed from the intent
        Intent intent = getIntent();
        bookstore = new LocalInkUser((ParseUser) intent.getParcelableExtra(ParseUser.class.getSimpleName()));

        // Set up recycler view with the books at this store
        storeBooks = new ArrayList<>();
        adapter = new BooksAdapter(this, storeBooks, null);
        binding.rvStoreBooks.setAdapter(adapter);
        binding.rvStoreBooks.setLayoutManager(new LinearLayoutManager(this));

        populateViews();
    }

    // Populate all the views in the layout with this bookstore's info
    private void populateViews() {

        Glide.with(this).load(bookstore.getProfileImage().getUrl()).into(binding.ivStoreProfile);
        binding.btnStoreName.setText(bookstore.getName());
        binding.tvAddress.setText(bookstore.getAddress());
        MapsUtils.startMap(getSupportFragmentManager().beginTransaction(), R.id.bookstoreMapView, bookstore.getUser());

        queryBooksSoldHere();

        // Open a google search for this store when the name is tapped
        binding.btnStoreName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String searchQuery = URLEncoder.encode(bookstore.getName().toString(), "UTF-8");
                    Uri uri = Uri.parse("http://www.google.com/#q=" + searchQuery);

                    // But if the bookstore has set a website, go to that website instead of the google search
                    if (bookstore.getWebsite() != null && !bookstore.getWebsite().isEmpty()) {
                        String url = bookstore.getWebsite();
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        uri = Uri.parse(url);
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                    // Check to make sure it can handle this request before making it
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(BookstoreDetailsActivity.this, "No application can handle this request."
                                + " Please install a web browser", Toast.LENGTH_LONG).show();
                    }

                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Could not encode search query for bookstore: " + bookstore.getName(), e);
                }
            }
        });
    }

    private void queryBooksSoldHere() {

        // Query for books from this bookstore
        ParseQuery<Book> query = ParseQuery.getQuery(Book.class);
        query.whereEqualTo(Book.KEY_BOOKSTORE, bookstore.getUser());
        query.findInBackground(new FindCallback<Book>() {
            @Override
            public void done(List<Book> books, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting books from Parse: " + e.getMessage());
                    return;
                }

                // add to the list of storeBooks and notify adapter
                storeBooks.clear();
                storeBooks.addAll(books);
                adapter.notifyDataSetChanged();
            }
        });
    }


}