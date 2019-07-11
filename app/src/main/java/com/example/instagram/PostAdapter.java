package com.example.instagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<Post> posts;
    private Context context;

    private final String TAG = "PostAdapter";

    public PostAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_post, parent, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.isLikedByUser = post.isLikedBy(ParseUser.getCurrentUser().getUsername());

        holder.ivHeart.setSelected(holder.isLikedByUser);
        Integer numLikes = post.getLikes();
        if (numLikes == 1) {
            holder.tvNumLikes.setText(String.format("%s like", numLikes));
        } else {
            holder.tvNumLikes.setText(String.format("%s likes", numLikes));
        }
        holder.tvDescription.setText(post.getDescription());
        holder.tvName.setText(post.getUser().getUsername());
        holder.tvCreatedAt.setText(getCreationDateTime(post.getCreatedAt()));
        Glide.with(context)
                .load(post.getImage().getUrl())
                .apply(bitmapTransform(new CropSquareTransformation()))
                .into(holder.ivPostImage);

        ParseUser user = post.getUser();
        ParseFile profilePic = user.getParseFile("profileImage");
        if (profilePic != null) {
            Glide.with(context)
                    .load(profilePic.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivProfileImage);
        } else {
            Glide.with(context)
                    .load(context.getResources().getIdentifier("ic_user_profile_filled", "drawable", context.getPackageName()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivPostImage) ImageView ivPostImage;
        @BindView(R.id.tvDescription) TextView tvDescription;
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvCreatedAt) TextView tvCreatedAt;
        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        @BindView(R.id.ivHeart) ImageView ivHeart;
        @BindView(R.id.tvNumLikes) TextView tvNumLikes;

        boolean isLikedByUser = false;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            ivHeart.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                Post post = posts.get(pos);
                if (!isLikedByUser) {
                    ivHeart.setSelected(true);
                    Integer numLikes = Integer.parseInt(tvNumLikes.getText().toString().split(" ")[0]);
                    isLikedByUser = true;
                    post.setLikes(numLikes + 1);
                    post.add(ParseUser.getCurrentUser().getUsername());
                    post.saveInBackground();
                    if (numLikes == 0) {
                        tvNumLikes.setText(String.format("%s like", numLikes + 1));
                    } else {
                        tvNumLikes.setText(String.format("%s likes", numLikes + 1));
                    }
                } else {
                    ivHeart.setSelected(false);
                    Integer numLikes = Integer.parseInt(tvNumLikes.getText().toString().split(" ")[0]);
                    isLikedByUser = false;
                    post.setLikes(numLikes - 1);
                    post.remove(ParseUser.getCurrentUser().getUsername());
                    post.saveInBackground();
                    if (numLikes == 2) {
                        tvNumLikes.setText(String.format("%s like", numLikes - 1));
                    } else {
                        tvNumLikes.setText(String.format("%s likes", numLikes - 1));
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Post clicked");
                Post post = posts.get(position);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("postId", post.getObjectId());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, ivPostImage, "postImage");
                context.startActivity(i, options.toBundle());
            }
        }
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    private String getCreationDateTime(Date rawDate) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("hh:mm MM/dd", Locale.US);
        String strDate = simpleDate.format(rawDate);
        return strDate;
    }
}
