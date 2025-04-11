package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UsernameActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        usernameEditText = findViewById(R.id.usernameEditText);
        nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Veuillez entrer un nom");
            } else {
                Intent intent = new Intent(UsernameActivity.this, EnterPhoneActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }
}
