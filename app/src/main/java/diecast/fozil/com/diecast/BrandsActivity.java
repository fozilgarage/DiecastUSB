package diecast.fozil.com.diecast;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.snowdream.android.widget.SmartImageView;

import java.io.File;
import java.util.List;

import databases.Brand;
import databases.Car;
import databases.DataBaseManager;


public class BrandsActivity extends AppCompatActivity {

    private DataBaseManager dataBaseManager;
    CatalogAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brands);

        setToolBar("Fabricantes");
        dataBaseManager = new DataBaseManager(this);

        listView = findViewById(R.id.lv_brands_catalog);

        loadBrandsList();

    }

    private void loadBrandsList() {
        List<Brand> brands = dataBaseManager.getBrands();
        adapter = new CatalogAdapter(getApplicationContext(), brands);
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

    private class CatalogAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        private List<Brand> brandsList;

        public CatalogAdapter(Context applicationContext, List<Brand> brand) {
            layoutInflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            brandsList = brand;
        }

        @Override
        public int getCount() {
            if (brandsList == null)
                return 0;
            else
                return brandsList.size();
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
                holder = new BrandsActivity.CatalogAdapter.ViewHolder();
                view = layoutInflater.inflate(R.layout.activity_grid_catalog_item, viewGroup, false);

                holder.tv_name = (TextView) view.findViewById(R.id.it_tv_text);
                holder.cat_iv_edit = view.findViewById(R.id.cat_iv_edit);
                holder.cat_iv_delete = view.findViewById(R.id.cat_iv_delete);

                view.setTag(holder);
            } else {
                holder = (CatalogAdapter.ViewHolder) view.getTag();
            }

            holder.tv_name.setText(brandsList.get(position).getName());

            holder.cat_iv_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BrandsActivity.this, AddBrands.class);
                    intent.putExtra("id_brand", brandsList.get(position).getId());
                    startActivityForResult(intent, 1);
                }
            });

            holder.cat_iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteBrand(brandsList.get(position));
                }
            });

            return view;
        }

        class ViewHolder{
            protected TextView tv_name;
            protected ImageView cat_iv_edit;
            protected  ImageView cat_iv_delete;
        }

        public void updateResults(List<Brand> results) {
            brandsList = results;
            notifyDataSetChanged();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                int result = data.getIntExtra("result", 0);
                if (result == 1) {
                    loadBrandsList();
                    Snackbar.make(findViewById(R.id.layout_brands), "Se agrega un nuevo fabricante", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    public void deleteBrand(final Brand brand) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BrandsActivity.this, R.style.myDialog);
        builder.setCancelable(true);
        builder.setTitle("Eliminar registro");
        builder.setMessage("¿Estas seguro de querer eliminar el registro '" + brand.getName() + "' del catalogo?. Todos los autos que tengan el fabricante se actualizaran con fabricante 'Desconocido'");
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataBaseManager.updateCarsDefaultBrand(brand);
                        dataBaseManager.deleteSeriesByBrand(brand);
                        dataBaseManager.deleteBrand(brand);
                        loadBrandsList();
                        Snackbar.make(findViewById(R.id.layout_brands), "Se elimino " + brand.getName() + " del catalogo de fabricantes", Snackbar.LENGTH_LONG).show();
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

    public void goToAddBrands(final View view) {
        Intent intent = new Intent(BrandsActivity.this, AddBrands.class);
        startActivityForResult(intent, 1);
    }
}
