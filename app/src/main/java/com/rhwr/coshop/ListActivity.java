package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private FirebaseDatabase database;
    private DatabaseReference listsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Initialiser la base de données Firebase
        database = FirebaseDatabase.getInstance();
        listsRef = database.getReference("lists");

        // Liste des listes (À implémenter avec un adapter pour afficher les données)
        listView = findViewById(R.id.listView);

        // Vous pouvez utiliser FirebaseRecyclerAdapter ou d'autres méthodes pour peupler la liste
        // Exemple d'utilisation d'un adapter avec les données récupérées de Firebase
    }
}
