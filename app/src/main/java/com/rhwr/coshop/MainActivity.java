package com.rhwr.coshop;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private TextView userEmailTextView;
    private ListView listView;
    private TextView emptyTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<String> listNames = new ArrayList<>();
    private ArrayList<String> listIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private ListenerRegistration listListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBaseContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_home);

        userEmailTextView = findViewById(R.id.userEmailTextView);
        listView = findViewById(R.id.listView);
        emptyTextView = findViewById(R.id.mainEmptyText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> Log.d("FCM", "Token: " + token));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedListId = listIds.get(position);
            Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
            intent.putExtra("list_id", selectedListId);
            startActivity(intent);
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText("Connecté : " + currentUser.getEmail());
        } else {
            navigateToAuthActivity();
        }

        checkNotificationPermission();
        fetchUserLists();
    }

    private void fetchUserLists() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        if (listListener != null) listListener.remove();

        listListener = db.collection("lists")
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        Log.e("MainActivity", "Erreur Firestore : ", error);
                        return;
                    }

                    listNames.clear();
                    listIds.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Map<String, Object> members = (Map<String, Object>) doc.get("members");
                        Boolean archived = doc.getBoolean("archived");

                        if (members != null && members.containsKey(user.getUid()) && (archived == null || !archived)) {
                            listNames.add(doc.getString("name"));
                            listIds.add(doc.getId());
                        }
                    }

                    emptyTextView.setVisibility(listNames.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                });
    }

    private void navigateToAuthActivity() {
        startActivity(new Intent(MainActivity.this, AuthActivity.class));
        finish();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listListener != null) {
            listListener.remove();
        }
    }
}
