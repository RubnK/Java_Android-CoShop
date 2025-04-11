package com.rhwr.coshop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductActivity extends AppCompatActivity {

    private EditText productEditText;
    private Button addButton;
    private FirebaseDatabase database;
    private DatabaseReference productsRef;
    private String listName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        productEditText = findViewById(R.id.productEditText);
        addButton = findViewById(R.id.addButton);

        // Obtenez le nom de la liste passée en extra
        listName = getIntent().getStringExtra("listName");

        // Initialisation de Firebase
        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("lists").child(listName).child("products");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String product = productEditText.getText().toString().trim();
                if (!product.isEmpty()) {
                    addProductToList(product);
                }
            }
        });
    }

    private void addProductToList(String product) {
        String productId = productsRef.push().getKey();
        if (productId != null) {
            productsRef.child(productId).setValue(product);
        }
    }
}
