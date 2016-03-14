package com.example.tuannguyen.spring2016urop;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiInfoBarf extends AppCompatActivity{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final String SILENCE_SSID = "MIT SECURE";
    private static final int threshold = -65;

    public static final int SILENT = 0;
    public static final int VIBRATE = 1;

    public static final int AUTO = 0;
    public static final int MANUAL = 1;

    private static final int NOTIFICATION_ID = 001;

    WifiManager wifiManager;
    List<ScanResult> scanResults;

    AudioManager audioManager;

    NotificationCompat.Builder nBuilderSilent;
    NotificationCompat.Builder nBuilderNormal;
    NotificationManager nManager;

    Timer t;
    TimerTask timer;

    Activity wifiInfoBarf;
    TextView expo;
    TextView silent;
    TextView scanList;
    Switch autoManual;
    Switch silenceVibrate;
    TextView numBuzzText;
    EditText numBuzzBox;
    TextView lenBuzzText;
    EditText lenBuzzBox;
    TextView lenSilentText;
    EditText lenSilentBox;

    int ringerMode;
    int actionMode;
    boolean inSilentZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asks for permission if not already given (especially necessary for Marshmallow)
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }

        setContentView(R.layout.activity_wifi_info_barf);

        wifiInfoBarf = this;

        // Stores the various view objects in the activity to change later
        expo = (TextView) this.findViewById(R.id.expo);

        silent = (TextView) this.findViewById(R.id.silent);

        scanList = (TextView) this.findViewById(R.id.scan_list);
        scanList.setMovementMethod(new ScrollingMovementMethod());

        numBuzzText = (TextView) this.findViewById(R.id.number_buzzes_text);
        lenBuzzText = (TextView) this.findViewById(R.id.length_buzzes_text);
        lenSilentText = (TextView) this.findViewById(R.id.length_silence_text);

        // Default modes for settings
        ringerMode = SILENT;
        actionMode = MANUAL;
        inSilentZone = false;

        // Specifies behavior for auto/manual switch
        autoManual = (Switch) this.findViewById(R.id.auto_manual);
        autoManual.setChecked(false);
        autoManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    actionMode = AUTO;
                }else{
                    actionMode = MANUAL;
                }

            }
        });

        // Specifies behavior for silence/vibrate switch
        silenceVibrate = (Switch) this.findViewById(R.id.silence_vibrate);
        silenceVibrate.setChecked(true);
        silenceVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    ringerMode = SILENT;
                } else {
                    ringerMode = VIBRATE;
                }

            }
        });

        // Stores textbox objects so we can use them as input
        numBuzzBox = (EditText) this.findViewById(R.id.number_buzzes_box);
        numBuzzBox.setText("1");
        lenBuzzBox = (EditText) this.findViewById(R.id.length_buzzes_box);
        lenBuzzBox.setText("100");
        lenSilentBox = (EditText) this.findViewById(R.id.length_silence_box);
        lenSilentBox.setText("100");

        // Creates the managers for WiFi and Audio settings
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Creates the intents that silences the phone when the notification is clicked
        Intent setSilent = new Intent(this, SilenceService.class);
        PendingIntent pSetSilent = PendingIntent.getService(this, 0, setSilent, 0);

        Intent setVibrate = new Intent(this, VibrateService.class);
        PendingIntent pSetVibrate = PendingIntent.getService(this, 0, setVibrate, 0);

        Intent setNormal = new Intent(this, NormalService.class);
        PendingIntent pSetNormal = PendingIntent.getService(this, 0, setNormal, 0);

        Intent doNothing = new Intent();
        PendingIntent pDoNothing = PendingIntent.getService(this, 0, doNothing, 0);

        // Creates necessary objects for creating notifications, associating pending intents
        nBuilderSilent = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_trollface)
                .setContentTitle("Please Silence Me!")
                .setContentText("You are entering a silent area.")
                .setAutoCancel(true)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_done_black_24dp,
                        "done", pDoNothing).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_vibration_black_24dp,
                        "vibrate", pSetVibrate).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_volume_off_black_24dp,
                        "silent", pSetSilent).build());

        nBuilderNormal = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_trollface)
                .setContentTitle("Turn on my Volume Again?")
                .setContentText("You have exited the area.")
                .setAutoCancel(true)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_done_black_24dp,
                        "done", pDoNothing).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_volume_up_black_24dp,
                        "sound", pSetNormal).build());

        // Creates a notification manager to issue the notifications
        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Creates a timer that scans WiFi and changes settings every N minutes
        timer = new TimerTask() {
            @Override
            public void run() {
                wifiInfoBarf.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Performs a WiFi scan and saves the resulting list
                        wifiManager.startScan();
                        scanResults = wifiManager.getScanResults();

                        nManager.cancel(NOTIFICATION_ID);

                        // If the scan is not empty, then print out the list
                        if(scanResults == null){
                            scanList.setText(R.string.null_scan);
                        }else{
                            String text = "";
                            boolean shouldSilence = false;
                            for(ScanResult scan : scanResults) {
                                text += "[" + scan.level + " dBm] (" + scan.BSSID + ") " +
                                        scan.SSID;
                                if(scan.SSID.equals(SILENCE_SSID) && scan.level > threshold){
                                    text += " <-- SHHHH!";
                                    shouldSilence = true;
                                }
                                text += "\n";
                            }

                            // If the "silence SSID" is found, then change the ringer mode
                            if(shouldSilence){
                                silent.setText(R.string.yes_silent);
                                switch (actionMode) {
                                    // If the app is allowed to automatically change the setting
                                    case AUTO:
                                        switch (ringerMode) {
                                            case SILENT:
                                                audioManager.setRingerMode(
                                                        AudioManager.RINGER_MODE_SILENT);
                                                break;

                                            case VIBRATE:
                                                audioManager.setRingerMode(
                                                        AudioManager.RINGER_MODE_VIBRATE);
                                                break;

                                            default:
                                                throw new IllegalStateException();
                                        }
                                        break;

                                    // If the app needs to send a notification out instead
                                    case MANUAL:
                                        if(!inSilentZone) {
                                            inSilentZone = true;

                                            int numBuzz;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                numBuzz = 1;
                                            } else {
                                                numBuzz = Integer.parseInt(
                                                        numBuzzBox.getText().toString());
                                            }

                                            int lenBuzz;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                lenBuzz = 100;
                                            } else {
                                                lenBuzz = Integer.parseInt(
                                                        lenBuzzBox.getText().toString());
                                            }

                                            int lenSilent;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                lenSilent = 100;
                                            } else {
                                                lenSilent = Integer.parseInt(
                                                        lenSilentBox.getText().toString());
                                            }

                                            long[] buzzPattern = new long[2*numBuzz + 1];

                                            buzzPattern[0] = lenSilent;
                                            for(int i = 0; i < numBuzz; i++){
                                                buzzPattern[2*i + 1] = lenBuzz;
                                                buzzPattern[2*i + 2] = lenSilent;
                                            }

                                            nBuilderSilent.setVibrate(buzzPattern);
                                            nManager.notify(NOTIFICATION_ID,
                                                    nBuilderSilent.build());
                                        }
                                        break;

                                    default:
                                        throw new IllegalStateException();
                                }
                            }else{
                                silent.setText(R.string.no_silent);
                                switch (actionMode){
                                    case AUTO:
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                        break;

                                    case MANUAL:
                                        if(inSilentZone) {
                                            inSilentZone = false;


                                            int numBuzz;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                numBuzz = 1;
                                            } else {
                                                numBuzz = Integer.parseInt(
                                                        numBuzzBox.getText().toString());
                                            }

                                            int lenBuzz;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                lenBuzz = 100;
                                            } else {
                                                lenBuzz = Integer.parseInt(
                                                        lenBuzzBox.getText().toString());
                                            }

                                            int lenSilent;
                                            if(numBuzzBox.getText().toString().equals("")){
                                                lenSilent = 100;
                                            } else {
                                                lenSilent = Integer.parseInt(
                                                        lenSilentBox.getText().toString());
                                            }

                                            long[] buzzPattern = new long[2*numBuzz + 1];

                                            buzzPattern[0] = lenSilent;
                                            for(int i = 0; i < numBuzz; i++){
                                                buzzPattern[2*i + 1] = lenBuzz;
                                                buzzPattern[2*i + 2] = lenSilent;
                                            }

                                            nBuilderNormal.setVibrate(buzzPattern);
                                            if(audioManager.getRingerMode()
                                                    != AudioManager.RINGER_MODE_NORMAL) {
                                                nManager.notify(NOTIFICATION_ID,
                                                        nBuilderNormal.build());
                                            }
                                        }
                                        break;

                                    default:
                                        break;
                                }
                            }
                            scanList.setText(text);
                        }
                    }
                });
            }
        };

        t = new Timer();
        t.scheduleAtFixedRate(timer, 0, 30000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_info_barf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
