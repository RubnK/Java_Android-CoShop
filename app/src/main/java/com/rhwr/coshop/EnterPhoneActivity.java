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

            sendVerificationCode(phoneNumber);
        });
    }

    private void sendVerificationCode(String phoneNumber) {

        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+33" + phoneNumber.substring(1);
        }

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
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto-verification possible (instant)
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(EnterPhoneActivity.this, "Échec : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("PhoneAuth", "VerificationFailed", e);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Intent intent = new Intent(EnterPhoneActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("username", username);
                    intent.putExtra("phone", phoneEditText.getText().toString().trim());
                    startActivity(intent);
                }
            };
}
