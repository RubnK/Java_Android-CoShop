package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerifyPhoneActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String verificationId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        mAuth = FirebaseAuth.getInstance();
        EditText codeEditText = findViewById(R.id.codeEditText);
        Button verifyButton = findViewById(R.id.verifyButton);

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
            linkPhoneWithEmail(credential);
        }
    }

    private void linkPhoneWithEmail(PhoneAuthCredential credential) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            if (username != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                                    saveUserToFirestore(user);
                                    goToMain();
                                });
                            } else {
                                saveUserToFirestore(user);
                                goToMain();
                            }
                        } else {
                            Toast.makeText(this, "Erreur de liaison : " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveUserToFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getDisplayName());
        userData.put("email", user.getEmail());
        userData.put("phoneNumber", user.getPhoneNumber());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Log ou toast si besoin
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur enregistrement Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
