package com.rhwr.coshop;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.annotations.Nullable;

public class AjoutMembreActivity extends AppCompatActivity {

    private static final int PICK_CONTACT = 1;
    EditText editPhoneNumber;
    Button buttonAddFromContacts, buttonAddMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajout_membre);

        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        buttonAddFromContacts = findViewById(R.id.buttonAddFromContacts);
        buttonAddMember = findViewById(R.id.buttonAddMember);

        buttonAddFromContacts.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        });

        buttonAddMember.setOnClickListener(view -> {
            String phone = editPhoneNumber.getText().toString().trim();
            if (!phone.isEmpty()) {
                ajouterMembre(phone);
            } else {
                Toast.makeText(this, "Entrez un numéro ou sélectionnez un contact", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(numberIndex);
                editPhoneNumber.setText(phoneNumber);
                cursor.close();
            }
        }
    }

    private void ajouterMembre(String phone) {
        // ici tu peux envoyer ce numéro à Firebase / ta DB locale / autre logique
        Toast.makeText(this, "Membre ajouté : " + phone, Toast.LENGTH_SHORT).show();

        // Exemple de retour à la page précédente avec un résultat
        Intent resultIntent = new Intent();
        resultIntent.putExtra("phoneNumber", phone);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

