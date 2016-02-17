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
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SDCardDetails extends AppCompatActivity implements TaskFragment.TaskCallbacks {

    private ShareActionProvider mShareActionProvider;
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
//        setContentView(R.layout.content_sdcard_details);
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
        avgFileSize.setText("- -");
        biggestFiles.setText("- -");
        mostFrequentFiles.setText("- -");
        enableOptionMenu(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        //enableOptionMenu(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        enableOptionMenu(false);
        return super.onPrepareOptionsMenu(menu);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void enableOptionMenu(boolean flag){
        MenuItem item= menu.findItem(R.id.menu_item_share);
        item.setEnabled(flag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
        }
        if(id == R.id.menu_item_share){
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }

        if (mShareActionProvider != null) {
            //mShareActionProvider.setShareIntent(shareIntent);
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
        avgFileSize.setText(receivedData[0] != null ? receivedData[0] : "No Data available");
        biggestFiles.setText(receivedData[1] != null ? receivedData[1] : "No Data available");
        mostFrequentFiles.setText(receivedData[2] != null ? receivedData[2] : "No Data available");
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        notificationManager.cancel(0);
                String shareContent = "Biggest Files:"+receivedData[1]+" Most Frequent File Extensions:"+receivedData[2]+" Average File Size:"+receivedData[0]+ " KB";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        ShareScanDetails(shareContent);
        enableOptionMenu(true);
    }

    private void ShareScanDetails(String shareContent){
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SDCard Media Scan Details");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
    }


    protected void startNotification() {
        notification = new NotificationCompat.Builder(SDCardDetails.this);
        notification.setContentTitle("Media Scan");
        notification.setContentText("Scanning sdcard for files");
        notification.setTicker("New Message Alert!");
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
