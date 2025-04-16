package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private EditText listNameEditText;
    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference listsRef;
    private ArrayList<String> listNames;
    private ArrayAdapter<String> adapter;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private SearchView searchView;
    private ArrayList<String> allListNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        toolbar = findViewById(R.id.toolBar);
        searchView = findViewById(R.id.searchView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        listNameEditText = findViewById(R.id.listNameEditText);
        listView = findViewById(R.id.listView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        listsRef = db.collection("lists");

        listNames = new ArrayList<>();
        allListNames = new ArrayList<>(); // Initialisation de la liste complète
        adapter = new ArrayAdapter<String>(this, R.layout.activity_list, listNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.activity_list, parent, false);
                }

                TextView listNameTextView = convertView.findViewById(R.id.listNameTextView);
                listNameTextView.setText(listNames.get(position));

                convertView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                    deleteList(listNames.get(position));
                });

                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
                    intent.putExtra("list_id", listNames.get(position));
                    startActivity(intent);
                });

                return convertView;
            }
        };
        listView.setAdapter(adapter);

        // Toolbar
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

        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                String listName = listNameEditText.getText().toString().trim();
                if (!listName.isEmpty()) {
                    createList(listName);
                } else {
                    Toast.makeText(this, "Entrez un nom de liste", Toast.LENGTH_SHORT).show();
                }
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

        // Vérifie si l'utilisateur est connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText("Connecté : " + currentUser.getEmail());
        } else {
            navigateToAuthActivity();
        }

        // Charger les listes existantes
        listsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            listNames.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                String name = document.getString("name");
                if (name != null) {
                    listNames.add(name);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("MainActivity", "Erreur Firestore", e);
        });
        loadUserLists();
        setupSearchView();
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLists(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLists(newText);
                return true;
            }
        });
    }

    private void filterLists(String text) {
        if (text.isEmpty()) {
            // Si le texte est vide, afficher toutes les listes
            listNames.clear();
            listNames.addAll(allListNames);
        } else {
            // Filtrer les listes selon le texte de recherche
            listNames.clear();
            text = text.toLowerCase();
            for (String listName : allListNames) {
                if (listName.toLowerCase().contains(text)) {
                    listNames.add(listName);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_barre_menu, menu);
        return true;
    }

    private void createList(String listName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Map<String, Object> list = new HashMap<>();
            list.put("name", listName);
            list.put("userId", userId);  // Ajout de l'ID utilisateur

            listsRef.add(list)
                    .addOnSuccessListener(doc -> {
                        listNames.add(listName);
                        adapter.notifyDataSetChanged();
                        listNameEditText.setText("");  // Effacer le champ après création
                    })
                    .addOnFailureListener(e ->
                            Log.e("MainActivity", "Erreur création", e)
                    );
        }
    }

    private void deleteList(String listName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            listsRef.whereEqualTo("name", listName)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (DocumentSnapshot doc : snapshots) {
                            doc.getReference().delete();
                        }
                        listNames.remove(listName);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Log.e("MainActivity", "Erreur suppression", e)
                    );
        }
    }
    private void loadUserLists() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Récupère uniquement les listes de l'utilisateur actuel
            listsRef.whereEqualTo("userId", userId).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        listNames.clear();
                        allListNames.clear(); // Vider la liste complète
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            if (name != null) {
                                listNames.add(name);
                                allListNames.add(name); // Ajouter à la liste complète
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        Log.e("MainActivity", "Erreur Firestore", e);
                    });
        }
    }
    private void setupRealtimeUpdates() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            listsRef.whereEqualTo("userId", userId)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e("MainActivity", "Erreur écoute", e);
                            return;
                        }

                        listNames.clear();
                        allListNames.clear(); // Vider la liste complète
                        for (DocumentSnapshot doc : snapshots) {
                            String name = doc.getString("name");
                            if (name != null) {
                                listNames.add(name);
                                allListNames.add(name); // Ajouter à la liste complète
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
        }
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
