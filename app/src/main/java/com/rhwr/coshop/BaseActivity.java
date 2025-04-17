package com.rhwr.coshop;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;

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
            }

            return false;
        });
    }
}
