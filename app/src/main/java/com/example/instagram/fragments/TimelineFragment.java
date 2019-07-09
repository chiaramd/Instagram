package com.example.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagram.R;
import com.example.instagram.model.Post;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TimelineFragment extends Fragment {
    private Unbinder unbinder;

    private final String TAG = "TimelineFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
    }

    @OnClick(R.id.btnRefresh)
    void refresh() {
        Log.d(TAG, "Refreshing posts...");
        loadTopPosts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void loadTopPosts() {
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

        postsQuery.findInBackground((objects, e) -> {
            if (e == null) {
                for (int i = 0; i < objects.size(); ++i) {
                    Log.d(TAG, "Post[" + i + "] = " + objects.get(i).getDescription() + "\nusername = " + objects.get(i).getUser().getUsername());
                }
            } else {
                e.printStackTrace();
            }
        });
    }
}
