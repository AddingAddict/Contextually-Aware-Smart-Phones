package com.example.tuannguyen.spring2016urop;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiInfoBarf extends AppCompatActivity{

    int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 1;

    WifiManager wifiManager;
    List<ScanResult> scanResults;

    Timer t;
    TimerTask timer;

    Activity wifiInfoBarf;
    TextView expo;
    TextView scanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info_barf);

        wifiInfoBarf = this;

        expo = (TextView) this.findViewById(R.id.expo);

        scanList = (TextView) this.findViewById(R.id.scan_list);
        scanList.setMovementMethod(new ScrollingMovementMethod());

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        timer = new TimerTask() {
            @Override
            public void run() {
                wifiInfoBarf.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wifiManager.startScan();
                        scanResults = wifiManager.getScanResults();

                        String text = "";
                        for (ScanResult scan : scanResults) {
                            text += "[" + scan.level + " dBm] (" + scan.BSSID + ") " +
                                    scan.SSID + "\n";
                        }
                        scanList.setText(text);
                    }
                });
            }
        };

        t = new Timer();
        t.scheduleAtFixedRate(timer, 0, 60000);
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
