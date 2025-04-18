package com.rhwr.coshop;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private final String listId;
    private final boolean isReadOnly;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(Context context, List<Product> products, String listId, boolean isReadOnly) {
        super(context, 0, products);
        this.listId = listId;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_item, parent, false);
        }

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

        setStrikeThrough(nameTextView, product.isPurchased());

        if (isReadOnly) {
            decreaseButton.setEnabled(false);
            increaseButton.setEnabled(false);
            purchasedCheckBox.setEnabled(false);
        } else {
            decreaseButton.setOnClickListener(v -> {
                int quantity = product.getQuantity();
                if (quantity > 1) updateProductQuantity(product, quantity - 1);
            });

            increaseButton.setOnClickListener(v -> updateProductQuantity(product, product.getQuantity() + 1));

            purchasedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setPurchased(isChecked);
                setStrikeThrough(nameTextView, isChecked);
                updateProductPurchasedState(product, isChecked);
            });
        }

        return convertView;
    }

    private void setStrikeThrough(TextView textView, boolean strike) {
        if (strike) {
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
                        snapshots.getDocuments().get(0).getReference().update("quantity", newQuantity);
                        // 🔥 Update lastUpdated
                        db.collection("lists").document(listId).update("lastUpdated", FieldValue.serverTimestamp());
                    }
                });
    }

    private void updateProductPurchasedState(Product product, boolean purchased) {
        db.collection("lists")
                .document(listId)
                .collection("products")
                .whereEqualTo("name", product.getName())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        snapshots.getDocuments().get(0).getReference().update("purchased", purchased);
                        // 🔥 Update lastUpdated
                        db.collection("lists").document(listId).update("lastUpdated", FieldValue.serverTimestamp());
                    }
                });
    }

}
