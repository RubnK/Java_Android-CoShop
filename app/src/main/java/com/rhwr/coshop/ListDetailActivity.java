package com.rhwr.coshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;

public class ListDetailActivity extends BaseActivity {
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
        setBaseContentView(R.layout.activity_list_detail);
        setupNavigationBar(findViewById(R.id.bottomNavigationView));

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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
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

            adapter = new ProductAdapter(this, productList, listId, isReadOnly);
            productListView.setAdapter(adapter);

            if (isReadOnly) {
                productEditText.setEnabled(false);
                quantityEditText.setEnabled(false);
                categorySpinner.setEnabled(false);
                addProductButton.setEnabled(false);
            } else {
                addProductButton.setOnClickListener(v -> {
                    String name = productEditText.getText().toString().trim();
                    String quantityStr = quantityEditText.getText().toString().trim();
                    int quantity = 1;
                    if (!quantityStr.isEmpty()) {
                        try {
                            quantity = Integer.parseInt(quantityStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Quantité invalide", Toast.LENGTH_SHORT).show();
                        }
                    }

                    String category = CATEGORIES[categorySpinner.getSelectedItemPosition()];
                    if (!name.isEmpty()) {
                        addProductToList(name, quantity, category);
                    }
                });
            }

            loadProducts();
        });
    }

    private void loadProducts() {
        db.collection("lists").document(listId).collection("products")
                .get()
                .addOnSuccessListener(query -> {
                    productList.clear();
                    for (DocumentSnapshot doc : query) {
                        String name = doc.getString("name");
                        Long quantity = doc.getLong("quantity");
                        String category = doc.getString("category");
                        Boolean purchased = doc.getBoolean("purchased");

                        if (name != null) {
                            productList.add(new Product(
                                    name,
                                    quantity != null ? quantity.intValue() : 1,
                                    category != null ? category : "Autre",
                                    purchased != null && purchased
                            ));
                        }
                    }

                    Collections.sort(productList, (p1, p2) -> {
                        int cmp = p1.getCategory().compareToIgnoreCase(p2.getCategory());
                        return cmp != 0 ? cmp : p1.getName().compareToIgnoreCase(p2.getName());
                    });

                    adapter.notifyDataSetChanged();
                });
    }

    private void addProductToList(String name, int quantity, String category) {
        Product newProduct = new Product(name, quantity, category, false);

        db.collection("lists").document(listId).collection("products")
                .add(newProduct)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Produit ajouté", Toast.LENGTH_SHORT).show();
                    productEditText.setText("");
                    quantityEditText.setText("");
                    categorySpinner.setSelection(0);
                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (id == R.id.manage_list && !isReadOnly) {
            Intent intent = new Intent(this, ManageListActivity.class);
            intent.putExtra("list_id", listId);
            startActivity(intent);
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
