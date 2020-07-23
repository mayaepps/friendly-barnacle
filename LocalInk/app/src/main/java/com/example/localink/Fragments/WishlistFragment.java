package com.example.localink.Fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.localink.Adapters.BooksAdapter;
import com.example.localink.Models.Book;
import com.example.localink.Models.LocalInkUser;
import com.example.localink.R;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class WishlistFragment extends Fragment {

    private static final String TAG = "WishlistFragment";
    private RecyclerView rvBooks;
    private List<Book> wishlistBooks;
    private BooksAdapter adapter;

    public WishlistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up recycler view with the adapter and linear layout
        rvBooks = view.findViewById(R.id.rvBooks);
        wishlistBooks = new ArrayList<>(); // Have to initialize allBooks before passing it into the adapter
        adapter = new BooksAdapter(getContext(), wishlistBooks, null);
        rvBooks.setAdapter(adapter);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up swipe listener that removes the swiped book
        ItemTouchHelper touchHelper = createTouchHelper();
        touchHelper.attachToRecyclerView(rvBooks);

        getWishlistBooks();
    }

    private void saveNewWishlist(List<Book> wishlist) {
        LocalInkUser localInkUser = new LocalInkUser(ParseUser.getCurrentUser());
        localInkUser.setWishlist(wishlist);
        localInkUser.getUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Log.e(TAG, "Could not save wishlist to Parse: " + e.getMessage(), e);
                } else {
                    Toast.makeText(getContext(), "New wishlist saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Gets the wishlist list stored in ParseUser and save the list to wishlistBooks
    private void getWishlistBooks() {
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    LocalInkUser localInkuser = new LocalInkUser(user);
                    List<Book> wishlist = localInkuser.getWishlist();
                    wishlistBooks.clear();
                    wishlistBooks.addAll(wishlist);
                    checkWishlistBooksExist();
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error getting wishlist: " + e.getMessage());
                }
            }
        });
    }

    // Goes through each of the books in the wishlist and makes sure they still exist in Parse
    // If they do not, it removes the bad book(s) from the wishlist
    private void checkWishlistBooksExist() {
        List<Book> booksToRemove = new ArrayList<>();
        for (int i = 0; i < wishlistBooks.size(); i++) {
            Book book = wishlistBooks.get(i);
            if (book.getTitle() == null) {
                booksToRemove.add(book);
            }
        }

        for (Book book : booksToRemove) {
            removeBookFromWishlist(book);
        }
        adapter.notifyDataSetChanged();
    }

    // Removes a single book from the current wishlist and saves the changes to Parse
    private void removeBookFromWishlist(final Book book) {
        wishlistBooks.remove(book);
        LocalInkUser user = new LocalInkUser(ParseUser.getCurrentUser());
        user.setWishlist(wishlistBooks);
        user.getUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Could not save wishlist in background: " + e.getMessage(), e);
                    return;
                }
                Log.i(TAG, "New wishlist saved to Parse.");

            }
        });
    }

    // Creates and returns a new ItemTouchHelper that listens for left swipes and removes the book that has been swiped
    private ItemTouchHelper createTouchHelper() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // When the book is swiped, remove it from the wishlist, notify the adapter of the change,
            // and save the changes to the list in Parse
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                wishlistBooks.remove(viewHolder.getAdapterPosition());
                saveNewWishlist(wishlistBooks);
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        return helper;
    }
}