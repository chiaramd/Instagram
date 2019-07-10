package com.example.instagram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UserProfileFragment extends Fragment {
    @BindView(R.id.btnLogout) Button btnLogout;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvUsername) TextView tvUsername;
    private Unbinder unbinder;

    private final String TAG = "UserProfileFragment";

    private boolean profileImageExists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        ParseUser user = ParseUser.getCurrentUser();

        ParseFile profilePic = user.getParseFile("profileImage");
        if (profilePic != null) {
            Glide.with(this)
                    .load(profilePic.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
            profileImageExists = true;
        } else {
            Log.d(TAG, "No profile image");
            profileImageExists = false;
        }

        tvUsername.setText(user.getUsername());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnLogout)
    void logout() {
        Log.d(TAG, "Logging user out...");
        ParseUser.logOut();
        Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.ivProfileImage)
    void addProfile() {
        if (!profileImageExists) {
            // TODO - add profile image
        }
    }
}
