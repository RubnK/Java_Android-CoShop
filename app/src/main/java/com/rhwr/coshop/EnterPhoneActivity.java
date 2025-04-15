package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class EnterPhoneActivity extends AppCompatActivity {

    private EditText phoneEditText;
    private Button sendCodeButton;
    private FirebaseAuth mAuth;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone);

        mAuth = FirebaseAuth.getInstance();

        username = getIntent().getStringExtra("username");
        phoneEditText = findViewById(R.id.phoneEditText);
        sendCodeButton = findViewById(R.id.sendCodeButton);

        sendCodeButton.setOnClickListener(v -> {
            String phoneNumber = phoneEditText.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
                phoneEditText.setError("Numéro invalide");
                return;
            }

            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+33" + phoneNumber.substring(1);
            }

            sendVerificationCode(phoneNumber);
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {}

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(EnterPhoneActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Intent intent = new Intent(EnterPhoneActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("username", username);
                    intent.putExtra("phone", phoneEditText.getText().toString().trim());
                    startActivity(intent);
                }
            };
    }