package databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import diecast.fozil.com.diecast.SplashActivity;

/**
 * Created by eduardo.benitez on 16/10/2017.
 */

public class DataBaseManager {

    public static final String TABLE_NAME_BRANDS = "brands";
    public static final String TABLE_NAME_CARS = "cars";
    public static final String TABLE_NAME_SERIES = "series";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_CREATED_AT = "created_at";

    public static final String KEY_ID_BRAND = "id_brand";
    public static final String KEY_ID_SERIE = "id_serie";
    public static final String KEY_CARS_IMAGE = "image";
    public static final String KEY_CARS_FAVORITE = "is_favorite";
    public static final String KEY_CARS_COUNT = "count";

    public static final String KEY_BRAND_EXTRA = "extra";

    public static final String KEY_SERIES_PARENT = "id_serie_parent";

    public static void main(final String[] args) {
        System.out.println(DataBaseManager.CREATE_TABLE_SERIES);
        System.out.println(DataBaseManager.CREATE_TABLE_BRANDS);
        System.out.println(DataBaseManager.CREATE_TABLE_CARS);
    }

    public static final String CREATE_TABLE_SERIES = "create table " + TABLE_NAME_SERIES + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_NAME + " text not null,"
            + KEY_ID_BRAND + " integer,"
            + KEY_SERIES_PARENT + " integer,"
            + KEY_CREATED_AT + " datetime not null default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
            + " FOREIGN KEY ("+ KEY_ID_BRAND +") REFERENCES "+TABLE_NAME_BRANDS+"("+KEY_ID+") on delete cascade);";


    public static final String CREATE_TABLE_BRANDS = "create table " + TABLE_NAME_BRANDS + " ("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_NAME + " text not null,"
            + KEY_BRAND_EXTRA + " text,"
            + KEY_CREATED_AT + " datetime not null default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')));";

    public static final String CREATE_TABLE_CARS = "create table " + TABLE_NAME_CARS + " ("
            + KEY_ID + " integer primary key autoincrement,"
            + KEY_NAME + " text not null,"
            + KEY_ID_BRAND + " integer,"
            + KEY_ID_SERIE + " integer,"
            + KEY_CARS_IMAGE + " text,"
            + KEY_CARS_FAVORITE + " integer default 0,"
            + KEY_CARS_COUNT + " integer default 1,"
            + KEY_CREATED_AT + " datetime default (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')),"
            + " FOREIGN KEY ("+ KEY_ID_BRAND +") REFERENCES "+TABLE_NAME_BRANDS+"("+KEY_ID+") on delete cascade,"
            + " FOREIGN KEY ("+ KEY_ID_SERIE +") REFERENCES "+TABLE_NAME_SERIES+"("+KEY_ID+") on delete cascade);";

    /*public static final String ALTER_TABLE_CARS_ADD_FAVORITE = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_CARS_FAVORITE + " integer default 0;";

    public static final String ALTER_TABLE_CARS_ADD_COUNT = "alter table " + TABLE_NAME_CARS +
            " add column " + KEY_CARS_COUNT + " integer default 1;";*/

    private SplashActivity.DataBaseHelper dataBaseHelper;
    private SQLiteDatabase db;

    public DataBaseManager(Context context) {
        dataBaseHelper = new SplashActivity.DataBaseHelper(context);
        db = dataBaseHelper.getWritableDatabase();

    }

    public void removeAll() {
        db.delete(TABLE_NAME_SERIES, null, null);
        db.delete(TABLE_NAME_BRANDS, null, null);
        db.delete(TABLE_NAME_CARS, null, null);
    }

    public ContentValues getCarValues(final Car car) {

        ContentValues values = getCarValuesWithoutId(car);
        values.put(KEY_ID, car.getId());

        return values;
    }

    /***********************************************/
    /*
    /*      Table Cars
    /*
    /**********************************************/

