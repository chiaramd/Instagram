package com.example.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.instagram.PostAdapter;
import com.example.instagram.R;
import com.example.instagram.model.Post;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TimelineFragment extends Fragment {
    @BindView(R.id.rvPosts) RecyclerView rvPosts;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    private Unbinder unbinder;

    private final String TAG = "TimelineFragment";

    ArrayList<Post> posts;
    PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        swipeContainer.setOnRefreshListener(this::refresh);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        populateTimeline();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void refresh() {
        Log.d(TAG, "Refreshing posts...");
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

        postsQuery.findInBackground((objects, e) -> {
            if (e == null) {
                adapter.clear();
                adapter.addAll(objects);
            } else {
                e.printStackTrace();
            }
        });
        swipeContainer.setRefreshing(false);
    }

    private void populateTimeline() {
        Log.d(TAG, "Populating timeline...");
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

        postsQuery.findInBackground((objects, e) -> {
            if (e == null) {
                /*for (int i = 0; i < objects.size(); ++i) {
                    Log.d(TAG, "Post[" + i + "] = " + objects.get(i).getDescription() + "\nusername = " + objects.get(i).getUser().getUsername());
                    posts.add(objects.get(i));
                    adapter.notifyItemInserted(posts.size() - 1);
                }*/
                adapter.clear();
                adapter.addAll(objects);
            } else {
                e.printStackTrace();
            }
        });
    }
}
