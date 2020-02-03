package com.universl.hp.hithatawadinawadan.Main.sub_activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.universl.hp.hithatawadinawadan.Main.FansActivity;
import com.universl.hp.hithatawadinawadan.Main.HomeActivity;
import com.universl.hp.hithatawadinawadan.Main.OtherActivity;
import com.universl.hp.hithatawadinawadan.Main.RomanticActivity;
import com.universl.hp.hithatawadinawadan.Main.SuccessActivity;
import com.universl.hp.hithatawadinawadan.Main.adapter.FansAdapter;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.MainActivity;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.UploadQuotesActivity;
import com.universl.hp.hithatawadinawadan.Util.DetailsActivity;
import com.universl.hp.hithatawadinawadan.Util.GlideApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class QuotesDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    WallpaperManager wallpaperManager ;
    List<QuotesResponse> quotesResponseList_photo;
    ArrayList<String> image_path;
    TextView count;
    ImageView favorite;
    private DatabaseReference databaseQuotes;
    Bitmap bitmap;String photoPath;int n;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes_details);

        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color='#000000'>Selected Quotes</font>"));

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
        count = findViewById(R.id.count);
        final ImageView imageView = findViewById(R.id.quotes_image);
        final FloatingActionButton upload,share;
        TextView user_name = findViewById(R.id.user_name);
        final ImageView wall = findViewById(R.id.quotes_wall);
        upload = findViewById(R.id.wallpaper);
        share = findViewById(R.id.share);
        favorite = findViewById(R.id.favorite);
        wallpaperManager  = WallpaperManager.getInstance(getApplicationContext());

        image_path = new ArrayList<>();
        quotesResponseList_photo = new ArrayList<>();

        user_name.setText(getIntent().getStringExtra("quotes_user_name"));
        GlideApp.with(getApplicationContext()).load(getIntent().getStringExtra("quotes_image")).fitCenter().into(imageView);
        count.setText(getIntent().getStringExtra("count"));
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuotesDetailsActivity.this,QuotesUploadActivity.class);
                startActivity(intent);
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bitmap = getBitmapFromView(imageView);
                requestStoragePermission();
            }
        });
    }

    private void startShare(Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        shareScreenshot();
    }
    private void shareScreenshot()
    {
        photoPath = Environment.getExternalStorageDirectory() + "/saved_images" + "/Image-" + n + ".jpg";
        File F = new File(photoPath);

        // TODO your package name as well add .fileprovider
        Uri U = FileProvider.getUriForFile(getApplicationContext(), "com.universl.hp.hithatawadinawadan.fileprovider", F);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/png");
        i.putExtra(Intent.EXTRA_TEXT, "# Hithata Wadina Vadan " +"\n"+ "https://play.google.com/store/apps/details?id=com.universl.hp.hithatawadinawadan");
        i.putExtra(Intent.EXTRA_STREAM, U);
        startActivityForResult(Intent.createChooser(i, "share via"), 1);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
    @SuppressLint("InlinedApi")
    private void requestStoragePermission(){
        String[] permissions = {WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startShare(bitmap);
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    STORAGE_PERMISSION_CODE);
            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(QuotesDetailsActivity.this,"Permission granted",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(QuotesDetailsActivity.this,"Permission not granted",Toast.LENGTH_LONG).show();
            }
        }
    }
    private void uploadFavorite(String image_path){
        String id = databaseQuotes.push().getKey();
        com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse response =
                new com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse(id,image_path);
        assert id != null;
        databaseQuotes.child(id).setValue(response);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("home")){
                Intent intent = new Intent(QuotesDetailsActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("fans")){
                Intent intent = new Intent(QuotesDetailsActivity.this,FansActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("success")){
                Intent intent = new Intent(QuotesDetailsActivity.this, SuccessActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("romantic")){
                Intent intent = new Intent(QuotesDetailsActivity.this, RomanticActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("other")){
                Intent intent = new Intent(QuotesDetailsActivity.this, OtherActivity.class);
                startActivity(intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("home")){
            Intent intent = new Intent(QuotesDetailsActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("fans")){
            Intent intent = new Intent(QuotesDetailsActivity.this,FansActivity.class);
            startActivity(intent);
            finish();
        }
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("success")){
            Intent intent = new Intent(QuotesDetailsActivity.this, SuccessActivity.class);
            startActivity(intent);
            finish();
        }
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("romantic")){
            Intent intent = new Intent(QuotesDetailsActivity.this, RomanticActivity.class);
            startActivity(intent);
            finish();
        }
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("other")){
            Intent intent = new Intent(QuotesDetailsActivity.this, OtherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
