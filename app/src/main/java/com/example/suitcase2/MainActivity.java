package com.example.suitcase2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper myDbHelper;
    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private Button addItemButton;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int PICK_CONTACT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request SMS permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        // Initialize DatabaseHelper
        myDbHelper = new DatabaseHelper(this);

        // Initialize RecyclerView, TextView, and Button
        recyclerView = findViewById(R.id.recycler_view);
        emptyMessage = findViewById(R.id.empty_message);
        addItemButton = findViewById(R.id.add_item_button);

        // Set up click listener for Add Item button
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Add_Items.class));
            }
        });

        // Initialize RecyclerView with LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Initialize itemList and ItemAdapter
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList, myDbHelper);
        recyclerView.setAdapter(itemAdapter);

        // Load items from database
        loadItems();
    }

    // Method to load items from database
    private void loadItems() {
        Cursor cursor = myDbHelper.getAllItems();

        // Clear existing itemList to avoid duplicates
        itemList.clear();

        // Iterate through the cursor and add items to itemList
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE));
            String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
            boolean purchased = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_PURCHASED)) == 1;

            // Create Item object and add to itemList
            Item item = new Item(id, name, price, description, image, purchased);
            itemList.add(item);
        }

        // Close the cursor after use
        cursor.close();

        // Notify the adapter that the dataset has changed
        itemAdapter.notifyDataSetChanged();

        // Check if there are items to show or display empty message
        checkEmpty();
    }

    // Method to check if RecyclerView should be visible or show empty message
    private void checkEmpty() {
        if (itemList.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Method to handle contact selection for SMS delegation
    public void chooseContactForDelegate() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    // Method to send SMS
    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

            try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String phoneNumber = cursor.getString(numberColumnIndex);

                    // Get selected item from ItemAdapter
                    Item selectedItem = itemAdapter.getSelectedItem();

                    if (selectedItem != null) {
                        // Compose SMS message
                        String message = "Item: " + selectedItem.getName() + "\nPrice: " + selectedItem.getPrice()
                                + "\nDescription: " + selectedItem.getDescription();

                        // Send SMS
                        sendSMS(phoneNumber, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call super method

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload items when returning to MainActivity
        loadItems();
    }
}
