package com.example.library;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<ListData> {

    private FirebaseAuth firebaseAuth;
    String userEmail;
    public ListAdapter(@NonNull Context context, ArrayList<ListData> dataArrayList) {
        super(context,R.layout.list_item,dataArrayList);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ListData listData = getItem(position);

        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }

        ImageView listImage = view.findViewById(R.id.listImage);
        TextView listTitle = view.findViewById(R.id.listTitle);
        TextView listAuthor = view.findViewById(R.id.listAuthor);
        ImageButton deleteButton = view.findViewById(R.id.delete_book);


        assert listData != null;
        Picasso.get().load(listData.url_image).into(listImage);
        listTitle.setText(listData.t_book);
        listAuthor.setText(listData.n_author);

        //get curent user
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        userEmail = null;

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        // Crează și afișează dialogul
        DeleteConfirmationDialog deleteDialog = new DeleteConfirmationDialog(
                getContext(),
                "Confirm delete",
                "Are you sure to delete this book?",
                "Delete",
                "Close"
        );



        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get elements from item (title, name_author)
                String title = listTitle.getText().toString();
                String author = listAuthor.getText().toString();
                Log.d("Delete status", "title=" + title + " author=" + author);

                remove(listData);
                notifyDataSetChanged(); // data is changing

                // code for delete inserțion from Firebase Table Books
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");

                // check in firebase
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String bookTitle = childSnapshot.child("Title_book").getValue(String.class);
                            String bookAuthor = childSnapshot.child("Author_name").getValue(String.class);
                            String bookUserEmail = childSnapshot.child("Email").getValue(String.class);

                            // check if user exist in our data base
                            if (title.equals(bookTitle) && author.equals(bookAuthor) && userEmail.equals(bookUserEmail)) {
                                // delete node
                                childSnapshot.getRef().removeValue();
                                Log.d("FirebaseDelete", "Șters din Firebase: " + bookTitle);
                                break; // Ieșim din buclă, avem potrivire
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("FirebaseDelete", "Error delete: " + error.getMessage());
                    }
                });
            }
        });




        return  view;
    }
}
