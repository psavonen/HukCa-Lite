package com.susiturva.susicam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.susiturva.susicam.databasehandlers.DatabaseHelper;
import com.susiturva.susicam.databasehandlers.DatabaseHelperHukcaKey;
import com.susiturva.susicam.databasehandlers.MyDBHandler;
import com.susiturva.susicam.databasehandlers.MyDBHandlerHukcaKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Therion extends Activity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    TextView sarjanumero;
    private DatabaseHelper db;
    private DatabaseHelperHukcaKey dbh;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApi;

    private List<String> list = new ArrayList<>();

    private static final int RC_SIGN_IN = 9001;
    private final String USERNAME_PASSWORD = "susipurkki:susipurkki";
    private List<MyDBHandlerHukcaKey> hukcakeyt = new ArrayList<>();
    private String hukcaKey;
    private static final String AUTH_URL = "https://toor.hopto.org/api/v1/users/login?id_token=";
    private static final String AUTH_URL_HUKCA_KEY = "https://toor.hopto.org/api/v1/users/login?hukca_key=";

    private static final String OWNER_URL = "https://toor.hopto.org/api/v1/users/watcher/";
    private final String XAPIKEY = "awidjilherg";
    private String email = "";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therion);
        db = new DatabaseHelper(this);
        dbh = new DatabaseHelperHukcaKey(this);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(com.susiturva.susicam.Therion.this, PERMISSIONS, PERMISSION_ALL);
//            String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
//            ActivityCompat.requestPermissions(this, requiredPermissions, 0);
        }
        List<MyDBHandler> srnumerot = db.getAllSarjanumerot();
        db.close();
        String srnumero = "";
        for(final MyDBHandler sarjanumero : srnumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        oneTapClient = Identity.getSignInClient(this);
        signIn();

        //account = GoogleSignIn.getLastSignedInAccount(this);
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

   }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int code = requestCode;

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    String username = credential.getId();
                    String password = credential.getPassword();

                    if (idToken !=  null) {
                        setHukcaKey(idToken);
                        dbh.insert(hukcaKey);
                        dbh.close();
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


    private void validateServerClientID() {
        String serverClientId = getString(R.string.server_client_id);
        String suffix = ".apps.googleusercontent.com";
        if (!serverClientId.trim().endsWith(suffix)) {
            String message = "Invalid server client ID in strings.xml, must end with " + suffix;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    private String getAuth(String authcode, String url) throws InterruptedException {
        final InputStream[] inputStream = new InputStream[1];
        final String[] userEmail = new String[1];
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url + authcode);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", XAPIKEY);
                int code = connection.getResponseCode();
                System.out.println(code);
                if (connection.getResponseCode() == 200) {
                    inputStream[0] = connection.getInputStream();
                    userEmail[0] =  this.convertStreamToString(inputStream[0]);
                    JSONObject jsonObject = new JSONObject(userEmail[0]);
                    email = jsonObject.getString("email");
                }
                if (connection.getResponseCode() == 500) {
                    String err = connection.getResponseMessage();
                    System.out.println(err);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
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
    private void getOwnedCameras(String hukcaKey) throws InterruptedException, JSONException {
        final InputStream[] inputStream = new InputStream[1];
        final String[] userEmail = new String[1];
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(OWNER_URL);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestMethod("GET");
                //connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", hukcaKey);

                if (connection.getResponseCode() == 200 || connection.getResponseCode() == 307) {
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
        String parsable = userEmail[0];
        if (parsable != null) {
            parsable = parsable.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("]", "");
            list = Arrays.asList(parsable.split("\\s*,\\s*"));
        } else {
            list.add("Sähköpostiasi (" + email + ") ei ole lisätty millekään kameralle.");
        }

        for (int i = 0; i < list.size(); i++) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout layout = findViewById(R.id.layout);
            Button buttonView = new Button(this);
            buttonView.setBackgroundColor(000000);
            buttonView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            buttonView.setText((CharSequence) list.get(i));
            int finalI = i;
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = db.getSarjanumerotCount();

                    try {
                        for (int y = 0; y < i; y++) {
                            db.deleteSarjanumeroById(y);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    db.insert(list.get(finalI));
                    db.close();

                    Intent intent = new Intent(Therion.this, MapsActivity.class);
                /*account = GoogleSignIn.getLastSignedInAccount(Therion.this);
                if (account != null) {*/
                    startActivity(intent);
                }
            });
            layout.addView(buttonView, p);
        }

    }
    private void setHukcaKey(String idToken) throws InterruptedException, JSONException {
        hukcakeyt.addAll(dbh.getAllHuckaKeyt());
        for (MyDBHandlerHukcaKey hukcakey : hukcakeyt) {
            hukcaKey = String.valueOf(hukcakey.getHukca_key());
        }

        String testiEk = getAuth(hukcaKey, AUTH_URL_HUKCA_KEY);
        if (testiEk == null) {
            JSONObject jso = new JSONObject(getAuth(idToken, AUTH_URL));
            String ValiHukca_key = jso.getString("hukca_key");
            int i = dbh.getHukcaKeytCount();
            try {
                for (int y = 0; y < i; y++) {
                    dbh.deleteHukcaKeyById(y);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            dbh.insert(ValiHukca_key);
            dbh.close();
            hukcaKey = ValiHukca_key;

            //System.out.println("Oli epäkelpo hukkis");
        }
        else {
            //System.out.println("Käypäinen hukkis");

        }
        try {
            getOwnedCameras(hukcaKey);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
