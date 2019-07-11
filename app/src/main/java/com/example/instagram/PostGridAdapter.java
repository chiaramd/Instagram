package com.example.instagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.model.Post;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class PostGridAdapter extends RecyclerView.Adapter<PostGridAdapter.ViewHolder> {
    ArrayList<Post> posts;
    private Context context;

    private final String TAG = "PostAdapter";

    public PostGridAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_post_grid, parent, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostGridAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        Glide.with(context)
                .load(post.getImage().getUrl())
                .apply(bitmapTransform(new CropSquareTransformation()))
                .into(holder.ivPostImage);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivPostImage) ImageView ivPostImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Post clicked");
                Post post = posts.get(position);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("postId", post.getObjectId());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, (View) ivPostImage, "postImage");

                context.startActivity(i, options.toBundle());
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
