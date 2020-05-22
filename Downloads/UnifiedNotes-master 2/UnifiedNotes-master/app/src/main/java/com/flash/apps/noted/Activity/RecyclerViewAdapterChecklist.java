package com.flash.apps.noted.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evernote.edam.type.Note;
import com.flash.apps.noted.Activity.CreateNote;
import com.flash.apps.noted.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecyclerViewAdapterChecklist extends RecyclerView.Adapter<RecyclerViewAdapterChecklist.NoteViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mTitle = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    private ArrayList<String> checkedStatus = new ArrayList<>();
    private Context mContext;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNoteDatabase;
    private String noteId;
    public List<String> key;
    private String ID;
    public String mBook;
    public RecyclerViewAdapterChecklist(ArrayList<String> content,ArrayList<String> checked,String noteId, Context context,List<String> keys){
        mContent=content;
        mContext=context;
       key=keys;
       ID=noteId;
       checkedStatus = checked;
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_checklist_view, parent, false);
        NoteViewHolder holder = new NoteViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {
        fAuth = FirebaseAuth.getInstance();
        ourNoteDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());

        Log.d(TAG, "onBindViewHolder: called");
        System.out.println("hello24 "+ mContent.get(position));
        holder.textContent.setText(mContent.get(position));
        if(checkedStatus.get(position).equals("")) {
            holder.checkBox.setChecked(false);
            holder.textContent.setPaintFlags(holder.textContent.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        }
        else {
            holder.checkBox.setChecked(true);
            holder.textContent.setPaintFlags(holder.textContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedStatus.get(position).equals("")) {
                    //holder.textContent.setPaintFlags(holder.textContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    Map updateMap = new HashMap();
                    //updateMap.put("NotebookName",  );
                    updateMap.put("Checked", "yes");
                    ourNoteDatabase.child(ID).child(key.get(position)).updateChildren(updateMap);
                }
                else{
                    Map updateMap = new HashMap();
                    //updateMap.put("NotebookName",  );
                    updateMap.put("Checked", "");
                    ourNoteDatabase.child(ID).child(key.get(position)).updateChildren(updateMap);

                }
            }
        });

        /*holder.noteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: "+mTitle.get(position));
                //holder.noteCard.setBackgroundColor(Color.BLUE);
                Intent intent = new Intent();
                intent.setClass(mContext,CreateNote.class);
                ID = Integer.toString(position);
                intent.putExtra("noteId",ID);
                intent.putExtra("Notebook",mBook);
                mContext.startActivity(intent);

            }
        });*/

    }
    public void delete(int position){
        mContent.remove(position);
        mTitle.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView textContent;
        RelativeLayout noteCard;
        CheckBox checkBox;
        public NoteViewHolder(View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.checktext);
            noteCard = itemView.findViewById(R.id.notecard1);
            checkBox = itemView.findViewById(R.id.checked);
        }
    }
}
