package universityofoulu.slam;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by mYz on 15-Dec-15.
 */
public class DatabaseThread extends Thread {

    private Context appCtx;
    private static final String TAG = "DatabaseThread";

    DatabaseThread(Context tempCtx){
        appCtx = tempCtx;
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


    }
}
