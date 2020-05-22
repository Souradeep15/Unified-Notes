package com.flash.apps.noted.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class checklist_layout extends AppCompatActivity {
    String book, noteId;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNotesDatabase;
    private List<String> key = new ArrayList<>();
    private ArrayList<String> mTitle = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_page);
        fAuth = FirebaseAuth.getInstance();
        ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        Intent intent = getIntent();
        book = intent.getStringExtra("bookTitle");

        initChecklist();
    }

    public void initChecklist() {
        if (book != null) {

            ourNotesDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    key.clear();
                    mTitle.clear();
                    //Intent newIntent = new Intent(this, CreateNote.class);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //here is your every postSystem.out.println("hello20"+ snapshot.getValue().toString());
                        if (snapshot.hasChild("Title") &&
                                (snapshot.child("NotebookName").getValue().toString().equals(book)) &&
                                snapshot.child("Archive").getValue().toString().equals("") &&
                                snapshot.child("Type").getValue().toString().equals("list")) {
                            mTitle.add(snapshot.child("Title").getValue().toString());

                            String id = snapshot.getKey();
                            key.add(id);
                            initRecyclerView();
                            //System.out.println(mTitle);
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

    private void initRecyclerView() {
        //Log.d(TAG, "initRecyclerView: init_recyclerview");
        final RecyclerView recyclerView = findViewById(R.id.main_notes_list);
        final RecyclerViewAdapterChecklistLayout adapter = new RecyclerViewAdapterChecklistLayout(mTitle, this, book, key);
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
        System.out.println("gridView" + key);
        System.out.println("gridView1" + noteId);
        ourNotesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(GridView.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    noteId = "";
                } else {
                    Log.e("NewNoteActivity", task.getException().toString());
                    Toast.makeText(checklist_layout.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void create_list(String title) {
        final DatabaseReference newNoteRef = ourNotesDatabase.push();
        final Map noteMap = new HashMap();
        noteMap.put("Type", "list");
        noteMap.put("NotebookName", book);
        noteMap.put("Title", title);
        noteMap.put("Timestamp", ServerValue.TIMESTAMP);
        noteMap.put("Archive", "");
        newNoteRef.setValue(noteMap);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_note_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_new_note_btn:

                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View promptsView = layoutInflater.inflate(R.layout.checklist_title, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setView(promptsView);
                final EditText input = promptsView.findViewById(R.id.editTextChecklistUserInput);

                alert.setCancelable(false).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        create_list(input.getText().toString());
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alert.create();

                // show it
                alertDialog.show();

                break;
        }
        return true;
    }
}

