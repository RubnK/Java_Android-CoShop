package com.rhwr.coshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText usernameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    public void attemptRegistration(View view) {
        final String username = usernameEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateForm(username, phone, email, password, confirmPassword)) {
            return;
        }

        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                writeNewUser(user.getUid(), username, email, phone);
                                sendEmailVerification(user); // Passez l'utilisateur pour être sûr
                                Toast.makeText(RegisterActivity.this, R.string.registration_success_check_email,
                                        Toast.LENGTH_SHORT).show();
                                finish(); // Retourner à l'écran d'accueil
                            }
                        } else {
                            // Gestion des erreurs d'authentification spécifiques
                            String errorMessage = getString(R.string.auth_failed);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                errorMessage = getString(R.string.error_weak_password);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = getString(R.string.error_invalid_email);
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = getString(R.string.error_email_exists);
                            } catch (FirebaseNetworkException e) {
                                errorMessage = getString(R.string.error_network);
                            } catch (FirebaseAuthException e) {
                                errorMessage = getString(R.string.error_auth_generic) + ": " + e.getMessage();
                            } catch (Exception e) {
                                Log.e(TAG, "createUserWithEmail:exception", e);
                            }
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        hideProgressBar();
                    }
                });
    }

    private void writeNewUser(String userId, String username, String email, String phone) {
        User user = new User(username, email, phone);
        mDatabase.child(userId).setValue(user);
    }

    private void sendEmailVerification(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "sendEmailVerification:success");
                                Toast.makeText(RegisterActivity.this, R.string.email_sent,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "sendEmailVerification failed.", task.getException());
                                Toast.makeText(RegisterActivity.this, R.string.verification_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "sendEmailVerification onFailure: ", e);
                            Toast.makeText(RegisterActivity.this, "Erreur lors de l'envoi de l'e-mail.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "sendEmailVerification: User is null!");
            Toast.makeText(RegisterActivity.this, "Erreur: Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm(String username, String phone, String email, String password, String confirmPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.required));
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError(getString(R.string.required));
            valid = false;
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            phoneEditText.setError(getString(R.string.invalid_phone_number));
            valid = false;
        } else {
            phoneEditText.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.required));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.required));
            valid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.weak_password));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.required));
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError(getString(R.string.error_passwords_do_not_match));
            valid = false;
        } else {
            confirmPasswordEditText.setError(null);
        }

        return valid;
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public static class User {
        public String username;
        public String email;
        public String phone;

        public User() {
            // Constructeur par défaut requis pour DataSnapshot.getValue(User.class)
        }

        public User(String username, String email, String phone) {
            this.username = username;
            this.email = email;
            this.phone = phone;
        }
    }
}