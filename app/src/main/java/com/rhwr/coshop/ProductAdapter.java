package com.rhwr.coshop;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private FirebaseFirestore db;
    private String listId;

    public ProductAdapter(Context context, List<Product> products, String listId) {
        super(context, 0, products);
        this.listId = listId;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_item, parent, false);
        }

        Product product = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.productNameTextView);
        TextView quantityTextView = convertView.findViewById(R.id.productQuantityTextView);
        TextView categoryTextView = convertView.findViewById(R.id.productCategoryTextView);
        Button decreaseButton = convertView.findViewById(R.id.decreaseButton);
        Button increaseButton = convertView.findViewById(R.id.increaseButton);
        CheckBox purchasedCheckBox = convertView.findViewById(R.id.purchasedCheckBox);

        nameTextView.setText(product.getName());
        quantityTextView.setText(String.valueOf(product.getQuantity()));
        categoryTextView.setText(product.getCategory());

        // Définir l'état de la case à cocher
        purchasedCheckBox.setChecked(product.isPurchased());

        // Appliquer le style barré au texte si le produit est acheté
        if (product.isPurchased()) {
            nameTextView.setPaintFlags(nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            nameTextView.setPaintFlags(nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Gérer le changement d'état de la case à cocher
        purchasedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setPurchased(isChecked);
            updateProductPurchasedState(product, isChecked);

            // Mettre à jour l'apparence du texte
            if (isChecked) {
                nameTextView.setPaintFlags(nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                nameTextView.setPaintFlags(nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });

        // Gérer le bouton de diminution
        decreaseButton.setOnClickListener(v -> {
            int currentQuantity = product.getQuantity();
            if (currentQuantity > 1) {
                updateProductQuantity(product, currentQuantity - 1, position);
            }
        });

        // Gérer le bouton d'augmentation
        increaseButton.setOnClickListener(v -> {
            int currentQuantity = product.getQuantity();
            updateProductQuantity(product, currentQuantity + 1, position);
        });

        return convertView;
    }

    private void updateProductQuantity(Product product, int newQuantity, int position) {
        // On commence par mettre à jour l'interface utilisateur immédiatement
        product.setQuantity(newQuantity);
        notifyDataSetChanged();

        // Ensuite on met à jour Firestore
        db.collection("lists")
                .document(listId)
                .collection("products")
                .whereEqualTo("name", product.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().get(0).getReference()
                                .update("quantity", newQuantity)
                                .addOnFailureListener(e -> {
                                    // En cas d'échec, on revient à l'ancienne quantité
                                    product.setQuantity(product.getQuantity());
                                    notifyDataSetChanged();
                                });
                    }
                });
    }

    private void updateProductPurchasedState(Product product, boolean isPurchased) {
        // Mettre à jour Firestore
        db.collection("lists")
                .document(listId)
                .collection("products")
                .whereEqualTo("name", product.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().get(0).getReference()
                                .update("purchased", isPurchased)
                                .addOnFailureListener(e -> {
                                    // En cas d'échec, on revient à l'état précédent
                                    product.setPurchased(!isPurchased);
                                    notifyDataSetChanged();
                                });
                    }
                });
    }
}