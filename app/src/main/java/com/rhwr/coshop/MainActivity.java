package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private Button signOutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEmailTextView = findViewById(R.id.userEmailTextView);
        signOutButton = findViewById(R.id.signOutButton);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText("Connecté en tant que : " + currentUser.getEmail());
        } else {
            navigateToAuthActivity();
            finish();
        }

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        navigateToAuthActivity();
        finish();
    }

    private void navigateToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}