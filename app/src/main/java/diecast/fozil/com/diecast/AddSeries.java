package diecast.fozil.com.diecast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import databases.Brand;
import databases.DataBaseManager;
import databases.Serie;

public class AddSeries extends AppCompatActivity {

    private DataBaseManager dataBaseManager;
    TextView tv_serie_name;
    List<Brand> brandList;
    List<Serie> seriesList;
    DiecastArrayAdapter brandAdapter;
    DiecastArrayAdapter seriesAdapter;
    Spinner sp_serie_brand;
    Spinner sp_serie_serie;
    Brand brand;
    Serie serie;
    int idSerieParent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_series);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Alta de serie");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        dataBaseManager = new DataBaseManager(this);

        tv_serie_name = (TextView) findViewById(R.id.tv_serie_name);
        sp_serie_brand = (Spinner) findViewById(R.id.sp_serie_brand);
        sp_serie_serie = (Spinner) findViewById(R.id.sp_serie_serie);

        generateBrandsSpinner();

        Intent intent = getIntent();
        int idBrand = intent.getIntExtra("id_brand", 0);
        if (idBrand > 0) {
            sp_serie_brand.setEnabled(false);
            sp_serie_brand.setSelection(getBrandSpinnerPosition(idBrand));
            generateSeriesSpinner(idBrand);
        }

        idSerieParent = intent.getIntExtra("id_serie", 0);
        if (idSerieParent > 0) {
            sp_serie_serie.setEnabled(false);
            sp_serie_serie.setVisibility(View.VISIBLE);
        }
    }

    private int getBrandSpinnerPosition(final int idBrand) {
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

    private int getSerieSpinnerPosition(final int idSerie) {
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

    private void generateBrandsSpinner() {
        List<Brand> brands = new ArrayList<Brand>();
        brands.add(new Brand("Seleccione un valor de la lista"));
        brandList = dataBaseManager.getBrands();
        if (brandList != null)
            brands.addAll(brandList);
        brandAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, brands);

        sp_serie_brand.setAdapter(brandAdapter);

        sp_serie_brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                brand = (Brand) sp_serie_brand.getSelectedItem();
                generateSeriesSpinner(brand.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void generateSeriesSpinner(final int idBrand) {
        seriesList = dataBaseManager.getSeries(idBrand);
        List<Serie> series = new ArrayList<Serie>();
        series.add(new Serie("Desconocido"));
        if (seriesList != null)
            series.addAll(seriesList);
        if (idBrand > 0)
            series.add(new Serie(-1, "Agregar Nuevo ..."));
        seriesAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, series);
        sp_serie_serie.setAdapter(seriesAdapter);

        sp_serie_serie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serie = (Serie) sp_serie_serie.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (idSerieParent > 0)
            sp_serie_serie.setSelection(getSerieSpinnerPosition(idSerieParent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void saveSerie(View view) {
        int idSaved = 0;
        int idParent = 0;
        if (tv_serie_name.getText().toString().equals("")) {
            tv_serie_name.setError("Este campo es obligatorio");
            return;
        }
        if (brand == null || brand.getId() <= 0) {
            Toast.makeText(this, "Debe agregar un fabricante", Toast.LENGTH_SHORT).show();
        } else {
            if (idSerieParent > 0) {
                if (serie == null || serie.getId() <= 0) {
                    Toast.makeText(this, "Debe agregar una serie", Toast.LENGTH_SHORT).show();
                } else {
                    dataBaseManager.insertSerie(new Serie(tv_serie_name.getText().toString(), brand, serie));
                    idParent = serie.getId();
                }
            } else {
                dataBaseManager.insertSerie(new Serie(tv_serie_name.getText().toString(), brand));

            }

            idSaved = dataBaseManager.getSerieByName(tv_serie_name.getText().toString(), brand.getId()).getId();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",1);
            returnIntent.putExtra("id_brand", brand.getId());
            returnIntent.putExtra("id_saved", idSaved);
            returnIntent.putExtra("id_parent", idParent);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}
