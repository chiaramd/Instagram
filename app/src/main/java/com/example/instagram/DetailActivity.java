package com.example.instagram;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class DetailActivity extends AppCompatActivity {
    @BindView(R.id.ivPostImage) ImageView ivPostImage;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvCreatedAt) TextView tvCreatedAt;

    Post post;

    private final String TAG = "DetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        String postId = getIntent().getStringExtra("postId");

        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser(); //TODO - no getTop()??

        try {
            post = postsQuery.get(postId);
        } catch (ParseException e) {
            Log.d(TAG, "Post not found");
            e.printStackTrace();
            finish();
        }

        tvDescription.setText(post.getDescription());
        tvName.setText(post.getUser().getUsername());
        String[] createArray = post.getCreatedAt().toString().split(" ");
        String[] createTimeArray = createArray[3].split(":");
        String created = String.format("Created at %s:%s on %s, %s %s", createTimeArray[0], createTimeArray[1], createArray[0], createArray[1], createArray[2]);
        tvCreatedAt.setText(created);
        Glide.with(this)
                .load(post.getImage().getUrl())
                .apply(bitmapTransform(new CropSquareTransformation()))
                .into(ivPostImage);

        ParseUser user = post.getUser();
        ParseFile profilePic = user.getParseFile("profileImage");
        if (profilePic != null) {
            Glide.with(this)
                    .load(profilePic.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        } else {
            Glide.with(this)
                    .load(this.getResources().getIdentifier("ic_user_profile_filled", "drawable", this.getPackageName()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
        }
    }
}
