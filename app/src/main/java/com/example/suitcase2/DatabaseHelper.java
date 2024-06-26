package com.example.suitcase2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static String DB_NAME = "suitcase.db";
    public static final String ITEMS_TABLE_NAME="Items";
    public static final String Col1 = "id";
    public static final String Col2 = "name";
    public static final String Col3 = "description";
    public static final String Col4 = "price";
    public static final String Col5 = "image";
    public static final String Col6 = "purchased";

    public static final int DB_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String itemstableQuery = "CREATE TABLE " + ITEMS_TABLE_NAME + " (" +Col1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Col2 + " TEXT NOT NULL, " +
                Col3 + " TEXT NOT NULL, " +
                Col4 + " TEXT NOT NULL, " +
                Col5 + " TEXT NOT NULL, " +
                Col6 + " INTEGER NOT NULL)"; // Fixed: Removed extra "TEXT" and added spaces

        try {
            db.execSQL(itemstableQuery); // Execute the query to create the table
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
        onCreate(db);
    }
    //insert items
    public Boolean insertItems(String name,
                               double price,
                               String description,
                               boolean purchased,
                               String image){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO " + ITEMS_TABLE_NAME + " VALUES (NULL, ?, ?, ?, ?)";
        SQLiteStatement sqLiteStatement = database.compileStatement(sql);
        sqLiteStatement.clearBindings();
        sqLiteStatement.bindString(1, name);
        sqLiteStatement.bindDouble(2, Double.valueOf(price));
        sqLiteStatement.bindString(3, description);
        sqLiteStatement.bindLong(4, Long.valueOf(purchased ? 1 : 0));
        long result = sqLiteStatement.executeInsert();
        database.close();
        return result != -1;
    }
    //    get data from database table and column data
    public Cursor getItemById(int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "SELECT * FROM " + ITEMS_TABLE_NAME + " WHERE " + Col1 + " = ?";
        return database.rawQuery(sql, new String[]{String.valueOf(id)});
    }
    //    Get all data from database
    public Cursor getAllItems() {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "SELECT * FROM " + ITEMS_TABLE_NAME;
        return database.rawQuery(sql, null);
    }
    //    Edit item method
    public Boolean updateItem(int id,
                              String name,
                              double price,
                              String description,
                              boolean purchased,
                              String image){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col2, name);
        contentValues.put(Col3, price);
        contentValues.put(Col4, description);
        contentValues.put(Col5, image);
        contentValues.put(Col6, purchased);
        int result = database.update(ITEMS_TABLE_NAME, contentValues, Col1 + " = ?",
                new String[]{String.valueOf(id)});
        Log.d("databaseHelper: ", "result" + result);
        database.close();
        return result != -1;
    }
    //delete item method
    public void deleteItem(long id){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(ITEMS_TABLE_NAME, Col1 + " = ?",
                new String[]{String.valueOf(id)});
        database.close();
    }
}
