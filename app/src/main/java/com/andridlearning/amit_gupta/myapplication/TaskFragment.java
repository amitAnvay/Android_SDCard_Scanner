package com.andridlearning.amit_gupta.myapplication;

/**
 * Created by Amit_Gupta on 9/22/15.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class TaskFragment extends Fragment {

    private long numFiles;
    private long totalSize;
    private String TAG = SDCardDetails.class.getSimpleName();
    private List<SDFile> myList;
    private HashMap<String, Integer> fileExtension;

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(String[] returnData);
    }

    private TaskCallbacks mCallbacks;
    private MediaScanTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        if(c instanceof TaskCallbacks)
        mCallbacks = (TaskCallbacks) c;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof TaskCallbacks)
            mCallbacks = (TaskCallbacks) activity;
    }


    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        //executeBackgroundTask();
    }

    public void executeBackgroundTask(){
        // Create and execute the background task.
        mTask = new MediaScanTask();
        mTask.execute();
    }

    public void cancelTask(){
        if(mTask != null){
            mTask.cancel(true);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class MediaScanTask extends AsyncTask<Void, Integer, Void> {

        private String[] returnStringData = new String[3];
        private int progress;

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {

            numFiles = 0;
            totalSize = 0;
            fileExtension = new HashMap<String, Integer>();
            myList = new ArrayList<SDFile>();
            final String state = Environment.getExternalStorageState();
            publishProgress(++progress);

            if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
                getAllFilesOfDir(Environment.getExternalStorageDirectory());
            }
            if(isCancelled()){
                publishProgress(0);
            }else {
                publishProgress(++progress);
            }
            double average_size = totalSize/(1024 * numFiles);

            String mostFrequentFileExtensions = Util.getMostFrequentFileExtenstions((TreeMap) Util.sortByValue(fileExtension));

            if(isCancelled()){
                publishProgress(0);
            }else {
                publishProgress(++progress);
            }
            StringBuilder tenBiggestFiles = new StringBuilder();

            for(int i = 0; i< myList.size() && i < 10; i++){
                tenBiggestFiles.append(myList.get(i).name+"\n");
            }
            returnStringData[0] = "" + average_size+ " KB";
            returnStringData[1] = tenBiggestFiles.toString();
            returnStringData[2] = mostFrequentFileExtensions;

            if(isCancelled()){
                publishProgress(0);
            }else {
                publishProgress(10000);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(returnStringData);
            }
        }

        private void getAllFilesOfDir(File directory) {
            Log.d(TAG, "Directory: " + directory.getAbsolutePath() + "\n");

            final File[] files = directory.listFiles();

            if ( files != null ) {
                for ( File file : files ) {
                    if(isCancelled()){
                        progress = 0;
                        break;
                    }
                    if ( file != null ) {
                        if ( file.isDirectory() ) {  // it is a folder...
                            getAllFilesOfDir(file);
                        }
                        else {  // it is a file...
                            numFiles++;
                            totalSize += file.length();

                            SDFile sDFile = new SDFile();
                            sDFile.name =file.getName();
                            int dotposition= sDFile.name.lastIndexOf(".");
                            if(dotposition != -1) {
                                String filename_Without_Ext = sDFile.name.substring(0, dotposition);
                                String file_Extension = sDFile.name.substring(dotposition + 1, sDFile.name.length());

                                Integer count = fileExtension.get(file_Extension);
                                if (count == null) {
                                    fileExtension.put(file_Extension, 1);
                                } else {
                                    fileExtension.put(file_Extension, count + 1);
                                }
                                sDFile.extension = sDFile.name.substring(dotposition + 1, sDFile.name.length());
                            }else {
                                System.out.println(" Index = -1");
                            }
                            sDFile.size= file.length();
                            performOrderedInsertion(sDFile);

                            if(isCancelled()){
                                publishProgress(0);
                                break;
                            }else {
                                publishProgress(++progress);
                            }
                            Log.d(TAG, "File: " + file.getAbsolutePath() + "\n");

                        }
                    }
                }
            }
        }

        private void performOrderedInsertion(SDFile sDFile){
            int size = myList.size();
            int lastIndx = myList.size() - 1;
            if(lastIndx != -1){
                while (lastIndx >= 0 && myList.get(lastIndx).size < sDFile.size){
                    lastIndx --;
                }
                myList.add(lastIndx+ 1, sDFile);

            }else {
                myList.add(0, sDFile);
            }
        }

    }

}
