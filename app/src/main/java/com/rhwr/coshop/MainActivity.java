package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

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

        fetchUserLists();
    }

    private void fetchUserLists() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("lists")
                .whereEqualTo("archived", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    listNames.clear();
                    listIds.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        Map<String, Object> members = (Map<String, Object>) doc.get("members");
                        if (members != null && members.containsKey(user.getUid())) {
                            listNames.add(doc.getString("name"));
                            listIds.add(doc.getId());
                        }
                    }

                    if (listNames.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Erreur Firestore", e));
    }

    private void navigateToAuthActivity() {
        startActivity(new Intent(MainActivity.this, AuthActivity.class));
        finish();
    }
}
