package com.example.tuannguyen.spring2016urop;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    //int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

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

        /*
        // Asks for permission if not already given
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
        */

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

                        if(scanResults == null){
                            scanList.setText(R.string.null_scan);
                        }else{
                            String text = "";
                            for(ScanResult scan : scanResults) {
                                text += "[" + scan.level + " dBm] (" + scan.BSSID + ") " +
                                        scan.SSID + "\n";
                            }
                            scanList.setText(text);
                        }
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
