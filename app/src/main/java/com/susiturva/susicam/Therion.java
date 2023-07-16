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

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.List;

public class Therion extends Activity {
    TextView sarjanumero;
    private DatabaseHelper db;
    public Button tallenna;
    public EditText sarjanumeroInput;
    private int My_PERMISSION_REQUEST_FINE_LOCATION = 0;
    private int My_PERMISSION_REQUEST_WRITE_ACCESS = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApi;

    public static final int MY_PERMISSIONS_STO = 0;
    private static final int RC_SIGN_IN = 9001;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therion);
        db = new DatabaseHelper(this);
        sarjanumeroInput = findViewById(R.id.sarjanumero);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
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

        validateServerClientID();
        String serverClientId = getString(R.string.server_client_id);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestScopes(new Scope(Scopes.EMAIL))
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .requestId()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        gso.getServerClientId();
        mGoogleApi =new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {

            signIn();
        }

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=Auth.GoogleSignInApi.getSignInIntent(mGoogleApi);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });
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
                account = GoogleSignIn.getLastSignedInAccount(Therion.this);
                if (account != null) {
                    startActivity(intent);
                }
                else {
                    Toast.makeText(Therion.this, "Kirjaudu Google tilille.",Toast.LENGTH_LONG).show();

                }
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
    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                Toast.makeText(this,"Kirjautuminen onnistui: " + account.getDisplayName(),Toast.LENGTH_LONG).show();
                String toukka = account.getIdToken();
                String toukka2 = account.getServerAuthCode();

                updateUI(account);
            }
            else
            {
                Toast.makeText(this,"Kirjautuminen epäonnistui.",Toast.LENGTH_LONG).show();
            }
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult completedTask) {
        GoogleSignInAccount account = completedTask.getSignInAccount();
        updateUI(account);

    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }
    private void validateServerClientID() {
        String serverClientId = getString(R.string.server_client_id);
        String suffix = ".apps.googleusercontent.com";
        if (!serverClientId.trim().endsWith(suffix)) {
            String message = "Invalid server client ID in strings.xml, must end with " + suffix;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

}
