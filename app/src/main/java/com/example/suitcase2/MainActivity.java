package com.example.suitcase2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the toolbar and navigation view if necessary
        DrawerLayout drawerLayout = findViewById(R.id.main);
        // Additional setup code for the toolbar, navigation view, and handling drawer actions can be added here
    }

    // Method to handle button click for adding items
    public void onAddItemClick(View view) {
        // Start Add_items activity
        Intent intent = new Intent(MainActivity.this, Add_Items.class);
        startActivity(intent);
    }
}
