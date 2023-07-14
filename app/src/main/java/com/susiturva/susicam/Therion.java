package com.susiturva.susicam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.susiturva.susicam.MapsActivity;

import java.util.List;

public class Therion extends Activity {
    TextView sarjanumero;
    private DatabaseHelper db;
    public Button tallenna;
    public EditText sarjanumeroInput;
    private int My_PERMISSION_REQUEST_FINE_LOCATION = 0;
    private int My_PERMISSION_REQUEST_WRITE_ACCESS = 0;
    private GoogleSignInClient mGoogleSignInClient;
    public static final int MY_PERMISSIONS_STO = 0;
    private static final int RC_SIGN_IN = 9001;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therion);
        db = new DatabaseHelper(this);
        sarjanumeroInput = findViewById(R.id.sarjanumero);
        tallenna = findViewById(R.id.tallenna);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(com.susiturva.susicam.Therion.this, PERMISSIONS, PERMISSION_ALL);
            String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(this, requiredPermissions, 0);
        }

        List<MyDBHandler> srnumerot = db.getAllSarjanumerot();
        String srnumero = "";
        for(final MyDBHandler sarjanumero : srnumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        sarjanumeroInput.setText(srnumero);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            signIn();
        }
        tallenna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String perse = sarjanumeroInput.getText().toString();
                int i = db.getSarjanumerotCount();

                try {
                    for (int y = 0; y < i; y++) {
                        db.deleteSarjanumeroById(y);
                    }
                }
                catch (Exception e){
                    System.out.println("********************************************************** EI TOIMI PUTSAUS ************************************************");
                }

                db.insert(perse);


                Intent intent = new Intent(Therion.this, MapsActivity.class);
                startActivity(intent);


            }

        });//Nappi loppuu
 }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



}
