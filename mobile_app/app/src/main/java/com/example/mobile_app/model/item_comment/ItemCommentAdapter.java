package com.example.mobile_app.model.item_comment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_app.R;
import com.example.mobile_app.controller.ProfilePictureManager;
import com.example.mobile_app.model.Comment;
import com.example.mobile_app.model.RecyclerViewInterface;
import com.example.mobile_app.model.UserStatic;

import java.util.List;

public class ItemCommentAdapter extends RecyclerView.Adapter<ItemCommentViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    Context mContext;
    List<Comment> comments;

    public ItemCommentAdapter(List<Comment> comments, Context context, RecyclerViewInterface recyclerViewInterface) {
        this.comments = comments;
        this.mContext = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ItemCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemCommentViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false),
                recyclerViewInterface
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCommentViewHolder holder, int position) {
        holder.text_username.setText(comments.get(position).getAuthor().getFull_name());
        holder.text_content.setText(comments.get(position).getText());
        holder.text_date.setText(comments.get(position).getDate());
        ProfilePictureManager.setProfilePicture(mContext, holder.image_pfp, Integer.parseInt(comments.get(position).getAuthor().getProfile_picture()));

        //button_edit.setVisibility(UserStatic.getIs_staff().equals("true") ? View.VISIBLE : View.GONE);
        holder.button_remove.setVisibility(UserStatic.getIs_staff().equals("true") ? View.VISIBLE : View.GONE);

        String roleText = "";
        /*
        switch (comments.get(position).getRole()) {
            case 0: roleText = mContext.getString(R.string.status_student); break;
            case 1: roleText = mContext.getString(R.string.status_teacher); break;
            case 2: roleText = mContext.getString(R.string.status_other); break;
            default: roleText = ""; break;
        }
         */
        holder.text_role.setText(roleText);

        holder.button_remove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(mContext.getString(R.string.ui_deleteCommentWarning));
            builder.setPositiveButton(R.string.ui_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(mContext, "Comment Deleted", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.ui_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            builder.create();
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}