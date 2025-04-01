package com.example.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import com.example.library.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SettingFragment extends Fragment implements FireBaseQueryTask.OnQueryCompleteListener {

    private FirebaseAuth firebaseAuth;
    String userEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        userEmail = null;

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            FireBaseQueryTask firebaseQueryTask = new FireBaseQueryTask(this);
            firebaseQueryTask.execute(userEmail);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onQueryComplete(ArrayList<ListData> resultList) {
        // debug Logat
        for (ListData data : resultList) {
            Log.d("FirebaseQuery", "Title: " + data.t_book);
            Log.d("FirebaseQuery", "Author: " + data.n_author);
            Log.d("FirebaseQuery", "Description: " + data.d_book);
            Log.d("FirebaseQuery", "Image URL: " + data.url_image);
        }

        // create own adaptor
        ListAdapter adapter = new ListAdapter(getContext(), resultList);

        // find ListView in layout
        ListView listView = requireView().findViewById(R.id.list_view);

        // set adapter to listView
        listView.setAdapter(adapter);

        // add listiner for each element from list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obține datele elementului selectat
                ListData selectedData = resultList.get(position);


            }
        });
    }



}
