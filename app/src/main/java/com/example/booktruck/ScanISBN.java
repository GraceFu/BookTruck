package com.example.booktruck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktruck.models.Book;
import com.example.booktruck.models.User;
import com.example.booktruck.services.BookService;
import com.example.booktruck.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.Code;

import java.util.ArrayList;

public class ScanISBN extends AppCompatActivity implements View.OnClickListener {

    private EditText editISBN;
    private Button CodeSender;
    FirebaseFirestore db;
    DocumentReference docRef;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.isbn_receive);

        editISBN = (EditText) findViewById(R.id.ISBNcode);

        CodeSender = (Button) findViewById(R.id.Code_Sender);
        CodeSender.setOnClickListener(this);
    }


    @Override
    public void onClick(final View view) {
        final String ISBN = editISBN.getText().toString();

        String parentClass = String.valueOf(getIntent().getStringExtra("ParentClass"));
        final Intent gotoBook = new Intent(this, ShowBookDetail.class);
        //final Intent errorPage = new Intent(this, BookNotExist.class);
        gotoBook.putExtra("ISBN", ISBN);
        if (parentClass.equalsIgnoreCase("HandOver")) {
            gotoBook.putExtra("ParentClass", "HandOver");
            startActivity(gotoBook);
        }
        else if (parentClass.equalsIgnoreCase("Receive")) {
            gotoBook.putExtra("ParentClass", "Receive");
            startActivity(gotoBook);
        }
        else if (parentClass.equalsIgnoreCase("Return")) {
            db = FirebaseFirestore.getInstance();
            docRef = db.collection("Users").document(getCurrentUsername());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists() && document.getData().containsKey("borrowed")) {
                            final ArrayList<String> list = (ArrayList<String>) document.getData().get("borrowed");
                            if (!list.contains(ISBN)) {
                                Log.d("SCAN_ISBN", "No such document");
                                //startActivity(errorPage);
                            } else {
                                gotoBook.putExtra("ParentClass", "Return");
                                startActivity(gotoBook);
                            }
                        }
                    }
                }
            });
        }
        else if (parentClass.equalsIgnoreCase("ConfirmReturn")) {
            db = FirebaseFirestore.getInstance();
            docRef = db.collection("Users").document(getCurrentUsername());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists() && document.getData().containsKey("owned")) {
                            final ArrayList<String> list = (ArrayList<String>) document.getData().get("owned");
                            if (!list.contains(ISBN)) {
                                Log.d("SCAN_ISBN", "No such document");
                                //startActivity(errorPage);
                            } else {
                                gotoBook.putExtra("ParentClass", "ConfirmReturn");
                                startActivity(gotoBook);
                            }
                        }
                    }
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

}



