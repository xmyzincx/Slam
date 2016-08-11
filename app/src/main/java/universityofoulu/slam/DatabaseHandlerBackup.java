package universityofoulu.slam;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mYz on 25-Dec-15.
 */
public class DatabaseHandlerBackup extends SQLiteOpenHelper{

    private static final String TAG = "DatabaseHandler";

    // All Static variables
    // Database Version
    private static final int DatabaseVersion = 1;

    // Database Name
    private static final String DatabaseName = "EmpaSensorDatabase";

    // Contacts table name
    private static final String Sensors_Data = "SensorsData";

    private static double lastRowTimeStamp=0;

    private final static double EPSILON =  0.0001;

    // Table Columns names
    private static final String KEY_ts = "TimeStamp";
    private static final String KEY_bvp = "BVP";
    private static final String KEY_accX = "Acc_X";
    private static final String KEY_accY = "Acc_Y";
    private static final String KEY_accZ = "Acc_Z";
    private static final String KEY_gsr = "GSR";
    private static final String KEY_tempr = "Temperature";
    private static final String KEY_ibi = "IBI";

    // Database creation sql statement
    private static final String Database_Create = "CREATE TABLE " + Sensors_Data + "("
            + KEY_ts + " REAL , "
            + KEY_bvp + " REAL , "
            + KEY_accX + " INTEGER , "
            + KEY_accY + " INTEGER , "
            + KEY_accZ + " INTEGER , "
            + KEY_gsr + " REAL , "
            + KEY_tempr + " REAL , "
            + KEY_ibi + " REAL , "
            + "PRIMARY KEY ( " + KEY_ts + " )"
            + ");";

    public DatabaseHandlerBackup(Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
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
        database.execSQL("DROP TABLE IF EXISTS " + Sensors_Data);

        // Create tables again
        onCreate(database);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding GSR
    void addGSR(float gsr, double TimeStamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        if (Math.abs(TimeStamp - lastRowTimeStamp) > EPSILON || TimeStamp == 0) {
            ContentValues values = new ContentValues();
            values.put(KEY_ts, TimeStamp);
            values.put(KEY_bvp, "");
            values.put(KEY_accX, "");
            values.put(KEY_accY, "");
            values.put(KEY_accZ, "");
            values.put(KEY_gsr, gsr);
            values.put(KEY_tempr, "");
            values.put(KEY_ibi, "");

            // Inserting Row
            database.insert(Sensors_Data, null, values);
            database.close(); // Closing database connection

            lastRowTimeStamp = TimeStamp;
        }
        else if (Math.abs(TimeStamp - lastRowTimeStamp) < EPSILON) {
            ContentValues values = new ContentValues();
            values.put(KEY_gsr, gsr);
            database.update(Sensors_Data, values, "KEY_ts = (SELECT max(KEY_ts) FROM Sensors_Data)", null);
        }
    }

    void addBVP(float bvp, double TimeStamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ts, TimeStamp);
        values.put(KEY_bvp, bvp);
        values.put(KEY_accX, "");
        values.put(KEY_accY, "");
        values.put(KEY_accZ, "");
        values.put(KEY_gsr, "");
        values.put(KEY_tempr, "");
        values.put(KEY_ibi, "");

        // Inserting Row
        database.insert(Sensors_Data, null, values);
        database.close(); // Closing database connection
    }

    void addIBI(float ibi, double TimeStamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ts, TimeStamp);
        values.put(KEY_bvp, "");
        values.put(KEY_accX, "");
        values.put(KEY_accY, "");
        values.put(KEY_accZ, "");
        values.put(KEY_gsr, "");
        values.put(KEY_tempr, "");
        values.put(KEY_ibi, ibi);

        // Inserting Row
        database.insert(Sensors_Data, null, values);
        database.close(); // Closing database connection
    }

    void addTemperature(float temperature, double TimeStamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ts, TimeStamp);
        values.put(KEY_bvp, "");
        values.put(KEY_accX, "");
        values.put(KEY_accY, "");
        values.put(KEY_accZ, "");
        values.put(KEY_gsr, "");
        values.put(KEY_tempr, temperature);
        values.put(KEY_ibi, "");

        // Inserting Row
        database.insert(Sensors_Data, null, values);
        database.close(); // Closing database connection
    }

    void addAcc(int xAxis, int yAxis, int zAxis, double TimeStamp) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ts, TimeStamp);
        values.put(KEY_bvp, "");
        values.put(KEY_accX, xAxis);
        values.put(KEY_accY, yAxis);
        values.put(KEY_accZ, zAxis);
        values.put(KEY_gsr, "");
        values.put(KEY_tempr, "");
        values.put(KEY_ibi, "");

        // Inserting Row
        database.insert(Sensors_Data, null, values);
        database.close(); // Closing database connection
    }


}
