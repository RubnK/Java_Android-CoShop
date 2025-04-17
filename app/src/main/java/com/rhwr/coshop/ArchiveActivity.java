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

public class ArchiveActivity extends BaseActivity {

    private ListView listView;
    private TextView archiveEmptyText;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listNames = new ArrayList<>();
    private ArrayList<String> listIds = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBaseContentView(R.layout.activity_archive);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_archive);
        setupNavigationBar(nav);

        listView = findViewById(R.id.archiveListView);
        archiveEmptyText = findViewById(R.id.archiveEmptyText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedListId = listIds.get(position);
            Intent intent = new Intent(ArchiveActivity.this, ListDetailActivity.class);
            intent.putExtra("list_id", selectedListId);
            startActivity(intent);
        });

        fetchArchivedLists();
    }

    private void fetchArchivedLists() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("lists")
                .whereEqualTo("archived", true)
                .get()
                .addOnSuccessListener(query -> {
                    listNames.clear();
                    listIds.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Map<String, Object> members = (Map<String, Object>) doc.get("members");
                        if (members != null && members.containsKey(currentUser.getUid())) {
                            String name = doc.getString("name");
                            listNames.add(name != null ? name : "(Sans nom)");
                            listIds.add(doc.getId());
                        }
                    }

                    archiveEmptyText.setVisibility(listNames.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ArchiveActivity", "Erreur récupération archives", e));
    }
}
