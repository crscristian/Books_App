package com.example.library;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;


public class BooksFragment extends Fragment {
    private String userEmail;
    private  View view;
    //here I using cardview as a selectphoto Button
    private MaterialCardView SelectPhoto;
    private ImageView imageView;
    private Uri ImageUri;
    private Bitmap bitmap;
    //add_book button for action to save in firebase data
    private Button add_book;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference mStorage;
    private String photoUrl;

    //info about book
    private EditText title_book, name_author, description_book;

    public BooksFragment(){

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_books,container,false);
        SelectPhoto = view.findViewById(R.id.image_bookMcardView);

        //get info form edit text
        title_book = view.findViewById(R.id.book_titleEt);
        name_author = view.findViewById(R.id.name_authorEt);
        description_book = view.findViewById(R.id.description_bookEt);

        imageView = view.findViewById(R.id.image_bookEt);
        add_book = view.findViewById(R.id.add_bookBtn);

        add_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadeImage();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorage = storage.getReference();

        userEmail = null;
        if (currentUser != null) {
            // get email user
            userEmail = currentUser.getEmail();

            Log.d("FirebaseEmail", "Adresa de email: " + userEmail);
        } else {
            // Nu există utilizator autentificat
            Log.d("FirebaseEmail", "Niciun utilizator autentificat");
        }

        SelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permision
                CheckStoragePermission();
            }
        });

        return view;
    }



    private void CheckStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }else{
                PickImageFromGallery();
            }
        }else{
            PickImageFromGallery();
        }

    }

    private void PickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //now we need to a launcher
        launcher.launch(intent);
    }
    ActivityResultLauncher<Intent>launcher
            =registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
            result->{
                 if(result.getResultCode() == Activity.RESULT_OK){
                     Intent data = result.getData();
                     if(data!=null && data.getData()!=null){
                         ImageUri = data.getData();

                         //now we need to convert our image to Bitmap
                         try{
                             bitmap = MediaStore.Images.Media.getBitmap(
                                     getActivity().getContentResolver(),
                                     ImageUri
                             );
                         }catch (IOException e){
                             e.printStackTrace();
                         }
                     }
                     //now we set image into imageView
                     if(ImageUri!=null){
                         imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                 ViewGroup.LayoutParams.WRAP_CONTENT,
                                 ViewGroup.LayoutParams.WRAP_CONTENT
                         ));
                         //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                         imageView.setImageBitmap(bitmap);
                     }
                 }
    }
    );


    //now I am going to upload image into Firebase Storage in store Image Url into Firebase firestore
    //make a method to UpLoad Image Into Firebase Storage
    private void UploadeImage(){
        //check image
        if(ImageUri != null){
            //create Storage Instance
            final  StorageReference myRef = mStorage.child("photo/"+ImageUri.getLastPathSegment());
            myRef.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //here need to get downloaderUrl to store in String
                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(uri !=null){
                                photoUrl = uri.toString();
                                UploadBookInfo();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT);
                }
            });
        }else{
            Toast.makeText(getContext(),"Please search a image",Toast.LENGTH_SHORT).show();
        }
    }

    //now need to Upload All other Information of book

    private  void UploadBookInfo(){
        String name_book = title_book.getText().toString().trim();
        String name_auth = name_author.getText().toString().trim();
        String desc_book = description_book.getText().toString().trim();
        if(TextUtils.isEmpty(name_book) && TextUtils.isEmpty(name_auth) && TextUtils.isEmpty(desc_book)){

            Toast.makeText(getContext(),"Please fill all fields",Toast.LENGTH_SHORT).show();
        }else{
            //timestamp
            long timestamp = System.currentTimeMillis();
            //setup data to add  in db
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("Email",userEmail);
            hashMap.put("Title_book",name_book);
            hashMap.put("Author_name",name_auth);
            hashMap.put("Description",desc_book);
            hashMap.put("Url_icon",photoUrl);
            //set data to db
            DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Books");
            String bookKey = ref.push().getKey();
            ref.child(bookKey)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //data add to db
                            Toast.makeText(getContext(), "Data is added...", Toast.LENGTH_SHORT).show();
                            title_book.setText("");
                            name_author.setText("");
                            description_book.setText("");
                            //reset image
                            // Replace desiredWidthDp and desiredHeightDp with the desired values in dp
                            int desiredWidthDp = 70; // Set your desired width in dp
                            int desiredHeightDp = 70; // Set your desired height in dp

                            // Convert dp to pixels
                            float scale = getResources().getDisplayMetrics().density;
                            int desiredWidth = (int) (desiredWidthDp * scale + 0.5f);
                            int desiredHeight = (int) (desiredHeightDp * scale + 0.5f);
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                    desiredWidth,
                                    desiredHeight
                            ));
                            int imageResourceId = R.drawable.ic_search_image_gray;
                            imageView.setImageResource(imageResourceId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),"Something gone wrong"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

        }



    }

}
