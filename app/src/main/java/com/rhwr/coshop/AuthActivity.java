package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView welcomeTextView;
    private Button loginButton;
    private Button registerButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        logoImageView = findViewById(R.id.logoImageView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Vérifier si l'utilisateur est déjà connecté et son e-mail est vérifié
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            navigateToMainActivity();
            finish(); // Empêcher le retour à l'écran d'authentification
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}