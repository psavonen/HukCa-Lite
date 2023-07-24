package com.susiturva.susicam;

public class MyDBHandler {

    private int id;
    private String sarjanumero;
   
    //Tietokannan tiedot
    public static final String TABLE_NAME = "hukca";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_SARJANUMERO = "sarjanumero";

    //Taulun luonti
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SARJANUMERO + " LONG"
                    
                    + ")";

    public MyDBHandler(int anInt, String string, String cursorString) {
    }

    public MyDBHandler(int id, String sarjanumero){
        this.id = id;
        this.sarjanumero = sarjanumero;
        


    }

    public MyDBHandler() {

    }

    public int getId(){
        return id;
    }

    public String getSarjanumero(){

        return sarjanumero;
    }

   

    public int setId(int id){
        this.id = id;
        return id;
    }

    public String setSarjanumero(String sarjanumero){
        this.sarjanumero = sarjanumero;
        return sarjanumero;
    }

   



    }

