package com.susiturva.susicam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.susiturva.susicam.DatabaseHandlers.DatabaseHelper;
import com.susiturva.susicam.DatabaseHandlers.DatabaseHelperHukcaKey;
import com.susiturva.susicam.DatabaseHandlers.MyDBHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Therion extends Activity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    TextView sarjanumero;
    private DatabaseHelper db;
    private DatabaseHelperHukcaKey dbh;
    public Button tallenna;
    public EditText sarjanumeroInput;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApi;

    private static final int RC_SIGN_IN = 9001;
    private final String USERNAME_PASSWORD = "susipurkki:susipurkki";
    private static final String AUTH_URL = "https://toor.hopto.org/api/v1/users/login?id_token=";
    private final String XAPIKEY = "awidjilherg";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therion);
        db = new DatabaseHelper(this);
        dbh = new DatabaseHelperHukcaKey(this);
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
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.server_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        e.printStackTrace();
                    }
                });
        /*validateServerClientID();
        String serverClientId = getString(R.string.server_client_id);
        *//*gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                //.requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestScopes(new Scope(Scopes.EMAIL))
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .requestId()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleApi =new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();*/
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            //signIn();
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
                    e.printStackTrace();
                }

                db.insert(perse);


                Intent intent = new Intent(Therion.this, MapsActivity.class);
                /*account = GoogleSignIn.getLastSignedInAccount(Therion.this);
                if (account != null) {*/
                    startActivity(intent);
               /* }
                else {
                    Toast.makeText(Therion.this, "Kirjaudu Google tilille.",Toast.LENGTH_LONG).show();

                }*/
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
        int code = requestCode;
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
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    String username = credential.getId();
                    String password = credential.getPassword();

                    if (idToken !=  null) {
                        JSONObject jso = new JSONObject(getAuth(idToken));
                        String hukca_key = jso.getString("hukca_key");
                        int i = dbh.getHukcaKeytCount();
                        try {
                            for (int y = 0; y < i; y++) {
                                dbh.deleteHukcaKeyById(y);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        dbh.insert(hukca_key);
                    } else if (password != null) {
                        // Got a saved username and password. Use them to authenticate
                        // with your backend.

                    }
                } catch (ApiException e) {
                    // ...
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }
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
    private String getAuth(String authcode) throws InterruptedException {
        final InputStream[] inputStream = new InputStream[1];
        final String[] userEmail = new String[1];
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(AUTH_URL + authcode);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", XAPIKEY);
                if (connection.getResponseCode() == 200) {
                    inputStream[0] = connection.getInputStream();
                    userEmail[0] =  this.convertStreamToString(inputStream[0]);
                }
                if (connection.getResponseCode() == 500) {
                    String err = connection.getResponseMessage();
                    System.out.println(err);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        return userEmail[0];
    }
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
