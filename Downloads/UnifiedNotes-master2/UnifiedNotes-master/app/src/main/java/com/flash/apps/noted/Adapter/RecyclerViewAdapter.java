package com.flash.apps.noted.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.edam.type.Note;
import com.flash.apps.noted.Activity.CreateNote;
import com.flash.apps.noted.Activity.FingerprintLock;
import com.flash.apps.noted.Activity.GridView;
import com.flash.apps.noted.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.NoteViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mTitle = new ArrayList<>();
    private ArrayList<String> mtag = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    private Context mContext;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNoteDatabase;
    private String noteId;
    public List<String> key;
    private String ID;
    public String mBook;
    public RecyclerViewAdapter(ArrayList<String> title, ArrayList<String> content, Context context, List<String> keys, ArrayList<String > tags){
        mTitle=title;
        mContent=content;
        mContext=context;
        key = keys;
        mtag=tags;
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_note_layout, parent, false);
        NoteViewHolder holder = new NoteViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: called");
        fAuth = FirebaseAuth.getInstance();
        ourNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        holder.textTitle.setText(mTitle.get(position));
        holder.textContent.setText(mContent.get(position));
        if(mtag.size()>0){
        holder.taging.setText(mtag.get(position));}

        holder.noteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: "+mTitle.get(position));
                //holder.noteCard.setBackgroundColor(Color.BLUE);
                Intent intent = new Intent();
                intent.setClass(mContext,CreateNote.class);
                ID = Integer.toString(position);
                intent.putExtra("noteId",key.get(position));

                //intent.putExtra("Notebook",mBook);
                mContext.startActivity(intent);

            }
        });
        holder.noteCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.getMenuInflater().inflate(R.menu.menu_archieve,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Map updateMap = new HashMap();
                        //updateMap.put("NotebookName",  );
                        updateMap.put("Archive", "yes");
                        ourNoteDatabase.child(key.get(position)).updateChildren(updateMap);
                        mContent.remove(position);
                        mTitle.remove(position);
                        notifyItemRemoved(position);
                        return false;
                    }
                });
                popup.show();
                return false;
            }
        });

    }
    public void filterList(ArrayList<String> filterdNames) {
        //this.mTitle = filterdNames;
        this.mTitle=filterdNames;
        //new RecyclerViewAdapter(filterdNames, mContent, mContext, key, mtag);
        notifyDataSetChanged();
    }

    public void delete(int position){
            mContent.remove(position);
            mTitle.remove(position);
            notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mTitle.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle, textContent,taging;
        RelativeLayout noteCard;
        Switch lock;
        public NoteViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.note_title);
            textContent = itemView.findViewById(R.id.note_content);
            noteCard = itemView.findViewById(R.id.note_card);
            taging = itemView.findViewById(R.id.tags);

        }
    }
}
