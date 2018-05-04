package diecast.fozil.com.diecast;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.snowdream.android.widget.SmartImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import databases.Brand;
import databases.Car;
import databases.DataBaseManager;
import databases.Serie;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    public static final String ACTIVITY_NAME = "MAIN_ACTIVITY";
    public static final int SAVE_CAR = 1;
    public static final int VIEW_CAR = 2;
    private DataBaseManager dataBaseManager;

    ListView listView;
    CatalogAdapter adapter;
    String querySearch;
    TextView tv_cars_count;
    LinearLayout ll_search_detail;
    DrawerLayout main_layout;
    SwipeRefreshLayout swipeRefreshLayout;
    Toolbar toolbarFilter;

    Spinner sp_car_brand;
    Spinner sp_car_serie;
    Spinner sp_car_subserie;

    List<Brand> brandList;
    List<Serie> seriesList;
    List<Serie> subseriesList;

    DiecastArrayAdapter brandAdapter;
    DiecastArrayAdapter seriesAdapter;
    DiecastArrayAdapter subseriesAdapter;

    int idBrand = -1;
    int idSerie = -1;
    int idSubserie = -1;

    ProgressDialog progressDialog;

    int clickOnFilterButton;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = setToolBar("Mi Garage");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        progressDialog = new ProgressDialog(MainActivity.this);

        String message = getIntent().getStringExtra("message");
        if (message != null && !message.equals(""))
            showMessage(message);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dataBaseManager = new DataBaseManager(this);

        listView = findViewById(R.id.lv_catalogo);
        tv_cars_count = findViewById(R.id.tv_cars_count);
        main_layout = findViewById(R.id.drawer_layout);
        ll_search_detail = findViewById(R.id.ll_search_detail);
        swipeRefreshLayout = findViewById(R.id.list_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               loadCatalog();
               swipeRefreshLayout.setRefreshing(false);
            }
        });
        toolbarFilter = findViewById(R.id.menuFilter);
        sp_car_brand = findViewById(R.id.sp_car_brand);
        sp_car_serie = findViewById(R.id.sp_car_serie);
        sp_car_subserie = findViewById(R.id.sp_car_subserie);
        adapter = new CatalogAdapter(getApplicationContext(), null);
        listView.setAdapter(adapter);
        loadCatalog();

    }

    private Toolbar setToolBar(final String name) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);

        return toolbar;
    }

    private int getCountCars(List<Car> cars) {
        int count = 0;
        for (Car car : cars) {
            if (car.getCount() > 0)
                count += car.getCount();
            else
                count++;
        }

        return count;
    }

    public void goToAddCar(View view) {
        Intent intent = new Intent(MainActivity.this, AddCars.class);
        startActivityForResult(intent, SAVE_CAR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.menuSearch);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, MainActivity.class)));
        searchView.setIconifiedByDefault(false);

        return true;

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        doSearch(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        doSearch(newText);
        return false;
    }

    public void doSearch(String query) {
        querySearch = query;
        new SearchTask().execute();
        if(query.equals(""))
            ll_search_detail.setVisibility(View.GONE);
        else
            ll_search_detail.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_brands) {
            Intent intent = new Intent(MainActivity.this, BrandsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_series) {
            Intent intent = new Intent(MainActivity.this, SeriesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_favorites) {

        } else if (id == R.id.nav_wishlist) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class SearchTask extends AsyncTask<Void, Void, Void> {

        List<Car> cars;

        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            cars = dataBaseManager.getListCars(querySearch, idBrand,idSerie,idSubserie,true);
            //MainActivity.getSupportActionBar().setSubtitle(cursor.getCount() + " Autos");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.updateResults(cars);
            int size = 0;
            for (Car car : cars)
                if (car.getCount() > 0)
                    size += car.getCount();
                else
                    size++;
            if (size == 1)
                tv_cars_count.setText(size + " Auto encontrado");
            else
                tv_cars_count.setText(size + " Autos encontrados");
        }

    }

    private class CatalogAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        private List<Car> carList;

        private CatalogAdapter(Context applicationContext, List<Car> cars) {
            layoutInflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            carList = cars;
        }

        @Override
        public int getCount() {
            if (carList == null)
                return 0;
            return carList.size();
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

            final ViewHolder holder;

            if (view == null) {
                holder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.activity_main_item, viewGroup, false);

                holder.smartImageView = view.findViewById(R.id.imagen1);
                holder.tv_name = view.findViewById(R.id.tv_name);
                holder.tv_detail = view.findViewById(R.id.tv_detail);
                holder.row_item_car = view.findViewById(R.id.row_item_car);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tv_name.setText(carList.get(position).getName());
            StringBuilder detail = new StringBuilder();
            String brand = "Desconocido";
            if (carList.get(position).getBrand().getName() != null)
                brand = carList.get(position).getBrand().getName();
            detail.append(brand);
            String serieName = "";
            if (carList.get(position).getSerie() != null) {
                if (carList.get(position).getSerie().getParent() != null && carList.get(position).getSerie().getParent().getId() > 0)
                    serieName = dataBaseManager.getSerieById(carList.get(position).getSerie().getParent().getId()).getName() + " - ";
                serieName += carList.get(position).getSerie().getName();
            }

            detail.append("\n").append(serieName);
            String createdAt = carList.get(position).getCreatedAt();
            if (!serieName.equals(""))
                detail.append("\n");
            detail.append(createdAt.length() > 16 ? createdAt.substring(0, createdAt.length() - 3) : createdAt);
            int count = carList.get(position).getCount();
            String item = " auto";
            if (count > 1)
                item = " autos";
            detail.append("\n").append(count).append(item);

            holder.tv_detail.setText(detail);

            String imageURL = carList.get(position).getImage();
            if (imageURL != null && !imageURL.equals("")) {
                File image = new File(imageURL);
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", image);
                Glide.with(MainActivity.this).load(photoUri).fitCenter().centerCrop().into(holder.smartImageView);
            } else {
                int id = getResources().getIdentifier("photo_not_available", "drawable", getPackageName());
                Glide.with(MainActivity.this).load(id).centerCrop().into(holder.smartImageView);
            }

            holder.row_item_car.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CarDetail.class);
                    intent.putExtra("id_car", carList.get(position).getId());
                    startActivityForResult(intent, VIEW_CAR);
                }
            });

            return view;
        }

        class ViewHolder{
            protected SmartImageView smartImageView;
            protected TextView tv_name;
            protected TextView tv_detail;
            protected LinearLayout row_item_car;
        }

        public void updateResults(List<Car> results) {
            carList = results;
            notifyDataSetChanged();
        }
    }

    private void showMessage(final String message) {
        Snackbar.make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menuFilter:
                toolbarFilter = findViewById(R.id.toolbar_filter);
                if (toolbarFilter.getVisibility() == View.VISIBLE) {
                    toolbarFilter.setVisibility(View.GONE);
                    if (idBrand >= 0)
                        loadCatalog();
                    idBrand = -1;
                    idSerie = -1;
                    idSubserie = -1;
                    sp_car_brand.setSelection(0);
                    sp_car_serie.setSelection(0);
                    sp_car_subserie.setSelection(0);
                } else {
                    toolbarFilter.setVisibility(View.VISIBLE);
                    generateBrandsSpinner();
                    clickOnFilterButton = 0;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void generateBrandsSpinner() {

        List<Brand> brands = new ArrayList<>();
        brandList = dataBaseManager.getBrands();
        brands.add(new Brand(-1,"Fabricantes (Todos)", null));
        brands.add(new Brand(0,"Desconocido", null));
        if (brandList != null)
            brands.addAll(brandList);
        brandAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, brands);

        sp_car_brand.setAdapter(brandAdapter);

        sp_car_brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Brand brand = (Brand) sp_car_brand.getSelectedItem();
                idBrand = brand.getId();
                if (idBrand > 0) {
                    sp_car_serie.setVisibility(View.VISIBLE);
                    generateSeriesSpinner(idBrand);
                } else {
                    sp_car_serie.setVisibility(View.GONE);
                    sp_car_subserie.setVisibility(View.GONE);
                    idSerie = -1;
                    idSubserie = -1;
                }
                if (clickOnFilterButton > 0)
                    loadCatalog();
                clickOnFilterButton++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void generateSeriesSpinner(final int idBrand) {
        seriesList = dataBaseManager.getSeries(idBrand);
        List<Serie> series = new ArrayList<>();
        series.add(new Serie(-1,"Series (Todas)"));
        series.add(new Serie(0,"Desconocido"));
        if (seriesList != null)
            series.addAll(seriesList);
        seriesAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, series);
        sp_car_serie.setAdapter(seriesAdapter);

        sp_car_serie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Serie serie = (Serie) sp_car_serie.getSelectedItem();
                idSerie = serie.getId();
                if (idSerie > 0) {
                    sp_car_subserie.setVisibility(View.VISIBLE);
                    generateSubserieSpinner(idSerie);
                } else {
                    sp_car_subserie.setVisibility(View.GONE);
                    idSubserie = -1;
                }
                loadCatalog();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void generateSubserieSpinner(final int idSerie) {
        subseriesList = dataBaseManager.getSubseries(idSerie);
        List<Serie> subseries = new ArrayList<>();
        subseries.add(new Serie(-1,"Subseries (Todas)"));
        subseries.add(new Serie(0,"Desconocido"));
        if (subseriesList != null)
            subseries.addAll(subseriesList);
        subseriesAdapter = new DiecastArrayAdapter(this, R.layout.spinner_row, subseries);
        sp_car_subserie.setAdapter(subseriesAdapter);

        sp_car_subserie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Serie serie = (Serie) sp_car_subserie.getSelectedItem();
                idSubserie = serie.getId();
                loadCatalog();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadCatalog() {
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        new LoadCatalogTask().execute();
    }

    private class LoadCatalogTask extends AsyncTask<Void, Void, Void> {

        List<Car> cars;

        @Override
        protected Void doInBackground(Void... voids) {
            cars = dataBaseManager.getListCars(querySearch, idBrand,idSerie,idSubserie,true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(ACTIVITY_NAME, "Se obtuvo " + cars.size() + " registros.");
            adapter.updateResults(cars);
            tv_cars_count.setText(getCountCars(cars) + " Autos");
            getSupportActionBar().setSubtitle(getCountCars(cars)+ " Autos");
            progressDialog.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VIEW_CAR:
                case SAVE_CAR:
                    loadCatalog();
                    break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
            searchView.setQuery(query, false);
            searchView.clearFocus();

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String uri = intent.getDataString();
            Toast.makeText(this, "Suggestion: "+ uri, Toast.LENGTH_LONG).show();
        }
    }

}
