package diecast.fozil.com.diecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import databases.Brand;
import databases.DataBaseManager;

public class AddBrands extends AppCompatActivity {

    private DataBaseManager dataBaseManager;

    TextView tv_brand_name;
    TextView tv_brand_extra;

    Button btn_save_brand;

    Brand brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_brands);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Alta de fabricante");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        dataBaseManager = new DataBaseManager(this);

        tv_brand_name = (TextView) findViewById(R.id.tv_brand_name);
        tv_brand_extra = (TextView) findViewById(R.id.tv_brand_extra);

        Intent intent = getIntent();
        int idBrand = intent.getIntExtra("id_brand", 0);
        if (idBrand > 0) {
            brand = dataBaseManager.getBrandById(idBrand);
            tv_brand_name.setText(brand.getName());
            tv_brand_extra.setText(brand.getExtra());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void saveBrand(View view) {
        if (tv_brand_name.getText().toString().equals("")) {
            tv_brand_name.setError("Este campo es obligatorio");
        } else {

            int idSaved = 0;
            if (brand == null) {
                brand = new Brand(tv_brand_name.getText().toString(), tv_brand_extra.getText().toString());
                dataBaseManager.insertBrand(brand);
                idSaved = dataBaseManager.getBrandByName(tv_brand_name.getText().toString()).getId();
            } else {
                brand.setName(tv_brand_name.getText().toString());
                brand.setExtra(tv_brand_extra.getText().toString());
                dataBaseManager.updateBrand(brand);
                idSaved = brand.getId();
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", 1);
            returnIntent.putExtra("id_saved", idSaved);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

}
