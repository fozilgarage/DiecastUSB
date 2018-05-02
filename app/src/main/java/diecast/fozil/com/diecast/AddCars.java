package diecast.fozil.com.diecast;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Utilities.FileUtils;
import databases.Brand;
import databases.Car;
import databases.DataBaseManager;
import databases.Serie;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddCars extends AppCompatActivity {

    public static final String QUITAR_IMAGEN = "Quitar imagen";
    private static String APP_DIRECTORY = "FozilGarage";
    private static String MEDIA_DIRECTORY =  APP_DIRECTORY + File.separator + "Temp";

    private static final int MY_PERMISSIONS = 100;
    private static final int PHOTO_CODE = 200;
    private static final int SELECT_PICTURE = 101;
    private static final int BRAND_SAVED = 3;
    private static final int SERIE_SAVED = 4;
    private static final int SPEECH_INPUT = 10;

    private DataBaseManager dataBaseManager;

    DiecastArrayAdapter brandAdapter;
    DiecastArrayAdapter seriesAdapter;
    DiecastArrayAdapter subseriesAdapter;

    TextView tv_car_name;
    ImageView iv_car_image;
    Spinner sp_car_brand;
    Spinner sp_car_serie;
    Spinner sp_car_subserie;
    LinearLayout l_add_cars;
    Button count_less;
    Button count_more;
    TextView cars_count;
    EditText et_car_hashtag;
    EditText et_car_price;
    EditText et_car_extra;
    EditText et_car_purchase;
    DatePickerDialog datePickerDialog;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    int idBrand;
    int idSerie;
    int idSubserie;
    String mPath;
    String imageName;
    int typeImageUpload = 0;
    Uri uriImageGallery;
    boolean hasImage;

    int idCar;
    List<Brand> brandList;
    List<Serie> seriesList;
    List<Serie> subseriesList;
    Car car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cars);

        setToolBar("Alta de auto");

        dataBaseManager = new DataBaseManager(this);

        tv_car_name = (TextView) findViewById(R.id.tv_car_name);
        sp_car_brand = (Spinner) findViewById(R.id.sp_car_brand);
        sp_car_serie = (Spinner) findViewById(R.id.sp_car_serie);
        sp_car_subserie = (Spinner) findViewById(R.id.sp_car_subserie);
        iv_car_image = (ImageView) findViewById(R.id.iv_car_image);
        l_add_cars = (LinearLayout) findViewById(R.id.l_add_car);
        cars_count = findViewById(R.id.cars_count);
        cars_count.setText("1");
        count_less = findViewById(R.id.count_less);
        count_more = findViewById(R.id.count_more);
        et_car_hashtag = findViewById(R.id.et_car_hashtag);
        et_car_price = findViewById(R.id.et_car_price);
        et_car_extra = findViewById(R.id.et_car_extra);
        et_car_purchase = findViewById(R.id.et_car_purchase);
        hasImage = false;

        count_less.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = Integer.parseInt(cars_count.getText().toString());
                if (count > 1)
                    count--;
                else
                    count = 1;
                cars_count.setText(count + "");
            }
        });

        count_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = Integer.parseInt(cars_count.getText().toString());
                count++;
                cars_count.setText(count + "");
            }
        });


        iv_car_image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadCarImage();
            }
        });

        et_car_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePurchaseClick(view);
            }
        });

        loadInfoCarEdit();

        generateBrandsSpinner();
    }

    private void loadInfoCarEdit() {

        Intent intent = getIntent();
        idCar = intent.getIntExtra("id_car", 0);
        if (idCar > 0) {
            loadCarInfo(idCar);
        }
    }

    private void loadCarInfo(int idCar) {
        car = dataBaseManager.getCar(idCar);
        tv_car_name.setText(car.getName());
        cars_count.setText(car.getCount() + "");
        et_car_hashtag.setText(car.getHashtags());
        et_car_price.setText(car.getPrice() + "");
        et_car_extra.setText(car.getExtra());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        et_car_purchase.setText(car.getPurchaseDate());


        /*if (car.getBrand() != null) {
            sp_car_brand.setSelection(getBrandCursorPosition(car.getBrand().getName()));
            generateSeriesSpinner(car.getBrand().getId());
            if (car.getSerie() != null)
                sp_car_serie.setSelection(getSerieCursorPosition(car.getSerie().getName()));
        }*/
        Bitmap bitmap = BitmapFactory.decodeFile(car.getImage());
        iv_car_image.setImageBitmap(bitmap);
    }

    private void loadCarImage() {
        if(mayRequestStoragePermission()) {
            CharSequence[] options = {"Tomar foto", "Elegir de galería", "Cancelar"};
            if (hasImage)
                options = new CharSequence[]{"Tomar foto", "Elegir de galería", QUITAR_IMAGEN, "Cancelar"};
            showOptions(options);
        }
    }

    private void generateBrandsSpinner() {

        List<Brand> brands = new ArrayList<Brand>();
        brandList = dataBaseManager.getBrands();
        brands.add(new Brand("Desconocido"));
        if (brandList != null)
            brands.addAll(brandList);
        brands.add(new Brand(-1, "Agregar Nuevo ...", null));
        brandAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, brands);

        sp_car_brand.setAdapter(brandAdapter);

        sp_car_brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Brand brand = (Brand) sp_car_brand.getSelectedItem();
                if (brand.getId() == -1) {
                    goToAddBrand();
                    sp_car_brand.setSelection(0);
                } else {
                    idBrand = brand.getId();
                    generateSeriesSpinner(brand.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (car != null && car.getBrand() != null) {
            sp_car_brand.setSelection(getBrandCursorPosition(car.getBrand().getId()));
        }
    }

    private void generateSeriesSpinner(final int idBrand) {
        List<Serie> series = new ArrayList<Serie>();
        series.add(new Serie("Desconocido"));


        if (idBrand > 0) {
            seriesList = dataBaseManager.getSeries(idBrand);
            if (seriesList != null)
                series.addAll(seriesList);
            series.add(new Serie(-1, "Agregar Nuevo ..."));
        }
        seriesAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, series);
        sp_car_serie.setAdapter(seriesAdapter);

        sp_car_serie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Serie serie = (Serie) sp_car_serie.getSelectedItem();
                if (serie.getId() == -1) {
                    goToAddSerie();
                    sp_car_serie.setSelection(0);
                } else {
                    idSerie = serie.getId();
                    generateSubserieSpinner(serie.getId());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (car != null && car.getSerie() != null) {
            if (car.getSerie().getParent() != null)
                sp_car_serie.setSelection(getSerieCursorPosition(car.getSerie().getParent().getId()));
            else
                sp_car_serie.setSelection(getSerieCursorPosition(car.getSerie().getId()));
        }
    }

    private void generateSubserieSpinner(final int idSerie) {
        subseriesList = dataBaseManager.getSubseries(idSerie);
        List<Serie> subseries = new ArrayList<Serie>();
        subseries.add(new Serie("Desconocido"));
        if (subseriesList != null)
            subseries.addAll(subseriesList);
        if (idBrand > 0)
            subseries.add(new Serie(-1, "Agregar Nuevo ..."));
        subseriesAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, subseries);
        sp_car_subserie.setAdapter(subseriesAdapter);

        sp_car_subserie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Serie serie = (Serie) sp_car_subserie.getSelectedItem();
                if (serie.getId() == -1) {
                    goToAddSubserie();
                    sp_car_subserie.setSelection(0);
                } else {
                    idSubserie = serie.getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (car != null && car.getSerie() != null && car.getSerie().getParent() != null) {
            sp_car_subserie.setSelection(getSubserieCursorPosition(car.getSerie().getId()));
        }
    }



    private int getBrandCursorPosition(final int idBrand) {
        int position = 0;
        if (idBrand > 0) {
            for (int i = 0; i < brandAdapter.getCount(); i++) {
                if (brandAdapter.getItem(i).getId() == idBrand) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    private int getSerieCursorPosition(final int idSerie) {
        int position = 0;
        if (idSerie > 0 && seriesAdapter != null) {
            for (int i = 0; i < seriesAdapter.getCount(); i++) {
                if (seriesAdapter.getItem(i).getId() == idSerie) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    private int getSubserieCursorPosition(final int idSubserie) {
        int position = 0;
        if (idSubserie > 0 && subseriesAdapter != null) {
            for (int i = 0; i < subseriesAdapter.getCount(); i++) {
                if (subseriesAdapter.getItem(i).getId() == idSubserie) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    private void setToolBar(final String name) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void showOptions(final CharSequence[] options) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddCars.this);
        builder.setTitle("Elige una opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which] == "Tomar foto") {
                    openCamera();
                } else if(options[which] == "Elegir de galería") {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    /*intent.putExtra("crop", "true");
                    intent.putExtra("scale", true);
                    intent.putExtra("outputX", 640);
                    intent.putExtra("outputY", 480);
                    intent.putExtra("aspectX", 4);
                    intent.putExtra("aspectY", 3);
                    intent.putExtra("return-data", true);*/

                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                } else if(options[which] == QUITAR_IMAGEN) {
                    typeImageUpload = 3;
                    int id = getResources().getIdentifier("photo_not_available", "drawable", getPackageName());
                    Glide.with(AddCars.this).load(id).centerCrop().into(iv_car_image);
                } else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if (!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if (isDirectoryCreated) {
            Long timestamp = System.currentTimeMillis() / 1000;
            imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(AddCars.this, BuildConfig.APPLICATION_ID + ".provider", newFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            /*intent.putExtra("crop", "true");
            intent.putExtra("outputX", 150);
            intent.putExtra("outputY", 150);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, MEDIA_DIRECTORY + "/algo.jpg");
            //intent.PutExtra("output", ParseUri("file://" & File.Combine(File.DirRootExternal, "temp.jpg")))
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());*/

            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this, new String[]{mPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("External Storage", "Scanned" + path + ":");
                            Log.i("External Storage", "Uri = " + uri);
                        }
                    });

                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    iv_car_image.setImageBitmap(bitmap);
                    hasImage = true;
                    typeImageUpload = 1;
                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    iv_car_image.setImageURI(path);
                    uriImageGallery = path;

                    /*Bundle extras = data.getExtras();
                    Bitmap photo = extras.getParcelable("data");
                    iv_car_image.setImageBitmap(photo);
                    uriImageGallery = getImageUri(this, photo);*/
                    hasImage = true;

                    typeImageUpload = 2;
                    break;
                case BRAND_SAVED:
                    Snackbar.make(l_add_cars, "Se agrego un elmento al catalogo de fabricantes", Snackbar.LENGTH_SHORT).show();
                    generateBrandsSpinner();
                    sp_car_brand.setSelection(getBrandCursorPosition(data.getIntExtra("id_saved", 0)));
                    break;
                case SERIE_SAVED:
                    Snackbar.make(l_add_cars, "Se agrego un elmento al catalogo de series", Snackbar.LENGTH_SHORT).show();
                    int idParent = data.getIntExtra("id_parent", 0);
                    if (idParent > 0) {
                        generateSubserieSpinner(idParent);
                        sp_car_subserie.setSelection(getSubserieCursorPosition(data.getIntExtra("id_saved", 0)));
                    } else {
                        generateSeriesSpinner(data.getIntExtra("id_brand", 0));
                        sp_car_serie.setSelection(getSerieCursorPosition(data.getIntExtra("id_saved", 0)));
                    }
                    break;
                case SPEECH_INPUT:
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tv_car_name.setText(result.get(0));
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddCars.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                iv_car_image.setEnabled(true);
                loadCarImage();
            } else {
                showExplanation();
            }
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddCars.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de imagen necesitas aceptar permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();
            }
        });
        builder.show();
    }

    private boolean mayRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        /*if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) ||
                (shouldShowRequestPermissionRationale(CAMERA))) {
            Snackbar.make(l_add_cars, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok,
                    new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }*/
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void saveCar(View view) {
        if (tv_car_name.getText().toString().equals("")) {
            tv_car_name.setError("Nombre es obligatorio");
            return;
        }

        long id = 0;

        int idSerieSelected = idSerie;
        if (idSubserie > 1)
            idSerieSelected = idSubserie;
        Serie serie = dataBaseManager.getSerieById(idSerieSelected);

        if(car == null) {
            car = new Car();
            car.setName(tv_car_name.getText().toString());
            if (idBrand > 0)
                car.setBrand(dataBaseManager.getBrandById(idBrand));
            car.setSerie(serie);
            car.setCount(Integer.parseInt(cars_count.getText().toString()));
            car.setHashtags(et_car_hashtag.getText().toString());
            Log.d("***", et_car_price.getText().toString());
            if (!et_car_price.getText().toString().equals(""))
                car.setPrice(Integer.parseInt(et_car_price.getText().toString()));
            car.setExtra(et_car_extra.getText().toString());
            //car.setPurchaseDate(dp_car_purchase.getDayOfMonth() + "/" + dp_car_purchase.getMonth() + "/" + dp_car_purchase.getYear());
            id = dataBaseManager.insertCar(car);
        } else {
            car.setName(tv_car_name.getText().toString());
            if (idBrand > 0)
                car.setBrand(dataBaseManager.getBrandById(idBrand));
            else
                car.setBrand(null);
            car.setSerie(serie);
            id = car.getId();
            car.setCount(Integer.parseInt(cars_count.getText().toString()));
            car.setHashtags(et_car_hashtag.getText().toString());
            car.setPrice(Double.parseDouble(et_car_price.getText().toString()));
            car.setExtra(et_car_extra.getText().toString());
            car.setPurchaseDate(et_car_purchase.getText().toString());
            dataBaseManager.updateCar(car);
        }


        if (typeImageUpload == 1 || typeImageUpload == 2)
            saveCarPicture(id);
        //Snackbar.make(view, R.string.app_name, Snackbar.LENGTH_SHORT).show();
        if (car.getId() < 1)
            Toast.makeText(AddCars.this, "Se agrego un nuevo elemento al catalogo de autos", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(AddCars.this, "Se edito elemento en catalogo de autos", Toast.LENGTH_LONG).show();
        //Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void saveCarPicture(final long idCar) {
        Long timestamp = System.currentTimeMillis() / 1000;
        String newNameImage = timestamp.toString() + ".jpg";

        File dstDirectory = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY);
        if (!dstDirectory.exists())
            dstDirectory.mkdirs();

        String mPathCarImage = dstDirectory + File.separator + newNameImage;

        try {
            if (typeImageUpload == 1)
                FileUtils.copy(new File(mPath), new File(mPathCarImage));
            else if (typeImageUpload == 2)
                FileUtils.copy(new File(getRealPathFromURI(uriImageGallery)), new File(mPathCarImage));
            dataBaseManager.updateImageCar((int) idCar, mPathCarImage);
            File tmpDirectory = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
            FileUtils.deleteFilesFromDirectory(tmpDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AddCars.this, "Error al guardar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void goToAddBrand() {
        Intent intent = new Intent(AddCars.this, AddBrands.class);
        startActivityForResult(intent, BRAND_SAVED);
    }

    public void goToAddSerie() {
        Intent intent = new Intent(AddCars.this, AddSeries.class);
        intent.putExtra("id_brand", idBrand);
        startActivityForResult(intent, SERIE_SAVED);
    }

    public void goToAddSubserie() {
        Intent intent = new Intent(AddCars.this, AddSeries.class);
        intent.putExtra("id_brand", idBrand);
        intent.putExtra("id_serie", idSerie);
        startActivityForResult(intent, SERIE_SAVED);
    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void getSpeechInput(final View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, SPEECH_INPUT);
        else
            Toast.makeText(this, "Tu dispositivo no soporta entrada de texto para esta aplicación.", Toast.LENGTH_LONG).show();
    }

    public void datePurchaseClick(final View view) {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        if (car != null && car.getPurchaseDate() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                c.setTime(sdf.parse(car.getPurchaseDate()));
            } catch (final ParseException pe) {
                Log.e("ERROR", pe.getMessage());
            }
        }
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        // date picker dialog
        datePickerDialog = new DatePickerDialog(AddCars.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // set day of month , month and year value in the edit text
                        String dateText = "";
                        if (dayOfMonth < 10) dateText = ("0" + dayOfMonth); else dateText = (dayOfMonth + "");
                        monthOfYear++;
                        if (monthOfYear < 10) dateText += "/0" + monthOfYear; else dateText += "/" + monthOfYear;
                        dateText += "/" + year;
                        et_car_purchase.setText(dateText);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
