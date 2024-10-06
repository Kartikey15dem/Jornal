package com.example.self;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.DataUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRecyclerAdaptor extends RecyclerView.Adapter<JournalRecyclerAdaptor.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdaptor(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdaptor.ViewHolder holder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        holder.name.setText(journal.getUserName());
        imageUrl = journal.getImageUrl();

        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded()
                .getSeconds() * 1000);
        holder.dateAdded.setText(timeAgo);
        /* Use picasso to download and show image */
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .into(holder.image);


    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, thoughts, dateAdded, name;
        public ImageView image;
        public ImageButton shareButton;
        String userId;
        String username;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            name = itemView.findViewById(R.id.journal_row_username);

            shareButton = itemView.findViewById(R.id.journal_row_share_button);
            shareButton.setOnClickListener(v -> {

                // Get the journal entry at the current position
                Journal journal = journalList.get(getAdapterPosition());

                // Get a reference to the image in Firebase Storage
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(journal.getImageUrl());

                // Create an intent to share the image
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");

                // Add a listener to the getDownloadUrl() task
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Set the download URL of the image as the extra data in the intent
                        shareIntent.putExtra(Intent.EXTRA_STREAM, downloadUri);

                        // Grant temporary read permission to the URI
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Start the activity to share the image
                        context.startActivity(shareIntent);
                    }
                });
            });
        }
    }
}


