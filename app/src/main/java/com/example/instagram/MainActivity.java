package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.etUsernameInput) EditText etUsernameInput;
    @BindView(R.id.etPasswordInput) EditText etPasswordInput;
    @BindView(R.id.btnLogin) Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checks if currently logged in
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
        } else {
            Toast.makeText(getApplicationContext(), "Logged in as " + currentUser.getUsername(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "Current user is " + currentUser.getUsername());
            final Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }

    }

    @OnClick(R.id.btnLogin)
    public void getLoginInfo() {
        final String username = etUsernameInput.getText().toString();
        final String password = etPasswordInput.getText().toString();

        login(username, password);
    }

    private void login(String username, String password) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e == null) {
                Log.d("LoginActivity", "Login successful");
                final Intent i = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            } else {
                Log.e("LoginActivity", "Login failure");
                e.printStackTrace();
            }
        });
    }

    @OnClick(R.id.btnSignUp)
    void signUp() {
        Intent i = new Intent(MainActivity.this, SignUpActivity.class);
        String username = etUsernameInput.getText().toString();
        String password = etPasswordInput.getText().toString();
        i.putExtra("username", username);
        i.putExtra("password", password);
        startActivity(i);
        finish();
    }
}

// N.B. to add in session logic:
// check if session is currently available for user
// in onCreate, direct to appropriate area