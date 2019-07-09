package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    @BindView(R.id.etUsernameInput) EditText etUsernameInput;
    @BindView(R.id.etEmailInput) EditText etEmailInput;
    @BindView(R.id.etPasswordInput) EditText etPasswordInput;
    @BindView(R.id.etConfirmPasswordInput) EditText etConfirmPasswordInput;

    private final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        Intent i = getIntent();
        String username = i.getStringExtra("username");
        String password = i.getStringExtra("password");

        if (!username.equals("")) {
            etUsernameInput.setText(username);
        }
        if (!password.equals("")) {
            etPasswordInput.setText(password);
        }
    }

    @OnClick(R.id.btnSignUp)
    public void signup() {
        final String username = etUsernameInput.getText().toString();
        final String email = etEmailInput.getText().toString();
        final String password = etPasswordInput.getText().toString();
        final String confirmPassword = etConfirmPasswordInput.getText().toString();
        if (password.contentEquals(confirmPassword)) {
            //make new user
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "Sign up successful!");
                        Intent i = new Intent(SignUpActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Log.d(TAG, "Sign up not successful");
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
        }
    }
}
