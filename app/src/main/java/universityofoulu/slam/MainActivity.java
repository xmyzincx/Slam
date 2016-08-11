package universityofoulu.slam;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import universityofoulu.slam.PreferenceMenu.PrefActivity;
import universityofoulu.slam.PreferenceMenu.PrefFragment;
import utilities.Constants;
import utilities.Utilities;

public class MainActivity extends Activity implements AppCompatCallback {

    // Application context is associated with the application
    // and will always be same throughout the life of application
    // therefore it does not change.

    // Activity context is associated with to the activity
    // and can be destroyed if the activity is destroyed
    // There may be multiple activities (more than likely) with a single application.


    private static final String TAG = "MainActivity";
    public static final int REQUEST_ENABLE_BT = 1;
    private Context ActCtx;     // Activity Context
    private Context AppCtx;     // Application Context
    private ControllerService service;
    private Handler ServiceHandler;
    private AppCompatDelegate delegate;
    private Boolean streaming = false;
    private Boolean debug = Constants.debug;
    ToggleButton toggle_btn;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message RcvdMesg) {

            Bundle RcvdBundle = RcvdMesg.getData();
            String mtype = RcvdBundle.getString("to");
            if (mtype == null) return;	// Discard message if "mtype" tag not found in message

            if (mtype.equals("UI")){
                // TODO do something for UI
                if(RcvdBundle.getString("mesg").equals("DSB")){
                    streaming = true;
                    delegate.invalidateOptionsMenu();
                }
                if(RcvdBundle.getString("mesg").equals("Network unavailable")){
                    Toast.makeText(MainActivity.this, "Please connect to the network.", Toast.LENGTH_LONG).show();
                }
                if(RcvdBundle.getString("mesg").equals("Stream Ended")){
                    toggle_btn.setChecked(false);
                    MesgToCS("STOP");

                    // Changing the button status
                    toggle_btn.setBackgroundResource(R.drawable.start_btn);
                    Toast.makeText(MainActivity.this, "Stream time ended.", Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, RcvdBundle.getString("mesg"));
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = ((ControllerService.MyBinder) binder).getService();
            Log.d(TAG, "Service Connected");
            service.setHandler(handler);
            ServiceHandler = service.getUIhandler();

            MesgToCS("Hello from MainActivity");

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "Service disconnected");
            service = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);

        // call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);

        // use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_main);

        // add the Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            // Device does not support Bluetooth
            Toast.makeText(MainActivity.this, "Sorry, your device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(MainActivity.this, "Bluetooth enabled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // This method will be invoked whenever delegate.invalidateOptionsMenu() is called.
    public boolean onPrepareOptionsMenu(Menu menu){
        // This is not safe, This can throw nullpointerexception
        //TODO Make this NullPointerException proof
        if(streaming){
            MenuItem item = menu.findItem(R.id.preferences).setVisible(false);
        }
        return true;
    }
    //  && (delegate.findViewById(R.id.preferences)!=null)

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent(this, PrefActivity.class);
                intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, PrefFragment.class.getName() );
                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Activity Started");
        //Utilities.logToFile(AppCtx, TAG, "Activity started2");
    }

    @Override
    protected void onResume() {

        AppCtx = getApplicationContext();
        super.onResume();
        try {
            Log.d(TAG, "Activity Resumed");
            Intent service = new Intent(AppCtx, ControllerService.class);
            AppCtx.bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
            AppCtx.startService(service);
        } catch (Exception e) {
            Log.e(TAG, "Error Binding to service on activity resume");
            if (debug)
                Utilities.logToFile(AppCtx, TAG, e.toString());
            e.printStackTrace();
        }

        // To check if internet connection is available or not.
        // If yes, then enable the start button
        // for streaming otherwise disable the button.
        Boolean NetworkStatus = Utilities.isNetworkAvailable(this);
        toggle_btn = (ToggleButton) findViewById(R.id.toggle_btn);
        if (NetworkStatus){
            toggle_btn.setClickable(true);
            toggle_btn.setVisibility(View.VISIBLE);
            toggle_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (toggle_btn.isChecked()) {
                        // Controller initialization message to service
                        MesgToCS("INIT");

                        // Changing the button status
                        toggle_btn.setBackgroundResource(R.drawable.stop_btn);
                        Toast.makeText(MainActivity.this, "Please turn on your wristband.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Wristband disconnect message to service
                        MesgToCS("STOP");

                        // Changing the button status
                        toggle_btn.setBackgroundResource(R.drawable.start_btn);
                    }

                }
            });
        } else if (!NetworkStatus){
            toggle_btn.setClickable(false);
            toggle_btn.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, "Please connect to WiFi network.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unbindService(mServiceConnection);
        Log.d(TAG, "Activity Paused");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "Activity Stopped");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "Activity Destroyed");
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private void MesgToCS(String mesg){
        if (ServiceHandler!=null){
            Message SendMesg = ServiceHandler.obtainMessage();
            Bundle SendBundle = new Bundle();
            SendBundle.putString("to", "CS");
            SendBundle.putString("mesg", mesg);
            SendMesg.setData(SendBundle);
            ServiceHandler.sendMessage(SendMesg);
        }
    }
}
