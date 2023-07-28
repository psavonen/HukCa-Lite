package com.susiturva.susicam.databasehandlers;

public class MyDBHandlerHukcaKey {

    private int id;
    private String hukca_key;

    //Tietokannan tiedot
    public static final String TABLE_NAME = "hukca_key_table";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_HUKCA_KEY = "hukca_key";

    //Taulun luonti
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_HUKCA_KEY + " TEXT"
                    + ")";

    public MyDBHandlerHukcaKey(int anInt, String string, String cursorString) {
    }

    public MyDBHandlerHukcaKey(int id, String hukca_key){
        this.id = id;
        this.hukca_key = hukca_key;



    }

    public MyDBHandlerHukcaKey() {

    }

    public int getId(){
        return id;
    }

    public String getHukca_key(){

        return hukca_key;
    }

   

    public int setId(int id){
        this.id = id;
        return id;
    }

    public String setHukca_key(String hukca_key){
        this.hukca_key = hukca_key;
        return hukca_key;
    }

   



    }

