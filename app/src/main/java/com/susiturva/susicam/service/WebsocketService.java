package com.susiturva.susicam.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.susiturva.susicam.MapsActivity;
import com.susiturva.susicam.R;
import com.susiturva.susicam.databasehandlers.DatabaseHelper;
import com.susiturva.susicam.databasehandlers.DatabaseHelperHukcaKey;
import com.susiturva.susicam.databasehandlers.MyDBHandler;
import com.susiturva.susicam.databasehandlers.MyDBHandlerHukcaKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebsocketService extends Service {
    public static final String CHANNEL_ID = "WebsocketServiceChannel";
    private static final String WSS = "wss://toor.hopto.org:443/api/v1/wsphone/";
    public static final String USERNAME_PASSWORD = "susipurkki:susipurkki";
    public static final String X_API_KEY = "awidjilherg";
    public static final int ROUTE_LENGTH = 20;
    private boolean runner = true;
    public Long session_id;
    public Double lat;
    public Double lng;
    public int alt;
    public int speed;
    public Double bearing;
    private float erotus;
    public int bat_soc;
    public Long uptime;
    public String last_update;
    public int active_video;
    public int active_audio;
    public int active_recording;
    public int camera_mode;
    public int ir_active;
    public int front_video_activated;
    public int back_video_activated;
    public int audio_activated;
    public int detected_id;
    public int detected_ago;
    public int recordingStatus;
    public String detected_object;
    public String ip_address;
    private DatabaseHelper db;
    private DatabaseHelperHukcaKey dbh;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    private List<MyDBHandlerHukcaKey> hukcakeyt = new ArrayList<>();
    private String srnumero;
    private String hukca_key;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();
    private Notification notification;

    private WebSocket ws = null;

    public WebsocketService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new DatabaseHelper(this);
        sarjanumerot.addAll(db.getAllSarjanumerot());
        for (MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        db.close();
        dbh = new DatabaseHelperHukcaKey(this);
        hukcakeyt.addAll(dbh.getAllHuckaKeyt());
        for (MyDBHandlerHukcaKey hukcakey : hukcakeyt) {
            hukca_key = String.valueOf(hukcakey.getHukca_key());
        }
        dbh.close();

        String serial_hash = srnumero;
       try {
            createWebSocketClient();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        createNotificationChannel();
        String input = "HukCa paikannuspalvelu";
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setChannelId(CHANNEL_ID)
                .setContentTitle("HukCa palvelu")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForegroundService(new Intent(this, WebsocketService.class));
        startForeground(1,notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

    }

    private void createWebSocketClient() throws URISyntaxException {
        String encoded = Base64.getEncoder().encodeToString((USERNAME_PASSWORD).getBytes(StandardCharsets.UTF_8));
        URI uri;
        try {
            uri = new URI(WSS + srnumero + "/" + hukca_key);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

        // Create a WebSocket. The timeout value set above is used.
        try {
            ws = factory.createSocket(uri);

            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    insideWebSocket(message);
                }
            });
            ws.addHeader("Authorization", "Basic " + encoded);
            ws.connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }


    private void insideWebSocket(String s) {
        JSONObject first = new JSONObject();
        JSONObject o = new JSONObject();
        JSONObject ob = new JSONObject();
        try {
            first = new JSONObject(s);
        } catch (JSONException e) {
        }
        try {
            if (!first.isNull("event_type")) {
                if (first.getString("event_type").equalsIgnoreCase("interval")) {
                    o = first.getJSONObject("message");
                    lat = o.getDouble("lat");
                    lng = o.getDouble("lng");
                    alt = o.getInt("alt");
                    speed = o.getInt("speed");
                    bat_soc = o.getInt("bat_soc");
                    bearing = o.getDouble("bearing");
                    last_update = o.getString("last_update");
                    active_video = o.getInt("active_video");
                    recordingStatus = o.getInt("active_recording");
                    if (!o.isNull("detected_id")) {
                        detected_id = o.getInt("detected_id");
                    }
                    detected_object = o.getString("detected_object");
                    if (!o.isNull("detected_ago")) {
                        detected_ago = o.getInt("detected_ago");
                    }

                    LatLng latLng = new LatLng(lat, lng);
                    Intent i =  new Intent("LocationUpdates");
                    Bundle b = new Bundle();
                   // b.putLong("SessionID", session_id);
                    b.putInt("Alt", alt);
                    b.putInt("Speed", speed);
                    b.putDouble("Bearing", bearing);
                    b.putInt("Bat_soc", bat_soc);
                    b.putString("Last_update", last_update);
                    //b.putString("Ip_address", ip_address);
                    //b.putInt("Camera_mode", camera_mode);
                    //b.putInt("Ir_active", ir_active);
                    //b.putInt("Front_video_activated", front_video_activated);
                    //b.putInt("Back_video_activated", back_video_activated);
                    //b.putInt("Audio_activated", audio_activated);
                    b.putInt("Recording_activated", active_recording);
                    b.putParcelable("LatLng", latLng);
                    b.putInt("Detected_ID", detected_id);
                    b.putString("Detected_Object", detected_object);
                    b.putInt("Detected_Ago", detected_ago);
                    b.putInt("Active_video", active_video);
                    i.putExtra("LocationUpdates", b);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                }

                if (first.getString("event_type").equalsIgnoreCase("route")) {
                    ob = first.getJSONObject("message");
                    JSONObject oc = ob.getJSONObject("geometry");
                    JSONArray obj3 = oc.getJSONArray("coordinates");
                    ArrayList<LatLng> latLngList = new ArrayList<>();
                    if (!oc.isNull("coordinates")) {
                        for (int i = 0; i < obj3.length(); i++) {
                            JSONArray arr = obj3.getJSONArray(i);
                            double lat = arr.getDouble(0);
                            double lon = arr.getDouble(1);
                            if (lat != 0.0)
                                latLngList.add(new LatLng(lon, lat));
                        }
                        Collections.reverse(latLngList);
                        if (latLngList.size() > 20) {
                            latLngList.subList(20, latLngList.size()).clear();
                        }
                        Intent i = new Intent("Route");
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("Route", latLngList);
                        i.putExtra("Route", b);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}