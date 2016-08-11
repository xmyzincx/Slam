package universityofoulu.slam;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import utilities.Constants;
import utilities.JSONRequest;
import utilities.Utilities;
import utilities.VolleyRequestQueue;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by mYz on 10-Dec-15.
 */
public class ControllerService extends Service implements EmpaDataDelegate, EmpaStatusDelegate, Response.Listener,
        Response.ErrorListener {

    private static final String TAG = "ControllerService";
    private Context SrvcCtx;
    private Context AppCtx;
    private IBinder mBinder = new MyBinder();
    private EmpaDeviceManager DeviceManager;
    private Handler UIHandler = null;
    private Boolean debug = Constants.debug;


    //TODO For future work to send bulk data to server
/*    private Handler STHandler;
    private SenderThread SThread;
    private static final int BVPsf = 16;
    private static final int Accsf = 8;
    private static final int TEMPsf = 1;
    private static final int GSRsf = 1;*/

    private DatabaseHandler database;
    private static String DeviceID;

    public volatile BlockingQueue<org.json.JSONObject> DatabaseBuffer = null;

    //private  volatile JSONObject DataFrame = new JSONObject();
    private org.json.JSONObject jsonRequestData;

    private String ServerAddress = null;


    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message RcvdMesg) {

            Bundle RcvdBundle = RcvdMesg.getData();
            String mtype = RcvdBundle.getString("to");

            if (mtype == null)
                return;

            if (mtype.equals("CS")&& UIHandler != null) { /* Forward Message to UI */
                String mesg = RcvdBundle.getString("mesg");

                if (mesg.equals("Hello from MainActivity")) {
                    Log.d(TAG, RcvdBundle.getString("mesg"));
                    MesgToUI("Hi from ControllerService");
                }
                if (mesg.equals("INIT")){
                    ControllerInit();
                }
                if (mesg.equals("STOP")){
                    DisconnectWristband();
                }
            }
        }
    };

    public void setHandler(Handler h) {
        UIHandler = h;
    }

    public Handler getUIhandler(){
        return handler;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SrvcCtx = this;
        AppCtx = SrvcCtx.getApplicationContext();
        Log.d(TAG, "Service started");

        return Service.START_NOT_STICKY;
    }

    private void ControllerInit(){

        Boolean NetworkStatus = Utilities.isNetworkAvailable(SrvcCtx);
        if (NetworkStatus) {
            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            DeviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            DeviceManager.authenticateWithAPIKey(Constants.EMPATICA_API_KEY);

            // TODO Enable this when data from other sensors need to be sent to server
            /*SThread = new SenderThread(AppCtx, GSRBuffer);
            STHandler = SThread.getSTHandler();
            SThread.start();*/

            database = new DatabaseHandler(this);

            DatabaseBuffer = new LinkedBlockingQueue<org.json.JSONObject>(400);

            //Log.d(TAG, "Database buffer size: " + DatabaseBuffer.size());

            // Get the scheduler
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            // Get a handler, starting now, with a 1 second delay
            final ScheduledFuture DbHandle = scheduler.scheduleAtFixedRate(new DataCollectorTask(), 0, 1, SECONDS);

            Log.d(TAG, "Controller started");

        }
    }

    private void DisconnectWristband(){
        DeviceManager.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SThread.interrupt();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Log.d(TAG, "Timeout error: " + error.toString());
            if (debug)
                Utilities.logToFile(AppCtx, TAG, error.toString());
        } else if (error instanceof AuthFailureError) {
            Log.d(TAG, "Auth. error: " + error.toString());
            if (debug)
                Utilities.logToFile(AppCtx, TAG, error.toString());
        } else if (error instanceof ServerError) {
            Log.d(TAG, "Server error: " + error.toString());
            if (debug)
                Utilities.logToFile(AppCtx, TAG, error.toString());
        } else if (error instanceof NetworkError) {
            Log.d(TAG, "Network error: " + error.toString());
            if (debug)
                Utilities.logToFile(AppCtx, TAG, error.toString());
        } else if (error instanceof ParseError) {
            Log.d(TAG, "Parse error: " + error.toString());
            if (debug)
                Utilities.logToFile(AppCtx, TAG, error.toString());
            error.printStackTrace();
        }
    }


    @Override
    public void onResponse(Object response) {
        //Log.d(TAG, "Response: " + response.toString());
    }

    public class MyBinder extends Binder {
        ControllerService getService() {
            return ControllerService.this;
        }
    }

    //////// Start of Empatica interface inherited methods ///////////

    @Override
    public void didUpdateStatus(EmpaStatus Status) {

        Boolean NetworkStatus = Utilities.isNetworkAvailable(SrvcCtx);

        // The device manager is ready for use
        if (Status == EmpaStatus.READY && NetworkStatus) {

            Log.d(TAG, Status.name() + " - Turn on your device");
            // Start scanning
            DeviceManager.startScanning();

            // The device manager has established a connection
        } else if (Status == EmpaStatus.CONNECTED) {
            Log.d(TAG, "Empatica connected.");

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Disconnect device
                    DeviceManager.disconnect();
                    MesgToUI("Stream Ended");
                    Log.d(TAG, "Stream time complete.");
                }
            }, Constants.StreamingTime);

            // The device manager disconnected from a device
        } else if (Status == EmpaStatus.DISCONNECTED) {
            Log.d(TAG, "Something fishy !!");
            // Network is unavailable or not connected. Send message to UI to make TOAST.
        } else if (!NetworkStatus){
            MesgToUI("Network unavailable");
        }
    }

    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php

        String WBBTMacAdd = bluetoothDevice.getAddress();

        if (allowed && Utilities.isValidPair(WBBTMacAdd)) {

            // Stop scanning. The first allowed device will do.
            DeviceManager.stopScanning();
            try {
                // Connect to the device
                DeviceManager.connectDevice(bluetoothDevice);

                // Set device ID
                Constants.setDeviceID(deviceName);
                DeviceID = bluetoothDevice.getAddress().replaceAll(":", "");

                // Get server address from the preferences
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                ServerAddress = SP.getString("server_address", getString(R.string.default_server_addesss));

                // Disable setting button in toolbar
                MesgToUI("DSB");

            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Log.d(TAG, "Sorry, you can't connect to this device");
                if (debug)
                    Utilities.logToFile(AppCtx, TAG, e.getMessage());
            }
        }
    }


    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus SensorStatus, EmpaSensorType SensorType) {

    }

    @Override
    public void didRequestEnableBluetooth() {
        // Already done in the Main Activity
    }

    @Override
    public void didReceiveGSR(float gsr, double TimeStamp) {
        /*Galvanic Skin Response or ElectroDermal Activity (EDA)
         Updating frequency 4 Hz or 250 ms*/
        // vGSR.add(TimeStamp);
        // vGSR.add(gsr);

        try {
            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "EDA");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", gsr);

            // Adding to database buffer
            DatabaseBuffer.add(jsonRequestData);

            // Sending to server
            // Getting instance for singelton volley request queue
            VolleyRequestQueue RequestQueue = VolleyRequestQueue.getInstance(SrvcCtx);
            final JSONRequest jsonRequest = new JSONRequest(Request.Method
                    .POST, ServerAddress,
                    jsonRequestData, this, this);
            jsonRequest.setTag(TAG);
            RequestQueue.getRequestQueue().add(jsonRequest);

            jsonRequestData = null;

        } catch (JSONException e) {
            e.printStackTrace();
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.getMessage());

        }
    }

    @Override
    public void didReceiveBVP(float bvp, double TimeStamp) {
        // Blood Volume Pulse
        // Updating frequency 64 Hz or 15.625 ms

        try {
            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "BVP");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", bvp);

            // Adding to database buffer
            DatabaseBuffer.add(jsonRequestData);

            // TODO for future work
            // Sending to server
            // Getting instance for singelton volley request queue
/*            VolleyRequestQueue RequestQueue = VolleyRequestQueue.getInstance(SrvcCtx);
            final JSONRequest jsonRequest = new JSONRequest(Request.Method
                    .POST, ServerAddress,
                    jsonRequestData, this, this);
            jsonRequest.setTag(TAG);
            RequestQueue.getRequestQueue().add(jsonRequest);

            jsonRequestData = null;*/

        } catch (JSONException e) {
            e.printStackTrace();
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.getMessage());

        }
    }

    @Override
    public void didReceiveIBI(float ibi, double TimeStamp) {
        // Inter Beat Interval.
        // No specific frequency

        try {
            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "IBI");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", ibi);

            // Adding to database buffer
            DatabaseBuffer.add(jsonRequestData);

            // TODO for future work
            // Sending to server
            // Getting instance for singelton volley request queue
/*            VolleyRequestQueue RequestQueue = VolleyRequestQueue.getInstance(SrvcCtx);
            final JSONRequest jsonRequest = new JSONRequest(Request.Method
                    .POST, ServerAddress,
                    jsonRequestData, this, this);
            jsonRequest.setTag(TAG);
            RequestQueue.getRequestQueue().add(jsonRequest);

            jsonRequestData = null;*/

        } catch (JSONException e) {
            e.printStackTrace();
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.getMessage());

        }
    }

    @Override
    public void didReceiveTemperature(float temperature, double TimeStamp) {
        // Updating frequency 4 Hz or 250 ms

        try {
            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "temperature");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", temperature);

            // Adding to database
            DatabaseBuffer.add(jsonRequestData);

            // TODO for future work
            // Sending to server
            // Getting instance for singelton volley request queue
/*            VolleyRequestQueue RequestQueue = VolleyRequestQueue.getInstance(SrvcCtx);
            final JSONRequest jsonRequest = new JSONRequest(Request.Method
                    .POST, ServerAddress,
                    jsonRequestData, this, this);
            jsonRequest.setTag(TAG);
            RequestQueue.getRequestQueue().add(jsonRequest);

            jsonRequestData = null;*/

        } catch (JSONException e) {
            e.printStackTrace();
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.getMessage());

        }
    }

    @Override
    public void didReceiveAcceleration(int xAxis, int yAxis, int zAxis, double TimeStamp) {
        // Updating frequency 32 Hz or 31.25 ms

        // TODO To be decided how to combine 3 axes into one value
        try {
            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "Acc_x");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", (double)xAxis);

            // Adding to database
            DatabaseBuffer.add(jsonRequestData);

            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "Acc_y");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", (double)yAxis);

            // Adding to database
            DatabaseBuffer.add(jsonRequestData);

            // Creating JSON object for sensor data
            jsonRequestData = new org.json.JSONObject();
            jsonRequestData.put("wristband_id", DeviceID);
            jsonRequestData.put("signal", "Acc_z");
            jsonRequestData.put("timestamp", TimeStamp);
            jsonRequestData.put("value", (double)zAxis);

            // Adding to database
            DatabaseBuffer.add(jsonRequestData);

            // TODO for future work
            /*// Sending to server
            // Getting instance for singelton volley request queue
            VolleyRequestQueue RequestQueue = VolleyRequestQueue.getInstance(SrvcCtx);
            final JSONRequest jsonRequest = new JSONRequest(Request.Method
                    .POST, ServerAddress,
                    jsonRequestData, this, this);
            jsonRequest.setTag(TAG);
            RequestQueue.getRequestQueue().add(jsonRequest);*/

        } catch (JSONException e) {
            e.printStackTrace();
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.getMessage());

        }
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double TimeStamp) {
        //String batteryPercentage = String.format("%.0f %%", battery * 100);
    }

    //////// End of Empatica interface inherited methods ///////////

    /*// This data collector thread is to bundle up the sensor data and send to to server as well as storing in the SQLite DB.
    Currently, it only supports to store the data into the DB while only EDA is sent to the server in its respective method.
    TODO Modify this to more general method i.e. collect all the data, store all the data in DB and send only specific data
    to the server
     */
    public class DataCollectorTask implements Runnable {

        List<org.json.JSONObject> DataToProcess = new LinkedList<org.json.JSONObject>();

        @Override
        public void run() {

            if (DatabaseBuffer.size() != 0 && DatabaseBuffer != null) {
                try{
                    DatabaseBuffer.drainTo(DataToProcess);
                    database.processData(DataToProcess);
                    DataToProcess.clear();
                }catch (Exception e){
                    Log.d(TAG, "Exception occurred at Databasebuffer" + e);
                    if (debug)
                        Utilities.logToFile(AppCtx, TAG, e.getMessage());

                }
            }
        }
    }

    private void MesgToUI(String mesg) {
        if (UIHandler != null) {
            Message SendMesg = UIHandler.obtainMessage();
            Bundle SendBundle = new Bundle();
            SendBundle.putString("to", "UI");
            SendBundle.putString("mesg", mesg);
            SendMesg.setData(SendBundle);
            UIHandler.sendMessage(SendMesg);
        }
    }
}

