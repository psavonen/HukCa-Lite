package com.susiturva.susicam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class Aloitus extends Activity {
    private DatabaseHelper db;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();

    String srnumero;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aloitus);


        db = new DatabaseHelper(this);
        sarjanumerot.addAll(db.getAllSarjanumerot());
        for(final MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());


        }
        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {
                if(srnumero == null) {
                    Intent intent = new Intent(Aloitus.this, Therion.class);
                    startActivity(intent);

                }
                else{
                    Intent intent = new Intent(Aloitus.this, MapsActivity.class);
                    startActivity(intent);
                }
            }
        }, 3000);
        ImageView imageView = findViewById(R.id.connect);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(Aloitus.this, MapsActivity.class);
                startActivity(intent);
                return false;
            }
        });
    }

}