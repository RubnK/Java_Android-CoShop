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

        // Liste des listes
        listView = findViewById(R.id.listView);
    }
}
