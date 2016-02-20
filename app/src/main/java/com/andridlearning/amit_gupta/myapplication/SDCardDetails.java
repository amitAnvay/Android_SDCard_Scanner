package com.andridlearning.amit_gupta.myapplication;

import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/***
 *
 * Created by Amit_Gupta on 2/14/16.
 *
 */

public class SDCardDetails extends AppCompatActivity implements TaskFragment.TaskCallbacks {

    private String TAG = SDCardDetails.class.getSimpleName();
    private Intent shareIntent=new Intent(Intent.ACTION_SEND);
    private TextView avgFileSize;
    private TextView biggestFiles;
    private TextView mostFrequentFiles;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private TaskFragment mTaskFragment;
    private ProgressBar progressBar;
    private Button startScanButton;
    private Button stopScanButton;
    private NotificationCompat.Builder notification;
    private NotificationManager notificationManager;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdcard_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        startScanButton = (Button)findViewById(R.id.startScan);
        stopScanButton = (Button)findViewById(R.id.stopScan);
        stopScanButton.setEnabled(false);


        FragmentManager fm = getFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        avgFileSize = (TextView)findViewById(R.id.avgFileSize);
        biggestFiles = (TextView)findViewById(R.id.biggestFiles);
        mostFrequentFiles = (TextView)findViewById(R.id.mostFrequentFiles);
        notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public void stopScan(View view){
        cancelScan();
    }

    public void startScan(View view){
        clearResults();
        startScanButton.setEnabled(false);
        mTaskFragment.executeBackgroundTask();
        stopScanButton.setEnabled(true);
        startNotification();
    }

    private void cancelScan(){
        mTaskFragment.cancelTask();
        notificationManager.cancel(0);
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        progressBar.setProgress(0);
        clearResults();
    }

    private void clearResults(){
        avgFileSize.setText(R.string.empty_string);
        biggestFiles.setText(R.string.empty_string);
        mostFrequentFiles.setText(R.string.empty_string);
        enableOptionMenu(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        enableOptionMenu(false);
        return super.onPrepareOptionsMenu(menu);
    }

    private void enableOptionMenu(boolean flag){
        MenuItem item= menu.findItem(R.id.menu_item_share);
        item.setEnabled(flag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
        }
        if(id == R.id.menu_item_share){
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share_via)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onProgressUpdate(int percent) {
        progressBar.setProgress(percent / 100);
    }

    @Override
    public void onCancelled() {
        progressBar.setProgress(0);
    }

    @Override
    public void onPostExecute(String[] receivedData) {
        avgFileSize.setText(receivedData[0] != null ? receivedData[0] : getString(R.string.no_data_available));
        biggestFiles.setText(receivedData[1] != null ? receivedData[1] : getString(R.string.no_data_available));
        mostFrequentFiles.setText(receivedData[2] != null ? receivedData[2] : getString(R.string.no_data_available));
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        notificationManager.cancel(0);
                String shareContent = getString(R.string.biggest_files)+receivedData[1]+getString(R.string.most_frequent_file_extensions)+receivedData[2]+getString(R.string.average_file_size)+receivedData[0]+ getString(R.string.kb);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        ShareScanDetails(shareContent);
        enableOptionMenu(true);
    }

    private void ShareScanDetails(String shareContent){
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sdcard_media_scan_details));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
    }


    protected void startNotification() {
        notification = new NotificationCompat.Builder(SDCardDetails.this);
        notification.setContentTitle(getString(R.string.media_scan));
        notification.setContentText(getString(R.string.scanning_sdcard_for_files));
        notification.setTicker(getString(R.string.new_message_alert));
        notification.setSmallIcon(R.drawable.scan);
        notificationManager.notify(0, notification.build());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancelScan();
        }
        return super.onKeyDown(keyCode, event);
    }
}
