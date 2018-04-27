package databases;

import android.content.Context;

/**
 * Created by eduardo.benitez on 24/04/2018.
 */

public class DataBaseManagerUpdates extends  DataBaseManager{

    public DataBaseManagerUpdates(Context context) {
        super(context);
    }

    /********************************************
    /*   Versión 2.0
    /*
    /******************************************/

    public static final String ALTER_TABLE_CARS_ADD_HASHTAG = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_CARS_HASHTAG + " text;";

    public static final String ALTER_TABLE_CARS_ADD_PRICE = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_CARS_PRICE + " integer;";

    public static final String ALTER_TABLE_CARS_ADD_EXTRA = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_EXTRA + " text;";

    public static final String ALTER_TABLE_CARS_ADD_PURCHASE_DATE = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_CARS_PURCHASE_DATE + " datetime;";
}
