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
    private boolean isReadOnly;

    public ProductAdapter(Context context, List<Product> products, String listId, boolean isReadOnly) {
        super(context, 0, products);
        this.listId = listId;
        this.db = FirebaseFirestore.getInstance();
        this.isReadOnly = isReadOnly;
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
        purchasedCheckBox.setChecked(product.isPurchased());

        updateStrikeStyle(nameTextView, product.isPurchased());

        if (isReadOnly) {
            decreaseButton.setEnabled(false);
            increaseButton.setEnabled(false);
            purchasedCheckBox.setEnabled(false);
        } else {
            purchasedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setPurchased(isChecked);
                updateStrikeStyle(nameTextView, isChecked);
                updateProductPurchasedState(product, isChecked);
            });

            decreaseButton.setOnClickListener(v -> {
                int currentQuantity = product.getQuantity();
                if (currentQuantity > 1) {
                    updateProductQuantity(product, currentQuantity - 1);
                }
            });

            increaseButton.setOnClickListener(v -> {
                int currentQuantity = product.getQuantity();
                updateProductQuantity(product, currentQuantity + 1);
            });
        }

        return convertView;
    }

    private void updateStrikeStyle(TextView textView, boolean isStruck) {
        if (isStruck) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void updateProductQuantity(Product product, int newQuantity) {
        product.setQuantity(newQuantity);
        notifyDataSetChanged();

        db.collection("lists")
                .document(listId)
                .collection("products")
                .whereEqualTo("name", product.getName())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        snapshots.getDocuments().get(0).getReference()
                                .update("quantity", newQuantity);
                    }
                });
    }

    private void updateProductPurchasedState(Product product, boolean isPurchased) {
        db.collection("lists")
                .document(listId)
                .collection("products")
                .whereEqualTo("name", product.getName())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        snapshots.getDocuments().get(0).getReference()
                                .update("purchased", isPurchased);
                    }
                });
    }
}
