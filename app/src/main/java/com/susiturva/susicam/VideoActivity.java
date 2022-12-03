package com.susiturva.susicam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

public class VideoActivity extends Activity {

    private String stream1 = MapsActivity.stream1;
    private String stream2 = MapsActivity.stream2;

    private ExoPlayer player;
    private ExoPlayer player2;

    private StyledPlayerView playerView;
    private StyledPlayerView playerView2;

    private Button vaihto;
    private Button takaisin;
    private Button yonako;


    private boolean switcher = false;
    private boolean nightVision = false;
    private boolean led = false;

    private final String CONTROL_URL_BASE = "https://toor.hopto.org/api/v1/control";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video);

    exoplayerTwoStreams();
        vaihto = findViewById(R.id.connect);
        takaisin = findViewById(R.id.takaisin);
        yonako = findViewById(R.id.yonako);

        yonako.setOnClickListener(v -> {
            String urli = CONTROL_URL_BASE + "/" + MapsActivity.srnumero + "/night_vision/";
            System.out.println(urli);
            if(!nightVision){
                setControl(urli + "1");
                nightVision = true;
            }
            else{
                setControl(urli + "0");
                nightVision = false;
            }
        });

        takaisin.setOnClickListener(v -> {
            Intent i = new Intent(VideoActivity.this, MapsActivity.class);
            startActivity(i);
        });

        vaihto.setOnClickListener(v -> {
            if(!switcher){
                playerView.setVisibility(View.INVISIBLE);
                playerView2.setVisibility(View.VISIBLE);
                switcher = true;
            }else{
                playerView.setVisibility(View.VISIBLE);
                playerView2.setVisibility(View.INVISIBLE);
                switcher = false;
            }
        });
    }

    private void exoplayerTwoStreams(){
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
        playerView.setUseController(false);
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
        playerView2.setUseController(false);
        playerView2.hideController();
        playerView2.setPlayer(player2);

        player2.addMediaItem(secondStream);

        // Prepare the player.
        player2.prepare();
        player2.play();
        playerView.setVisibility(View.VISIBLE);
        playerView2.setVisibility(View.INVISIBLE);
        Toast.makeText(VideoActivity.this,
                "Pieni hetki, video yhdistyy." ,
                Toast.LENGTH_LONG).show();
    }
    private void setControl(String url){
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
}
