package com.example.library;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class FireBaseQueryTask extends AsyncTask<String, Void, ArrayList<ListData>> {
    private DatabaseReference databaseReference;
    private OnQueryCompleteListener listener;

    public interface OnQueryCompleteListener {
        void onQueryComplete(ArrayList<ListData> resultList);
    }

    public FireBaseQueryTask(OnQueryCompleteListener listener) {
        this.listener = listener;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Books");
    }

    @Override
    protected ArrayList<ListData> doInBackground(String... emails) {
        ArrayList<ListData> resultList = new ArrayList<>();
        String currentUserEmail = emails[0];

        Query query = databaseReference.orderByChild("Email").equalTo(currentUserEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String title = snapshot.child("Title_book").getValue(String.class);
                        String author = snapshot.child("Author_name ").getValue(String.class);
                        String description = snapshot.child("Description").getValue(String.class);
                        String imageUrl = snapshot.child("Url_icon").getValue(String.class);

                        ListData data = new ListData(title, author, currentUserEmail, description, imageUrl);
                        resultList.add(data);
                    }
                }

                if (listener != null) {
                    listener.onQueryComplete(resultList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Log.e("FirebaseQuery", "Error: " + databaseError.getMessage());
            }
        });

        return resultList;
    }
}
