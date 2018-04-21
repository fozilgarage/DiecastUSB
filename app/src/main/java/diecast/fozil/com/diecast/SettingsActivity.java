package diecast.fozil.com.diecast;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import Utilities.FileUtils;
import databases.Car;
import databases.DataBaseManager;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SettingsActivity extends AppCompatActivity {

    Button btExportDB;
    Button btImportDB;
    Button btCleanDB;
    View layoutSettings;
    DataBaseManager dataBaseManager;

    private static final int MY_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setToolBar("Configuración");

        dataBaseManager = new DataBaseManager(this);

        layoutSettings = findViewById(R.id.layout_settings);
        btExportDB = (Button) findViewById(R.id.bt_export_db);
        btImportDB = (Button) findViewById(R.id.bt_import_db);
        btCleanDB = (Button) findViewById(R.id.bt_clean_db);

        btExportDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mayRequestStoragePermission()) {
                    exportDB();
                }
            }
        });

        btImportDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDB();
            }
        });

        btCleanDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanDB();
            }
        });
    }

    private void importDB() {
        Intent intent = new Intent(SettingsActivity.this, ImportDBActivity.class);
        startActivity(intent);
    }

    private void exportDB() {
        final ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        SimpleDateFormat  sdf = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
        String dateFormated = sdf.format(new Date());
        final String fileName = "Catalogo_" + dateFormated + ".xlsx";
        File sdCard = Environment.getExternalStorageDirectory();
        String directoryPath = "/Download/FozilGarage";
        final File directory = new File(sdCard.getAbsolutePath() + directoryPath);

        List<Car> carList = dataBaseManager.getListCars(false);
        if (carList.size() > 0) {
            FileUtils.exportToExcel(carList, directory, fileName, dataBaseManager);
            Snackbar snackbar = Snackbar.make(layoutSettings, "Archivo creado en " + directoryPath + "/" + fileName, Snackbar.LENGTH_LONG).setAction("ABRIR", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File xls = new File(directory.getAbsolutePath() + "/" + fileName);
                    Uri path = FileProvider.getUriForFile(SettingsActivity.this, BuildConfig.APPLICATION_ID + ".provider", xls);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "application/vnd.ms-excel");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(SettingsActivity.this, "No Application available to view XLSX: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            snackbar.show();
        } else {
            Snackbar.make(layoutSettings, "No existen registros para exportar", Snackbar.LENGTH_LONG).show();
        }
        progressDialog.hide();
    }

    private void cleanDB() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this, R.style.myDialog);
        builder.setCancelable(true);
        builder.setTitle("Eliminar toda la información");
        builder.setMessage("A continuación se eliminara toda la información de la aplicación. ¿Desea continuar?");
        builder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataBaseManager.removeAll();
                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        intent.putExtra("message", "Se han eliminado todo el catalogo");
                        startActivity(intent);
                        finish();
                    }
                });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setToolBar(final String name) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean mayRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SettingsActivity.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                exportDB();
            } else {
                showExplanation();
            }
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
