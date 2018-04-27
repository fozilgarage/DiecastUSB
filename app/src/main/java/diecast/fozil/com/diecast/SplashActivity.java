package diecast.fozil.com.diecast;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import databases.DataBaseManager;
import databases.DataBaseManagerUpdates;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);
    }

    /**
     * Created by eduardo.benitez on 16/10/2017.
     */

    public static class DataBaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "diecast.db";

        private static final int DB_SCHEME_VERSION = 2;

        public DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_SCHEME_VERSION);
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
    }
}
