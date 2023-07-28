package com.susiturva.susicam.databasehandlers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "HukCa4.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(MyDBHandler.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + MyDBHandler.TABLE_NAME);


        // Create tables again
        onCreate(db);
    }

    public long insert(String sarjanumero){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(MyDBHandler.COLUMN_SARJANUMERO, String.valueOf(sarjanumero));


        long id = db.insert(MyDBHandler.TABLE_NAME, null, values);

        db.close();

        return id;
    }
    public MyDBHandler getSarjanumero(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MyDBHandler.TABLE_NAME,
                new String[]{MyDBHandler.COLUMN_ID, MyDBHandler.COLUMN_SARJANUMERO},
                MyDBHandler.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        @SuppressLint("Range") MyDBHandler sarjanumero = new MyDBHandler(
                cursor.getInt(cursor.getColumnIndex(MyDBHandler.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_SARJANUMERO)));


        cursor.close();

        return sarjanumero;

    }
    public String getSarjis() {
        String sarjis = "";
        //Valitse kaikki
        String selectQuery = "SELECT * FROM " + MyDBHandler.TABLE_NAME + " WHERE " + MyDBHandler.COLUMN_ID + "=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MyDBHandler sarja = new MyDBHandler();
                sarja.setSarjanumero(cursor.getString(0));
                sarjis = sarja.getSarjanumero();
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sarjis;
    }

    @SuppressLint("Range")
    public List<MyDBHandler> getAllSarjanumerot(){
        List<MyDBHandler> sarjanumerot = new ArrayList<>();

        //Valitse kaikki

        //String selectQuery = "SELECT * FROM " + MyDBHandler.TABLE_NAME + " ORDER BY " + MyDBHandler.COLUMN_PAIVAYS + " ASC";
        String selectQuery = "SELECT * FROM " + MyDBHandler.TABLE_NAME + " ORDER BY " + MyDBHandler.COLUMN_ID+ " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                @SuppressLint("Range") MyDBHandler sarjanumero = new MyDBHandler(cursor.getInt(cursor.getColumnIndex(MyDBHandler.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_SARJANUMERO)));
                sarjanumero.setId(cursor.getInt(cursor.getColumnIndex(MyDBHandler.COLUMN_ID)));
                sarjanumero.setSarjanumero(cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_SARJANUMERO)));


                sarjanumerot.add(sarjanumero);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sarjanumerot;

    }

    public int getSarjanumerotCount(){
        String countQuery = "SELECT * FROM " + MyDBHandler.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }



    public void deleteSarjanumero(MyDBHandler sarjanumero){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MyDBHandler.TABLE_NAME, MyDBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(sarjanumero.getId())});
        db.close();
    }
    public void deleteSarjanumeroById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MyDBHandler.TABLE_NAME, MyDBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
    public int updateSarjanumero(long sarjanumero) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyDBHandler.COLUMN_SARJANUMERO, sarjanumero);


        return db.update(MyDBHandler.TABLE_NAME, values, MyDBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(0)});

    }

}
