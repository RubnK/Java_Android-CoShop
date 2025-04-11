package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class VerifyPhoneActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Button verifyButton;
    private FirebaseAuth mAuth;

    private String verificationId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        mAuth = FirebaseAuth.getInstance();

        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);

        verificationId = getIntent().getStringExtra("verificationId");
        username = getIntent().getStringExtra("username");

        verifyButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();

            if (TextUtils.isEmpty(code) || code.length() < 6) {
                codeEditText.setError("Code invalide");
                return;
            }

            verifyCode(code);
        });
    }

    private void verifyCode(String code) {
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && username != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username).build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("Firebase", "Nom d'utilisateur mis à jour");
                                        }
                                        goToMain();
                                    });
                        } else {
                            goToMain();
                        }
                    } else {
                        Toast.makeText(this, "Échec de la vérification", Toast.LENGTH_LONG).show();
                        Log.e("FirebaseAuth", "SignIn failed", task.getException());
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
