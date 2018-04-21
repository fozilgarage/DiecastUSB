package diecast.fozil.com.diecast;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import databases.Brand;
import databases.DataBaseManager;
import databases.Serie;

public class SeriesActivity extends AppCompatActivity {

    private DataBaseManager dataBaseManager;
    CatalogAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series);

        setToolBar("Series");

        dataBaseManager = new DataBaseManager(this);

        listView = findViewById(R.id.lv_series_catalog);

        loadSeriesList();
    }

    private void loadSeriesList() {
        List<Serie> series = dataBaseManager.getSeries();
        adapter = new CatalogAdapter(getApplicationContext(), series);
        listView.setAdapter(adapter);
    }

    private Toolbar setToolBar(final String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        return toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void goToAddSeries(View view) {
        Intent intent = new Intent(SeriesActivity.this, AddSeries.class);
        startActivityForResult(intent, 1);
    }

    private class CatalogAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        private List<Serie> seriesList;

        public CatalogAdapter(Context applicationContext, List<Serie> serie) {
            layoutInflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            seriesList = serie;
        }

        @Override
        public int getCount() {
            if (seriesList == null)
                return 0;
            else
                return seriesList.size();
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

            CatalogAdapter.ViewHolder holder;

            if (view == null) {
                holder = new CatalogAdapter.ViewHolder();
                view = layoutInflater.inflate(R.layout.activity_grid_catalog_item, viewGroup, false);

                holder.tv_name = (TextView) view.findViewById(R.id.it_tv_text);
                holder.cat_iv_edit = view.findViewById(R.id.cat_iv_edit);
                holder.cat_iv_delete = view.findViewById(R.id.cat_iv_delete);

                view.setTag(holder);
            } else {
                holder = (CatalogAdapter.ViewHolder) view.getTag();
            }

            StringBuilder name = new StringBuilder();
            name.append(seriesList.get(position).getName());
            name.append(" - ");
            name.append(seriesList.get(position).getBrand().getName());
            holder.tv_name.setText(name);

            holder.cat_iv_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SeriesActivity.this, AddSeries.class);
                    intent.putExtra("id_serie", seriesList.get(position).getId());
                    startActivityForResult(intent, 1);
                }
            });

            holder.cat_iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteSerie(seriesList.get(position));
                }
            });

            return view;
        }

        class ViewHolder{
            protected TextView tv_name;
            protected ImageView cat_iv_edit;
            protected  ImageView cat_iv_delete;
        }

        public void updateResults(List<Serie> results) {
            seriesList = results;
            notifyDataSetChanged();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                int result = data.getIntExtra("result", 0);
                if (result == 1) {
                    loadSeriesList();
                    Snackbar.make(findViewById(R.id.layout_brands), "Se agrega un nuevo fabricante", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    public void deleteSerie(final Serie serie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesActivity.this, R.style.myDialog);
        builder.setCancelable(true);
        builder.setTitle("Eliminar registro");
        builder.setMessage("¿Estas seguro de querer eliminar el registro '" + serie.getName() + "' del catalogo?. Todos los autos que tengan el fabricante se actualizaran con fabricante 'Desconocido'");
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dataBaseManager.updateCarsDefaultBrand(serie);
                        //dataBaseManager.deleteSeriesByBrand(serie);
                        //dataBaseManager.deleteBrand(serie);
                        loadSeriesList();
                        Snackbar.make(findViewById(R.id.layout_brands), "Se elimino " + serie.getName() + " del catalogo de fabricantes", Snackbar.LENGTH_LONG).show();
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
}
