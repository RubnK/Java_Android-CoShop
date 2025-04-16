package com.rhwr.coshop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateListDialogFragment extends DialogFragment {

    private EditText listNameEditText;
    private Button createButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_list_dialog, container, false);

        listNameEditText = view.findViewById(R.id.listNameEditText);
        createButton = view.findViewById(R.id.createButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createButton.setOnClickListener(v -> {
            String listName = listNameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(listName)) {
                Toast.makeText(getContext(), "Nom de liste requis", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            // Préparation de la liste avec intégration directe du créateur comme admin
            Map<String, Object> list = new HashMap<>();
            list.put("name", listName);
            list.put("createdAt", new Date());
            list.put("archived", false);
            list.put("members", Collections.singletonMap(user.getUid(), "admin")); // clé = UID, valeur = "admin"

            db.collection("lists").add(list)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(getContext(), "Liste créée", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Erreur lors de la création : " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}
