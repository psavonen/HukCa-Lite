package com.susiturva.susicam.DatabaseHandlers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelperHukcaKey extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "HukC4.db";

    public DatabaseHelperHukcaKey(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(MyDBHandlerHukcaKey.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + MyDBHandlerHukcaKey.TABLE_NAME);


        // Create tables again
        onCreate(db);
    }

    public long insert(String hukca_key){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY, String.valueOf(hukca_key));


        long id = db.insert(MyDBHandlerHukcaKey.TABLE_NAME, null, values);

        db.close();

        return id;
    }
    public MyDBHandlerHukcaKey getHukcaKey(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MyDBHandlerHukcaKey.TABLE_NAME,
                new String[]{MyDBHandlerHukcaKey.COLUMN_ID, MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY},
                MyDBHandlerHukcaKey.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        @SuppressLint("Range") MyDBHandlerHukcaKey hukca_key = new MyDBHandlerHukcaKey(
                cursor.getInt(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY)));


        cursor.close();

        return hukca_key;

    }

    @SuppressLint("Range")
    public List<MyDBHandlerHukcaKey> getAllHuckaKeyt(){
        List<MyDBHandlerHukcaKey> hukca_keyt = new ArrayList<>();

        //Valitse kaikki

        //String selectQuery = "SELECT * FROM " + MyDBHandlerHukcaKey.TABLE_NAME + " ORDER BY " + MyDBHandlerHukcaKey.COLUMN_PAIVAYS + " ASC";
        String selectQuery = "SELECT * FROM " + MyDBHandlerHukcaKey.TABLE_NAME + " ORDER BY " + MyDBHandlerHukcaKey.COLUMN_ID+ " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                @SuppressLint("Range") MyDBHandlerHukcaKey hukca_key = new MyDBHandlerHukcaKey(cursor.getInt(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY)));
                hukca_key.setId(cursor.getInt(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_ID)));
                hukca_key.setHukca_key(cursor.getString(cursor.getColumnIndex(MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY)));


                hukca_keyt.add(hukca_key);
            } while (cursor.moveToNext());
        }
        db.close();
        return hukca_keyt;

    }

    public int getHukcaKeytCount(){
        String countQuery = "SELECT * FROM " + MyDBHandlerHukcaKey.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }



    public void deleteHukcaKey(MyDBHandlerHukcaKey sarjanumero){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MyDBHandlerHukcaKey.TABLE_NAME, MyDBHandlerHukcaKey.COLUMN_ID + " = ?",
                new String[]{String.valueOf(sarjanumero.getId())});
        db.close();
    }
    public void deleteHukcaKeyById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MyDBHandlerHukcaKey.TABLE_NAME, MyDBHandlerHukcaKey.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
    public int updateHukcaKey(String hukca_key) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyDBHandlerHukcaKey.COLUMN_HUKCA_KEY, hukca_key);


        return db.update(MyDBHandlerHukcaKey.TABLE_NAME, values, MyDBHandlerHukcaKey.COLUMN_ID + " = ?",
                new String[]{String.valueOf(0)});

    }

}
