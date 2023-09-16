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
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.StrictMode;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerView;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.susiturva.susicam.databasehandlers.DatabaseHelper;
import com.susiturva.susicam.databasehandlers.DatabaseHelperHukcaKey;
import com.susiturva.susicam.databasehandlers.MyDBHandler;
import com.susiturva.susicam.databasehandlers.MyDBHandlerHukcaKey;
import com.susiturva.susicam.service.WebsocketService;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private static final String WSS = "wss://toor.hopto.org:443/api/v1/wsphone/";
    private static final String AUTH_URL_TOKEN = "https://toor.hopto.org/api/v1/users/login?id_token=";
    private static final String AUTH_URL_HUKCA_KEY = "https://toor.hopto.org/api/v1/users/login?hukca_key=";
    private StorageVolume storageVolume;
    private ImageView detectionDialog;
    private GoogleMap mMap;
    private TextView koiraNopeus;
    private TextView omaNopeus;
    private TextView vmatka;
    public static TextView onOff;
    private PlayerView playerView;
    private PlayerView playerView2;
    private PlayerView playerViewAudio;
    private PlayerView fullscreenFrame1;
    private PlayerView fullscreenFrame;
    private LatLng latLong = new LatLng(0, 0);
    private LatLng latLng;
    LatLng lastKnown = new LatLng(69.9774861, 25.7147569);
    private ArrayList<LatLng> route = new ArrayList<>();
    private Polyline gpsTrack;
    private Polyline lineInBetween;
    private Double diagonalInches;
    private static final double EARTH_RADIUS = 6378100.0;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 0; // 5 Meters
    private int alt;
    private int speed;
    private int videoCounter = 4;
    private int bat_soc;
    private int recordingStatus;
    private int camera_mode;
    private int checkId;
    private int offset;
    private int active_video;
    private int detected_id;
    private int detected_ago;
    private int ir_active;
    private final int SOUND_EFFECT_VOLUME = 100;
    private static final int RC_SIGN_IN = 9001;
    private static final int FLEXIBLE_APP_UPDATE_REQ_CODE = 123;
    private final Double SCREEN_SIZE_THRESHOLD = 6.9;
    private ProgressBar battery;
    private Marker marker = null;
    private Marker pmarker;
    private Marker puhelin;
    private String detected_object;
    private String last_update;
    private String rtsp_url_stream_1;
    private String rtsp_url_stream_2;
    private String rtsp_url_audio;
    private String stream_1_key;
    private String stream_2_key;
    private String audio_key;
    private String audioFilename;
    private DatabaseHelper db;
    private DatabaseHelperHukcaKey dbh;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    private List<MyDBHandlerHukcaKey> hukcakeyt = new ArrayList<>();
    private String srnumero;
    private String hukca_key;
    private boolean firstTime = true;
    private boolean switcher = false;
    private boolean piilossa = true;
    private boolean sound = false;
    private boolean nightVision = false;
    private boolean irLeds = false;
    private boolean broadcast = false;
    private boolean huckaOn = false;
    private boolean recordVideoOn = false;
    private boolean videoCheck = false;
    private boolean fullscreenBackSwitch = false;
    private boolean tunnistuskytkenta = false;
    private ImageButton btnSwitch;
    private ImageButton fullscreenBack;
    private Button toiminnot;
    private Button koira;
    private Button menu;
    private Button keskitaPuhelimeen;

    private ImageButton aani;
    private ImageButton valot;
    private ImageButton yovalo;
    private ImageButton fullscreen;
    private ImageButton lahetys;
    private ImageButton sendSound;
    private ImageButton detection;
    private ImageButton recordVideo;
    private ImageButton usbMode;

    private ExoPlayer player;
    private ExoPlayer player2;
    private ExoPlayer playerAudio;
    private ExoPlayer dummyPlayer;
    private ExoPlayer dummyPlayer2;

    private final String CONTROL_URL_BASE = "https://toor.hopto.org/api/v1/control/";
    private final String DETECTOR_CONTORL_URL_BASE = "https://toor.hopto.org/api/v1/detector/control/";
    private final String URL_BASE = "https://toor.hopto.org/api/v1";
    private final String USERNAME_PASSWORD = "susipurkki:susipurkki";
    private final String XAPIKEY = "awidjilherg";
    public static final String RTSP_SUSIPURKKI = "rtsp://susipurkki:";
    public static String stream1;
    public static String stream2;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    private String AudioSavePathInDevice = null;
    private String audiofile;
    private MediaRecorder mediaRecorder;

    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private GoogleSignInAccount account;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private WebSocketClient webSocketClient;
    private Looper looper;
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
            /*case R.id.tunnistus:
                if (!tunnistuskytkenta) {
                    tunnistusPaalle();
                    tunnistuskytkenta = true;
                } else {
                    tunnistusPois();
                    tunnistuskytkenta = false;
                }
                return true;*/
            case R.id.sarjanumero:
                Intent intent = new Intent(com.susiturva.susicam.MapsActivity.this, Therion.class);
                startActivity(intent);
                return true;
            case R.id.massamuisti:
                massamuisti();
                return true;
            case R.id.katsoja:
                Intent katsojaIntent = new Intent(com.susiturva.susicam.MapsActivity.this, WatchersActivity.class);
                startActivity(katsojaIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem myItem = menu.findItem(R.id.SN);
        myItem.setTitle("S/N: " + srnumero);
        MenuItem versItem = menu.findItem(R.id.version);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            versItem.setTitle("V: " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

       /* MenuItem tunnistusTitle = menu.findItem(R.id.tunnistus);
        if (!tunnistuskytkenta) {
            tunnistusTitle.setTitle("Hahmontunnistus päälle");
        } else {
            tunnistusTitle.setTitle("Hahmontunnistus pois");
        }*/
        return true;
    }

    @SuppressLint("MissingInflatedId")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());*/
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        );

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        oneTapClient = Identity.getSignInClient(this);
        //account = GoogleSignIn.getLastSignedInAccount(this);
        signIn();
        /*String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                //.requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestScopes(new Scope(Scopes.EMAIL))
                .requestScopes(new Scope(Scopes.PROFILE))
                //.requestServerAuthCode(serverClientId)
                .requestId()
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        account = GoogleSignIn.getLastSignedInAccount(this);
//        String serverAuthCode = account.getServerAuthCode();*/
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        checkUpdate();
        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                removeInstallStateUpdateListener();
            } else {
                Toast.makeText(getApplicationContext(), "InstallStateUpdatedListener: state: " + state.installStatus(), Toast.LENGTH_LONG).show();
            }
        };

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            diagonalInches = getScreenSizeInches(MapsActivity.this);
            //dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        } catch (Exception e) {
            e.printStackTrace();
        }
        db = new DatabaseHelper(this);
        dbh = new DatabaseHelperHukcaKey(this);
        Runnable snRunner = new Runnable() {
            @Override
            public void run() {
                sarjanumerot.addAll(db.getAllSarjanumerot());
                db.close();
            }
        };
        Thread snThread = new Thread(snRunner);
        snThread.start();
        try {
            snThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        Runnable hukcaRunner = new Runnable() {
            @Override
            public void run() {
                hukcakeyt.addAll(dbh.getAllHuckaKeyt());
                dbh.close();
            }
        };
        Thread hukcaThread = new Thread(hukcaRunner);
        hukcaThread.start();
        try {
            hukcaThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MyDBHandlerHukcaKey hukcakey : hukcakeyt) {
            hukca_key = String.valueOf(hukcakey.getHukca_key());
        }
        playerView = findViewById(R.id.player);
        playerView.hideController();
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setPlayer(player);
        playerView2 = findViewById(R.id.player2);
        playerView2.setPlayer(player2);
        playerView2.hideController();
        playerView2.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setUseController(false);
        playerView2.setUseController(false);
        playerView.setKeepContentOnPlayerReset(true);
        playerViewAudio = findViewById(R.id.playerAudio);
        fullscreenFrame1 = findViewById(R.id.fullscreen1);
        fullscreenFrame = findViewById(R.id.fullscreen);
        fullscreenFrame.hideController();
        fullscreenFrame.setUseController(false);
        fullscreenFrame.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        fullscreenFrame1.hideController();
        fullscreenFrame1.setUseController(false);
        fullscreenFrame1.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        fullscreenBack = findViewById(R.id.fullscreenBack);
        btnSwitch = findViewById(R.id.connect);
        koira = findViewById(R.id.keskitaKoiraan);
        menu = findViewById(R.id.menu);
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
        detection = findViewById(R.id.detection);
        detectionDialog = findViewById(R.id.popup);
        recordVideo = findViewById(R.id.recordVideo);
        usbMode = findViewById(R.id.usbMode);
        keskitaPuhelimeen = findViewById(R.id.keskitaPuhelimeen);
        try {
            exoplayerTwoStreams();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            new Handler().postDelayed(() ->
                    {

                           /* Runnable videoRunner = new Runnable() {
                                @Override
                                public void run() {

                                    for (int i = 0; i < videoCounter; i++) {
                                        if (!exoplayerPlaying(player)) {
                                            new Handler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        exoplayerTwoStreams();
                                                    } catch (InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            });
                                            try {
                                                Thread.sleep(4000);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        if (!exoplayerPlaying(player) && i == videoCounter) {
                                            Toast.makeText(MapsActivity.this,
                                                    "Ei video yhteyttä.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        }
                                    }
                                }
                            };
                            Thread videoThread = new Thread(videoRunner);
                            videoThread.start();*/

                        /* if (!exoplayerPlaying(player)) {
                             exoplayerTwoStreams();
                         }*/
                        if (!isMyServiceRunning(WebsocketService.class)) {
                            startService();
                        }
                    },
                    16000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (diagonalInches >= SCREEN_SIZE_THRESHOLD) {
                playerView.setVisibility(View.VISIBLE);
                playerView2.setVisibility(View.VISIBLE);
            }
            else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLogoWhenNoStream();
        btnSwitch.setOnClickListener(v -> {
            if (!exoplayerPlaying(player)) {
                try {
                        exoplayerTwoStreams();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            btnSwitch.setBackground(null);
           //showLogoWhenNoStream();
           if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
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
                keskitaPuhelimeen.setVisibility(View.VISIBLE);
                aani.setVisibility(View.VISIBLE);
                lahetys.setVisibility(View.VISIBLE);
                if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
                    menu.setVisibility(View.VISIBLE);
                }
                fullscreen.setVisibility(View.VISIBLE);
                piilossa = false;
                sendSound.setVisibility(View.VISIBLE);
                recordVideo.setVisibility(View.INVISIBLE);
                usbMode.setVisibility(View.INVISIBLE);
                toiminnot.setText(">");
            } else {
                valot.setVisibility(View.INVISIBLE);
                yovalo.setVisibility(View.INVISIBLE);
                koira.setVisibility(View.INVISIBLE);
                keskitaPuhelimeen.setVisibility(View.INVISIBLE);
                aani.setVisibility(View.INVISIBLE);
                if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
                    menu.setVisibility(View.INVISIBLE);
                }
                fullscreen.setVisibility(View.INVISIBLE);
                lahetys.setVisibility(View.INVISIBLE);
                sendSound.setVisibility(View.INVISIBLE);
                recordVideo.setVisibility(View.INVISIBLE);
                usbMode.setVisibility(View.INVISIBLE);
                piilossa = true;
                toiminnot.setText("<");
            }
        });
        recordVideo.setTooltipText(getString(R.string.recordVideo_tooltip));
        recordVideo.setOnClickListener(v -> {
            if (!recordVideoOn) {
                String urli = URL_BASE + "/record/" + srnumero + "/true?source=front&storage=camera";
                String urlib = URL_BASE + "/record/" + srnumero + "/true?source=back&storage=camera";
                setControl(urli);
                setControl(urlib);
                //recordVideo.setImageResource(R.drawable.record_video_stop);
                Toast.makeText(MapsActivity.this, "Nauhoitus päällä",
                        Toast.LENGTH_SHORT).show();
                recordVideoOn = true;

            } else {
                String urli = URL_BASE + "/record/" + srnumero + "/false?source=front&storage=camera";
                String urlib = URL_BASE + "/record/" + srnumero + "/false?source=back&storage=camera";
                setControl(urli);
                setControl(urlib);
                //recordVideo.setImageResource(R.drawable.record_video);
                Toast.makeText(MapsActivity.this, "Nauhoitus pois päältä",
                        Toast.LENGTH_SHORT).show();
                recordVideoOn = false;
            }
        });
        detection.setOnClickListener(view -> {
            Uri uri = null;
            try {
                uri = getAndSaveImage(detected_id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (uri != null) {
                detectionDialog.setImageURI(uri);
            }
            detectionDialog.setTooltipText("HAVAINTO");
            detectionDialog.setVisibility(View.VISIBLE);
            detectionDialog.setOnClickListener(v -> {
                detectionDialog.setVisibility(View.INVISIBLE);
            });
            detection.setVisibility(View.INVISIBLE);
        });

        koira.setTooltipText(getString(R.string.koira_tooltip));
        koira.setOnClickListener(v -> firstTime = true);

        keskitaPuhelimeen.setTooltipText(getString(R.string.puhelin_tooltip));
        keskitaPuhelimeen.setOnClickListener(v -> {
            try {
                if (latLng != null) {

                    new Handler().postDelayed(() -> {
                        new Handler().postDelayed(() ->
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)), 100);
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    }, 100);
                }
                else {
                    Toast.makeText(MapsActivity.this, "Ei GPS yhteyttä ",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (diagonalInches <= SCREEN_SIZE_THRESHOLD) {
            menu.setTooltipText(getString(R.string.menu_tooltip));
            menu.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {
                    getSupportActionBar().show();
                    toolbar.showOverflowMenu();
                }
            });
        }
        usbMode.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + srnumero + "/activate_usb/true";
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Varmistus");
            builder.setMessage("Haluatko varmasti asettaa laitteen massamuisti-tilaan? Kun haluat palata takaisin massamuisti-tilasta, sammuta ja käynnistä laite uudelleen.");
            builder.setPositiveButton("Hyväksy",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setControl(urli);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
        lahetys.setTooltipText(getString(R.string.lahetys_tooltip));
        lahetys.setOnClickListener(v -> {

            String sound = "pistol";
            String urli = URL_BASE + "/soundeffect/" + srnumero + "/" + sound + "/" + SOUND_EFFECT_VOLUME;
            setControl(urli);
            Toast.makeText(MapsActivity.this,
                    "Lähettää ääntä.",
                    Toast.LENGTH_LONG).show();

        });
        sendSound.setTooltipText(getString(R.string.sendSound_tooltip));
        sendSound.setOnClickListener(view -> {
            if (!broadcast) {
                Date date = new Date();
                audiofile = date.getTime() + "Audio.3gp";
                AudioSavePathInDevice = getApplicationContext().getFilesDir().getPath() + "/" + audiofile;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
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
                Toast.makeText(MapsActivity.this, "Nauhoitetaan ääntä",
                        Toast.LENGTH_SHORT).show();
                sendSound.setImageResource(R.drawable.baseline_voice_over_off_24);
                broadcast = true;
                try {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaRecorder.stop();
                                Map<String, String> params = new HashMap<String, String>(2);
                                params.put("file", AudioSavePathInDevice);
                                String result = sendAudio(AudioSavePathInDevice, params);
                                Toast.makeText(MapsActivity.this, "Lähetetään ääntä",
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {

                            }
                            sendSound.setImageResource(R.drawable.baseline_record_voice_over_24);
                            broadcast = false;
                        }
                    }, 5000);

                } catch (Exception e) {

                }
            } else {
                try {
                    mediaRecorder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Map<String, String> params = new HashMap<String, String>(2);
                params.put("file", AudioSavePathInDevice);
                try {
                    String result = sendAudio(AudioSavePathInDevice, params);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //playAndDeleteAudio();
                Toast.makeText(MapsActivity.this, "Lähetetään ääntä",
                        Toast.LENGTH_LONG).show();
                sendSound.setImageResource(R.drawable.baseline_record_voice_over_24);
                broadcast = false;
            }
        });
        aani.setTooltipText(getString(R.string.aani_tooltip));
        try {
            if (playerAudio != null) {
                playerAudio.prepare();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        aani.setOnClickListener(v -> {
            if (!sound) {
                playerAudio.play();
                aani.setImageResource(R.drawable.baseline_volume_up_24);
                sound = true;
            } else {
                playerAudio.stop();
                exoplayerAudio();
                aani.setImageResource(R.drawable.baseline_volume_off_24);
                sound = false;
            }

        });
        yovalo.setTooltipText(getString(R.string.yovalo_tooltip));
        yovalo.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + srnumero + "/night_vision/";
            if (!nightVision) {
                setControl(urli + "1");
                yovalo.setImageResource(R.drawable.baseline_bedtime_24);
                nightVision = true;
            } else {
                setControl(urli + "0");
                yovalo.setImageResource(R.drawable.baseline_brightness_7_24);
                nightVision = false;
            }
        });
        valot.setTooltipText(getString(R.string.valot_tooltip));
        valot.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + srnumero + "/ir_led/";
            if (!irLeds) {
                setControl(urli + "1");
                valot.setImageResource(R.drawable.baseline_flashlight_on_24);
                irLeds = true;
            } else {
                setControl(urli + "0");
                valot.setImageResource(R.drawable.baseline_flashlight_off_24);
                irLeds = false;
            }
        });
        fullscreen.setTooltipText(getString(R.string.fullscreen_tooltip));
        fullscreen.setOnClickListener(v -> {
            if (!fullscreenBackSwitch) {
                btnSwitch.setVisibility(View.INVISIBLE);
                fullscreenFrame1.setVisibility(View.VISIBLE);
                fullscreenFrame.setVisibility(View.VISIBLE);
                fullscreenBack.setVisibility(View.VISIBLE);
                playerView2.setVisibility(View.INVISIBLE);
                playerView.setVisibility(View.INVISIBLE);
                playerView.setPlayer(dummyPlayer);
                playerView2.setPlayer(dummyPlayer2);
                fullscreenFrame1.setPlayer(player);
                fullscreenFrame.setPlayer(player2);
                fullscreenBackSwitch = true;
            } else {
                playerView2.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.VISIBLE);
                fullscreenFrame1.setVisibility(View.INVISIBLE);
                fullscreenFrame.setVisibility(View.INVISIBLE);
                fullscreenBack.setVisibility(View.INVISIBLE);
                fullscreenFrame1.setPlayer(dummyPlayer);
                fullscreenFrame.setPlayer(dummyPlayer2);
                playerView.setPlayer(player);
                btnSwitch.setVisibility(View.VISIBLE);
                playerView2.setPlayer(player2);
                fullscreenBackSwitch = false;
            }
        });

        fullscreenBack.setOnClickListener(v -> {
            fullscreenFrame1.setPlayer(dummyPlayer);
            fullscreenFrame.setPlayer(dummyPlayer2);
            playerView.setPlayer(player);
            playerView2.setPlayer(player2);
            fullscreenFrame1.setVisibility(View.INVISIBLE);
            fullscreenFrame.setVisibility(View.INVISIBLE);
            playerView2.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.VISIBLE);
            btnSwitch.setVisibility(View.VISIBLE);
            fullscreenBack.setVisibility(View.INVISIBLE);
            fullscreenBackSwitch = false;
        });
        checkGPSandNetwork();
        try {
            speedAndLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void onResume() {
        super.onResume();
        if (!isMyServiceRunning(WebsocketService.class)) {
            startService();
        }
        try {
            new Handler().postDelayed(() ->
                            exoplayerAudio(),
                    4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("LocationUpdates"));
        LocalBroadcastManager.getInstance(this).registerReceiver(bMessageReceiver, new IntentFilter("Route"));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        TileProvider wmsTileProvider = TileProviderFactory.getOsgeoWmsTileProvider();
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));
        mMap.setMaxZoomPreference(16);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
    }

       @OptIn(markerClass = UnstableApi.class)
    private void exoplayerAudio() {
        MediaItem firstStream = MediaItem.fromUri(RTSP_SUSIPURKKI + audio_key + "@" + rtsp_url_audio);
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(2000, 8000, 2000, 2000)
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .build();

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
    @OptIn(markerClass = UnstableApi.class)
    private void exoplayerTwoStreams() throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getStreamers(srnumero);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        stream1 = RTSP_SUSIPURKKI + stream_1_key + "@" + rtsp_url_stream_1;
        stream2 = RTSP_SUSIPURKKI + stream_2_key + "@" + rtsp_url_stream_2;

        MediaItem firstStream = MediaItem.fromUri(stream1);
        MediaItem secondStream = MediaItem.fromUri(stream2);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(2000, 8000, 2000, 2000)
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .build();

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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                playerView = findViewById(R.id.player);
                playerView.hideController();
                playerView.setPlayer(player);
            }
        });
        try {
            player.addMediaItem(firstStream);
            // Prepare the player.
            player.prepare();
        } catch(Exception e) {

        }
        player.setPlayWhenReady(true);
        player2 = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setLoadControl(loadControl)
                .build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                playerView2 = findViewById(R.id.player2);
                playerView2.hideController();
                playerView2.setPlayer(player2);
            }
        });

        player2.addMediaItem(secondStream);

        // Prepare the player.
        player2.prepare();
        player2.setPlayWhenReady(true);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                playerView.setVisibility(View.VISIBLE);

                playerView2.setVisibility(View.INVISIBLE);
                if (diagonalInches > SCREEN_SIZE_THRESHOLD) {
                    playerView2.setVisibility(View.VISIBLE);
                }
            }
        });

        handlePlaybackError();
        Toast.makeText(MapsActivity.this,
                "Pieni hetki, video yhdistyy.",
                Toast.LENGTH_SHORT).show();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void exoplayer(int stream) {
        stream1 = RTSP_SUSIPURKKI + stream_1_key + "@" + rtsp_url_stream_1;
        stream2 = RTSP_SUSIPURKKI + stream_2_key + "@" + rtsp_url_stream_2;

        MediaItem firstStream = MediaItem.fromUri(stream1);
        MediaItem secondStream = MediaItem.fromUri(stream2);

        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(500, 500, 500, 500)
                .setTargetBufferBytes(200 * 64 * 1024)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(2000, false)
                .build();

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
        if (stream == 1) {
            player.addMediaItem(firstStream);
        } else {
            player.addMediaItem(secondStream);
        }

        // Prepare the player.
        player.prepare();
        player.play();

        Toast.makeText(MapsActivity.this,
                "Pieni hetki, video yhdistyy.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isMyServiceRunning(WebsocketService.class)) {
            stopService();
        }
        player.release();
        player2.release();
        playerAudio.release();

    }


    private void setControl(String url) {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        Thread thread = new Thread(() -> {
            try {
                URL on = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", XAPIKEY);
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
    private String sendAudio(String filepath, Map<String, String> params) throws InterruptedException {
        final InputStream[] inputStream = {null};
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
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

                URL on = new URL(URL_BASE + "/audio_delivery/" + srnumero + "?force_push=false&auto_play=true&auto_delete=true&volume=100");
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", XAPIKEY);
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
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + AudioSavePathInDevice + "\"" + lineEnd);
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
                boolean deleted = deleteFile(audiofile);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        return audioFilename;
    }

    public Uri getAndSaveImage(int detectionId) throws InterruptedException {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
        storageVolume = storageVolumes.get(0);
        String dir = "";
        String filename = "detection" + new SimpleDateFormat("yyyyddMMHHmm").format(new Date()) + ".png";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            dir = storageVolume.getDirectory() + "/Pictures/HukCa/" + filename;
        }
        Uri urlz = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            urlz = Uri.parse(dir);
        }
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            file = new File(storageVolume.getDirectory() + "/Pictures/HukCa/");
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        Uri finalUrlz = urlz;
        Thread thread = new Thread(() -> {
            Bitmap bitmap;
            try {
                URL on = new URL(URL_BASE + "/detector/download/" + srnumero + "/" + detectionId);
                //URL on = new URL(URL_BASE + "/detector/download/8ef63c6a/496");
                HttpsURLConnection connection = (HttpsURLConnection) on.openConnection();
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestProperty("x-apikey", XAPIKEY);
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = null;
                    try {
                        responseBody = connection.getInputStream();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bitmap = BitmapFactory.decodeStream(responseBody);
                    OutputStream outputStream = new FileOutputStream(String.valueOf(finalUrlz));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        String[] mimes = new String[1];
        mimes[0] = "image/png";
        try {
            MediaScannerConnection.scanFile(this,
                    new String[]{urlz.toString()},
                    mimes, null);
            MediaStore.Images.Media.insertImage(this.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            this.sendBroadcast(new Intent(
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlz;
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
        HandlerThread handlerThread = new HandlerThread("speedAndLocationThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        try {
            LocationListener locationListener = location -> {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                double dlat = location.getLatitude();
                double dlgn = location.getLongitude();
                final double[] currentSpeed = {location.getSpeed()};
                final String[] setti = new String[1];
                final double[] dist = new double[1];

                        currentSpeed[0] = (double) (currentSpeed[0] * 3.6);
                        currentSpeed[0] = (double) Math.round(currentSpeed[0] * 1d) / 1d;
                        setti[0] = (int)currentSpeed[0] + "  ";
                        float[] results = new float[1];
                        double lat = latLong.latitude;
                        double lng = latLong.longitude;
                        Location.distanceBetween(lat, lng, dlat, dlgn, results);
                        float distance = results[0] / 1000;
                        dist[0] = (double) Math.round(distance * 100d) / 100d;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        omaNopeus.setText(setti[0]);
                        vmatka.setText("Et:" + dist[0] + "km");
                    }
                });

                        /*if (diagonalInches >= SCREEN_SIZE_THRESHOLD) {
                            if (puhelin != null) {
                                puhelin.remove();
                            }
                            puhelin = mMap.addMarker(new MarkerOptions().position(latLng).title("Puhelin").icon(BitmapDescriptorFactory.fromResource(R.drawable.human_male_icon_green)));
                            puhelin.setTag(new Float(0.0));
                        }*/


               /* MarkerOptions options = new MarkerOptions();
                options.position(getCoords(latLng));
                try {
                    options.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(latLng))).title("100m");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                options.zIndex(1000);
                if (pmarker != null) {
                    pmarker.remove();
                }
                pmarker = mMap.addMarker(options);*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (lineInBetween != null)  {
                            lineInBetween.remove();
                        }
                        drawLineInBetween(latLng, latLong );
                    }
                });
            };

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener, looper);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();

                    Toast.makeText(MapsActivity.this,
                            "KYTKE PAIKANNUS PÄÄLLE",
                            Toast.LENGTH_LONG).show();


        }
    }

    private void handlePlaybackError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
               //showLogoWhenNoStream();
                try {
                    if (powerService()) {

                        new Handler().postDelayed(() ->
                                        player.setPlayWhenReady(true),
                                10000);
                    }
                    else {
                        /*Toast.makeText(MapsActivity.this,
                                "Ei yhteyttä laitteeseen",
                                Toast.LENGTH_LONG).show();*/
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                }
        });
        player2.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                new Handler().postDelayed(() ->
                                player2.setPlayWhenReady(true),
                        10000);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isMyServiceRunning(WebsocketService.class)) {
            stopService();
        }
        /*try{
            player.release();
        } catch(Exception e){
            e.printStackTrace();
        }
        try {
            player2.release();
        } catch(Exception e){
            e.printStackTrace();
        }
        try {
            playerAudio.release();
        } catch(Exception e){
            e.printStackTrace();
        }*/
        //stopService();
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    public boolean checkGPSandNetwork() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("ASETA PAIKANNUS PÄÄLLE")
                    .setPositiveButton("Paikannus", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getApplicationContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    })
                    .setNegativeButton("Peru", null)
                    .show();
        }
        return gps_enabled;
    }

    public boolean powerService() throws ParseException {
        Date dateNow = new Date();
        float sinceMidnight = dateNow.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        float millis = 0;
        try {
            date = sdf.parse(last_update);
            millis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            //MapsActivity.onOff.setText("OFF | ");
            float erotus = sinceMidnight - millis;

                    onOff.setText("ON  | ");

            huckaOn = true;
            stopTimer();
            startTimer();
            if (erotus > 6000) {

                        onOff.setText("OFF  | ");

                huckaOn = false;
            } else {

                        onOff.setText("ON  | ");

                huckaOn = true;
                //MapsActivity.firstTime = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return huckaOn;
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MapsActivity.onOff.setText("OFF | ");
                    }
                });
            }
        };
        timer.schedule(timerTask, 5000, 5000);
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);
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

    public void Detector() {
        if (detected_object != null && detected_id != checkId && detected_ago < 30 && detected_object != "null") {
            checkId = detected_id;
            detection.setVisibility(View.VISIBLE);
            final MediaPlayer mp = MediaPlayer.create(MapsActivity.this, R.raw.red_alert);
            Thread thread = new Thread(() -> {
                try {
                    for (int i = 0; i < 2; i++) {
                        mp.start();
                        try {
                            Thread.sleep(2000);
                            mp.release();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            Toast.makeText(com.susiturva.susicam.MapsActivity.this,
                "HAVAINTO: " + detected_object,
                Toast.LENGTH_SHORT).show();
        }
    }

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void massamuisti() {
        String urli = CONTROL_URL_BASE + srnumero + "/activate_usb/true";
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Varmistus");
        builder.setMessage("Haluatko varmasti asettaa laitteen massamuisti-tilaan? Kun haluat palata takaisin massamuisti-tilasta, sammuta ja käynnistä laite uudelleen.");
        builder.setPositiveButton("Hyväksy",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setControl(urli);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean exoplayerPlaying(ExoPlayer exoPlayer) {
        if (exoPlayer.getPlaybackState() == Player.STATE_READY || exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
            return true;
        } else {
            return false;
        }
    }
    private boolean exoplayerBuffering(ExoPlayer exoPlayer) {
        if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
            return true;
        } else {
            return false;
        }
    }
    private void tunnistusPaalle() {
        String urli = DETECTOR_CONTORL_URL_BASE + srnumero + "/front/true?interval=5&sound_volume=1";
        Toast.makeText(MapsActivity.this,
                "Hahmontunnistus päällä",
                Toast.LENGTH_SHORT).show();
        setControl(urli);
    }

    private void tunnistusPois() {
        String urli = DETECTOR_CONTORL_URL_BASE + srnumero + "/front/false?interval=5&sound_volume=1";
        Toast.makeText(MapsActivity.this,
                "Hahmontunnistus pois päältä",
                Toast.LENGTH_SHORT).show();
        setControl(urli);
    }
    @Override
    public void openOptionsMenu() {
        Configuration cfg = getResources().getConfiguration();
        if( (cfg.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)>Configuration.SCREENLAYOUT_SIZE_LARGE){
            int shelf = cfg.screenLayout;
            cfg.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            cfg.screenLayout = shelf;
        }else{
            super.openOptionsMenu();
        }
    }

    private void drawLineInBetween(LatLng dogo, LatLng phone) {
        try {
            PolylineOptions polylineOptions = new PolylineOptions();
            List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20), new Dash(30), new Gap(20));
            polylineOptions.pattern(pattern);
            polylineOptions.color(Color.BLUE);
            polylineOptions.width(4);
            List<LatLng> lattis = new ArrayList<>();
            lattis.add(dogo);
            lattis.add(phone);
            lineInBetween = mMap.addPolyline(polylineOptions);
            lineInBetween.setPoints(lattis);
            lineInBetween.setZIndex(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {

        double lat1 = radiusInMeters / EARTH_RADIUS;
        double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));

        double lat2 = lat + lat1 * 180 / Math.PI;
        double lng2 = lng + lng1 * 180 / Math.PI;

        Point p1 = mMap.getProjection().toScreenLocation(new LatLng(lat, lng));
        Point p2 = mMap.getProjection().toScreenLocation(new LatLng(lat2, lng2));

        return Math.abs(p1.x - p2.x);
    }
    private Bitmap getBitmap(LatLng latLng) {
        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(0x110000FF);
        paint1.setStyle(Paint.Style.FILL);

        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(0xFF0000FF);
        paint2.setStyle(Paint.Style.STROKE);

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.human_male_icon_green);

        int radius = offset = convertMetersToPixels(latLng.latitude, latLng.longitude, 100);

        if (radius < icon.getWidth() / 2) {

            radius = icon.getWidth() / 2;
        }

        Bitmap b = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        if (radius != icon.getWidth() / 2) {

            c.drawCircle(radius, radius, radius, paint1);
            c.drawCircle(radius, radius, radius, paint2);
        }
        c.drawBitmap(icon, radius - icon.getWidth() / 2, radius - icon.getHeight() / 2, new Paint());

        return b;
    }
    private LatLng getCoords(LatLng latLng) {

        Projection proj = mMap.getProjection();
        Point p = proj.toScreenLocation(latLng);
        p.set(p.x, p.y + offset);

        return proj.fromScreenLocation(p);
    }
    private void showLogoWhenNoStream(){
        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(player != null) {
                        if (!exoplayerPlaying(player)) {
                            btnSwitch.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.hucka_play, null));
                        } else {
                            btnSwitch.setBackground(null);
                            btnSwitch.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            }
        });
    }
    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Päivitys peruutettu", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_OK) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                //Toast.makeText(getApplicationContext(),"Päivitys valmiina! Käynnistä ohjelma uudelleen.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Päivitys epäonnistui " + resultCode, Toast.LENGTH_LONG).show();
                checkUpdate();
            }
        }
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken !=  null) {
                        setHukcaKey(idToken);
                        if(!isMyServiceRunning(WebsocketService.class)) {
                            startService();
                        }
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
    private void popupSnackBarForCompleteUpdate() {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), "Uusi sovellus on valmiina!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Asenna", view -> {
                    if (appUpdateManager != null) {
                        appUpdateManager.completeUpdate();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.purple_500))
                .show();
    }
    private void removeInstallStateUpdateListener() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
