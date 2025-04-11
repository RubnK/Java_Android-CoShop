package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ListDetailActivity extends AppCompatActivity {

    private EditText productEditText;
    private Button addProductButton;
    private ListView productListView;
    private ArrayList<String> productList;
    private ProductAdapter adapter;

    private String listId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        // Initialiser FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Récupérer l'ID de la liste passé via l'Intent
        listId = getIntent().getStringExtra("list_id");

        if (listId == null) {
            // Gérer l'erreur si l'ID est null
            Log.e("ListDetailActivity", "Liste ID est null");
            return;
        }

        // Initialiser les vues
        productEditText = findViewById(R.id.productEditText);
        addProductButton = findViewById(R.id.addProductButton);
        productListView = findViewById(R.id.productListView);

        // Initialiser la liste des produits
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        productListView.setAdapter(adapter);

        // Charger les produits
        loadProducts();

        // Ajouter un produit
        addProductButton.setOnClickListener(v -> {
            String productName = productEditText.getText().toString().trim();
            if (!productName.isEmpty()) {
                addProductToList(productName);
            } else {
                Toast.makeText(ListDetailActivity.this, "Veuillez entrer un nom de produit", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        db.collection("lists")
                .document(listId)
                .collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("name");
                            if (productName != null) {
                                productList.add(productName);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ListDetailActivity.this, "Erreur lors du chargement des produits", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addProductToList(String productName) {
        db.collection("lists")
                .document(listId)
                .collection("products")
                .add(new Product(productName))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ListDetailActivity.this, "Produit ajouté", Toast.LENGTH_SHORT).show();
                    productEditText.setText("");
                    loadProducts();  // Recharge les produits après l'ajout
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ListDetailActivity.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                });
    }
}
