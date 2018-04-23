package diecast.fozil.com.diecast;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;

import databases.Car;
import databases.DataBaseManager;

//import com.facebook.share.model.ShareLinkContent;

public class CarDetail extends AppCompatActivity {

    ImageView imageView;
    TextView carName;
    TextView carBrandName;
    TextView carCreatedAt;
    TextView carCount;
    TextView carSerie;
    TextView carSubserie;

    public static final String ACTIVITY_NAME = "CAR_DETAIL_ACTIVITY";
    private DataBaseManager dataBaseManager;
    Car car;
    int idCar;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        dataBaseManager = new DataBaseManager(this);

        carName = (TextView) findViewById(R.id.detail_name);
        carBrandName = (TextView) findViewById(R.id.detail_brand);
        carCreatedAt = (TextView) findViewById(R.id.detail_created_at);
        carCount = (TextView) findViewById(R.id.detail_count);
        carSerie = (TextView) findViewById(R.id.detail_serie);
        carSubserie = (TextView) findViewById(R.id.detail_subserie);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                showSnackBar("Imagen publicada en Facebook exitosamente");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                showSnackBar("Error: " + error);
            }
        });

        setToolBar();

        imageView = (ImageView) findViewById(R.id.image_paralax);

        Intent intent = getIntent();
        idCar = intent.getIntExtra("id_car", 0);

        car = dataBaseManager.getCar(idCar);

        carName.setText(car.getName());

        if (car.getBrand() != null)
            carBrandName.setText(car.getBrand().getName());
        else
            carBrandName.setText(TextsDefault.UNKNOWN.getText());


        carCreatedAt.setText(car.getCreatedAt().substring(0, car.getCreatedAt().length() - 3));
        carCount.setText(car.getCount()+"");
        if (car.getSerie() != null) {
            if (car.getSerie().getParent() != null && car.getSerie().getParent().getId() > 0) {
                carSerie.setText(dataBaseManager.getSerieById(car.getSerie().getParent().getId()).getName());
                carSubserie.setText(car.getSerie().getName());
            } else {
                carSerie.setText(car.getSerie().getName());
                carSubserie.setText(TextsDefault.UNKNOWN.getText());
            }
        } else {
            carSerie.setText(TextsDefault.UNKNOWN.getText());
            carSubserie.setText(TextsDefault.UNKNOWN.getText());
        }


        CollapsingToolbarLayout collapser = (CollapsingToolbarLayout) findViewById(R.id.collapser);
        collapser.setTitle(car.getName());

        loadImageParallax(car.getImage());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarDetail.this, ImageFullScreen.class);
                intent.putExtra("image_url", car.getImage());
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareMenu();
            }
        });
    }


    public void deleteCar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CarDetail .this, R.style.myDialog);
        builder.setCancelable(true);
        builder.setTitle("Eliminar registro");
        builder.setMessage("¿Estas seguro de querer eliminar el registro del catalogo?");
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(ACTIVITY_NAME, "Elimina ID_CAR = " + idCar);
                        dataBaseManager.deleteCar(idCar);
                        Intent intent = new Intent(CarDetail.this, MainActivity.class);
                        startActivity(intent);
                        finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
            case R.id.action_favorite:
                showSnackBar("Añadir a favoritos");
                 return true;
            case R.id.action_delete:
                deleteCar();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(CarDetail.this, AddCars.class);
                intent.putExtra("id_car", car.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSnackBar(final String message) {
        Snackbar.make(findViewById(R.id.coordinator), message, Snackbar.LENGTH_LONG).show();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void loadImageParallax(final String imageURL) {
        if (imageURL != null && !imageURL.equals("")) {
            File image = new File(imageURL);
            Uri photoUri = FileProvider.getUriForFile(CarDetail.this, BuildConfig.APPLICATION_ID + ".provider", image);
            Glide.with(this).load(photoUri).fitCenter().centerCrop().into(imageView);
        } else {
            /*int id = getResources().getIdentifier("noimage", "drawable", getPackageName());
            Glide.with(this).load(id).fitCenter().centerCrop().into(imageView);*/
        }
    }

    private void showShareMenu() {
        final String facebookOption = "Facebook";
        final String whatsappOption = "Enviar imágen a ...";
        final String cancelOption = "Cancelar";
        final CharSequence[] options = {facebookOption, whatsappOption, cancelOption};
        final AlertDialog.Builder builder = new AlertDialog.Builder(CarDetail.this);
        builder.setTitle("Compartir en");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which] == facebookOption) {

                    String imageURL = car.getImage();
                    File image = new File(imageURL);
                    Uri photoUri = FileProvider.getUriForFile(CarDetail.this, BuildConfig.APPLICATION_ID + ".provider", image);
                    SharePhoto photo = new SharePhoto.Builder().setImageUrl(photoUri).build();

                    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();

                    if (ShareDialog.canShow(SharePhotoContent.class)) {
                        shareDialog.show(content);
                    }
/*
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse("http://fozilgarage.blogspot.com"))
                                .build();
                        shareDialog.show(linkContent);
                    }
*/
                } else if(options[which] == whatsappOption) {
                    compartirImagen();
                } else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void compartirImagen() {

        String imageURL = car.getImage();
        File image = new File(imageURL);
        Uri photoUri = FileProvider.getUriForFile(CarDetail.this, BuildConfig.APPLICATION_ID + ".provider", image);
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, photoUri);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Selecciona aplicación para compartir"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
