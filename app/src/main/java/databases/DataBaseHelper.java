package databases;

/**
 * Created by eduardo.benitez on 02/05/2018.
 * asidefocil@gmail.com
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "diecast.db";

    private static final int DB_SCHEME_VERSION = 2;

    private final Context myContext;
    private static DataBaseHelper mInstance;
    private static SQLiteDatabase myWritableDb;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_SCHEME_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBaseManager.CREATE_TABLE_SERIES);
        db.execSQL(DataBaseManager.CREATE_TABLE_BRANDS);
        db.execSQL(DataBaseManager.CREATE_TABLE_CARS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(DataBaseManagerUpdates.ALTER_TABLE_CARS_ADD_HASHTAG);
                db.execSQL(DataBaseManagerUpdates.ALTER_TABLE_CARS_ADD_PRICE);
                db.execSQL(DataBaseManagerUpdates.ALTER_TABLE_CARS_ADD_EXTRA);
                db.execSQL(DataBaseManagerUpdates.ALTER_TABLE_CARS_ADD_PURCHASE_DATE);
                //  case 2:
                //      db.execSQL(DataBaseManager.CREATE_TABLE_SERIES);
                //  case 3:
                //      db.execSQL(DataBaseManager.INSERT_INITIAL_VALUES_SERIES);
        }
    }

    /**
     * Get default instance of the class to keep it a singleton
     *
     * @param context
     *            the application context
     */
    public static DataBaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataBaseHelper(context);
        }
        return mInstance;
    }

    /**
     * Returns a writable database instance in order not to open and close many
     * SQLiteDatabase objects simultaneously
     *
     * @return a writable instance to SQLiteDatabase
     */
    public SQLiteDatabase getDataBase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }

        return myWritableDb;
    }

    @Override
    public void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }
}