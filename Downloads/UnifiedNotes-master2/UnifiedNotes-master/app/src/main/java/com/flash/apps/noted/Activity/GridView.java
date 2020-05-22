package com.flash.apps.noted.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flash.apps.noted.Adapter.RecyclerViewAdapter;
import com.flash.apps.noted.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GridView extends AppCompatActivity {
    private static final String TAG = "GridView";
    private ArrayList<String> mTitle = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    private FirebaseAuth fAuth;
    private ArrayList<String> mTags = new ArrayList<>();
    //public final List<String> key = new ArrayList<>();
    //public final List<String> key1 = new ArrayList<>();
    public String book;
    public String id;
    private DatabaseReference ourNotesDatabase;
    private List<String> key = new ArrayList<>() ;
    private String noteId;
    private EditText editTextSearch;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_page);
        editTextSearch =  findViewById(R.id.searchtext);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString());
            }
        });
        Log.e(TAG, "onCreate: Started");
        initNotes();
    }
    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<String> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (String s : mTitle) {
            //if the existing elements contains the search input
            if (s.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        //calling a method of the adapter class and passing the filtered list
        adapter.filterList(filterdNames);

    }

    private void initNotes(){
        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        }
        Intent intent = getIntent();
        book = intent.getStringExtra("bookName");
        //System.out.println("Key generated "+ourNotesDatabase.getKey());
        if(book!=null) {

            ourNotesDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    key.clear();
                    mTitle.clear();
                    mContent.clear();
                    mTags.clear();
                    //Intent newIntent = new Intent(this, CreateNote.class);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //here is your every postSystem.out.println("hello20"+ snapshot.getValue().toString());
                        if (snapshot.hasChild("Title") && snapshot.hasChild("Content") &&
                                snapshot.child("NotebookName").getValue().toString().equals(book)&&
                                snapshot.child("Archive").getValue().toString().equals("")&&
                                snapshot.child("Type").getValue().toString().equals("note")) {

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
                    System.out.println("hello "+book);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init_recyclerview");
        final RecyclerView recyclerView = findViewById(R.id.main_notes_list);

       adapter = new RecyclerViewAdapter(mTitle,mContent,this, key,mTags);
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
                    Toast.makeText(GridView.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
   /* protected void onResume(){
        Log.e(TAG, "onCreate: Resumed");
        mTitle.clear();
        mContent.clear();
        super.onResume();
    }
    protected void onPause(){
        Log.e(TAG, "onCreate: paused");
        mTitle.clear();
        mContent.clear();
        super.onPause();
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_note_create, menu);
        getMenuInflater().inflate(R.menu.menu_checklist, menu);
        getMenuInflater().inflate(R.menu.menu_archive,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_new_note_btn:

                Intent newIntent = new Intent(this, CreateNote.class);
                Intent intent = getIntent();

                String book = intent.getStringExtra("bookName");
                newIntent.putExtra("bookTitle",book);
                startActivity(newIntent);
                break;
            case R.id.main_checklist_btn:
                Intent checklist_intent = new Intent(this, checklist_layout.class);
                Intent intent2 = getIntent();
                String book2 = intent2.getStringExtra("bookName");
                checklist_intent.putExtra("bookTitle",book2);
                startActivity(checklist_intent);
                break;
            case R.id.main_archive:
                Intent archive_intent = new Intent(this , archive.class);
                Intent intent1 = getIntent();

                String book1 = intent1.getStringExtra("bookName");
                archive_intent.putExtra("bookTitle",book1);
                startActivity(archive_intent);

        }

        return true;
    }


}





