package com.flash.apps.noted.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.Toast;

import com.flash.apps.noted.Adapter.RecyclerViewAdapter;
import com.flash.apps.noted.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class archive extends AppCompatActivity {
    private static final String TAG = "archive";
    private ArrayList<String> mTitle = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    private FirebaseAuth fAuth;
    //public final List<String> key = new ArrayList<>();
    //public final List<String> key1 = new ArrayList<>();
    public String book;
    public String id;
    private DatabaseReference ourNotesDatabase;
    private List<String> key = new ArrayList<>() ;
    private ArrayList<String> mTags = new ArrayList<>();
    private String noteId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_layout);
        Log.e(TAG, "aonCreate: Started");
        initNotes();


    }
    private void initNotes(){
        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        }
        Intent intent = getIntent();
        book = intent.getStringExtra("bookTitle");
        //System.out.println("Key generated "+ourNotesDatabase.getKey());
        if(book!=null) {

            ourNotesDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    key.clear();
                    mTitle.clear();
                    mContent.clear();
                    //Intent newIntent = new Intent(this, CreateNote.class);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //here is your every postSystem.out.println("hello20"+ snapshot.getValue().toString());
                        if (snapshot.hasChild("Title") && snapshot.hasChild("Content") &&
                                (snapshot.child("NotebookName").getValue().toString().equals(book))&&
                                snapshot.child("Archive").getValue().toString().equals("yes")) {
                            mTitle.add(snapshot.child("Title").getValue().toString());
                            mContent.add(snapshot.child("Content").getValue().toString());
                            id = snapshot.getKey();
                            key.add(id);
                            initRecyclerView();
                            //System.out.println(mTitle);
                        }
                        if(snapshot.hasChild("Tags")){
                            mTags.add(snapshot.child("Tags").getValue().toString());
                        }
                        //else{continue;}


                    }
                    //sendKey();
                    //System.out.println("hello"+key);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init_recyclerview");
        final RecyclerView recyclerView = findViewById(R.id.archive_notes_list);
        final RecyclerViewAdapterArchive adapter = new RecyclerViewAdapterArchive(mTitle,mContent,this,book, key,mTags);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                //adapter.delete(viewHolder.getAdapterPosition());
                deleteNote(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

    }
    public void deleteNote(int position) {

        noteId = key.get(position);
        deleteNote();
        //onCreate(null);

    }
    private void deleteNote() {
        System.out.println("gridView"+key);
        System.out.println("gridView1"+noteId);
        ourNotesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(GridView.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    noteId = "";
                } else {
                    Log.e("NewNoteActivity", task.getException().toString());
                    Toast.makeText(archive.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
