package com.example.tuannguyen.casp.util;

/**
 * Created by tuannguyen on 4/26/16.
 */
import android.app.NotificationManager;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Tuan Nguyen
 * Handles the app's state transitions and behaviour
 */
public class Behaviour {
    /**
     * The various states the app can be in
     * <p>
     *     SEARCHING = the app is looking for a positive wifi source that has a near constant power reading over the time span of one minute
     *     WAITING = the app is resting, and is not scanning for wifi sources
     *     SILENCING = the app is currently in a silent zone, and is waiting to exit the silent zone
     * </p>
     */
    public enum State {
        SEARCHING, WAITING, SILENCING
    }

    private static final int NOTIFICATION_ID = 001;

    /**
     * Current state
     */
    State state;

    /**
     * Counter for keeping track how long the app spends in each state
     */
    int counter;

    /**
     * Map between MAC Addresses and lists of associated signal readings
     */
    HashMap<String, List<Integer>> recentScans;

    /**
     * Builder for "entering silent zone" notification
     */
    NotificationCompat.Builder nBuilderSilent;

    /**
     * Builder for "exiting silent zone" notification
     */
    NotificationCompat.Builder nBuilderNormal;

    /**
     * Manages sending notifications
     */
    NotificationManager nManager;

    /**
     * Object that reads the vibration patter the user selects
     */
    VibrationNotification vNotifier;

    /**
     * Manages changing audio settings
     */
    AudioManager audioManager;

    /**
     * Constructor for Behaviour class
     */
    public Behaviour(NotificationCompat.Builder silent, NotificationCompat.Builder normal,
                     NotificationManager nm, VibrationNotification vn, AudioManager am) {
        this.state = State.SEARCHING;
        this.counter = 0;
        recentScans = new HashMap<String, List<Integer>>();

        nBuilderSilent = silent;
        nBuilderNormal = normal;
        nManager = nm;
        vNotifier = vn;
        audioManager = am;
    }

    /**
     * Returns the app's current state
     * @return State enum
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the current state
     * @param newState The new state
     */
    public void setState(State newState) {
        this.state = newState;
    }

    /**
     * Adds a list of scan results with the silent SSID above the threshold
     * <p>
     *     Required for the SEARCHING state, so that it can calculate whether or not the user is moving through
     *     silent SSID's or sitting in one place
     * </p>
     * @param newScanResult A list of scan results with the silent SSID above the threshold
     */
    public void addScan(List<ScanResult> newScanResult) {
        for(ScanResult scan : newScanResult) {
            String mac = scan.BSSID;

            if(!recentScans.containsKey(scan))
                recentScans.put(mac, new ArrayList<Integer>());

            recentScans.get(mac).add(scan.level);
        }
    }

    /**
     * Handles the logic for switching states
     * <p>
     *     Specifically, if it is within one of the timed states (WAITING or SEARCHING), it will increment the
     *     counter. Also handles the logic for determining if the user is moving or sitting still.
     *
     *     Handles pushing the notification/automatically changing the volume settings, based on the given ringer mode
     *     and action mode
     * </p>
     * @param rm The app's current ringer mode
     * @param am The app's current ringer mode
     */
    public void updateState(RingerMode rm, ActionMode am) {
        switch (this.state) {
            case SEARCHING:
                this.counter++;
                if(this.counter >= 6) {
                    boolean setSilent = false;

                    // parses through list of recorded signal levels for each MAC Address. Will only put in
                    // silencing mode if it calculates that the user is sitting still
                    for(String BSSID : recentScans.keySet()) {
                        List<Integer> levels = recentScans.get(BSSID);

                        // The app must see that it makes at least 3 good readings. If so, then it sees if the
                        // recorded levels are within a certain range of each other
                        if(levels.size() > 2) {
                            Collections.sort(levels);
                            int range = levels.get(levels.size()-1) - levels.get(0);
                            if(range < 10)
                                setSilent = true;
                        }
                    }
                    recentScans.clear();
                    if(setSilent) {
                        setState(State.SILENCING);
                        switch (am.getMode()) {
                            // If the app is allowed to automatically change the setting
                            case AUTO:
                                switch (rm.getMode()) {
                                    case SILENCE:
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
                            case NOTIFY:
                                vNotifier.setVibrationPattern(nBuilderSilent);
                                nManager.notify(NOTIFICATION_ID,
                                        nBuilderSilent.build());
                                break;

                            default:
                                throw new IllegalStateException();
                        }
                    } else
                        setState(State.WAITING);
                    this.counter = 0;
                }
                break;

            case SILENCING:
                setState(State.WAITING);
                switch (am.getMode()) {
                    case AUTO:
                        audioManager.setRingerMode(
                                AudioManager.RINGER_MODE_NORMAL);
                        break;

                    case NOTIFY:
                        vNotifier.setVibrationPattern(nBuilderNormal);
                        if (audioManager.getRingerMode()
                                != AudioManager.RINGER_MODE_NORMAL) {
                            nManager.notify(NOTIFICATION_ID,
                                    nBuilderNormal.build());
                        }
                        break;

                    default:
                        throw new IllegalStateException();
                }

            case WAITING:
                this.counter++;
                if(this.counter >= 18) {
                    setState(State.SEARCHING);
                    this.counter = 0;
                }
                break;

            default:
                throw new IllegalStateException();
        }
    }
}
