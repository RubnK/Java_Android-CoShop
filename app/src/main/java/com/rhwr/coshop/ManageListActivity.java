package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ManageListActivity extends AppCompatActivity {

    private ListView membersListView, contactsListView;
    private TextView emptyTextView;
    private Button saveButton, archiveButton, leaveButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String listId;
    private String listOwnerId;

    private Map<String, String> uidToUsername = new HashMap<>();
    private Set<String> memberUids = new HashSet<>();
    private ArrayList<String> allUserUids = new ArrayList<>();

    private ArrayAdapter<String> membersAdapter;
    private ArrayAdapter<String> contactsAdapter;

    private boolean isAdmin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        membersListView = findViewById(R.id.membersListView);
        contactsListView = findViewById(R.id.contactsListView);
        emptyTextView = findViewById(R.id.emptyTextView);
        saveButton = findViewById(R.id.saveButton);
        archiveButton = findViewById(R.id.archiveButton);
        leaveButton = findViewById(R.id.leaveButton);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        listId = getIntent().getStringExtra("list_id");

        if (listId == null || currentUser == null) {
            finish();
            return;
        }

        loadUsersAndList();

        saveButton.setOnClickListener(v -> updateListMembers());
        archiveButton.setOnClickListener(v -> archiveList());
        leaveButton.setOnClickListener(v -> leaveList());
    }

    private void loadUsersAndList() {
        db.collection("users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userDoc : userSnapshots) {
                String uid = userDoc.getId();
                String username = userDoc.getString("username");
                uidToUsername.put(uid, username != null ? username : uid);
                allUserUids.add(uid);
            }

            db.collection("lists").document(listId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        Map<String, Object> membersMap = (Map<String, Object>) doc.get("members");
                        if (membersMap != null) {
                            memberUids.clear();
                            for (Map.Entry<String, Object> entry : membersMap.entrySet()) {
                                String uid = entry.getKey();
                                String role = String.valueOf(entry.getValue());

                                memberUids.add(uid);

                                // Si c'est l'admin, on le stocke
                                if ("admin".equals(role)) {
                                    listOwnerId = uid;
                                }
                            }
                        }

                        // Vérifie si l'utilisateur actuel est l'admin
                        isAdmin = currentUser.getUid().equals(listOwnerId);


                        updateUI();
                    });
        });
    }

    private void updateUI() {
        saveButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        archiveButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        leaveButton.setVisibility(!isAdmin ? View.VISIBLE : View.GONE);
        displayMembers();
        displayContacts();
    }

    private void displayMembers() {
        ArrayList<String> displayNames = new ArrayList<>();
        for (String uid : memberUids) {
            String username = uidToUsername.get(uid);
            String suffix = uid.equals(currentUser.getUid()) ? " (vous)" : (isAdmin ? " ❌" : "");
            displayNames.add(username + suffix);
        }

        membersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        membersListView.setAdapter(membersAdapter);

        if (isAdmin) {
            membersListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedDisplay = displayNames.get(position);
                String uid = getUidFromDisplayName(selectedDisplay);

                if (!uid.equals(currentUser.getUid())) {
                    memberUids.remove(uid);
                    updateUI();
                } else {
                    Toast.makeText(this, "Vous ne pouvez pas vous supprimer", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayContacts() {
        ArrayList<String> contacts = new ArrayList<>();
        for (String uid : allUserUids) {
            if (!memberUids.contains(uid)) {
                contacts.add(uidToUsername.get(uid));
            }
        }

        if (contacts.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            contactsListView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            contactsListView.setVisibility(View.VISIBLE);

            contactsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contacts);
            contactsListView.setAdapter(contactsAdapter);

            if (isAdmin) {
                contactsListView.setOnItemClickListener((parent, view, position, id) -> {
                    String username = contacts.get(position);
                    String uidToAdd = getUidFromDisplayName(username);

                    if (uidToAdd != null && !memberUids.contains(uidToAdd)) {
                        memberUids.add(uidToAdd);
                        updateUI();
                    }
                });
            }
        }
    }

    private void updateListMembers() {
        DocumentReference listRef = db.collection("lists").document(listId);
        Map<String, Object> membersMap = new HashMap<>();
        for (String uid : memberUids) {
            if (uid.equals(listOwnerId)) {
                membersMap.put(uid, "admin");
            } else {
                membersMap.put(uid, "member");
            }
        }
        listRef.update("members", membersMap)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Membres mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void archiveList() {
        db.collection("lists").document(listId)
                .update("archived", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Liste archiviée", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void leaveList() {
        if (memberUids.contains(currentUser.getUid())) {
            memberUids.remove(currentUser.getUid());
            updateListMembers();
            Toast.makeText(this, "Vous avez quitté la liste", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }
    }

    private String getUidFromDisplayName(String displayName) {
        for (Map.Entry<String, String> entry : uidToUsername.entrySet()) {
            if (displayName.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}