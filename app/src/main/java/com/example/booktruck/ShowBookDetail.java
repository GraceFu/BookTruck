package com.example.booktruck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktruck.services.BookService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class ShowBookDetail extends AppCompatActivity {

    TextView authorText, statusText, ownerText, titleText, ISBNView;
    String titleContent, authorContent, statusContent, ownerContent;
    FirebaseFirestore db;
    Button editBtn, deleteBtn;
    String ISBN;

    DocumentReference bookRef;
    CollectionReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail);

        //显示的书必须满足条件
        //必需是owner拥有的书
        titleText = findViewById(R.id.titleView);
        authorText = findViewById(R.id.authorView);
        statusText = findViewById(R.id.statusView);
        ownerText = findViewById(R.id.ownerView);
        ISBNView = findViewById(R.id.ISBNView);

        Intent gotoBook = getIntent();
        ISBN = gotoBook.getStringExtra("ISBN");
        Log.i("RECICVE_ISBN",ISBN);

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");
        bookRef = db.collection("Books").document(ISBN);

        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        getSupportActionBar().setTitle(titleContent);
                        titleText.setText("Title: "+ data.get("title").toString());
                        authorText.setText("Author: "+ data.get("author").toString());
                        statusText.setText("Status: "+ data.get("status").toString());
                        ownerText.setText("Owner: "+ data.get("owner").toString());
                        ISBNView.setText("ISBN: "+ data.get("ISBN").toString());
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });


        String parentClass = String.valueOf(getIntent().getStringExtra("ParentClass"));

        if(parentClass.equalsIgnoreCase("Receive")) {

            Button button = (Button) findViewById(R.id.confirmButton);
            button.setText(R.string.confirm_receiving);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // when borrower receive a book
                    // set borrower to current user
                    setBorrower();
                    // add book to current user's borrowed list
                    addBookToBorrowedList();
                    // mark book un available
                    setStatusToBorrowed();
                    // remove book from accepted
                    deleteBookFromAcceptedList();
                }
            });

        }
        else if(parentClass.equalsIgnoreCase("ViewBook")) {

        }else if(parentClass.equalsIgnoreCase("MyBookList")) {
            editBtn = findViewById(R.id.editButton);
            deleteBtn = findViewById(R.id.deleteButton);
            editBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        }
        else { // parentClass == "HandOver"
            Button button = (Button) findViewById(R.id.confirmButton);
            button.setText(R.string.confirm_handover);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatusToHandOvered();
                }
            });
        }
    }

    public String getCurrentUsername() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String username = "";
        String[] array = email.split("@");
        for (int i=0; i<array.length-1; i++) {
            username += array[i];
        }
        return username;
    }
    public void setBorrower() {
        DocumentReference bookRef = db.collection("Books").document(ISBN);
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        data.put("borrower",getCurrentUsername());
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }
    public void setBorrowerToEmpty(){
        DocumentReference bookRef = db.collection("Books").document(ISBN);
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        data.put("borrower","");
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }


    public void deleteBookFromOwnedList() {
        DocumentReference userRef = this.userRef.document(getCurrentUsername());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        ArrayList<String> ownedList = (ArrayList<String>) data.get("owned");
                        ownedList.remove(ISBN);
                        userRef.set(data);
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }


    public void deleteBookFromAcceptedList() {
        DocumentReference userRef = this.userRef.document(getCurrentUsername());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        ArrayList<String> acceptedList = (ArrayList<String>) data.get("accepted");
                        acceptedList.remove(ISBN);
                        userRef.set(data);
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }


    public void onBookDetailDelete (View view) {
        bookRef.delete();
        deleteBookFromOwnedList();
        Intent gotoDestination = new Intent(this, MyBookList.class);
        startActivity(gotoDestination);

    }

    public void onBookDetailEdit(View view){
        Intent gotoDestination = new Intent(this, EditBook.class);
        gotoDestination.putExtra("ISBN", ISBN);
        startActivity(gotoDestination);
    }


    public void setStatusToAvailable(){
        DocumentReference bookRef = db.collection("Books").document(ISBN);
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        data.put("status","available");
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }


    public void setStatusToHandOvered(){
        DocumentReference bookRef = db.collection("Books").document(ISBN);
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        data.put("status","handovered");
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }

    public void setStatusToBorrowed(){
        DocumentReference bookRef = db.collection("Books").document(ISBN);
        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("GET_BOOK_BY_ISBN", "DocumentSnapshot data: " + document.getData().get("title").toString());
                        Map<String, Object> data = document.getData();
                        data.put("status","Borrowed");
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }

    public void deleteBookFromBorrowedList() {
        DocumentReference userRef = this.userRef.document(getCurrentUsername());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        ArrayList<String> borrowedList = (ArrayList<String>) data.get("borrowed");
                        borrowedList.remove(ISBN);
                        userRef.set(data);
                    } else {
                        Log.d("GET_BOOK_BY_ISBN", "No such document");
                    }
                } else {
                    Log.d("GET_BOOK_BY_ISBN", "get failed with ", task.getException());
                }
            }
        });
    }
    public void addBookToBorrowedList() {
        DocumentReference userRef = this.userRef.document(getCurrentUsername());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        ArrayList<String> borrowedList = (ArrayList<String>) data.get("borrowed");
                        borrowedList.add(ISBN);
                        userRef.set(data);
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
