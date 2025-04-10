package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private TextView emailTextView;
    private Button signOutButton;
    private Button createGroupButton;
    private Button joinGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation de Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Récupération des éléments de l'interface utilisateur
        emailTextView = findViewById(R.id.emailTextView);
        signOutButton = findViewById(R.id.signOutButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        joinGroupButton = findViewById(R.id.joinGroupButton);

        // Configuration des listeners pour les boutons
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implémenter la logique pour créer un groupe
                Log.d(TAG, "Bouton Créer un groupe cliqué");
                // Lancer une nouvelle activité ou afficher un dialogue
            }
        });

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implémenter la logique pour rejoindre un groupe
                Log.d(TAG, "Bouton Rejoindre un groupe cliqué");
                // Lancer une nouvelle activité ou afficher un dialogue
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Vérifier si l'utilisateur est connecté au démarrage de l'activité
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // L'utilisateur est connecté, afficher son email
            emailTextView.setText(getString(R.string.signed_in_fmt, currentUser.getEmail()));
        } else {
            // L'utilisateur n'est pas connecté, le rediriger vers l'activité de connexion
            navigateToLogin();
        }
    }

    private void signOut() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}