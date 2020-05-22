package com.flash.apps.noted.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flash.apps.noted.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapterChecklistLayout extends RecyclerView.Adapter<RecyclerViewAdapterChecklistLayout.NoteViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mTitle = new ArrayList<>();
    private ArrayList<String> mContent = new ArrayList<>();
    private Context mContext;
    public FirebaseAuth fAuth;
    private DatabaseReference ourNoteDatabase;
    private String noteId;
    public List<String> key;
    private String ID;
    public String mBook;
    public RecyclerViewAdapterChecklistLayout(ArrayList<String> title, Context context,String book,List<String> keys){
        mTitle = title;
        mContext=context;
        mBook=book;
        key=keys;
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checklist_linear_layout, parent, false);
        NoteViewHolder holder = new NoteViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: called");

        holder.textTitle.setText(mTitle.get(position));

        holder.noteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: "+mTitle.get(position));
                //holder.noteCard.setBackgroundColor(Color.BLUE);
                Intent intent = new Intent();
                intent.setClass(mContext,checklist.class);
                ID = Integer.toString(position);
                intent.putExtra("noteId",key.get(position));
                intent.putExtra("Notebook",mBook);
                mContext.startActivity(intent);

            }
        });

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
        TextView textTitle;
        RelativeLayout noteCard;
        public NoteViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.check_title);
            noteCard = itemView.findViewById(R.id.check_card);
        }
    }
}
