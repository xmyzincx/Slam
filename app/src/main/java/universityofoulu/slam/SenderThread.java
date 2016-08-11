package universityofoulu.slam;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.simple.JSONArray;

import java.util.concurrent.LinkedBlockingQueue;

import utilities.Utilities;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by mYz on 15-Dec-15.
 */
public class SenderThread extends Thread implements Response.Listener,
        Response.ErrorListener {

    private Context appCtx;
    private static final String TAG = "SenderThread";
    private static String ServerAddress = "http://10.20.44.229:12234";
    private static int ServerPort    = 36964;
    boolean NetworkCheck;
    private String payload;
    private JSONArray JsonArrayData = new JSONArray();
    private org.json.JSONObject JSONObjectData = new org.json.JSONObject();

    private LinkedBlockingQueue<JSONArray> PacketBuffer;

    SenderThread(Context tempCtx, LinkedBlockingQueue<JSONArray> tempBuffer){
        this.appCtx = tempCtx;
        this.PacketBuffer = tempBuffer;
    }

    Handler CSHandler = new Handler() {

        @Override
        public void handleMessage(Message RcvdMesg) {
            Bundle RcvdBundle = RcvdMesg.getData();
            String mtype = RcvdBundle.getString("to");

            if (mtype == null)
                return;

            if (mtype.equals("ST")) {
                //TODO Send the message via handler
            }
        }
    };



    public Handler getSTHandler(){
        return CSHandler;
    }

    public void run() {

        NetworkCheck = Utilities.isNetworkAvailable(appCtx);
        if(NetworkCheck=true){
            while(true) {
                try {
                    JsonArrayData = PacketBuffer.poll(10, SECONDS);
                    //TODO for future work

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "on error response" + error.getClass().getName());
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
        } else if (error instanceof AuthFailureError) {
            //TODO
        } else if (error instanceof ServerError) {
            //TODO
        } else if (error instanceof NetworkError) {
            //TODO
        } else if (error instanceof ParseError) {
            //TODO
        }
    }

    @Override
    public void onResponse(Object response) {
        Log.d(TAG, "on response");
    }
}
