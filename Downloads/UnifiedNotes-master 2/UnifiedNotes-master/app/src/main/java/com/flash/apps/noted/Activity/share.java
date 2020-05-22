package com.flash.apps.noted.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.flash.apps.noted.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class share  extends AppCompatActivity {
    private EditText et_to,et_sub,et_msg;
    private Button send_btn;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNotesDatabase;
    String noteID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_share);
        et_to= findViewById(R.id.et_to);
        et_sub= findViewById( R.id.et_sub );
        et_msg= findViewById( R.id.et_msg );
        send_btn= findViewById( R.id.id_btn );
        Intent intent = getIntent();
        noteID = intent.getStringExtra("noteId");
        fAuth = FirebaseAuth.getInstance();
        ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid()).child(noteID);
        ourNotesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String title = dataSnapshot.child("Title").getValue().toString();
                final String content = dataSnapshot.child("Content").getValue().toString();
                et_sub.setText(title);
                et_msg.setText(content);
                send_btn.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String recipientList = et_to.getText().toString();
                        String[] recipients = recipientList.split(",");

                        String subject=title;
                        String msg =content;

                        Intent intent = new Intent( Intent.ACTION_SEND );
                        intent.putExtra( Intent.EXTRA_EMAIL,recipients );
                        intent.putExtra( Intent.EXTRA_SUBJECT,subject );
                        intent.putExtra( Intent.EXTRA_TEXT,msg);

                        intent.setType( "meassage/rfc822" );
                        startActivity( Intent.createChooser( intent,"choose an email client" ) );
                    }
                } );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