//        account = GoogleSignIn.getLastSignedInAccount(this);
        /*if (account == null) {
            Toast.makeText(getApplicationContext(), "Kirjaudu Google tilille", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MapsActivity.this, Therion.class);
            startActivity(intent);
        }*/
    }
    private void getStreamers(String serialHash) {
        URL endpoint = null;

        try {
            endpoint = new URL("https://toor.hopto.org/api/v1/config/" + serialHash);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL finalEndpoint = endpoint;

        HttpsURLConnection myConnection =
                null;
        try {
            assert finalEndpoint != null;
            myConnection = (HttpsURLConnection) finalEndpoint.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = Base64.getEncoder().encodeToString(USERNAME_PASSWORD.getBytes(StandardCharsets.UTF_8));
        myConnection.setRequestProperty("Authorization", "Basic " + encoded);
        myConnection.setRequestProperty("x-apikey", XAPIKEY);

        try {
            if (myConnection.getResponseCode() == 200){
                InputStream responseBody = myConnection.getInputStream();
                BufferedReader bR = new BufferedReader(  new InputStreamReader(responseBody));
                String line = "";

                StringBuilder responseStrBuilder = new StringBuilder();
                while((line =  bR.readLine()) != null){

                    responseStrBuilder.append(line);
                }
                responseBody.close();

                JSONObject result= new JSONObject(responseStrBuilder.toString());
                rtsp_url_stream_1 = result.getString("rtsp_url_stream_1");
                rtsp_url_stream_2 = result.getString("rtsp_url_stream_2");
                rtsp_url_audio = result.getString("rtsp_url_audio");
                stream_1_key = result.getString("stream_1_key");
                stream_2_key = result.getString("stream_2_key");
                audio_key = result.getString("audio_key");
            }
            else{
                myConnection.disconnect();
            }
            myConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public static double getScreenSizeInches(Activity activity){
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int mWidthPixels = displayMetrics.widthPixels;
        int mHeightPixels = displayMetrics.heightPixels;

        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try{
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {}
        }

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(mWidthPixels / dm.xdpi, 2);
        double y = Math.pow(mHeightPixels / dm.ydpi, 2);
        return Math.sqrt(x + y);
    }

    private void recordingStatus() throws InterruptedException {
        if (recordingStatus == 0) {
            recordVideo.setImageResource(R.drawable.record_video);
        } else {
            recordVideo.setImageResource(R.drawable.record_video_stop);
        }
    }
    @SuppressLint("SetTextI18n")

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
                        .setFilterByAuthorizedAccounts(false)
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
        if(!isMyServiceRunning(WebsocketService.class)) {
            startService();
        }
    }
    private void setHukcaKey(String idToken) throws InterruptedException, JSONException {
        hukcakeyt.addAll(dbh.getAllHuckaKeyt());
        for (MyDBHandlerHukcaKey hukcakey : hukcakeyt) {
            hukca_key = String.valueOf(hukcakey.getHukca_key());
        }

        String testiEk = getAuth(hukca_key, AUTH_URL_HUKCA_KEY);
        if (testiEk == null) {
            JSONObject jso = new JSONObject(getAuth(idToken, AUTH_URL_TOKEN));
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
            if(!isMyServiceRunning(WebsocketService.class)) {
                startService();
            }
            //System.out.println("Oli epäkelpo hukkis");
        }
        else {
            //System.out.println("Käypäinen hukkis");
            if(!isMyServiceRunning(WebsocketService.class)) {
                startService();
            }
        }
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
    public void startService() {
        Intent serviceIntent = new Intent(this, WebsocketService.class);
        serviceIntent.putExtra("inputExtra", "HukCa taustapalvelu");
        ContextCompat.startForegroundService(this, serviceIntent);
        startForegroundService(serviceIntent);

    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, WebsocketService.class);
        stopService(serviceIntent);
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("LocationUpdates");
            try {
                LatLng latLongUnchecked = b.getParcelable("LatLng");
                LatLng check = new LatLng(0,0);
                assert latLongUnchecked != null;
                if (!latLongUnchecked.equals(check)) {
                    latLong = latLongUnchecked;
                }
                if (firstTime) {
                    new Handler().postDelayed(() -> {
                        new Handler().postDelayed(() ->
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong)), 100);
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        firstTime = false;
                    }, 100);
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
            alt = b.getInt("Alt");
            speed = b.getInt("Speed");
            last_update = b.getString("Last_update");
            camera_mode = b.getInt("Camera_mode");
            ir_active = b.getInt("Ir_active");
            recordingStatus = b.getInt("Recording_activated");
            try {
                recordingStatus();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                detected_id = b.getInt("Detected_ID");
            } catch (Exception e) {
            }
            try {
                detected_object = b.getString("Detected_Object");
            } catch (Exception e) {
            }
            try {
                detected_ago = b.getInt("Detected_Ago");
            } catch (Exception e) {
            }
            try {
                try {
                    if(tunnistuskytkenta) {
                        Detector();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setBattery();

               /* if (!powerService()) {
                    showLogoWhenNoStream();
                }*/
                active_video = b.getInt("Active_video");
                if (active_video == 2 && !exoplayerPlaying(player2)) {
                    if (!exoplayerPlaying(player2) && !exoplayerBuffering(player2)) {
                        if (!videoCheck) {
                            new Handler().postDelayed(() ->
                                    {
                                        try {
                                            exoplayerTwoStreams();
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    },
                                    14000);
                            if(!isMyServiceRunning(WebsocketService.class)) {
                                startService();
                            }
                            videoCheck = true;
                        }
                    } else {
                        videoCheck = false;
                    }
                } /*else {
                    showLogoWhenNoStream();
                }*/
                koiraNopeus.setText(String.valueOf(speed));
                //if (checkGPSandNetwork()) {

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
                if (gpsTrack != null) {
                    gpsTrack.remove();
                }
                route = a.getParcelableArrayList("Route");
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED);
                polylineOptions.width(8);
                gpsTrack = mMap.addPolyline(polylineOptions);
                gpsTrack.setZIndex(1000);
                gpsTrack.setPoints(route);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}


