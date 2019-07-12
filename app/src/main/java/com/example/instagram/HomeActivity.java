package com.example.instagram;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.TimelineFragment;
import com.example.instagram.fragments.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationView;

    private final String TAG = "HomeActivity";
    public int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        final FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.miNewPost:
                        Log.d(TAG, "Home item clicked");
                        fragment = new ComposeFragment();
                        break;
                    case R.id.miUserProfile:
                        Log.d(TAG, "New post item clicked");
                        fragment = new UserProfileFragment();
                        break;
                    case R.id.miHome:
                    default:
                        Log.d(TAG, "User profile item clicked");
                        fragment = new TimelineFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "FRAGMENT_TAG").commit();
                return true;
            }
        });
        // set default selection
        bottomNavigationView.setSelectedItemId(R.id.miHome);
    }
}