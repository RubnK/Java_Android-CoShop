package com.rhwr.coshop;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ManageListActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 100;

    private ListView membersListView, contactsListView;
    private TextView emptyTextView;
    private Button saveButton, archiveButton, leaveButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String listId;
    private String listOwnerId;
    private boolean isArchived = false;

    private Map<String, String> uidToUsername = new HashMap<>();
    private Set<String> memberUids = new HashSet<>();
    private List<String> contactUserUids = new ArrayList<>();

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            loadUsersAndList();
        }

        saveButton.setOnClickListener(v -> updateListMembers());
        archiveButton.setOnClickListener(v -> {
            if (isArchived) {
                unarchiveList();
            } else {
                archiveList();
            }
        });
        leaveButton.setOnClickListener(v -> leaveList());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadUsersAndList();
        } else {
            Toast.makeText(this, "Permission contacts requise", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Set<String> getPhoneContacts() {
        Set<String> contactNumbers = new HashSet<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (number != null) {
                    number = number.replaceAll("[^+\\d]", "");
                    if (!number.isEmpty()) contactNumbers.add(number);
                }
            }
            cursor.close();
        }
        return contactNumbers;
    }

    private void loadUsersAndList() {
        Set<String> contactNumbers = getPhoneContacts();

        db.collection("users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userDoc : userSnapshots) {
                String uid = userDoc.getId();
                String username = userDoc.getString("username");
                String phone = userDoc.getString("phoneNumber");

                uidToUsername.put(uid, username != null ? username : uid);

                if (phone != null && contactNumbers.contains(phone.replaceAll("[^+\\d]", ""))) {
                    contactUserUids.add(uid);
                }
            }

            db.collection("lists").document(listId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        Map<String, Object> membersMap = (Map<String, Object>) doc.get("members");
                        isArchived = Boolean.TRUE.equals(doc.getBoolean("archived"));

                        if (membersMap != null) {
                            memberUids.clear();
                            for (Map.Entry<String, Object> entry : membersMap.entrySet()) {
                                String uid = entry.getKey();
                                String role = String.valueOf(entry.getValue());
                                memberUids.add(uid);
                                if ("admin".equals(role)) listOwnerId = uid;
                            }
                        }

                        isAdmin = currentUser.getUid().equals(listOwnerId);
                        updateUI();
                    });
        });
    }

    private void updateUI() {
        saveButton.setVisibility((isAdmin && !isArchived) ? View.VISIBLE : View.GONE);
        archiveButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        archiveButton.setText(isArchived ? "Désarchiver" : "Archiver");
        leaveButton.setVisibility(!isAdmin ? View.VISIBLE : View.GONE);

        displayMembers();
        displayContacts();
    }

    private void displayMembers() {
        List<String> displayNames = new ArrayList<>();
        for (String uid : memberUids) {
            String username = uidToUsername.get(uid);
            String suffix = uid.equals(currentUser.getUid()) ? " (vous)" : (isAdmin && !isArchived ? " ❌" : "");
            displayNames.add((username != null ? username : uid) + suffix);
        }

        membersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        membersListView.setAdapter(membersAdapter);

        if (isAdmin && !isArchived) {
            membersListView.setOnItemClickListener((parent, view, position, id) -> {
                String uid = new ArrayList<>(memberUids).get(position);
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
        List<String> contacts = new ArrayList<>();
        for (String uid : contactUserUids) {
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

            if (isAdmin && !isArchived) {
                contactsListView.setOnItemClickListener((parent, view, position, id) -> {
                    String username = contacts.get(position);
                    String uidToAdd = null;
                    for (Map.Entry<String, String> entry : uidToUsername.entrySet()) {
                        if (username.equals(entry.getValue())) {
                            uidToAdd = entry.getKey();
                            break;
                        }
                    }

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
            membersMap.put(uid, uid.equals(listOwnerId) ? "admin" : "member");
        }
        listRef.update("members", membersMap)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Membres mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void archiveList() {
        db.collection("lists").document(listId)
                .update("archived", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Liste archivée", Toast.LENGTH_SHORT).show();
                    isArchived = true;
                    updateUI();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void unarchiveList() {
        db.collection("lists").document(listId)
                .update("archived", false)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Liste désarchivée", Toast.LENGTH_SHORT).show();
                    isArchived = false;
                    updateUI();
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
}
