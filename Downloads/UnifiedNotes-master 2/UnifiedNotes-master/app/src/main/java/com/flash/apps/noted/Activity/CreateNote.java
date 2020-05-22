package com.flash.apps.noted.Activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.lang.*;

import com.bumptech.glide.Glide;
import com.flash.apps.noted.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateNote extends AppCompatActivity {

    private EditText etTitle, etContent;
    //private Toolbar mToolbar;
    public FirebaseAuth fAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    public ImageView mImageView;
    public ImageView cImageaview;
    Button play,pause,stop;
    String title;
    String content;
    public String noteID = "";
    public String position;
    public String id;
    private List<String> key = new ArrayList<>() ;
    //public String position;
    //public int ID=0;
    // public Uri mImageUri;
    private DatabaseReference ourNoteDatabase;
    private boolean isExist;
    private boolean delete = false;
    MediaPlayer mediaPlayer;
    private String selectedText;
    String formatted = null;
    private EditText edit ;
    private StorageReference mStorageReference;
    public Uri mImageUri;
    public Uri fileUri;
    public long mfile;
    public String dialogText = "";
    String bookName;

    public void getTypeface(int id) {
        edit =  findViewById( R.id.editID ) ;
        Spanned text = Html.fromHtml( selectedText );
        edit.setText(text.toString());
        if(edit.getTypeface() !=null){
            if(selectedText.contains( "<b>" )){
                SpannableStringBuilder sb = new SpannableStringBuilder(text);
                sb.setSpan( new StyleSpan( Typeface.NORMAL ),0,sb.length()-1,0 );
                formatted = String.valueOf( sb.subSequence(0,sb.length()) );
            }else if (selectedText.contains( "<i>")) {
                SpannableStringBuilder sb = new SpannableStringBuilder(text);
                sb.setSpan( new StyleSpan( Typeface.NORMAL ),0,sb.length()-1,0 );
                formatted = String.valueOf( sb.subSequence(0,sb.length()) );
            }else if (selectedText.contains( "<u>")) {
                SpannableStringBuilder sb = new SpannableStringBuilder(text);
                sb.setSpan( new StyleSpan( Typeface.NORMAL ),0,sb.length()-1,0 );
                formatted = String.valueOf( sb.subSequence(0,sb.length()) );
            }
            else
            {
                if (id == R.id.action_bold) {
                    formatted = "<b>" + selectedText + "</b>";
                }
                if (id == R.id.action_italic) {
                    formatted = "<i>" + selectedText + "</i>";
                }
                if (id == R.id.action_underline) {
                    formatted = "<u>" + selectedText + "</u>";
                }

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme( R.style.darkTheme );
        }else
            setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note);
        fAuth = FirebaseAuth.getInstance();
        ourNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
        etTitle =  findViewById(R.id.etTitle);
        etContent =  findViewById(R.id.etContent);
        mImageView = findViewById(R.id.image_CreateNote);
        cImageaview = findViewById(R.id.image_Note);
        //System.out.println("hello");
        //getActionBar().setDisplayHomeAsUpEnabled(false);

        etContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.items, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                int id = item.getItemId();
                int selStart = 0;
                int selEnd = 0;
                int min = 0;
                int max = etContent.getText().toString().length();

                if (etContent.isFocused()) {
                    selStart = etContent.getSelectionStart();
                    selEnd = etContent.getSelectionEnd();
                    if (selStart > selEnd) {
                        selEnd = etContent.getSelectionStart();
                        selStart = etContent.getSelectionEnd();
                    }
                }
                //selectedText = (String) editText.getText().subSequence( selStart, selEnd );

                String textBefore = Html.toHtml( new SpannableString( etContent.getText().subSequence( 0, selStart ) ) );
                selectedText = Html.toHtml( new SpannableString( etContent.getText().subSequence( selStart, selEnd ) ) );
                String textAfter = Html.toHtml( new SpannableString( etContent.getText().subSequence( selEnd, max ) ) );

                if (!selectedText.equals( "" ) || selStart != selEnd) {
                    getTypeface(id);
                    StringBuilder builder = new StringBuilder();
                    builder.append( textBefore );
                    builder.append( formatted );
                    builder.append( textAfter );

                    etContent.setText( Html.fromHtml( builder.toString() ) );
                    etContent.setSelection( selEnd );
                    textBefore = "";
                    textAfter = "";
                    selectedText = "";
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);

        try {
            noteID = getIntent().getStringExtra("noteId");
            bookName= getIntent().getStringExtra("Notebook");
            if (!noteID.trim().equals("")) {
                isExist = true;
                putData();
            } else {
                isExist = false;
            }
        }catch (Exception e){}
        mStorageReference = FirebaseStorage.getInstance().getReference().child("ImagePicker");

    }



    @Override
    public void onBackPressed() {

        title = etTitle.getText().toString().trim();
        content = etContent.getText().toString().trim();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
            createNote(title, content);
        } else {
            Toast.makeText(CreateNote.this, "Fill empty fields" , Toast.LENGTH_SHORT).show();
        }

        super.onBackPressed();

    }

    public void putData(){
        isExist = true;
        if (isExist) {
            System.out.println("hello"+noteID);
            ourNoteDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Title") && dataSnapshot.hasChild("Content")) {
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String content = dataSnapshot.child("Content").getValue().toString();

                        etTitle.setText(title);
                        etContent.setText(content);
                    }
                    if(dataSnapshot.hasChild("ImageUri")){
                    Uri imageuri = Uri.parse(dataSnapshot.child("ImageUri").getValue().toString());
                        System.out.println("hello78 "+imageuri.toString());
                    Picasso.with(CreateNote.this).load(imageuri).into(mImageView);
                    mImageUri = imageuri;
                    }
                    if(dataSnapshot.hasChild("AudioUrl")){
                        play.setVisibility(View.VISIBLE);
                        pause.setVisibility(View.VISIBLE);

                        try {
                            String url = dataSnapshot.child("AudioUrl").getValue().toString();
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(url);
                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                        }catch (Exception e){}
                        play.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mediaPlayer.start();
                            }
                        });
                        pause.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mediaPlayer.pause();
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
   /* private void stopPlaying() {
            mediaPlayer.release();
            mediaPlayer = null;
    }
    @Override
    protected void onStop() {
        super.onStop();
        stopPlaying();
    }
   /* public void pause(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    public void stop(View v) {
        stopPlaying();
    }*/


    public void createNote (String title, String content) {
        //ImageDisplay image = new ImageDisplay();
        //String text = image.fileName();
        //String url = image.fileUrl();
        //Upload upload = new Upload(text,url);
        final String bookname= getIntent().getStringExtra("bookTitle");
        if(bookName != null)
            System.out.print("BookName Updated"+ bookName);
        if (fAuth.getCurrentUser() != null) {
            if (isExist) {
                Map updateMap = new HashMap();
                //updateMap.put("NotebookName",  );
                updateMap.put("Title", etTitle.getText().toString().trim());
                updateMap.put("Content", etContent.getText().toString().trim());
                updateMap.put("Timestamp", ServerValue.TIMESTAMP);
                if(mImageUri!=null)
                updateMap.put("ImageUri",mImageUri.toString());
                if(!dialogText.equals("")){
                    updateMap.put("Tags",dialogText);
                    dialogText="";}
                //updateMap.put("Image", upload);

                ourNoteDatabase.child(noteID).updateChildren(updateMap);


            } else {
                final DatabaseReference newNoteRef = ourNoteDatabase.push();
                // noteID = newNoteRef.getKey();
                final Map noteMap = new HashMap();
                noteMap.put("NotebookName", bookname );
                noteMap.put("Type", "note");
                noteMap.put("Title", title);
                noteMap.put("Content", content);
                noteMap.put("Timestamp", ServerValue.TIMESTAMP);
                noteMap.put("Archive", "");
                if(mImageUri!=null)
                    noteMap.put("ImageUri",mImageUri.toString());
                if(!dialogText.equals("")){
                   noteMap.put("Tags",dialogText);
                   dialogText="";}

                else
                    noteMap.put("Tags", "");

                //noteMap.put("Image", upload);

                newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Snackbar.make(findViewById(R.id.NoteCreation), "Note Added", Snackbar.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(CreateNote.this, "Error adding to database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }else{
            Toast.makeText(this, "USER IS NOT SIGNED IN", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_new_image, menu);
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        getMenuInflater().inflate(R.menu.menu_audio, menu);
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        getMenuInflater().inflate(R.menu.menu_share, menu);
        getMenuInflater().inflate(R.menu.menu_tag, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.home:
                finish();
                break;
            case R.id.main_new_image_btn:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                break;
            case R.id.main_camera_btn:
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String fileName = "new-photo-name.jpg";
                //create parameters for Intent with filename
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
                //imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
                fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                //create new Intent

                i.putExtra("data",fileUri);
                System.out.println("hello56"+fileUri);

                startActivityForResult(i, CAMERA_REQUEST_CODE);
                break;
            case R.id.main_new_audio_btn:
                Intent intent1 = new Intent(this,audio_getter.class);
                intent1.putExtra("noteId",noteID);
                startActivity(intent1);
                break;
            case R.id.main_new_rem_btn:
                Intent reminder_intent = new Intent(this, reminder.class);
                reminder_intent.putExtra("noteId",noteID);
                startActivity(reminder_intent);
                break;
            case R.id.main_new_share_btn:
                Intent share_intent = new Intent(this, share.class);
                share_intent.putExtra("noteId",noteID);
                startActivity(share_intent);
                break;
            case R.id.main_tag_btn:
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View promptsView = layoutInflater.inflate(R.layout.tagpromt,null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setView(promptsView);
                final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        //result.setText(userInput.getText());
                                        dialogText = userInput.getText().toString();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();


                break;
            /*case R.id.main_delete_btn:
                delete = true;
                break;*/
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e(TAG,"ActivityResult: Started");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            System.out.println("hello56"+ mImageUri);
            Picasso.with(this).load(mImageUri).into(mImageView);

            mfile = System.currentTimeMillis();
            StorageReference fileReference = mStorageReference.child(mfile + "." + getFileExtension(mImageUri));
            fileReference.putFile(mImageUri);
        }if(requestCode == CAMERA_REQUEST_CODE){
            // String extra = getIntent().getExtras("data");
            //System.out.println("hello561 "+ mfile);
            File file = new File(getPath(fileUri));
            //Uri uri = file.toURI();
            Uri flUri = Uri.fromFile(file);
            System.out.println("hello56"+ file);
            //decodeFile(getPath(fileUri));
            Glide.with(this).load(flUri).into(cImageaview);


            //Picasso.with(this).load(fileUri).into(mImageView);

            //uploadFile(fileUri);
            //file.delete();
        }
    }
   public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
   /* public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);

        mImageView.setImageBitmap(bitmap);

    }*/

    private String getFileExtension(Uri uri){
        //Log.e(TAG,"FileExtension: Started");
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    /*private void uploadFile(){
        //Log.e(TAG,"UploadFile: Started");

        if (mImageUri != null) {
            final StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

           /* fileReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    //hoja = task.getResult().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }else {
            Toast.makeText(this,"No File Selected",Toast.LENGTH_SHORT).show();
        }
        //System.out.println("gaana "+ hoja);
    }*/

  
}