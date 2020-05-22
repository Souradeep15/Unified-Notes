package com.flash.apps.noted.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.allyants.notifyme.NotifyMe;
import com.flash.apps.noted.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class reminder extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    Calendar now = Calendar.getInstance();
    TimePickerDialog tpd;
    DatePickerDialog dpd;
    EditText etTitle,etContent;
    private FirebaseAuth fAuth;
    private DatabaseReference ourNotesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Button btnNotify = findViewById(R.id.btnNotify);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            ourNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        }
        String noteID = getIntent().getStringExtra("noteId");
        ourNotesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Title") && dataSnapshot.hasChild("Content")) {
                    String title = dataSnapshot.child("Title").getValue().toString();
                    String content = dataSnapshot.child("Content").getValue().toString();

                    etTitle.setText(title);
                    etContent.setText(content);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dpd = DatePickerDialog.newInstance(
                reminder.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        tpd = TimePickerDialog.newInstance(
                reminder.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                false
        );

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        now.set(Calendar.YEAR,year);
        now.set(Calendar.MONTH,monthOfYear);
        now.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        now.set(Calendar.HOUR_OF_DAY,hourOfDay);
        now.set(Calendar.MINUTE,minute);
        now.set(Calendar.SECOND,second);
        Intent intent = new Intent(getApplicationContext(),TestActivity.class);
        intent.putExtra("test","I am a String");
        NotifyMe notifyMe = new NotifyMe.Builder(getApplicationContext())
                .title(etTitle.getText().toString())
                .content(etContent.getText().toString())
                .color(255,0,0,255)
                .led_color(255,255,255,255)
                .time(now)
                .addAction(intent,"Snooze",false)
                .key("test")
                .addAction(new Intent(),"Dismiss",true,false)
                .addAction(intent,"Done")
                .large_icon(R.mipmap.ic_launcher_round)
                .build();
        Toast.makeText(getApplicationContext(), "Your reminder is set!",
                Toast.LENGTH_LONG).show();
    }
}