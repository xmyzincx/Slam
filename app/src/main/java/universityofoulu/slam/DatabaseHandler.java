package universityofoulu.slam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import utilities.Constants;
import utilities.Utilities;

/**
 * Created by mYz on 25-Dec-15.
 */
public class DatabaseHandler extends SQLiteOpenHelper{



    private static final String TAG = "DatabaseHandler";

    // All Static variables
    // Database Version
    private static final int DatabaseVersion = 2;

    // Database Name
    private static final String DatabaseName = "SLAM";

    // Contacts table name
    private static final String Table_Sensors_Data = "SensorsData";

    // Table Columns names
    private static final String KEY_id = "id";
    private static final String KEY_wristband_id = "wristband_id";
    private static final String KEY_signal = "signal";
    private static final String KEY_timestamp = "timestamp";
    private static final String KEY_value = "value";
    private static final String KEY_created = "created";

    private Context SrvcCtx;

    private Boolean debug = Constants.debug;


    // Database creation sql statement
    private static final String Database_Create = "CREATE TABLE " + Table_Sensors_Data + "("
            + KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + KEY_wristband_id + " CHAR(16) , "
            + KEY_signal + " CHAR(16) , "
            + KEY_timestamp + " REAL , "
            + KEY_value + " REAL , "
            + KEY_created + " REAL "
            + ");";

    public DatabaseHandler(Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
        SrvcCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Creating Database and Tables
        Log.d(TAG, Database_Create);

        database.execSQL(Database_Create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop older table if existed
        database.execSQL("DROP TABLE IF EXISTS " + Table_Sensors_Data);

        // Create tables again
        onCreate(database);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding data in the database
    void processData(List<JSONObject> DataToProcess){

        // you can use INSERT only
        String sql = "INSERT INTO " + Table_Sensors_Data + " ( "
                + KEY_wristband_id  + ", "
                + KEY_signal + ", "
                + KEY_timestamp  + ", "
                + KEY_value + ", "
                + KEY_created +
                " ) VALUES ( ?, ?, ?, ?, ? )";

        SQLiteDatabase db = this.getWritableDatabase();

        /*
         * According to the docs http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
         * Writers should use beginTransactionNonExclusive() or beginTransactionWithListenerNonExclusive(SQLiteTransactionListener)
         * to start a transaction. Non-exclusive mode allows database file to be in readable by other threads executing queries.
         */
        db.beginTransaction();

        SQLiteStatement statement = db.compileStatement(sql);

        for(JSONObject tempJSON : DataToProcess){
            try {
                statement.bindString(1, (String) tempJSON.get(KEY_wristband_id));
                statement.bindString(2, (String)tempJSON.get(KEY_signal));
                statement.bindDouble(3, (Double) tempJSON.get(KEY_timestamp));
                statement.bindDouble(4, (Double) tempJSON.get(KEY_value));
                statement.bindLong(5, Calendar.getInstance().getTimeInMillis());
            } catch (JSONException e) {
                e.printStackTrace();
                if (debug)
                    Utilities.logToFile(SrvcCtx.getApplicationContext(), TAG, e.toString());

            }
            statement.execute();
            statement.clearBindings();
        }
        DataToProcess.clear();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
