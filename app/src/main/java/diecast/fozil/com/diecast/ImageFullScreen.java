package diecast.fozil.com.diecast;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

public class ImageFullScreen extends AppCompatActivity {

    public static final String EXTRA_SPACE_PHOTO = "ImageFullScreen.SPACE_PHOTO";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);

        mImageView = (ImageView) findViewById(R.id.iv_image_fullscreen);

        Intent intent = getIntent();
        String imageURL = intent.getStringExtra("image_url");

        if (imageURL != null && !imageURL.equals("")) {
            File image = new File(imageURL);
            Uri photoUri = FileProvider.getUriForFile(ImageFullScreen.this, BuildConfig.APPLICATION_ID + ".provider", image);
            Glide.with(this).load(photoUri)
                    .error(R.drawable.photo_not_available)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImageView);
        }

        /*
        FloatingActionButton btnClose = (FloatingActionButton) findViewById(R.id.btn_close_fullscreen);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
    }
}
