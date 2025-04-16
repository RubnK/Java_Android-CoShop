package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private EditText listNameEditText;
    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference listsRef;
    private ArrayList<String> listIds;
    private ArrayList<String> listNames;
    private ArrayAdapter<String> adapter;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_barre_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolBar);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        listNameEditText = findViewById(R.id.listNameEditText);
        listView = findViewById(R.id.listView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        listsRef = db.collection("lists");

        listIds = new ArrayList<>();
        listNames = new ArrayList<>();

        adapter = new ArrayAdapter<String>(this, R.layout.activity_list, listNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.activity_list, parent, false);
                }

                TextView listNameTextView = convertView.findViewById(R.id.listNameTextView);
                listNameTextView.setText(listNames.get(position));

                convertView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                    deleteList(listIds.get(position));
                });

                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
                    intent.putExtra("list_id", listIds.get(position));
                    startActivity(intent);
                });

                return convertView;
            }
        };
        listView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(view ->
                Toast.makeText(MainActivity.this, "Menu navigation cliqué", Toast.LENGTH_SHORT).show()
        );

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_fav) {
                Toast.makeText(MainActivity.this, "Voir les favoris", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_notif) {
                Toast.makeText(MainActivity.this, "Voir les notifications", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_search) {
                Toast.makeText(MainActivity.this, "Recherche", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                CreateListDialogFragment dialog = new CreateListDialogFragment();
                dialog.show(getSupportFragmentManager(), "CreateListDialog");
                return true;
            } else if (id == R.id.nav_home) {
                Toast.makeText(this, "Accueil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_archive) {
                Toast.makeText(this, "Archives", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profil) {
                Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_paramettre) {
                signOut();
                return true;
            }
            return false;
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText("Connecté : " + currentUser.getEmail());
        } else {
            navigateToAuthActivity();
        }

        setupRealtimeUpdates();
    }

    private void deleteList(String listId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        listsRef.document(listId).get().addOnSuccessListener(doc -> {
            Map<String, Object> members = (Map<String, Object>) doc.get("members");
            if (members != null && "admin".equals(members.get(currentUser.getUid()))) {
                listsRef.document(listId).delete().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Liste supprimée", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "Vous n'êtes pas admin de cette liste", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRealtimeUpdates() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        listsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("MainActivity", "Erreur Firestore : ", e);
                return;
            }

            listIds.clear();
            listNames.clear();

            for (DocumentSnapshot doc : snapshots) {
                Map<String, Object> members = (Map<String, Object>) doc.get("members");
                if (members != null && members.containsKey(currentUser.getUid())) {
                    listIds.add(doc.getId());
                    listNames.add(doc.getString("name"));
                }
            }

            adapter.notifyDataSetChanged();
        });
    }

    private void signOut() {
        mAuth.signOut();
        navigateToAuthActivity();
    }

    private void navigateToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
