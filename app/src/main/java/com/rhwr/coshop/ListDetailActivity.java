package com.rhwr.coshop;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class ListDetailActivity extends AppCompatActivity {
    private EditText productEditText;
    private EditText quantityEditText;
    private Spinner categorySpinner;
    private Button addProductButton;
    private ListView productListView;
    private ArrayList<Product> productList;
    private ProductAdapter adapter;

    private String listId;
    private boolean isReadOnly = false;
    private FirebaseFirestore db;

    private enum SortMode { NONE, ALPHABETICAL, CATEGORY }
    private SortMode currentSortMode = SortMode.NONE;

    private final String[] CATEGORIES = new String[]{
            "Fruits et légumes",
            "Produits laitiers",
            "Viandes et poissons",
            "Épicerie",
            "Boissons",
            "Hygiène et beauté",
            "Entretien ménager",
            "Surgelés",
            "Pain et pâtisseries",
            "Autre"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        db = FirebaseFirestore.getInstance();

        listId = getIntent().getStringExtra("list_id");

        if (listId == null) {
            Log.e("ListDetailActivity", "Liste ID est null");
            finish();
            return;
        }

        productEditText = findViewById(R.id.productEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        addProductButton = findViewById(R.id.addProductButton);
        productListView = findViewById(R.id.productListView);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        categorySpinner.setAdapter(spinnerAdapter);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, listId, isReadOnly);
        productListView.setAdapter(adapter);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails de la liste");
        }

        checkIfArchivedAndInitialize();
    }

    private void checkIfArchivedAndInitialize() {
        db.collection("lists").document(listId).get().addOnSuccessListener(doc -> {
            Boolean archived = doc.getBoolean("archived");
            isReadOnly = archived != null && archived;

            if (isReadOnly) {
                productEditText.setEnabled(false);
                quantityEditText.setEnabled(false);
                categorySpinner.setEnabled(false);
                addProductButton.setEnabled(false);
            } else {
                addProductButton.setOnClickListener(v -> {
                    String productName = productEditText.getText().toString().trim();
                    String quantityStr = quantityEditText.getText().toString().trim();

                    if (productName.isEmpty()) {
                        Toast.makeText(ListDetailActivity.this, "Veuillez entrer un nom de produit", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int quantity = 1;
                    if (!quantityStr.isEmpty()) {
                        try {
                            quantity = Integer.parseInt(quantityStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(ListDetailActivity.this, "Quantité invalide, utilisation de 1", Toast.LENGTH_SHORT).show();
                        }
                    }

                    String category = CATEGORIES[categorySpinner.getSelectedItemPosition()];
                    addProductToList(productName, quantity, category);
                });
            }

            loadProducts();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_alphabetical) {
            currentSortMode = SortMode.ALPHABETICAL;
            sortProductList();
            return true;
        } else if (id == R.id.sort_by_category) {
            currentSortMode = SortMode.CATEGORY;
            sortProductList();
            return true;
        } else if (id == R.id.manage_list && !isReadOnly) {
            Intent intent = new Intent(this, ManageListActivity.class);
            intent.putExtra("list_id", listId);
            startActivity(intent);
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortProductList() {
        if (currentSortMode == SortMode.ALPHABETICAL) {
            Collections.sort(productList, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        } else if (currentSortMode == SortMode.CATEGORY) {
            Collections.sort(productList, (p1, p2) -> {
                int categoryCompare = p1.getCategory().compareToIgnoreCase(p2.getCategory());
                if (categoryCompare == 0) {
                    return p1.getName().compareToIgnoreCase(p2.getName());
                }
                return categoryCompare;
            });
        }
        adapter.notifyDataSetChanged();
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
                            Long quantityLong = document.getLong("quantity");
                            String category = document.getString("category");

                            int quantity = quantityLong != null ? quantityLong.intValue() : 1;

                            if (productName != null) {
                                Product product = new Product(productName, quantity, category != null ? category : "Autre");
                                productList.add(product);
                            }
                        }

                        if (currentSortMode != SortMode.NONE) {
                            sortProductList();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ListDetailActivity.this, "Erreur lors du chargement des produits", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addProductToList(String productName, int quantity, String category) {
        Product newProduct = new Product(productName, quantity, category);

        db.collection("lists")
                .document(listId)
                .collection("products")
                .add(newProduct)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ListDetailActivity.this, "Produit ajouté", Toast.LENGTH_SHORT).show();
                    productEditText.setText("");
                    quantityEditText.setText("");
                    categorySpinner.setSelection(0);
                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ListDetailActivity.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                });
    }
}
