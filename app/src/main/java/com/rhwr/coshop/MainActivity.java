package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private EditText listNameEditText;
    private Button createListButton;
    private Button signOutButton;
    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference listsRef;
    private ArrayList<String> listNames;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        userEmailTextView = findViewById(R.id.userEmailTextView);
        listNameEditText = findViewById(R.id.listNameEditText);
        createListButton = findViewById(R.id.createListButton);
        signOutButton = findViewById(R.id.signOutButton);
        listView = findViewById(R.id.listView);

        // Initialisation de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        listsRef = db.collection("lists");

        // Liste des noms de liste (pour l'affichage dans ListView)
        listNames = new ArrayList<>();

        // Adapter personnalisé pour afficher les listes avec un bouton de suppression
        adapter = new ArrayAdapter<String>(this, R.layout.activity_list, listNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.activity_list, parent, false);
                }

                TextView listNameTextView = convertView.findViewById(R.id.listNameTextView);
                Button deleteButton = convertView.findViewById(R.id.deleteButton);

                // Récupérer le nom de la liste et l'afficher
                String listName = listNames.get(position);
                listNameTextView.setText(listName);

                // Configurer le bouton de suppression
                deleteButton.setOnClickListener(v -> {
                    deleteList(listName);
                });

                // Gérer le clic sur l'élément pour passer à ListDetailActivity
                convertView.setOnClickListener(v -> {
                    String selectedListName = listNames.get(position);
                    // Utilise l'identifiant du document Firestore pour passer à ListDetailActivity
                    Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
                    intent.putExtra("list_id", selectedListName);  // Passe l'ID de la liste ou le nom
                    startActivity(intent);
                });

                return convertView;
            }
        };
        listView.setAdapter(adapter);

        // Vérifier si l'utilisateur est connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText("Connecté en tant que : " + currentUser.getEmail());
        } else {
            navigateToAuthActivity();
            finish();
        }

        // Définir un listener pour la déconnexion
        signOutButton.setOnClickListener(v -> signOut());

        // Nouveau listener pour créer une liste
        createListButton.setOnClickListener(v -> {
            String listName = listNameEditText.getText().toString().trim();
            if (!listName.isEmpty()) {
                Log.d("MainActivity", "Tentative de création de la liste : " + listName);
                createList(listName);
            } else {
                Log.d("MainActivity", "Nom de la liste vide !");
            }
        });

        // Écouter les changements dans Firestore pour afficher les listes
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
            Log.e("MainActivity", "Erreur lors de la récupération des listes", e);
        });
    }

    private void signOut() {
        mAuth.signOut();
        navigateToAuthActivity();
        finish();
    }

    private void navigateToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void createList(String listName) {
        // Créer une nouvelle liste dans Firestore
        Map<String, Object> list = new HashMap<>();
        list.put("name", listName);

        listsRef.add(list)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MainActivity", "Liste créée avec succès : " + documentReference.getId());
                    listNames.add(listName);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w("MainActivity", "Erreur lors de la création de la liste", e);
                });
    }

    private void deleteList(String listName) {
        // Suppression de la liste dans Firestore
        listsRef.whereEqualTo("name", listName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    listNames.remove(listName);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Erreur lors de la suppression de la liste", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Erreur lors de la récupération de la liste", e);
                });
    }
}