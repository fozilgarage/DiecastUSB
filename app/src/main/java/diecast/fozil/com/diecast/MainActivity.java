package diecast.fozil.com.diecast;

import android.app.ProgressDialog;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.snowdream.android.widget.SmartImageView;

import java.io.File;
import java.util.List;

import databases.Car;
import databases.DataBaseManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTIVITY_NAME = "MAIN_ACTIVITY";
    private DataBaseManager dataBaseManager;

    ListView listView;
    CatalogAdapter adapter;
    String querySearch;
    TextView tv_cars_count;
    LinearLayout ll_search_detail;
    DrawerLayout main_layout;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = setToolBar("Mi Garage");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        String message = getIntent().getStringExtra("message");
        if (message != null && !message.equals(""))
            showMessage(message);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dataBaseManager = new DataBaseManager(this);

        listView = (ListView) findViewById(R.id.lv_catalogo);
        tv_cars_count = (TextView) findViewById(R.id.tv_cars_count);
        main_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ll_search_detail = (LinearLayout) findViewById(R.id.ll_search_detail);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               loadCatalog();
               swipeRefreshLayout.setRefreshing(false);
            }
        });
        loadCatalog();

    }

    private Toolbar setToolBar(final String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);

        return toolbar;
    }

    private void loadCatalog() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        try {
            List<Car> cars = dataBaseManager.getListCars(true);
            Log.i(ACTIVITY_NAME, "Se obtuvo " + cars.size() + " registros.");
            adapter = new CatalogAdapter(getApplicationContext(), cars);
            listView.setAdapter(adapter);
            tv_cars_count.setText(getCountCars(cars) + " Autos");
            getSupportActionBar().setSubtitle(getCountCars(cars)+ " Autos");
        } catch (Exception e) {
            Log.e(ACTIVITY_NAME, e.getMessage());
            Snackbar.make(main_layout, "Error al obtner datos: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }

        progressDialog.hide();
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
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        final MenuItem menuItem = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setQueryHint("Buscar por nombre");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //doSearch(query);
                //searchView.setQuery("", false);
                //searchView.setIconified(true);
                if(query.equals(""))
                    ll_search_detail.setVisibility(View.GONE);
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

        });

        return super.onCreateOptionsMenu(menu);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            cars = dataBaseManager.getListCars(querySearch, true);
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
            tv_cars_count.setText(size + " Autos");
        }

    }

    private class CatalogAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        private List<Car> carList;

        public CatalogAdapter(Context applicationContext, List<Car> cars) {
            layoutInflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            carList = cars;
        }

        @Override
        public int getCount() {
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

            ViewHolder holder;

            if (view == null) {
                holder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.activity_main_item, viewGroup, false);

                holder.smartImageView = (SmartImageView) view.findViewById(R.id.imagen1);
                holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
                holder.tv_detail = (TextView) view.findViewById(R.id.tv_detail);
                holder.row_item_car = (LinearLayout) view.findViewById(R.id.row_item_car);

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
            detail.append(createdAt.substring(0, createdAt.length() - 3));
            int count = carList.get(position).getCount();
            String item = " auto";
            if (count > 1)
                item = " autos";
            detail.append("\n").append(count + item);

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
                    startActivity(intent);
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
}
