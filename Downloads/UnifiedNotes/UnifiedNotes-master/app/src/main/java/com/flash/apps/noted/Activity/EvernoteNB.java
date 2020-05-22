package com.flash.apps.noted.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Notebook;
import com.flash.apps.noted.Adapter.RecyclerViewAdapterEVN;
import com.flash.apps.noted.BuildConfig;
import com.flash.apps.noted.R;

import java.util.ArrayList;
import java.util.List;

public class EvernoteNB extends AppCompatActivity {

    private static final String TAG = "EvernoteNB";
    private ArrayList<String> notebookNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evernote_nb);
        getNotebookNames();
    }

    private void getNotebookNames(){

        if(getIntent()!=null)
        {
            System.out.println("Hello" + getIntent().getStringArrayListExtra("notebooks"));
        }
        notebookNames = getIntent().getStringArrayListExtra("notebooks");

        Log.d(TAG,"Clicked on:" + notebookNames);
        initRecyclerView();

    }

    private void initRecyclerView(){

        RecyclerView recyclerView = findViewById(R.id.evernoteNB);
        RecyclerViewAdapterEVN recyclerViewAdapterEVN = new RecyclerViewAdapterEVN(notebookNames, this);
        recyclerView.setAdapter(recyclerViewAdapterEVN);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}
