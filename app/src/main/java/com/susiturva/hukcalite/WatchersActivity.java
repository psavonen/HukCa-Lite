package com.susiturva.hukcalite;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.susiturva.hukcalite.databasehandlers.DatabaseHelper;
import com.susiturva.hukcalite.databasehandlers.DatabaseHelperHukcaKey;
import com.susiturva.hukcalite.databasehandlers.MyDBHandler;
import com.susiturva.hukcalite.databasehandlers.MyDBHandlerHukcaKey;
import com.susiturva.susicam.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WatchersActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseHelper db;
    private DatabaseHelperHukcaKey dbh;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    private List<MyDBHandlerHukcaKey> hukcakeyt = new ArrayList<>();
    public static String srnumero;
    public static String hukca_key;
    private final String USERNAME_PASSWORD = "susipurkki:susipurkki";
    private final String XAPIKEY = "awidjilherg";
    private int duration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchers);
        db = new DatabaseHelper(this);
        dbh = new DatabaseHelperHukcaKey(this);
        sarjanumerot.addAll(db.getAllSarjanumerot());
        for (MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        hukcakeyt.addAll(dbh.getAllHuckaKeyt());
        for (MyDBHandlerHukcaKey hukcakey : hukcakeyt) {
            hukca_key = String.valueOf(hukcakey.getHukca_key());
        }
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        EditText input = findViewById(R.id.email);
        Button poistu = findViewById(R.id.poistu);
        poistu.setOnClickListener(v -> {
            Intent intent = new Intent(WatchersActivity.this, MapsActivity.class);
            startActivity(intent);
        });
        Button tallenna = findViewById(R.id.tallenna);
        tallenna.setOnClickListener(v -> {
            setControl("https://toor.hopto.org/api/v1/users/watcher?email="
                    + input.getText().toString()
                    + "&serial_hash="
                    + srnumero
                    + "&watch_duration="
                    + duration);
            finish();
            startActivity(getIntent());
        });
        ArrayList<Object> listdata = new ArrayList<Object>();
        String watchers;
        try {
            watchers = getWatchers("https://toor.hopto.org/api/v1/watchers/" + srnumero);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            JSONArray jsonArray = new JSONArray(watchers);
            if( jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    listdata.add(jsonArray.get(i));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < listdata.stream().count(); i++) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout layout = findViewById(R.id.layout);
            Button buttonView = new Button(this);
            buttonView.setBackgroundColor(000000);
            buttonView.setText((CharSequence) listdata.get(i));
            buttonView.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.x_btn_s,0);
            int finalI = i;
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog myQuittingDialogBox =new AlertDialog.Builder(WatchersActivity.this)
                            //set message, title, and icon
                            .setTitle("Poista")
                            .setMessage("Haluatko poistaa rivin?")
                            .setIcon(R.drawable.x_btn)
                            .setPositiveButton("Poista", new DialogInterface.OnClickListener() {
                                int arraySize = listdata.size();
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //your deleting code
                                    deleteWatcher("https://toor.hopto.org/api/v1/users/watcher?email=", listdata.get(finalI) + "&serial_hash=" + srnumero);
                                    recreate();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
            layout.addView(buttonView, p);
        }
    }

    private void setControl(String url) {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", hukca_key);
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = connection.getInputStream();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WatchersActivity.this,
                                    "Seuraaja lisätty",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    int input = connection.getResponseCode();
                    System.out.println(input);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }
    private String getWatchers(String url) throws InterruptedException {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        final InputStream[] inputStream = new InputStream[1];
        final String[] response = new String[1];
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                //connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", hukca_key);
                if (connection.getResponseCode() == 200) {
                    inputStream[0] = connection.getInputStream();
                    response[0] =  this.convertStreamToString(inputStream[0]);
                }
               else {
                    int input = connection.getResponseCode();
                    System.out.println(input);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        return response[0];
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if(pos == 0) {
            duration = 60 * 60 * 24 * 7;
        }
        else if (pos == 1) {
            duration = 60 * 60 * 24 * 30;
        }
        else if (pos == 2) {
            duration = 60 * 60 * 24 * 365;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
    private void deleteWatcher(String url, String mail) {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url + mail);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", hukca_key);
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = connection.getInputStream();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WatchersActivity.this,
                                    "Seuraaja poistettu",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    int input = connection.getResponseCode();
                    System.out.println(input);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}