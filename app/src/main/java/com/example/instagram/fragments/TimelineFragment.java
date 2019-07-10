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

import com.example.instagram.EndlessRecyclerViewScrollListener;
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
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;

    private boolean scrollable = true;

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
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMorePosts();
            }
        };

        rvPosts.addOnScrollListener(scrollListener);
        populateTimeline();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMorePosts();
            }
        };*/
//        rvPosts.addOnScrollListener(scrollListener);
        scrollListener.resetState();

    }

    // TODO - resume at scroll position after Detail Activity finishes

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
        scrollListener.resetState();

    }

    private void populateTimeline() {
        Log.d(TAG, "Populating timeline...");
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
    }

    private void loadMorePosts() {
        if (scrollable) {
            scrollable = false;
            Log.d(TAG, "Loading more posts...");
            final Post.Query postsQuery = new Post.Query();
            postsQuery.getNext(posts.size()).withUser();


            postsQuery.findInBackground((objects, e) -> {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        posts.add(objects.get(i));
                        adapter.notifyItemInserted(posts.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
                scrollable = true;
            });
        }
    }


}