    public ContentValues getCarValuesWithoutId(final Car car) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, car.getName());
        values.put(KEY_ID_BRAND, car.getBrand() != null ? car.getBrand().getId() : null);
        values.put(KEY_ID_SERIE, car.getSerie() != null ? car.getSerie().getId() : null);
        values.put(KEY_CARS_IMAGE, car.getImage());
        values.put(KEY_CARS_FAVORITE, car.isFavorite());
        values.put(KEY_CARS_COUNT, car.getCount() > 0 ? car.getCount() : 1);

        return values;
    }

    private String[] getCarColumns() {
        return new String[]{KEY_ID, KEY_NAME, KEY_ID_BRAND, KEY_ID_SERIE, KEY_CARS_IMAGE,
                KEY_CREATED_AT, KEY_CARS_FAVORITE, KEY_CARS_COUNT};
    }

    public long insertCar(final Car car) {
        ContentValues values = getCarValuesWithoutId(car);
        return db.insert(TABLE_NAME_CARS, null, values);
    }

    public void deleteCar(int idCar) {
        db.delete(TABLE_NAME_CARS, KEY_ID + "=?", new String[]{idCar + ""});
    }

    public void updateCar(final Car car) {
        ContentValues values = getCarValues(car);
        db.update(TABLE_NAME_CARS, values, KEY_ID + "=?", new String[]{car.getId() + ""});
    }

    public List<Car> getListCars(final String carName, final int idBrand, final int idSerie,
                                 final int idSubserie, final boolean isDescOrder) {
        List<Car> carList = new ArrayList<Car>();
        String[] carColumns = getCarColumns();
        String whereSentence = "";
        String[] params = new String[]{};
        String order = "asc";
        if (isDescOrder)
            order = "desc";
        if (carName != null && !carName.equals("")) {
            String names[] = carName.split(" ");
            params = new String[names.length];
            int i = 0;
            for (String name : names) {
                if (!whereSentence.equals(""))
                    whereSentence += " or  ";
                whereSentence += KEY_NAME + " like ?";
                params[i++] ="%" + name + "%";
            }
            //whereSentence = KEY_NAME + " like ?";
            //params = new String[]{"%" + carName + "%"};
        }

        if (idBrand >= 0) {
            if (whereSentence != "") {
                whereSentence += "and ";
            }
            whereSentence += TABLE_NAME_CARS + "." + KEY_ID_BRAND + "=" + idBrand;
        }

        if (idSerie >= 0) {
            if (whereSentence != "") {
                whereSentence += " and ";
            }
            whereSentence += "(" + TABLE_NAME_CARS + "." + KEY_ID_SERIE + "=" + idSerie + " OR " +
                TABLE_NAME_SERIES + "." + KEY_SERIES_PARENT + "=" + idSerie + ")";
        }

        if (idSubserie >= 0) {
            if (whereSentence != "") {
                whereSentence += " and ";
            }
            whereSentence += TABLE_NAME_CARS + "." + KEY_ID_SERIE + "=" + idSubserie;
        }
        String query = "SELECT * FROM " + TABLE_NAME_CARS;
        if (idSerie >= 0)
            query += " LEFT JOIN " + TABLE_NAME_SERIES + " ON " + TABLE_NAME_CARS + "." + KEY_ID_SERIE + " = " + TABLE_NAME_SERIES + "." + KEY_ID;
        if (!whereSentence.equals(""))
            query += " where " + whereSentence;

        Cursor cursor = db.rawQuery(query, params);

        //Cursor cursor = db.query(TABLE_NAME_CARS, carColumns, whereSentence, params, null, null, KEY_ID + " " + order);

        if (cursor.moveToFirst()) {
            do {
                Car car = new Car();
                car.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))));
                car.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                car.setBrand(getBrandById(cursor.getInt(cursor.getColumnIndex(KEY_ID_BRAND))));
                car.setSerie(getSerieById(cursor.getInt(cursor.getColumnIndex(KEY_ID_SERIE))));
                car.setImage(cursor.getString(cursor.getColumnIndex(KEY_CARS_IMAGE)));
                car.setCreatedAt(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
                car.setCount(cursor.getInt(cursor.getColumnIndex(KEY_CARS_COUNT)));

                carList.add(car);
            } while (cursor.moveToNext());
        }

        return carList;
    }

    public List<Car> getListCars(final boolean isDescOrder) {
        return getListCars(null, -1, -1, -1, isDescOrder);
    }

    public List<Car> getListCars(final int idBrand, final int idSerie, final int idSubserie, final boolean isDescOrder) {
        return getListCars(null, idBrand, idSerie, idSubserie, isDescOrder);
    }

    public Car getCar(final int idCar) {
        String[] carColumns = getCarColumns();
        Cursor cursor = db.query(TABLE_NAME_CARS, carColumns, KEY_ID + " = ?", new String[]{idCar + ""}, null, null, null);
        Car car = new Car();
        try {
            if (cursor.moveToFirst()) {
                car.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))));
                car.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                String brand = cursor.getString(cursor.getColumnIndex(KEY_ID_BRAND));
                if (brand != null && !brand.equals(""))
                    car.setBrand(getBrandById(Integer.parseInt(brand)));
                String serie = cursor.getString(cursor.getColumnIndex(KEY_ID_SERIE));
                if (serie != null && !serie.equals(""))
                    car.setSerie(getSerieById(Integer.parseInt(serie)));
                car.setImage(cursor.getString(cursor.getColumnIndex(KEY_CARS_IMAGE)));
                car.setCreatedAt(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
                boolean isFavorite = false;
                if (cursor.getInt(cursor.getColumnIndex(KEY_CARS_FAVORITE)) == 1)
                    isFavorite = true;
                car.setFavorite(isFavorite);
                car.setCount(cursor.getInt(cursor.getColumnIndex(KEY_CARS_COUNT)));
            }
        }finally {
            cursor.close();
        }
        return car;
    }

    public void updateImageCar(final int idCar, final String imageUrl) {
        ContentValues values = new ContentValues();
        values.put(KEY_CARS_IMAGE, imageUrl);
        db.update(TABLE_NAME_CARS, values, KEY_ID + "=?", new String[]{idCar + ""});
    }

    /***********************************************/
    /*
    /*      Table Brands
    /*
    /**********************************************/

    public ContentValues getBrandValues(final Brand brand) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, brand.getName());
        values.put(KEY_BRAND_EXTRA, brand.getExtra());

        return values;
    }

    private String[] getBrandColumns() {
        return new String[]{KEY_ID, KEY_NAME, KEY_BRAND_EXTRA, KEY_CREATED_AT};
    }

    public void insertBrand(final Brand brand) {
        ContentValues values = getBrandValues(brand);
        db.insert(TABLE_NAME_BRANDS, null, values);
    }

    private Cursor getBrandsCursor() {
        String[] brandColumns = getBrandColumns();
        return db.query(TABLE_NAME_BRANDS, brandColumns, null, null, null, null, KEY_NAME);
    }

    public List<Brand> getBrands() {
        Cursor brandsCursor = getBrandsCursor();

        List<Brand> brandList = null;
        if (brandsCursor.moveToFirst()) {
            brandList = new ArrayList<Brand>();
            do {
                Brand brand = new Brand();
                brand.setId(Integer.parseInt(brandsCursor.getString(brandsCursor.getColumnIndex(KEY_ID))));
                brand.setName(brandsCursor.getString(brandsCursor.getColumnIndex(KEY_NAME)));
                brand.setExtra(brandsCursor.getString(brandsCursor.getColumnIndex(KEY_BRAND_EXTRA)));
                brand.setCreatedAt(brandsCursor.getString(brandsCursor.getColumnIndex(KEY_CREATED_AT)));

                brandList.add(brand);
            } while (brandsCursor.moveToNext());
        }

        return brandList;

    }

    public Brand getBrandByName(final String brandName) {
        String[] brandColumns = getBrandColumns();
        Cursor cursor = db.query(TABLE_NAME_BRANDS, brandColumns, KEY_NAME + " = ?", new String[]{brandName}, null, null, null);

        Brand brand = null;
        if (cursor != null && cursor.getCount() > 0) {
            brand = new Brand();
            cursor.moveToFirst();
            brand.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            brand.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            brand.setExtra(cursor.getString(cursor.getColumnIndex(KEY_BRAND_EXTRA)));
            brand.setCreatedAt(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
        }

        return brand;
    }

    public Brand getBrandById(final int idBrand) {
        String[] brandColumns = getBrandColumns();
        Cursor result = db.query(TABLE_NAME_BRANDS, brandColumns, KEY_ID + "=?", new String[]{idBrand + ""}, null, null, null);
        return getBrandFromCursor(result);
    }

    public Brand getBrandFromCursor(final Cursor cursorBrand) {
        Brand brand = new Brand();
        try {
            if (cursorBrand.moveToFirst()) {
                brand.setId(Integer.parseInt(cursorBrand.getString(cursorBrand.getColumnIndex(KEY_ID))));
                brand.setName(cursorBrand.getString(cursorBrand.getColumnIndex(KEY_NAME)));
                brand.setExtra(cursorBrand.getString(cursorBrand.getColumnIndex(KEY_BRAND_EXTRA)));
                brand.setCreatedAt(cursorBrand.getString(cursorBrand.getColumnIndex(KEY_CREATED_AT)));
            }
        }finally {
            cursorBrand.close();
        }
        return brand;
    }

    public void updateBrand(final Brand brand) {
        ContentValues values = getBrandValues(brand);
        db.update(TABLE_NAME_BRANDS, values, KEY_ID + "=?", new String[]{brand.getId() + ""});
    }

    public void updateCarsDefaultBrand(Brand brand) {
        String query = "update " + TABLE_NAME_CARS + " set " + KEY_ID_BRAND + " = NULL, " + KEY_ID_SERIE +  " = NULL where " + KEY_ID_BRAND + " = ?";
        db.execSQL(query, new String[] {brand.getId() + ""});
    }

    public void deleteBrand(final Brand brand) {
        db.delete(TABLE_NAME_BRANDS, KEY_ID + " = ? ", new String[]{brand.getId()+""});
    }


    /***********************************************/
    /*
    /*      Table Series
    /*
    /**********************************************/

    private String[] getSerieColumns() {
        return new String[]{KEY_ID, KEY_NAME, KEY_ID_BRAND, KEY_SERIES_PARENT, KEY_CREATED_AT};
    }

    private Cursor getSeriesCursor(final int idBrand) {

        String selection = null;
        String[] selectionArgs = null;
        if (idBrand > 0) {
            selection = "(" + KEY_SERIES_PARENT + " IS NULL OR " + KEY_SERIES_PARENT + " = 0)  AND "
                    + KEY_ID_BRAND + " = ?";
            selectionArgs = new String[]{idBrand + ""};
        }

        String[] serieColumns = getSerieColumns();

        return db.query(TABLE_NAME_SERIES, serieColumns,
                selection, selectionArgs, null,
                null, KEY_NAME);
    }

    public List<Serie> getSeries() {
        return getSeries(0);
    }

    public List<Serie> getSeries(final int idBrand) {
        Cursor cursorSerie = getSeriesCursor(idBrand);

        List<Serie> seriesList = null;
        if (cursorSerie.moveToFirst()) {
            seriesList = new ArrayList<Serie>();
            do {
                Serie serie = new Serie();
                serie.setId(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID))));
                serie.setName(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_NAME)));
                serie.setBrand(getBrandById(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID_BRAND)))));
                String idParent = cursorSerie.getString(cursorSerie.getColumnIndex(KEY_SERIES_PARENT));
                if (idParent != null)
                    serie.setParent(getSerieById(Integer.parseInt(idParent)));

                serie.setCreatedAt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_CREATED_AT)));


                seriesList.add(serie);
            } while (cursorSerie.moveToNext());
        }

        return seriesList;

    }

    private Cursor getSubseriesCursor(final int idSerie) {
        String[] serieColumns = getSerieColumns();
        return db.query(TABLE_NAME_SERIES, serieColumns, KEY_SERIES_PARENT + " = ?",
                new String[]{idSerie+""}, null, null,
                KEY_NAME);
    }

    public List<Serie> getSubseries(final int idBrand) {
        Cursor cursorSerie = getSubseriesCursor(idBrand);

        List<Serie> seriesList = null;
        if (cursorSerie.moveToFirst()) {
            seriesList = new ArrayList<Serie>();
            do {
                Serie serie = new Serie();
                serie.setId(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID))));
                serie.setName(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_NAME)));
                serie.setBrand(getBrandById(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID_BRAND)))));
                String idParent = cursorSerie.getString(cursorSerie.getColumnIndex(KEY_SERIES_PARENT));
                if (idParent != null)
                    serie.setParent(getSerieById(Integer.parseInt(idParent)));

                serie.setCreatedAt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_CREATED_AT)));

                seriesList.add(serie);
            } while (cursorSerie.moveToNext());
        }

        return seriesList;

    }

    public Serie getSerieById(final int idSerie) {
        String[] serieColumns = getSerieColumns();
        Cursor result = db.query(TABLE_NAME_SERIES, serieColumns, KEY_ID + "=?", new String[]{idSerie + ""}, null, null, null);
        return getSerieFromCursor(result);
    }

    public Serie getSerieFromCursor(final Cursor cursorSerie) {
        Serie serie = null;
        try {
            if (cursorSerie.moveToFirst()) {
                serie = new Serie();
                serie.setId(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID))));
                serie.setName(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_NAME)));
                serie.setBrand(getBrandById(Integer.parseInt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_ID_BRAND)))));
                String idParent = cursorSerie.getString(cursorSerie.getColumnIndex(KEY_SERIES_PARENT));
                if (idParent != null)
                    serie.setParent(getSerieById(Integer.parseInt(idParent)));

                serie.setCreatedAt(cursorSerie.getString(cursorSerie.getColumnIndex(KEY_CREATED_AT)));
            }
        }finally {
            cursorSerie.close();
        }
        return serie;
    }

    public Serie getSerieByName(final String serieName, final int idBrand) {
        String[] serieColumns = getSerieColumns();
        Cursor cursor = db.query(TABLE_NAME_SERIES, serieColumns, KEY_NAME + " = ? and "
                + KEY_ID_BRAND + " = ?", new String[]{serieName, idBrand + ""}, null,
                null, null);

        Serie serie = null;
        if (cursor != null && cursor.getCount() > 0) {
            serie = new Serie();
            cursor.moveToFirst();
            serie.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            serie.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            serie.setBrand(getBrandById(cursor.getInt(cursor.getColumnIndex(KEY_ID_BRAND))));
            String parent = cursor.getString(cursor.getColumnIndex(KEY_SERIES_PARENT));
            if (parent != null && !parent.equals(""))
                serie.setParent(getSerieById(Integer.parseInt(parent)));
            serie.setIsDefault(false);
            serie.setCreatedAt(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
        }

        return serie;
    }

    public void insertSerie(final Serie serie) {
        ContentValues values = getSerieValues(serie);
        db.insert(TABLE_NAME_SERIES, null, values);
    }

    public ContentValues getSerieValues(final Serie serie) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, serie.getName());
        values.put(KEY_ID_BRAND, serie.getBrand().getId());
        if (serie.getParent() != null)
            values.put(KEY_SERIES_PARENT, serie.getParent().getId());

        return values;
    }

    public void deleteSeriesByBrand(final Brand brand) {
        db.delete(TABLE_NAME_SERIES, KEY_ID_BRAND + " = ? ", new String[]{brand.getId() + ""});
    }
}
