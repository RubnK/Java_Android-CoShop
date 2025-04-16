package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView infoTextView;
    private Button checkButton;
    private FirebaseAuth mAuth;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        infoTextView = findViewById(R.id.infoTextView);
        checkButton = findViewById(R.id.checkButton);
        mAuth = FirebaseAuth.getInstance();
        username = getIntent().getStringExtra("username");

        checkButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (user.isEmailVerified()) {
                        Intent intent = new Intent(this, EnterPhoneActivity.class);
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "E-mail non vérifié", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}