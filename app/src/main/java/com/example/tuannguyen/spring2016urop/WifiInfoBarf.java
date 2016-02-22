package com.example.tuannguyen.spring2016urop;

import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class WifiInfoBarf extends AppCompatActivity implements ListAdapter{

    int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info_barf);
        if (findViewById(R.id.fragment_containter) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment scanList = new ScanList();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_containter, scanList, "scan_list").commit();
        }
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

    public void onPause() {
        super.onPause();
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();

        ListFragment scanList = (ListFragment) getSupportFragmentManager()
                .findFragmentByTag("scan_list");

        for(ScanResult scan: scanResults) {

        }


    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /*
    public void wifiScan(View view) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        List<ScanResult> scanResult = wifiManager.getScanResults();
    }
    */
}
