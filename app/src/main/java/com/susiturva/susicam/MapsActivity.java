package com.susiturva.susicam;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.susiturva.susicam.service.LocationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView koiraNopeus;
    private TextView omaNopeus;
    private TextView vmatka;
    private StyledPlayerView playerView;
    private StyledPlayerView playerView2;
    private StyledPlayerView playerViewAudio;
    private LatLng latLong = new LatLng(0, 0);
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private boolean firstTime = true;
    private Double alt;
    private Double speed;
    private Double bearing;
    private float dpWidth;
    private Long uptime;
    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 Meters
    private int bat_soc;
    private int active_streams;
    private int camera_mode;
    private int ir_active;
    private int My_PERMISSION_REQUEST_FINE_LOCATION = 0;
    private int My_PERMISSION_REQUEST_WRITE_ACCESS = 0;
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


    private DatabaseHelper db;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    public static String srnumero;

    private boolean switcher = false;
    private boolean piilossa = true;
    private boolean sound = false;
    private boolean nightVision = false;
    private boolean irLeds = false;

    private Button btnSwitch;
    private Button toiminnot;
    private Button koira;

    private ImageButton aani;
    private ImageButton valot;
    private ImageButton yovalo;
    private ImageButton fullscreen;
    private ImageButton lahetys;

    private ExoPlayer player;
    private ExoPlayer player2;
    private ExoPlayer playerAudio;

    private final String CONTROL_URL_BASE = "https://toor.hopto.org/api/v1/control";

    public static String stream1;
    public static String stream2;
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;

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
        lahetys = findViewById(R.id.lahetys);
        valot = (ImageButton) findViewById(R.id.led);
        yovalo = (ImageButton) findViewById(R.id.yovalo);
        aani = (ImageButton) findViewById(R.id.aani);
        //fullscreen = findViewById(R.id.zoom);
        battery = findViewById(R.id.battery);
        koiraNopeus = findViewById(R.id.koiraNopeus);
        omaNopeus = findViewById(R.id.omaNopeus);
        vmatka = findViewById(R.id.valimatka);
        toiminnot = findViewById(R.id.toiminnot);
        toiminnot.setText("<");
        if(getActionBar() != null){
            //getActionBar().hide();
        }
        startService();
        new Handler().postDelayed(() ->
                        exoplayerTwoStreams(),
                4000);
        new Handler().postDelayed(() ->
                        exoplayerAudio(),
                4000);

       if(dpWidth > 820) {
           playerView.setVisibility(View.VISIBLE);
           playerView2.setVisibility(View.VISIBLE);
       }
        btnSwitch.setOnClickListener(v -> {
            if(dpWidth < 820) {
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
                lahetys.setVisibility(View.INVISIBLE);
                //fullscreen.setVisibility(View.VISIBLE);
                piilossa = false;
                toiminnot.setText(">");
            } else {
                valot.setVisibility(View.INVISIBLE);
                yovalo.setVisibility(View.INVISIBLE);
                koira.setVisibility(View.INVISIBLE);
                aani.setVisibility(View.INVISIBLE);
                //fullscreen.setVisibility(View.INVISIBLE);
                lahetys.setVisibility(View.INVISIBLE);
                piilossa = true;
                toiminnot.setText("<");
            }
        });

        koira.setOnClickListener(v -> firstTime = true);

        aani.setOnClickListener(v -> {
            if (!sound) {
                playerAudio.prepare();
                playerAudio.play();
                aani.setImageResource(R.drawable.volume_off_black_24dp);
                sound = true;
            } else {
                playerAudio.stop();
                exoplayerAudio();
                aani.setImageResource(R.drawable.volume_up_black_24dp);
                sound = false;
            }

        });
        yovalo.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + "/" + srnumero + "/night_vision/";
            if (!nightVision) {
                setControl(urli + "1");
                yovalo.setImageResource(R.drawable.brightness_7_black_24dp);
                nightVision = true;
            } else {
                setControl(urli + "0");
                yovalo.setImageResource(R.drawable.bedtime_black_24dp);
                nightVision = false;
            }
        });
        valot.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + "/" + srnumero + "/ir_led/";
            if (!irLeds) {
                setControl(urli + "1");
                valot.setImageResource(R.drawable.flash_off_black_24dp);
                irLeds = true;
            } else {
                setControl(urli + "0");
                valot.setImageResource(R.drawable.flash_on_black_24dp);
                irLeds = false;
            }
        });
        /*fullscreen.setOnClickListener(v -> {
            Intent i = new Intent(MapsActivity.this, VideoActivity.class);
            startActivity(i);
        });*/
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
        startService();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("LocationUpdates"));
        LocalBroadcastManager.getInstance(this).registerReceiver(aMessageReceiver, new IntentFilter("Streams"));
        firstTime = true;
        exoplayerAudio();
        exoplayerTwoStreams();
    }

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
                    marker = mMap.addMarker(new MarkerOptions().title("koira").position(latLong));
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
                koiraNopeus.setText(speed.toString());
                speedAndLocation();
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
        if(dpWidth > 820) {
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
        LocationListener locationListener = location -> {

            try {
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
                float distance = results[0]/1000;
                double dist = (double)Math.round(distance * 100d) / 100d;
                vmatka.setText("Et:" + dist + "km");
                if(puhelin != null){
                    puhelin.remove();
                }
                puhelin = mMap.addMarker(new MarkerOptions().position(latLng).title("Puhelin").icon(BitmapDescriptorFactory.defaultMarker(150)));
                puhelin.setTag(new Float(0.0));

            } catch (SecurityException e) {
                e.printStackTrace();
            }

        };
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
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
        playerAudio.release();
        stopService();
    }
}


