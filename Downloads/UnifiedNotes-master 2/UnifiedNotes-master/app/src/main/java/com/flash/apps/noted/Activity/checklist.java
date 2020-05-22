package com.flash.apps.noted.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class checklist extends AppCompatActivity {

    private EditText etTitle, etContent;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNotesDatabase;
    String check_title;
    String check_content;
    String noteID,stuff,id;
    ArrayList<String> checked = new ArrayList<>();
    private List<String> key = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    boolean isExist;
    int i;
    String noteId;
    boolean checkedStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Intent intent = getIntent();
        noteID = intent.getStringExtra("noteId");
        //etTitle = findViewById(R.id.etChecklistTitle);
        etContent = findViewById(R.id.etchecklistContent);
        fAuth = FirebaseAuth.getInstance();
        ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid()).child(noteID);


        etContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String temp = etContent.getText().toString();

                    if (temp.length() > 0) {
                        createList( temp);
                        initNotes();
                    }
                }
                etContent.setText("");
                return false;
            }
        });
        initNotes();
    }

    public void initNotes() {

            ourNotesDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    key.clear();
                    mContent.clear();
                    checked.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //here is your every postSystem.out.println("hello20"+ snapshot.getValue().toString());
                        if (snapshot.hasChild("Content")) {
                            mContent.add(snapshot.child("Content").getValue().toString());
                            checked.add(snapshot.child("Checked").getValue().toString());
                            id = snapshot.getKey();
                            System.out.println("hello24" +mContent);
                            key.add(id);
                            initRecycle();

                        }
                        //else{continue;}


                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }

    public void initRecycle() {
        final RecyclerView recyclerView = findViewById(R.id.checklist_notes_list);
        final RecyclerViewAdapterChecklist adapter = new RecyclerViewAdapterChecklist(mContent,checked,noteID,this, key);
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
        System.out.println("hello48 "+noteID);
        System.out.println("hello48 "+noteId);
        ourNotesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(GridView.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    noteId = "";
                } else {
                    Log.e("NewNoteActivity", task.getException().toString());
                    Toast.makeText(checklist.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void createList( String content) {

        final DatabaseReference newNoteRef = ourNotesDatabase.push();
        // noteID = newNoteRef.getKey();
        final Map noteMap = new HashMap();
        noteMap.put("Content", content);
        noteMap.put("Checked","");
        //noteMap.put("Image", upload);

        newNoteRef.setValue(noteMap);
        Map updateMap = new HashMap();
        //updateMap.put("NotebookName",  );
        updateMap.put("Timestamp", ServerValue.TIMESTAMP);
        //updateMap.put("Image", upload);

        ourNotesDatabase.updateChildren(updateMap);


    }
}
