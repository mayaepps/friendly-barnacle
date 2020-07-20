package com.example.localink.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.localink.Adapters.BooksAdapter;
import com.example.localink.BookDetailsActivity;
import com.example.localink.Models.Book;
import com.example.localink.Models.LocalInkUser;
import com.example.localink.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class BookshelfFragment extends Fragment {

    private static final String TAG = "BookshelfFragment";
    private RecyclerView rvBooks;
    private BooksAdapter adapter;
    private List<Book> storeBooks;

    public BookshelfFragment() {
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
        return inflater.inflate(R.layout.fragment_bookshelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Instantiate my OnClickListener from the interface in BooksAdapter
        BooksAdapter.OnClickListener clickListener = new BooksAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                return;
            }

            // Delete book from Parse if it is long clicked
            @Override
            public void onLongClick(int position) {
                Book book = storeBooks.get(position);
                // This method would remove all pointers to the removed book from all the wishlists
                // But it doesn't quite work yet
                removeBookfromAllWishlists(book);
                storeBooks.remove(position);
                adapter.notifyItemChanged(position);
                book.deleteInBackground();
                Toast.makeText(getContext(), "Removing " + book.getTitle() + " from bookshelf", Toast.LENGTH_SHORT).show();
            }
        };

        // Set up recycler view with the adapter and linear layout
        rvBooks = view.findViewById(R.id.rvBooks);
        storeBooks = new ArrayList<>(); // Have to initialize storeBooks before passing it into the adapter
        adapter = new BooksAdapter(getContext(), storeBooks, clickListener);
        rvBooks.setAdapter(adapter);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        queryBooks();

    }

    // Query all the books whose bookstore is the same as the currently logged in bookstore (current user)
    private void queryBooks() {
        ParseUser.getCurrentUser().fetchInBackground();
        LocalInkUser user = new LocalInkUser(ParseUser.getCurrentUser());

        // Create the query for books
        ParseQuery<Book> query = ParseQuery.getQuery(Book.class);

        // Only get the books from this bookstore
        query.whereEqualTo(Book.KEY_BOOKSTORE, user.getUser());

        // Make the query
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

    // Removes all pointers to a specific book from every wishlist
    private synchronized void removeBookfromAllWishlists(final Book book) {
        // specify what type of data to query - Book.class
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.include(LocalInkUser.KEY_WISHLIST);
        // Get only the reader users
        query.whereEqualTo(LocalInkUser.KEY_IS_BOOKSTORE, false);

        // start an asynchronous call for users
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting users: " + e.getMessage(), e);
                    return;
                }
                // For each of the users, check if the book to be removed is in the wishlist
                // If it is, remove the pointer to the removed book
                LocalInkUser localInkUser;
                for (final ParseUser user : users) {
                    localInkUser = new LocalInkUser(user);
                    List<Book> books = localInkUser.getWishlist();
                    Log.i(TAG, user.getUsername() + ": " + books.toString());
                }
            }
        });
    }
}