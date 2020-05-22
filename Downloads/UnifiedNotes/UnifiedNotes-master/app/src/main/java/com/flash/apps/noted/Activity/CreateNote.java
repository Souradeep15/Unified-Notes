package com.flash.apps.noted.Activity;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.*;

import com.flash.apps.noted.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


import java.util.HashMap;
import java.util.Map;

public class CreateNote extends AppCompatActivity {

    private EditText etTitle, etContent;
    //private Toolbar mToolbar;
    public FirebaseAuth fAuth;

    String title;
    String content;

    private DatabaseReference ourNoteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note);


            etTitle =  findViewById(R.id.etTitle);
            etContent =  findViewById(R.id.etContent);
            //mToolbar = (Toolbar) findViewById(R.id.new_Note_Toolbar);
            //setSupportActionBar(mToolbar);
            fAuth = FirebaseAuth.getInstance();
            ourNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        }

    @Override
    public void onBackPressed() {

        title = etTitle.getText().toString().trim();
        content = etContent.getText().toString().trim();
        createNote(title, content);
        super.onBackPressed();

    }

        public void createNote(String title, String content) {
            if (fAuth.getCurrentUser() != null) {
            DatabaseReference newNoteRef = ourNoteDatabase.push();
            Map noteMap = new HashMap();
            noteMap.put("Title", title);
            noteMap.put("Content", content);
            noteMap.put("Timestamp", ServerValue.TIMESTAMP);
            newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Snackbar.make(findViewById(R.id.NoteCreation), "Note Added", Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(CreateNote.this, "Error adding to database" , Toast.LENGTH_SHORT).show();
                    }
                }
            });
            } else {
                Toast.makeText(this, "USER IS NOT SIGNED IN", Toast.LENGTH_SHORT).show();
            }
        }





    }









