package com.flash.apps.noted.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.asyncclient.EvernoteSearchHelper;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;
import com.flash.apps.noted.BuildConfig;
import com.flash.apps.noted.EvernoteNotesFetch;
import com.flash.apps.noted.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.in;


public class Login extends AppCompatActivity {

    private Button btnLogin, loginEvernote;
    private Button gSignin;
    private TextView clickHere, forgotPassword;
    private EditText emailaddress;
    private EditText userPassword;
    private ProgressBar progressBar;
    private CheckBox rememberMe;

    private TextInputLayout txlemail;
    private TextInputLayout txlpassword;

    private FirebaseAuth firebaseAuth;
    private SharedPreferences loginRemember;
    private SharedPreferences.Editor loginRememberEditor;

    private boolean saveLogin;
    private final static int SIGNIN = 123;
    private static final String TAG = "";

    private EvernoteSession mEvernoteSession;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

    private static final String CONSUMER_KEY = "deepenpanchal";
    private static final String CONSUMER_SECRET = "da85d51591b63a6d";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    private GoogleSignInClient googleSignInClient;
    //private final EvernoteSearchHelper.Search mSearch;



    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, Login.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.login);
        loginEvernote = findViewById(R.id.loginEvernote);
        gSignin = findViewById(R.id.gSignin);
        clickHere = findViewById(R.id.clickhere);
        forgotPassword = findViewById(R.id.forgotpassword);
        emailaddress = findViewById(R.id.emailaddress);
        userPassword = findViewById(R.id.password);
        txlemail = findViewById(R.id.text_input_layout);
        txlpassword = findViewById(R.id.text_input_layout2);
        progressBar = findViewById(R.id.progressBar);
        rememberMe = findViewById(R.id.rememberme);
        firebaseAuth = FirebaseAuth.getInstance();
        loginRemember = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginRememberEditor = loginRemember.edit();

        clickHere.setTextColor(ContextCompat.getColor(Login.this, R.color.colorPrimary));
        btnLogin.setTextColor(ContextCompat.getColor(Login.this, R.color.buttonText));

        //Initialize the Evernote Consumer Key, Consumer Secret and Session
        evernoteInit();

        //if (firebaseAuth.getCurrentUser()!=null)
        //{
          //  startActivity(new Intent(Login.this, EvernoteNB.class));
        //}


        //If user hasn't registered and clicks on 'Click Here'
        clickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        //if clicked on Forgot Password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgotPass.class));
            }
        });

        //If user clicks on 'Signin with Google'
        gSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //Remember me checkbox functionality
        saveLogin = loginRemember.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            emailaddress.setText(loginRemember.getString("username", ""));
            userPassword.setText(loginRemember.getString("password", ""));
            rememberMe.setChecked(true);
        }

        //User clicks on 'Login with Evernote'
        loginEvernote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the user is already logged in
                if (!EvernoteSession.getInstance().isLoggedIn()) {
                    //return;
                    EvernoteSession.getInstance().authenticate(Login.this);
                }

                //Fetch the notebooks from Evernote into a list
                EvernoteNotesFetch enf = new EvernoteNotesFetch();
                HashMap<String, String> mapOfGUID = enf.getNotebookList();

                ArrayList<String> namesList = new ArrayList<>();
                for ( String key : mapOfGUID.keySet() ) {
                    System.out.println("Deepen:"+key);
                    namesList.add(key);
                }
                for (String k:namesList)
                    System.out.println("Names:"+k);
                Intent intent = new Intent(Login.this, EvernoteNB.class);
                intent.putStringArrayListExtra("notebooks", namesList);
                startActivity(intent);
                //evernoteFetchNotebooks();

            }
        });

       //Normal signin with email address and password
        signinEmailPass();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent, SIGNIN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        switch(requestCode){

            case EvernoteSession.REQUEST_CODE_LOGIN:
                if(resultCode == Activity.RESULT_OK)
                {
                    startActivity(new Intent(Login.this, EvernoteNB.class));
                }
                else {
                    Toast.makeText(Login.this, "Error Logging in with Evernote", Toast.LENGTH_SHORT).show();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;

        }

        if (requestCode == SIGNIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            startActivity(new Intent(Login.this, NoteBoard.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Aut Fail", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    public void evernoteInit(){

        String consumerKey;
        if ("Your consumer key".equals(CONSUMER_KEY)) {
            consumerKey = BuildConfig.EVERNOTE_CONSUMER_KEY;
        } else {
            // isn't the default value anymore
            consumerKey = CONSUMER_KEY;
        }

        String consumerSecret;
        if ("Your consumer secret".equals(CONSUMER_SECRET)) {
            consumerSecret = BuildConfig.EVERNOTE_CONSUMER_SECRET;
        } else {
            // isn't the default value anymore
            consumerSecret = CONSUMER_SECRET;
        }

        //Set up the Evernote singleton session, use EvernoteSession.getInstance() later
        mEvernoteSession = new EvernoteSession.Builder(Login.this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
                .setForceAuthenticationInThirdPartyApp(true)
//                .setLocale(Locale.SIMPLIFIED_CHINESE)
                .build(consumerKey, consumerSecret)
                .asSingleton();

    }


    public void evernoteFetchNotebooks(){


        //Evernote notestore client
        //EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

        /*
        //Create a new note
        Note note = new Note();
        System.out.println("GUID:"+note.getGuid());
        note.setTitle("My title");
        note.setContent(EvernoteUtil.NOTE_PREFIX + "My content" + EvernoteUtil.NOTE_SUFFIX);

        noteStoreClient.createNoteAsync(note, new EvernoteCallback<Note>() {
            @Override
            public void onSuccess(Note result) {
                Toast.makeText(getApplicationContext(), result.getTitle() + " has been created", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onException(Exception exception) {
                Log.e(TAG, "Error creating note", exception);
            }
        });

        */

        //Synchronous method to fetch notebooks, but we aren't using this.
/*
        Note note1 = new Note();
        System.out.println("Note:"+note1.getContent());
        try {
            List<Notebook> nBookList= noteStoreClient.listNotebooks();
            System.out.println("Kuch bhi");
            for (Notebook n: nBookList) {
                System.out.println("Name : "+n.getName());

            }

        } catch (Exception e){
            System.out.println("AAAAA");
            System.out.println("AAAAA"+e.getMessage());
        }
*/



        //Fetch list of notebooks



    }




    public void signinEmailPass(){

        //Normal Email address and password login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emailaddress.getWindowToken(), 0);

                final String email = emailaddress.getText().toString();
                String password = userPassword.getText().toString();

                if (rememberMe.isChecked()) {
                    loginRememberEditor.putBoolean("saveLogin", true);
                    loginRememberEditor.putString("username", email);
                    loginRememberEditor.putString("password", password);
                    loginRememberEditor.commit();
                } else {
                    loginRememberEditor.clear();
                    loginRememberEditor.commit();
                }

                //Check if the fields are entered or not
                if(TextUtils.isEmpty(email))
                {
                    txlemail.setError("Email address is required");
                    return;
                }

                else
                {
                    txlemail.setError(null);
                }

                if(TextUtils.isEmpty(password))
                {
                    txlpassword.setError("Password can't be empty");
                }

                else
                {
                    txlpassword.setError(null);
                }

                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(Login.this, getString(R.string.authError), Toast.LENGTH_LONG).show();
                        }

                        else
                        {
                            Intent toDashboard = new Intent(Login.this, Dashboard.class);
                            toDashboard.putExtra("email", email);
                            startActivity(toDashboard);
                            finish();
                        }



                    }
                });

            }
        });
    }


}

//For signout

/*
 mAuth = FirebaseAuth.getInstance();

         mAuthListner = new FirebaseAuth.AuthStateListener() {
@Override
public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser()==null)
        {
        startActivity(new Intent(Home_screen.this, singin_activity.class));
        }
        }
        };

        button.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        mAuth.signOut();


        }

        */