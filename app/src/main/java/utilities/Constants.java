package utilities;

import android.os.Environment;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import universityofoulu.slam.R;

/**
 * Created by mYz on 08-Dec-15.
 */
public class Constants {

    public static String results_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/SLAM_test";
    public static final String EMPATICA_API_KEY = "f48eca6e62e34cc0be48217bd8d3d494";
    public static String DeviceID;
    public static final long StreamingTime = 600000; // Streaming time in miliseconds.
    public static final boolean debug = true;

    // Map for device pairing.
    // Each tablet will work only with the respective E4.
    public static final Map<String, String> validPairs =
            Collections.unmodifiableMap(
                    /* This is an anonymous
                       inner class - a sub-class of j.u.HashMap */
                    new HashMap<String, String>() {
                        {
                            //instance initializer.
                               //Tab BT MAC Address  //E4 BT MAC Address
                            put("D8:50:E6:89:A4:8E", "00:07:80:A7:BF:D4"); // 1JH
                            put("D8:50:E6:89:8F:FF", "00:07:80:A7:C0:0F"); // 1EV
                            put("D8:50:E6:89:90:01", "00:07:80:A7:BA:8D"); // 1JM
                            put("D8:50:E6:89:90:17", "00:07:80:A7:C0:07"); // 2CS
                            put("D8:50:E6:89:90:15", "00:07:80:A7:BF:F3"); // 2TL
                            put("D8:50:E6:89:90:25", "00:07:80:A7:BF:7F"); // 2AK
                            put("D8:50:E6:89:90:33", "00:07:80:A7:BB:C0"); //3NM
                            put("D8:50:E6:89:90:3B", "00:07:80:1F:8D:B5"); //3SS
                            put("D8:50:E6:89:90:1F", "00:07:80:A7:BB:C6"); //3NK
                            put("D8:50:E6:89:90:A5", "00:07:80:A7:BB:26"); //4AL
                            put("D8:50:E6:89:A4:D4", "00:07:80:A7:BF:63"); //4SO
                            put("D8:50:E6:89:8D:84", "00:07:80:A7:BF:73"); //4TJ
                        }
                    });



    public static void setDeviceID(String DID){
        DeviceID = DID;
    }
}
