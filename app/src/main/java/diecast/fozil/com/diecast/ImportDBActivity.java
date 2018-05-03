package diecast.fozil.com.diecast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import databases.Brand;
import databases.Car;
import databases.DataBaseManager;
import databases.Serie;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ImportDBActivity extends AppCompatActivity {

    ListView lvBackupFiles;
    BackupFilesAdapter backupFilesAdapter;
    private DataBaseManager dataBaseManager;

    private static final int MY_PERMISSIONS = 100;

    ProgressDialog progressDialog;

    File fileImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_db);

        progressDialog = new ProgressDialog(ImportDBActivity.this);

        dataBaseManager = new DataBaseManager(this);

        setToolBar("Importar/Restaurar datos");

        lvBackupFiles = (ListView) findViewById(R.id.lv_backup_files);

        loadExcelFiles();

    }

    private void loadExcelFiles() {
        if(mayRequestStoragePermission()) {
            File sdCard = Environment.getExternalStorageDirectory();
            String directoryPath = "/Download/FozilGarage";
            final File directory = new File(sdCard.getAbsolutePath() + directoryPath);

            List<File> backupFiles = getBackupFiles(directory);

            if (backupFiles != null && backupFiles.size() > 0) {
                backupFilesAdapter = new BackupFilesAdapter(getApplicationContext(), backupFiles);
                lvBackupFiles.setAdapter(backupFilesAdapter);
            }
        }
    }

    private List<File> getBackupFiles(final File folder) {
        List<File> files = new ArrayList<File>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(getBackupFiles(fileEntry));
            } else {
                files.add(fileEntry);
            }
        }
        return files;
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
                Toast.makeText(ImportDBActivity.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                loadExcelFiles();
            } else {
                showExplanation();
            }
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImportDBActivity.this);
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
                finish();
            }
        });
        builder.show();
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

    private class BackupFilesAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        private List<File> backupFiles;
        Context context;

        public BackupFilesAdapter(Context applicationContext, List<File> files) {
            layoutInflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            backupFiles = files;
            context = applicationContext;
        }

        @Override
        public int getCount() {
            return backupFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.activity_excel_files_item, viewGroup, false);

                viewHolder.rowItem = (LinearLayout) view.findViewById(R.id.layout_excel_files);
                viewHolder.textView = (TextView) view.findViewById(R.id.it_tv_text);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.textView.setText(backupFiles.get(position).getName());

            viewHolder.rowItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImportDBActivity.this, R.style.myDialog);
                    builder.setCancelable(true);
                    builder.setTitle("Restaurar/Importar datos");
                    builder.setMessage("¿Estas seguro de querer cargar la información del archivo?");
                    builder.setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fileImport = backupFiles.get(position);
                                    new ImportDataTask().execute();
                                }
                            });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            return view;
        }

        class ViewHolder{
            protected TextView textView;
            protected LinearLayout rowItem;
        }

    }

    private void importData(File file) {

        try {
            Log.i("***", "Abriendo archivo excel: " + file.getAbsolutePath().toString());

            OPCPackage myFileSystem = OPCPackage.open(file);

            // Create a workbook using the File System
            XSSFWorkbook myWorkBook = new XSSFWorkbook (myFileSystem);

            // Get the first sheet from workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            int rowCount = 0;
            while(rowIter.hasNext()){
                dataBaseManager.dbOpen();
                XSSFRow myRow = (XSSFRow) rowIter.next();
                if (myRow.getCell(0) != null) {
                    if (rowCount > 0 && myRow.getCell(1) != null) {
                        Car car = new Car();
                        car.setName(myRow.getCell(1).getStringCellValue());
                        if (myRow.getCell(2) != null) {
                            car.setBrand(getOrCreateBrandByName(myRow.getCell(2).getStringCellValue()));
                            car.setSerie(getSerie(myRow.getCell(3) != null ? myRow.getCell(3).getStringCellValue() : "", myRow.getCell(4) != null ? myRow.getCell(4).getStringCellValue() : "", car.getBrand()));
                        }
                        car.setFavorite(isFavorite(myRow.getCell(5).getStringCellValue()));
                        car.setCount(Integer.parseInt(myRow.getCell(6).getStringCellValue()));
                        car.setPrice(myRow.getCell(7).getNumericCellValue());
                        car.setPurchaseDate(myRow.getCell(8).getStringCellValue());
                        car.setExtra(myRow.getCell(9).getStringCellValue());
                        car.setImage(myRow.getCell(10).getStringCellValue());
                        car.setCreatedAt(myRow.getCell(11).getStringCellValue());

                        dataBaseManager.insertCar(car);
                    }
                    rowCount++;
                }
                dataBaseManager.dbClose();
            }

            Snackbar.make(findViewById(R.id.layout_excel_files), "Se agregaron" + (rowCount -1) + "registros.", Snackbar.LENGTH_LONG).show();
            Intent intent = new Intent(ImportDBActivity.this, MainActivity.class);
            intent.putExtra("message", "Se dieron de alta " + (rowCount - 1) + " registros.");
            startActivity(intent);
            finish();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    private Brand getOrCreateBrandByName(final String brandName) {
        Brand brand = dataBaseManager.getBrandByName(brandName);
        if (brand == null && !brandName.equals("")) {
            brand = new Brand();
            brand.setName(brandName);
            dataBaseManager.insertBrand(brand);
            brand = dataBaseManager.getBrandByName(brandName);
        }
        return brand;
    }

    private Serie getSerie(final String serieName, final String subserieName, final Brand brand) {
        if (serieName.equals(""))
            return null;
        Serie serie = getOrCreateSerieByName(serieName, brand, null);
        if (!subserieName.equals(""))
            serie = getOrCreateSerieByName(subserieName, brand, serie);

        return serie;
    }

    private Serie getOrCreateSerieByName(final String serieName, final Brand brand,
                                         final Serie parent) {
        Serie serie = dataBaseManager.getSerieByName(serieName, brand.getId());
        if (serie == null) {
            serie = new Serie();
            serie.setName(serieName);
            serie.setBrand(dataBaseManager.getBrandById(brand.getId()));
            if (parent != null)
                serie.setParent(dataBaseManager.getSerieById(parent.getId()));
            dataBaseManager.insertSerie(serie);
            serie = dataBaseManager.getSerieByName(serieName, brand.getId());
        }
        return serie;
    }


    private boolean isFavorite(final String text) {
        boolean isFavBool = false;
        if (text.equals("SI"))
            isFavBool = true;
        return isFavBool;
    }

    private class ImportDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Importando datos...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            importData(fileImport);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
        }
    }

}
