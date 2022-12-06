package com.susiturva.susicam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.susiturva.susicam.service.LocationService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    private TextView koiraNopeus;
    private TextView omaNopeus;
    private TextView vmatka;
    public static TextView onOff;
    private StyledPlayerView playerView;
    private StyledPlayerView playerView2;
    private StyledPlayerView playerViewAudio;
    private LatLng latLong = new LatLng(0, 0);
    private List<LatLng>points = new ArrayList<>();
    private ArrayList<LatLng>route = new ArrayList<>();
    private Polyline gpsTrack;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private Double alt;
    private Double speed;
    private Double bearing;
    //private float dpWidth;
    private Double diagonalInches;
    private Long uptime;
    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 0; // 5 Meters
    private int bat_soc;
    private int active_streams;
    private int camera_mode;
    private int ir_active;
    private int My_PERMISSION_REQUEST_FINE_LOCATION = 0;
    private int My_PERMISSION_REQUEST_WRITE_ACCESS = 0;
    private final int SCREEN_WIDTH_THRESHOLD = 1080;
    private final Double SCREEN_SIZE_THRESHOLD = 6.5;
    private final int SOUND_EFFECT_VOLUME = 100;
    private ProgressBar battery;
    private Marker marker = null;
    private Marker puhelin;
    private String last_update;
    private String ip_address;
    private String rtsp_url_stream_1;
    private String rtsp_url_stream_2;
    private String rtsp_url_audio;
    private String stream_1_key;
    private String stream_2_key;
    private String audio_key;
    private String audioFilename;

    private DatabaseHelper db;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    public static String srnumero;

    public static boolean firstTime = true;
    private boolean switcher = false;
    private boolean piilossa = true;
    private boolean sound = false;
    private boolean nightVision = false;
    private boolean irLeds = false;
    private boolean broadcast = false;

    private Button btnSwitch;
    private Button toiminnot;
    private Button koira;

    private ImageButton aani;
    private ImageButton valot;
    private ImageButton yovalo;
    private ImageButton fullscreen;
    private ImageButton lahetys;
    private ImageButton sendSound;

    private ExoPlayer player;
    private ExoPlayer player2;
    private ExoPlayer playerAudio;

    private final String CONTROL_URL_BASE = "https://toor.hopto.org/api/v1/control";
    private final String URL_BASE = "https://toor.hopto.org/api/v1";

    public static String stream1;
    public static String stream2;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    private String AudioSavePathInDevice = null;
    private MediaRecorder mediaRecorder ;
    private Random random ;
    private String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    private MediaPlayer mediaPlayer ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.restart:
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            case R.id.sarjanumero:
                Intent intent = new Intent(com.susiturva.susicam.MapsActivity.this, Therion.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem myItem = menu.findItem(R.id.SN);
        myItem.setTitle("S/N: " + srnumero);
        return true;
    }
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
            diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
            //dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        db = new DatabaseHelper(this);
        sarjanumerot.addAll(db.getAllSarjanumerot());
        for (MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        playerView = findViewById(R.id.player);
        playerView.hideController();
        playerView.setPlayer(player);
        playerView2 = findViewById(R.id.player2);
        playerView2.setPlayer(player2);
        playerView2.hideController();
        playerView.setUseController(false);
        playerView2.setUseController(false);
        playerViewAudio = findViewById(R.id.playerAudio);

        btnSwitch = findViewById(R.id.connect);
        koira = findViewById(R.id.keskitaKoiraan);
        lahetys = (ImageButton) findViewById(R.id.lahetys);
        valot = (ImageButton) findViewById(R.id.led);
        yovalo = (ImageButton) findViewById(R.id.yovalo);
        aani = (ImageButton) findViewById(R.id.aani);
        fullscreen = (ImageButton) findViewById(R.id.zoom);
        sendSound = (ImageButton) findViewById(R.id.sendSound);
        battery = findViewById(R.id.battery);
        koiraNopeus = findViewById(R.id.koiraNopeus);
        omaNopeus = findViewById(R.id.omaNopeus);
        vmatka = findViewById(R.id.valimatka);
        onOff = findViewById(R.id.onOff);
        toiminnot = findViewById(R.id.toiminnot);
        toiminnot.setText("<");

        startService();

        try {
            if (diagonalInches >= SCREEN_SIZE_THRESHOLD) {
                playerView.setVisibility(View.VISIBLE);
                playerView2.setVisibility(View.VISIBLE);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        btnSwitch.setOnClickListener(v -> {
            if(diagonalInches <= SCREEN_SIZE_THRESHOLD) {
                if (!switcher) {
                    playerView.setVisibility(View.INVISIBLE);
                    playerView2.setVisibility(View.VISIBLE);
                    //exoplayer(2);
                    switcher = true;
                } else {
                    playerView.setVisibility(View.VISIBLE);
                    playerView2.setVisibility(View.INVISIBLE);
                    //exoplayer(1);
                    switcher = false;
                }
            }
        });

        toiminnot.setOnClickListener(v -> {
            if (piilossa) {
                valot.setVisibility(View.VISIBLE);
                yovalo.setVisibility(View.VISIBLE);
                koira.setVisibility(View.VISIBLE);
                aani.setVisibility(View.VISIBLE);
                lahetys.setVisibility(View.VISIBLE);
                if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
                    fullscreen.setVisibility(View.VISIBLE);
                }
                piilossa = false;
                sendSound.setVisibility(View.VISIBLE);
                toiminnot.setText(">");
            } else {
                valot.setVisibility(View.INVISIBLE);
                yovalo.setVisibility(View.INVISIBLE);
                koira.setVisibility(View.INVISIBLE);
                aani.setVisibility(View.INVISIBLE);
                if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
                    fullscreen.setVisibility(View.INVISIBLE);
                }
                lahetys.setVisibility(View.INVISIBLE);
                sendSound.setVisibility(View.INVISIBLE);
                piilossa = true;
                toiminnot.setText("<");
            }
        });

        koira.setOnClickListener(v -> firstTime = true);
        lahetys.setOnClickListener(v -> {

            String sound = "pistol";

            String urli = URL_BASE + "/soundeffect/" + srnumero + "/" + sound + "/" + SOUND_EFFECT_VOLUME;
            setControl(urli);
            Toast.makeText(MapsActivity.this,
                    "Lähettää ääntä." ,
                    Toast.LENGTH_LONG).show();

        } );
        sendSound.setOnClickListener(view -> {
            if (!broadcast) {
                Date date = new Date();
                AudioSavePathInDevice = getApplicationContext().getFilesDir().getPath() + "/" + date.getTime() + "Audio.mp3";
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                            10);
                } else {
                    MediaRecorderReady();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                                  }
                try {

                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Toast.makeText(MapsActivity.this, "Nauhoitetaan ääntä",
                        Toast.LENGTH_LONG).show();
                sendSound.setImageResource(R.drawable.voice_over_off_black_24dp);
                broadcast = true;
            }else{
                try {
                    mediaRecorder.stop();
                }catch(Exception e){
                    e.printStackTrace();
                }
                Map<String, String> params = new HashMap<String, String>(2);
                params.put("file", AudioSavePathInDevice);
                String result = sendAudio(AudioSavePathInDevice, params);
                deleteAudio(result);
                sendSound.setImageResource(R.drawable.record_voice_over_black_24dp);
                broadcast = false;
            }
        });
        aani.setOnClickListener(v -> {
            if (!sound) {
                playerAudio.prepare();
                playerAudio.play();
                aani.setImageResource(R.drawable.volume_up_black_24dp);
                sound = true;
            } else {
                playerAudio.stop();
                exoplayerAudio();
                aani.setImageResource(R.drawable.volume_off_black_24dp);
                sound = false;
            }

        });
        yovalo.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + "/" + srnumero + "/night_vision/";
            if (!nightVision) {
                setControl(urli + "1");
                yovalo.setImageResource(R.drawable.bedtime_black_24dp);
                nightVision = true;
            } else {
                setControl(urli + "0");
                yovalo.setImageResource(R.drawable.brightness_7_black_24dp);
                nightVision = false;
            }
        });
        valot.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + "/" + srnumero + "/ir_led/";
            if (!irLeds) {
                setControl(urli + "1");
                valot.setImageResource(R.drawable.flashlight_on_40);
                irLeds = true;
            } else {
                setControl(urli + "0");
                valot.setImageResource(R.drawable.flashlight_off_40);
                irLeds = false;
            }
        });

        fullscreen.setOnClickListener(v -> {
            Intent i = new Intent(MapsActivity.this, VideoActivity.class);
            startActivity(i);
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    protected void onResume() {
        super.onResume();
        if(!isMyServiceRunning(LocationService.class)) {
            startService();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("LocationUpdates"));
        LocalBroadcastManager.getInstance(this).registerReceiver(aMessageReceiver, new IntentFilter("Streams"));
        LocalBroadcastManager.getInstance(this).registerReceiver(bMessageReceiver, new IntentFilter("Route"));
        firstTime = true;
        try {
            new Handler().postDelayed(() ->
                            exoplayerTwoStreams(),
                    4000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try {
            new Handler().postDelayed(() ->
                            exoplayerAudio(),
                    4000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        TileProvider wmsTileProvider = TileProviderFactory.getOsgeoWmsTileProvider();
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));
        mMap.setMaxZoomPreference(16);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));


    }

    public void startService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("inputExtra", "SusiCam taustapalvelu");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("LatLng");
            try {
                latLong = b.getParcelable("LatLng");
                if (firstTime) {
                    new Handler().postDelayed(() -> {
                        new Handler().postDelayed(() ->
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong)), 500);
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        firstTime = false;
                    }, 500);
                }
                if (marker != null) {
                    marker.remove();
                    marker = null;
                }
                if (marker == null) {

                    marker = mMap.addMarker(new MarkerOptions().title("koira").position(latLong).icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_icon_red_24)));
                }


            } catch (Exception e) {
            }
            bat_soc = b.getInt("Bat_soc");
            alt = b.getDouble("Alt");
            speed = b.getDouble("Speed");
            last_update = b.getString("Last_update");
            camera_mode = b.getInt("Camera_mode");
            ir_active = b.getInt("Ir_active");
            try {
                setBattery();
                powerService();
                koiraNopeus.setText(speed.toString());
                if (checkGPSandNetwork()) {
                    try{
                        speedAndLocation();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private BroadcastReceiver aMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle a = intent.getBundleExtra("Streams");

            try {
                rtsp_url_stream_1 = a.getString("rtsp_url_stream_1");
                rtsp_url_stream_2 = a.getString("rtsp_url_stream_2");
                rtsp_url_audio = a.getString("rtsp_url_audio");
                stream_1_key = a.getString("stream_1_key");
                stream_2_key = a.getString("stream_2_key");
                audio_key = a.getString("audio_key");
                /*try {
                    exoplayerTwoStreams();
                    exoplayerAudio();
                }
                catch(Exception e){
                    e.printStackTrace();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver bMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle a = intent.getBundleExtra("Route");

            try {
                route = a.getParcelableArrayList("Route");
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED);
                polylineOptions.width(8);
                gpsTrack = mMap.addPolyline(polylineOptions);
                gpsTrack.setPoints(route);
                gpsTrack.setZIndex(1000);
               } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void exoplayerAudio() {
        MediaItem firstStream = MediaItem.fromUri("rtsp://susipurkki:" + audio_key + "@" + rtsp_url_audio);
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(500, 500, 500, 500)
                .setTargetBufferBytes(200 * 64 * 1024)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .createDefaultLoadControl();

        final long defaultMaxInitialBitrate = Integer.MAX_VALUE;

        final DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter
                .Builder(this)
                .setInitialBitrateEstimate(defaultMaxInitialBitrate)
                .build();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        playerAudio = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setLoadControl(loadControl)
                .build();

        playerViewAudio.setPlayer(playerAudio);
        playerAudio.addMediaItem(firstStream);

        // Prepare the player.
        playerAudio.prepare();
        //playerAudio.play();

    }

    private void exoplayerTwoStreams() {
        stream1 = "rtsp://susipurkki:" + stream_1_key + "@" + rtsp_url_stream_1;
        stream2 = "rtsp://susipurkki:" + stream_2_key + "@" + rtsp_url_stream_2;

        MediaItem firstStream = MediaItem.fromUri(stream1);
        MediaItem secondStream = MediaItem.fromUri(stream2);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(500, 500, 500, 500)
                .setTargetBufferBytes(200 * 64 * 1024)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .createDefaultLoadControl();

        final long defaultMaxInitialBitrate = Integer.MAX_VALUE;

        final DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter
                .Builder(this)
                .setInitialBitrateEstimate(defaultMaxInitialBitrate)
                .build();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setLoadControl(loadControl)
                .build();

        playerView = findViewById(R.id.player);
        playerView.hideController();
        playerView.setPlayer(player);
        player.addMediaItem(firstStream);

        // Prepare the player.
        player.prepare();
        player.play();

        player2 = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setLoadControl(loadControl)
                .build();
        playerView2 = findViewById(R.id.player2);
        playerView2.hideController();
        playerView2.setPlayer(player2);

        player2.addMediaItem(secondStream);

        // Prepare the player.
        player2.prepare();
        player2.play();
        playerView.setVisibility(View.VISIBLE);
        playerView2.setVisibility(View.INVISIBLE);
        if(diagonalInches > SCREEN_SIZE_THRESHOLD) {
           playerView2.setVisibility(View.VISIBLE);
       }
        handlePlaybackError();
        Toast.makeText(MapsActivity.this,
                "Pieni hetki, video yhdistyy." ,
                Toast.LENGTH_LONG).show();
    }
    private void exoplayer(int stream) {
        stream1 = "rtsp://susipurkki:" + stream_1_key + "@" + rtsp_url_stream_1;
        stream2 = "rtsp://susipurkki:" + stream_2_key + "@" + rtsp_url_stream_2;

        MediaItem firstStream = MediaItem.fromUri(stream1);
        MediaItem secondStream = MediaItem.fromUri(stream2);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(500, 500, 500, 500)
                .setTargetBufferBytes(200 * 64 * 1024)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .createDefaultLoadControl();

        final long defaultMaxInitialBitrate = Integer.MAX_VALUE;

        final DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter
                .Builder(this)
                .setInitialBitrateEstimate(defaultMaxInitialBitrate)
                .build();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setLoadControl(loadControl)
                .build();

        playerView = findViewById(R.id.player);
        playerView.hideController();
        playerView.setPlayer(player);
        if(stream == 1) {
            player.addMediaItem(firstStream);
        }
        else{
            player.addMediaItem(secondStream);
        }

        // Prepare the player.
        player.prepare();
        player.play();

        Toast.makeText(MapsActivity.this,
                "Pieni hetki, video yhdistyy." ,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        player2.release();
        playerAudio.release();
        stopService();
    }

    private void setControl(String url) {
        String encoded = Base64.getEncoder().encodeToString(("susipurkki:susipurkki").getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", "awidjilherg");
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = connection.getInputStream();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }
    private String sendAudio(String filepath, Map<String, String> params) {
        final InputStream[] inputStream = {null};
        String encoded = Base64.getEncoder().encodeToString(("susipurkki:susipurkki").getBytes(StandardCharsets.UTF_8));
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String twoHyphens = "--";
        String lineEnd = "\r\n";
        final String[] result = {""};

        final int[] bytesRead = new int[1];
        final int[] bytesAvailable = new int[1];
        final int[] bufferSize = new int[1];
        final byte[][] buffer = new byte[1][1];
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        Thread thread = new Thread(() -> {
            try {
                File file = new File(filepath);


                FileInputStream fileInputStream = new FileInputStream(file);

                URL on = new URL("https://toor.hopto.org/api/v1/audio_delivery/" + srnumero);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", "awidjilherg");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; " + "filename=\"" + q[idx] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: " + "audio/mpeg" + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                outputStream.writeBytes(lineEnd);

                bytesAvailable[0] = fileInputStream.available();
                bufferSize[0] = Math.min(bytesAvailable[0], maxBufferSize);
                buffer[0] = new byte[bufferSize[0]];

                bytesRead[0] = fileInputStream.read(buffer[0], 0, bufferSize[0]);
                while (bytesRead[0] > 0) {
                    outputStream.write(buffer[0], 0, bufferSize[0]);
                    bytesAvailable[0] = fileInputStream.available();
                    bufferSize[0] = Math.min(bytesAvailable[0], maxBufferSize);
                    bytesRead[0] = fileInputStream.read(buffer[0], 0, bufferSize[0]);
                }

                outputStream.writeBytes(lineEnd);

                // Upload POST Data
                Iterator<String> keys = params.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = params.get(key);

                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Type: multipart/form-data;" + boundary);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" +AudioSavePathInDevice + "\"" + lineEnd);
                    outputStream.writeBytes("Content-Type: audio/mpeg" + boundary);

                }

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                if (200 != connection.getResponseCode()) {
                    System.out.println(connection.getResponseMessage());
                    throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
                }
                inputStream[0] = connection.getInputStream();

                result[0] = this.convertStreamToString(inputStream[0]);
                audioFilename = result[0];
                audioFilename = audioFilename.replace("filename", "").replaceAll("\"", "").replaceAll("\\}", "").replaceAll("\\{", "").replaceAll(":", "");

                fileInputStream.close();
                inputStream[0].close();
                outputStream.flush();
                outputStream.close();



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return audioFilename;
    }
    public void deleteAudio(String filename){
        String encoded = Base64.getEncoder().encodeToString(("susipurkki:susipurkki").getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {

                URL on = new URL("https://toor.hopto.org/api/v1/audio_delivery/" + srnumero + "/" + filename);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", "awidjilherg");
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = connection.getInputStream();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }
    public void setBattery() {
        Drawable progressDraw = battery.getProgressDrawable().mutate();
        battery.setMax(100);
        battery.setMin(0);
        if (bat_soc >= 80) {
            progressDraw.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        } else if (bat_soc <= 79 && bat_soc >= 49) {
            progressDraw.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        } else {
            progressDraw.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        }
        battery.setProgress(bat_soc);

    }

    @SuppressLint("MissingPermission")
    public void speedAndLocation() {
        try {
            LocationListener locationListener = location -> {

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    double dlat = location.getLatitude();
                    double dlgn = location.getLongitude();
                    double currentSpeed = location.getSpeed();
                    currentSpeed = (double) (currentSpeed * 3.6);
                    currentSpeed = (double) Math.round(currentSpeed * 1d) / 1d;
                    String setti = currentSpeed + "  ";
                    omaNopeus.setText(setti);
                    float[] results = new float[1];
                    double lat = latLong.latitude;
                    double lng = latLong.longitude;
                    Location.distanceBetween(lat, lng, dlat, dlgn, results);
                    float distance = results[0] / 1000;
                    double dist = (double) Math.round(distance * 100d) / 100d;
                    vmatka.setText("Et:" + dist + "km");
                    if (puhelin != null) {
                        puhelin.remove();
                    }
                    puhelin = mMap.addMarker(new MarkerOptions().position(latLng).title("Puhelin").icon(BitmapDescriptorFactory.fromResource(R.drawable.human_male_icon_green)));
                    puhelin.setTag(new Float(0.0));
            };

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            Toast.makeText(MapsActivity.this,
                    "KYTKE PAIKANNUS PÄÄLLE" ,
                    Toast.LENGTH_LONG).show();
        }
    }
    private void handlePlaybackError(){
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(MapsActivity.this,
                        "Ei video yhteyttä." ,
                        Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() ->
                                player.setPlayWhenReady(true),
                        4000);

            }
        });
        player2.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                new Handler().postDelayed(() ->
                                player2.setPlayWhenReady(true),
                        4000);
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        player.release();
        player2.release();
        playerAudio.release();
        stopService();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }
    public boolean checkGPSandNetwork(){
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("ASETA PAIKANNUS PÄÄLLE")
                    .setPositiveButton("Paikannus", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getApplicationContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    })
                    .setNegativeButton("Peru",null)
                    .show();
        }
        return gps_enabled;
    }
    public void powerService() throws ParseException {
        Calendar rightNow = Calendar.getInstance();
        float sinceMidnight = rightNow.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(last_update);
        float millis = date.getTime();
        try{
            //MapsActivity.onOff.setText("OFF | ");
            float erotus = sinceMidnight - millis;
           onOff.setText("ON  | ");
            stopTimer();
            startTimer();
            if (erotus > 5000) {
                onOff.setText("OFF | ");
            } else {
                onOff.setText("ON  | ");
                //MapsActivity.firstTime = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
    }
    private void startTimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){
                        //your code is here


                        //stopTimer();
                        MapsActivity.onOff.setText("OFF | ");

                    }
                });
            }
        };
        timer.schedule(timerTask, 5000, 5000);
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
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


