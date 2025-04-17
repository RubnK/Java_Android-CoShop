package com.rhwr.coshop;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setBaseContentView(@LayoutRes int layoutResID) {
        super.setContentView(R.layout.activity_base); // Layout avec BottomNavigationView
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        highlightCurrentMenuItem(nav);
        setupNavigationBar(nav);

        if (!(this instanceof MainActivity || this instanceof ArchiveActivity)) {
            nav.getMenu().findItem(R.id.nav_add).setEnabled(false);
            nav.getMenu().findItem(R.id.nav_add).getIcon().setAlpha(100); // visuellement grisé
        }
    }

    private void highlightCurrentMenuItem(BottomNavigationView nav) {
        int itemId;
        if (this instanceof MainActivity) {
            itemId = R.id.nav_home;
        } else if (this instanceof ArchiveActivity) {
            itemId = R.id.nav_archive;
        } else {
            itemId = -1; // Aucun menu à surligner
        }

        if (itemId != -1) {
            nav.setSelectedItemId(itemId);
        }
    }

    protected void setupNavigationBar(BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home && !(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_archive && !(this instanceof ArchiveActivity)) {
                startActivity(new Intent(this, ArchiveActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_profil) {
                // Rediriger vers l’activité Profil si elle existe
                return true;

            } else if (id == R.id.nav_paramettre) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_add) {
                CreateListDialogFragment dialog = new CreateListDialogFragment();
                dialog.show(getSupportFragmentManager(), "CreateListDialog");

                // Désélectionner le bouton "add"
                nav.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < nav.getMenu().size(); i++) {
                    nav.getMenu().getItem(i).setChecked(false);
                }
                nav.getMenu().setGroupCheckable(0, true, true);

                return false; // <- Très important : indique que l’action ne "navigue" pas
            }




            return false;
        });
    }
}
