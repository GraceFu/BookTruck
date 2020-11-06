package com.example.booktruck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktruck.models.Book;
import com.example.booktruck.models.User;
import com.example.booktruck.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class ViewBook extends AppCompatActivity {

    private ListView bookListView;
    private FirebaseFirestore db;
    private ArrayList<String> bookISBN = new ArrayList<>();
    private ArrayList<String> bookArray = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    public String getCurrentUsername() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String username = "";
        String[] array = email.split("@");
        for (int i=0; i<array.length-1; i++) {
            username += array[i];
        }
        return username;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_book_layout);
        db = FirebaseFirestore.getInstance();
        bookListView = findViewById(R.id.view_book_list);

        DocumentReference docRef = db.collection("Users").document(getCurrentUsername());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists() && document.getData().containsKey("borrowed")) {
                        ArrayList<String> list = (ArrayList<String>) document.getData().get("borrowed");
                        for (int i=0; i<list.size(); i++) {
                            String ISBN = list.get(i);
                            DocumentReference bookRef = db.collection("Books").document(ISBN);
                            int finalI = i;
                            bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                                            Map<String, Object> data = document.getData();
                                            bookArray.add(data.get("title").toString());
                                            bookISBN.add(ISBN);
                                            if (finalI == list.size()-1) {
                                                showBooks();
                                            }
                                        } else {
                                            Log.d("GET_BOOK_BY_ISBN", "No such document");
                                        }
                                    } else {
                                        Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    protected void showBooks() {
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.content, bookArray);
        bookListView.setAdapter(arrayAdapter);
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent bookDetail = new Intent(ViewBook.this, ShowBookDetail.class);
                bookDetail.putExtra("ParentClass", "ViewBook");
                bookDetail.putExtra("ISBN", bookISBN.get(position));
                startActivity(bookDetail);
            }
        });
    }
}
