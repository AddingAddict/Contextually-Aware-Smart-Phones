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

import com.example.tuannguyen.spring2016urop.enums.ActionMode;
import com.example.tuannguyen.spring2016urop.enums.RingerMode;
import com.example.tuannguyen.spring2016urop.enums.State;
import com.example.tuannguyen.spring2016urop.services.NormalService;
import com.example.tuannguyen.spring2016urop.services.SilenceService;
import com.example.tuannguyen.spring2016urop.services.VibrateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Tuan Nguyen
 */
public class WifiInfoBarf extends AppCompatActivity{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    /**
     * SSID that signals a silent zone
     */
    private static final String SILENCE_SSID = "MIT SECURE";

    /**
     * SSID that signals a non-silent zone
     */
    private static final String SOUND_SSID = "MIT GUEST";

    /**
     * Minimum signal strength to be considered
     */
    private static final int threshold = -65;

    /**
     * Behaviour object in charge of changing the state of the app
     */
    Behaviour behaviour;

    /**
     * Ringer mode (vibrate or silent) that the phone will switch to in AUTO mode
     */
    RingerMode ringerMode;

    /**
     * Mode of action. AUTO = change setting automatically, MANUAL = push a notification to change settings
     */
    ActionMode actionMode;

    /**
     * Manager in charge of dealing with wifi scans
     */
    WifiManager wifiManager;

    /**
     * List of most recent scan results
     */
    List<ScanResult> scanResults;

    /**
     * Manager in charge of changing audio settings
     */
    AudioManager audioManager;

    /**
     * Timer object runs every 10 seconds
     */
    Timer t;

    /**
     * Dictates the action every time Timer t is called (based on app's state)
     */
    TimerTask timer;

    /**
     * Object referring to this activity (necessary for creating TimerTask timer only)
     */
    Activity wifiInfoBarf;

    /**
     * View object that clues what state the app is in (mostly for debugging purposes)
     */
    TextView currentState;

    /**
     * View object that lists all the wifi information that's important for the app (mostly for debugging purposes)
     */
    TextView scanList;

    /**
     * Switch that toggles between action modes
     */
    Switch autoManual;

    /**
     * Switch that toggles between ringer modes
     */
    Switch silenceVibrate;

    /**
     * View object that contains the number of notification buzzes
     */
    EditText numBuzzBox;

    /**
     * View object that contains the length of each notification buzz
     */
    EditText lenBuzzBox;

    /**
     * View object that contains the length between each notification buzz
     */
    EditText lenSilentBox;

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
        currentState = (TextView) this.findViewById(R.id.silent);

        scanList = (TextView) this.findViewById(R.id.scan_list);
        scanList.setMovementMethod(new ScrollingMovementMethod());

        // Default modes for settings
        ringerMode = RingerMode.SILENT;
        actionMode = ActionMode.MANUAL;

        // Specifies behavior for auto/manual switch
        autoManual = (Switch) this.findViewById(R.id.auto_manual);
        autoManual.setChecked(false);
        autoManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    actionMode = ActionMode.AUTO;
                }else{
                    actionMode = ActionMode.MANUAL;
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
                    ringerMode = RingerMode.SILENT;
                    if(actionMode.equals(ActionMode.AUTO) &&
                            behaviour.state.equals(State.SILENCING)) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                } else {
                    ringerMode = RingerMode.VIBRATE;
                    if(actionMode.equals(ActionMode.AUTO) &&
                            behaviour.state.equals(State.SILENCING)) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    }
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
        NotificationCompat.Builder nBuilderSilent = new NotificationCompat.Builder(this)
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

        NotificationCompat.Builder nBuilderNormal = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_trollface)
                .setContentTitle("Turn on my Volume Again?")
                .setContentText("You have exited the area.")
                .setAutoCancel(true)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_done_black_24dp,
                        "done", pDoNothing).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_volume_up_black_24dp,
                        "sound", pSetNormal).build());

        // Creates a notification manager to issue the notifications
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        behaviour = new Behaviour(nBuilderSilent, nBuilderNormal, nManager, audioManager);

        // Creates a timer that scans WiFi and changes settings every N minutes
        timer = new TimerTask() {
            @Override
            public void run() {
                wifiInfoBarf.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // If the app is in the WAITING state, then it should not initiate a wifi scan
                        if(behaviour.state == State.WAITING) {
                            currentState.setText(R.string.waiting);
                            behaviour.updateState(ringerMode, actionMode, numBuzzBox, lenBuzzBox, lenSilentBox);

                        // In any other state, it should initiate a wifi scan
                        } else {
                            if(behaviour.state == State.SEARCHING)
                                currentState.setText(R.string.searching);
                            else //behaviour.state == State.SILENCING
                                currentState.setText(R.string.silencing);

                            // Performs a WiFi scan and saves the resulting list
                            wifiManager.startScan();
                            scanResults = wifiManager.getScanResults();

                            // If the scan is not empty, then print out the list
                            if (scanResults == null) {
                                scanList.setText(R.string.null_scan);
                            } else {
                                /// TODO: 4/26/16
                                /* This is a bit of a mess. It reads through the scans, and if it is searching for a
                                 * "silent SSID", it will pass the scans above the threshold to the behaviour object
                                 * so that it can calculate if the user is moving or sitting still.
                                 *
                                 * If it is already silenced, then it will seek out "normal SSID's" to tell the app
                                 * to exit silent mode
                                 *
                                 * Also prints a nice list for debugging purposes
                                 */
                                String text = "";
                                int maxSoundSSIDLevel = -100;
                                int maxSilentSSIDLevel = -100;
                                List<ScanResult> silentScans = new ArrayList<ScanResult>();
                                for (ScanResult scan : scanResults) {
                                    text += "[" + scan.level + " dBm] (" + scan.BSSID + ") " +
                                            scan.SSID;
                                    if (scan.SSID.equals(SILENCE_SSID) &&
                                            scan.level > threshold) {
                                        text += " <-- Silencing SSID!";
                                        if(scan.level > maxSilentSSIDLevel)
                                            maxSilentSSIDLevel = scan.level;
                                        if(behaviour.state == State.SEARCHING)
                                            silentScans.add(scan);
                                    } else if (scan.SSID.equals(SOUND_SSID) &&
                                            scan.level > maxSoundSSIDLevel) {
                                        text += " <-- Normal SSID";
                                        maxSoundSSIDLevel = scan.level;
                                    }
                                    text += "\n";
                                }
                                if(behaviour.state == State.SEARCHING)
                                    behaviour.addScan(silentScans);


                                if (behaviour.state == State.SILENCING && maxSilentSSIDLevel < maxSoundSSIDLevel)
                                    behaviour.updateState(ringerMode, actionMode, numBuzzBox, lenBuzzBox, lenSilentBox);

                                scanList.setText(text);
                            }
                        }
                    }
                });
            }
        };

        t = new Timer();
        t.scheduleAtFixedRate(timer, 0, 10000);
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
