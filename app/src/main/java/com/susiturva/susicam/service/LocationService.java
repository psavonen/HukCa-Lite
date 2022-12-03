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
import android.util.JsonReader;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.susiturva.susicam.DatabaseHelper;
import com.susiturva.susicam.MapsActivity;
import com.susiturva.susicam.MyDBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class LocationService extends Service {
    public static final String CHANNEL_ID = "LocationServiceChannel";
    public static final String SUSIPURKKI_SUSIPURKKI = "susipurkki:susipurkki";
    public static final String X_API_KEY = "awidjilherg";
    public static final int ROUTE_LENGTH = 20;
    private boolean runner = true;
    public Long session_id;
    public Double lat;
    public Double lng;
    public Double alt;
    public Double speed;
    public Double bearing;
    private float erotus;
    public int bat_soc;
    public Long uptime;
    public String last_update;
    public int active_video;
    public int active_audio;
    public int camera_mode;
    public int ir_active;
    public int front_video_activated;
    public int back_video_activated;
    public int audio_activated;
    public String ip_address;
    private DatabaseHelper db;
    private List<MyDBHandler> sarjanumerot = new ArrayList<>();
    private String srnumero;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new DatabaseHelper(this);
        sarjanumerot.addAll(db.getAllSarjanumerot());
        for(MyDBHandler sarjanumero : sarjanumerot) {
            srnumero = String.valueOf(sarjanumero.getSarjanumero());
        }
        String serial_hash = srnumero;
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SusiCam palvelu")
                .setContentText(input)
                //.setSmallIcon(R.drawable.alphaw) TODO change this
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        Runnable runnable = () -> {

            while(runner) {
                try {
                    location(serial_hash);
                    try {
                        powerService();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }catch(Exception e){}

                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    streamers(serial_hash);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        Runnable runnable1 = () -> {

            while(runner) {
                try {
                    route(serial_hash);

                }catch(Exception e){}

                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread2 = new Thread(runnable1);
        thread2.start();

        return START_STICKY;
    }
    @Override
    public void onCreate(){
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy(){
        runner = false;
    }

    private void location(String serialHash){

        URL endpoint = null;

        try {
            endpoint = new URL("https://toor.hopto.org/api/v1/location/" + serialHash);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        URL finalEndpoint = endpoint;

        HttpsURLConnection connection =
                null;
        try {
            connection = (HttpsURLConnection) finalEndpoint.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = Base64.getEncoder().encodeToString(SUSIPURKKI_SUSIPURKKI.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestProperty("x-apikey", X_API_KEY);

        try {

            if (connection.getResponseCode() == 200){
                InputStream responseBody = connection.getInputStream();

                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseBody, StandardCharsets.UTF_8);

                JsonReader jsonReader = new JsonReader(responseBodyReader);

                jsonReader.beginObject();

                try {
                    while(jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        if (key.equals("session_id")) {
                            session_id = jsonReader.nextLong();
                        }
                        String latKey = jsonReader.nextName();
                        if(latKey.equalsIgnoreCase("lat"))
                        {
                            lat = jsonReader.nextDouble();
                        }
                        String lngKey = jsonReader.nextName();
                        if (lngKey.equalsIgnoreCase("lng")){
                            lng = jsonReader.nextDouble();
                        }
                        String altKey = jsonReader.nextName();
                        if(altKey.equalsIgnoreCase("alt")){
                            alt = jsonReader.nextDouble();
                        }
                        String speedKey = jsonReader.nextName();
                        if(speedKey.equalsIgnoreCase("speed")){
                            speed = jsonReader.nextDouble();
                        }
                        String bearingKey = jsonReader.nextName();
                        if(bearingKey.equalsIgnoreCase("bearing")){
                            bearing = jsonReader.nextDouble();
                        }
                        String batSocKey = jsonReader.nextName();
                        if(batSocKey.equalsIgnoreCase("bat_soc")){
                            bat_soc = jsonReader.nextInt();
                        }
                        String uptimeKey = jsonReader.nextName();
                        if(uptimeKey.equalsIgnoreCase("uptime")){
                            uptime = jsonReader.nextLong();
                        }
                        String LastUpdateKey = jsonReader.nextName();
                        if(LastUpdateKey.equalsIgnoreCase("last_update")){
                            last_update = jsonReader.nextString();
                        }
                        String activeVideoKey = jsonReader.nextName();
                        if(activeVideoKey.equalsIgnoreCase("active_video")){
                            active_video = jsonReader.nextInt();
                        }
                        String activeAudioKey = jsonReader.nextName();
                        if(activeAudioKey.equalsIgnoreCase("active_audio")){
                            active_audio = jsonReader.nextInt();
                        }
                        String ipAddressKey = jsonReader.nextName();
                        if(ipAddressKey.equalsIgnoreCase("ip_address")){
                            ip_address = jsonReader.nextString();
                        }
                        String cameraModeKey = jsonReader.nextName();
                        if(cameraModeKey.equalsIgnoreCase("camera_mode")){
                            camera_mode = jsonReader.nextInt();
                        }
                        String irActiveKey = jsonReader.nextName();
                        if(irActiveKey.equalsIgnoreCase("ir_active")){
                            ir_active = jsonReader.nextInt();
                        }
                        String front_video_activatedKey = jsonReader.nextName();
                        if(front_video_activatedKey.equalsIgnoreCase("front_video_activated")){
                            front_video_activated = jsonReader.nextInt();
                        }
                        String back_video_activatedKey = jsonReader.nextName();
                        if(back_video_activatedKey.equalsIgnoreCase("back_video_activated")){
                            back_video_activated = jsonReader.nextInt();
                        }
                        String audio_activatedKey = jsonReader.nextName();
                        if(audio_activatedKey.equalsIgnoreCase("audio_activated")){
                            audio_activated = jsonReader.nextInt();
                        }


                        LatLng latLng = new LatLng(lat, lng);
                        Intent i = new Intent("LocationUpdates");
                        Bundle b = new Bundle();
                        b.putLong("SessionID", session_id);
                        b.putDouble("Alt", alt);
                        b.putDouble("Speed", speed);
                        b.putDouble("Bearing", bearing);
                        b.putInt("Bat_soc", bat_soc);
                        b.putString("Last_update", last_update);
                        b.putString("Ip_address", ip_address);
                        b.putInt("Camera_mode", camera_mode);
                        b.putInt("Ir_active", ir_active);
                        b.putInt("Front_video_activated", front_video_activated);
                        b.putInt("Back_video_activated", back_video_activated);
                        b.putInt("Audio_activated", audio_activated);
                        b.putParcelable("LatLng", latLng);
                        i.putExtra("LatLng", b);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);


                    }
                }
                catch(Exception e){}
            }


            else{
            }
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

    private void streamers(String serialHash){
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
        String encoded = Base64.getEncoder().encodeToString(SUSIPURKKI_SUSIPURKKI.getBytes(StandardCharsets.UTF_8));
        myConnection.setRequestProperty("Authorization", "Basic " + encoded);
        myConnection.setRequestProperty("x-apikey", X_API_KEY);

        try {

            if (myConnection.getResponseCode() == 200){
                InputStream responseBody = myConnection.getInputStream();

                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseBody, StandardCharsets.UTF_8);

                JsonReader jsonReader = new JsonReader(responseBodyReader);

                jsonReader.beginObject();

                try {

                        String serialHashKey = jsonReader.nextName();
                        String serial_Hash = jsonReader.nextString();
                        String serialKeyKey = jsonReader.nextName();
                        String serialKey = jsonReader.nextString();
                        String startTimeKey = jsonReader.nextName();
                        String startTime = jsonReader.nextString();
                        String send_stream_1_addressKey = jsonReader.nextName();
                        String send_stream_1_address = jsonReader.nextString();
                        String send_stream_2_addressKey = jsonReader.nextName();
                        String send_stream_2_address = jsonReader.nextString();
                        String send_audio_addressKey = jsonReader.nextName();
                        String send_audio_address = jsonReader.nextString();
                        String send_stream_1_portKey = jsonReader.nextName();
                        String send_stream_1_port = jsonReader.nextString();
                        String send_stream_2_portKey = jsonReader.nextName();
                        String send_stream_2_port = jsonReader.nextString();
                        String send_audio_portKey = jsonReader.nextName();
                        String send_audio_port = jsonReader.nextString();
                        String send_stream_1_optionsKey = jsonReader.nextName();
                        String send_stream_1_options = jsonReader.nextString();
                        String send_stream_2_optionsKey = jsonReader.nextName();
                        String send_stream_2_options = jsonReader.nextString();
                        String send_audio_optionsKey = jsonReader.nextName();
                        String send_audio_options = jsonReader.nextString();
                        String activate_stream_1Key = jsonReader.nextName();
                        String activate_stream_1 = jsonReader.nextString();
                        String activate_stream_2Key = jsonReader.nextName();
                        String activate_stream_2 = jsonReader.nextString();
                        String activate_audioKey = jsonReader.nextName();
                        String activate_audio = jsonReader.nextString();
                        String activate_ir_ledKey = jsonReader.nextName();
                        String activate_ir_led = jsonReader.nextString();
                        String activate_night_visionKey = jsonReader.nextName();
                        String activate_night_vision = jsonReader.nextString();
                        String shutdown_allKey = jsonReader.nextName();
                        String shutdown_all = jsonReader.nextString();
                        String update_micro_fwKey = jsonReader.nextName();
                        String update_micro_fw = jsonReader.nextString();

                        String rtsp_url_stream_1Key = jsonReader.nextName();
                        String rtsp_url_stream_1 = jsonReader.nextString();
                        String rtsp_url_stream_2Key = jsonReader.nextName();
                        String rtsp_url_stream_2 = jsonReader.nextString();
                        String rtsp_url_audioKey = jsonReader.nextName();
                        String rtsp_url_audio = jsonReader.nextString();

                        String hls_url_stream_1Key = jsonReader.nextName();
                        String hls_url_stream_1 = jsonReader.nextString();
                        String hls_url_stream_2Key = jsonReader.nextName();
                        String hls_url_stream_2 = jsonReader.nextString();


                        String stream_1_keyKey = jsonReader.nextName();
                        String stream_1_key = jsonReader.nextString();
                        String stream_2_keyKey = jsonReader.nextName();
                        String stream_2_key = jsonReader.nextString();
                        String audio_keyKey = jsonReader.nextName();
                        String audio_key = jsonReader.nextString();

                        Intent i = new Intent("Streams");
                        Bundle a = new Bundle();
                        a.putString("rtsp_url_stream_1", rtsp_url_stream_1);
                        a.putString("rtsp_url_stream_2", rtsp_url_stream_2);
                        a.putString("rtsp_url_audio", rtsp_url_audio);
                        a.putString("stream_1_key", stream_1_key);
                        a.putString("stream_2_key", stream_2_key);
                        a.putString("audio_key", audio_key);
                        i.putExtra("Streams", a);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);



                }
                catch(Exception e){e.printStackTrace();}
            }


            else{
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void route(String serialHash){

        URL endpoint = null;

        try {
            endpoint = new URL("https://toor.hopto.org/api/v1/route/" + serialHash + "/geojson/" + session_id + "?every_nth_point=20");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        URL finalEndpoint = endpoint;

        HttpsURLConnection connection =
                null;
        try {
            connection = (HttpsURLConnection) finalEndpoint.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = Base64.getEncoder().encodeToString(SUSIPURKKI_SUSIPURKKI.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestProperty("x-apikey", X_API_KEY);

        try {
            if(session_id != null) {
                if (connection.getResponseCode() == 200) {

               Scanner scan = new Scanner(connection.getInputStream());
                String str = new String();
                while(scan.hasNext())
                    str += scan.nextLine();
                scan.close();

                JSONObject obj = new JSONObject(str);

                    JSONObject obj2 = obj.getJSONObject("geometry");
                    JSONArray obj3 = obj2.getJSONArray("coordinates");
                    ArrayList<LatLng> latLngList = new ArrayList<>();

                    for(int i = 0; i < obj3.length(); i++){
                        JSONArray arr = obj3.getJSONArray(i);
                       double lat = arr.getDouble(0);
                       double lon = arr.getDouble(1);
                       if(lat != 0.0)
                            latLngList.add(new LatLng(lon, lat));
                        }
                    Collections.reverse(latLngList);
                    latLngList.subList(ROUTE_LENGTH, latLngList.size()).clear();

                        Intent i = new Intent("Route");
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("Route", latLngList );
                        i.putExtra("Route", b);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

                } else {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void powerService() throws ParseException {
        Calendar rightNow = Calendar.getInstance();
        float sinceMidnight = rightNow.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(last_update);
        float millis = date.getTime();
        try{
            MapsActivity.onOff.setText("OFF | ");
            erotus = sinceMidnight - millis;
            MapsActivity.onOff.setText("ON  | ");
            stopTimer();
            startTimer();
            if (erotus > 5000) {
                MapsActivity.onOff.setText("OFF | ");
            } else {
                MapsActivity.onOff.setText("ON  | ");
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
}