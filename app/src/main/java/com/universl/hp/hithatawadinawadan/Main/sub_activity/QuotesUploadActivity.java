package com.universl.hp.hithatawadinawadan.Main.sub_activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.universl.hp.hithatawadinawadan.Main.HomeActivity;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Main.service.ApiService;
import com.universl.hp.hithatawadinawadan.Main.util.AppController;
import com.universl.hp.hithatawadinawadan.Main.util.Constant;
import com.universl.hp.hithatawadinawadan.Main.util.FileUtils;
import com.universl.hp.hithatawadinawadan.Main.util.InternetConnection;
import com.universl.hp.hithatawadinawadan.Main.util.MyApplication;
import com.universl.hp.hithatawadinawadan.R;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuotesUploadActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int STORAGE_PERMISSION_CODE = 111;
    private static final int PICK_IMAGE_REQUEST = 1100;
    private ImageView imageView;
    private Uri filePath;
    private EditText title;
    private Context context;
    private Activity activity;
    private LinearLayout relativeLayout;
    FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mAuth;
    private String TAG = "abc";
    private PopupWindow popupWindow;
    private final int RC_SIGN_IN = 100;
    private String user_name;
    private AdView adView;
    private ArrayList<Uri> arrayList;
    private View parentView;
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    @SuppressLint("CutPasteId")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes_upload);

        requestStoragePermission();
        FloatingActionButton chose_image,chose_send;
        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color='#000000'>Upload Quotes</font>"));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageView = findViewById(R.id.quotes_image);
        title = findViewById(R.id.title);
        parentView = findViewById(R.id.activity_main);
        chose_image = findViewById(R.id.wallpaper);
        chose_send = findViewById(R.id.send);

        relativeLayout = findViewById(R.id.activity_main);
        context = getApplicationContext();
        activity = QuotesUploadActivity.this;

        arrayList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    open_Popup_window();
                }
            }
        };
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(QuotesUploadActivity.this,"Something went wrong !",Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        chose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Post"),PICK_IMAGE_REQUEST);*/
                if (askForPermission())
                    showChooser();
            }
        });
        chose_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uploadImage();
                //uploadQuotes();
                uploadImageToServer();
            }
        });
        initAds();
    }

    //Ads
    private void initAds() {
        adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @SuppressLint("InflateParams")
    private void open_Popup_window(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = null;
        if (inflater != null) {
            customView = inflater.inflate(R.layout.popup_window,null);
        }

        popupWindow = new PopupWindow(
                customView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }

        SignInButton sign = null;
        ImageView close;
        if (customView != null) {
            sign = customView.findViewById(R.id.sign_in_button);

        }
        if (sign != null) {
            close = customView.findViewById(R.id.close);
            sign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
        }

        findViewById(R.id.activity_main).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER_HORIZONTAL,0,0);
            }
        });
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user_email_address = account.getEmail();
                            // Sign in success, update UI with the signed-in user's information
                            popupWindow.dismiss();
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(QuotesUploadActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private String getPath(Uri uri){
        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
        assert cursor != null;
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,MediaStore.Images.Media._ID + " =? "
                ,new String[]{document_id},null);
        assert cursor != null;
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }
    private void uploadImage() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null){
            user_name = account.getGivenName();
        }

        @SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                String date = mdformat.format(calendar.getTime());
                String path = getPath(filePath);
                String upload_id = UUID.randomUUID().toString();
                try {
                    new MultipartUploadRequest(QuotesUploadActivity.this,upload_id,Constant.UPLOAD_QUOTES_URL)
                            .addFileToUpload(path,"image")
                            .addParameter("name","Ryan")
                            .addParameter("title",title.getText().toString().trim())
                            .addParameter("date",date)
                            .addParameter("status","False")
                            .addParameter("user_name",user_name)
                            .addParameter("category","Fans Quotes")
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2)
                            .startUpload();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                final AlertDialog.Builder alert = new AlertDialog.Builder(QuotesUploadActivity.this);
                alert.setTitle("හිතට වදින වදන්");
                alert.setMessage("Successfully Uploaded!");
                alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageView.setImageBitmap(null);
                        title.setText("");
                        Intent intent = new Intent(QuotesUploadActivity.this,QuotesUserProfileActivity.class);
                        intent.putExtra("profile","upload");
                        startActivity(intent);
                        finish();
                    }
                });
                alert.create().show();

            }
        }
        new Network().execute();
    }
    private void uploadQuotes(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null){
            user_name = account.getGivenName();
        }else {
            user_name = "App User";
        }

        final ProgressDialog progress = new ProgressDialog(QuotesUploadActivity.this);
        progress.setTitle(getString(R.string.app_name));
        progress.setMessage("Data is uploading !");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        String date = mdformat.format(calendar.getTime());
        String path = getPath(filePath);

        SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, Constant.UPLOAD_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progress.dismiss();
                        final AlertDialog.Builder alert = new AlertDialog.Builder(QuotesUploadActivity.this);
                        alert.setTitle("හිතට වදින වදන්");
                        alert.setMessage("Successfully Uploaded. Your photo will be published after review.");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageView.setImageBitmap(null);
                                title.setText("");
                                Intent intent = new Intent(QuotesUploadActivity.this,QuotesUserProfileActivity.class);
                                intent.putExtra("profile","upload");
                                startActivity(intent);
                                finish();
                            }
                        });
                        alert.create().show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        simpleMultiPartRequest.addFile("image",path);
        simpleMultiPartRequest.addStringParam("name","Ryan");
        simpleMultiPartRequest.addStringParam("date",date);
        simpleMultiPartRequest.addStringParam("title",title.getText().toString().trim());
        simpleMultiPartRequest.addStringParam("status","False");
        simpleMultiPartRequest.addStringParam("user_name",user_name);
        simpleMultiPartRequest.addStringParam("category","Fans Quotes");

        AppController.getInstance().addToRequestQueue(simpleMultiPartRequest);
    }
    @SuppressLint("InlinedApi")
    private void requestStoragePermission(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(QuotesUploadActivity.this,"Permission granted",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(QuotesUploadActivity.this,"Permission not granted",Toast.LENGTH_LONG).show();
            }
        }*/
        if (requestCode == Constant.REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                showChooser();
            } else {
                // Permission Denied
                Toast.makeText(QuotesUploadActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            if(data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int currentItem = 0;
                while(currentItem < count) {
                    Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                    currentItem = currentItem + 1;
                    Log.d("Uri Selected", imageUri.toString());
                    try {
                        // Get the file path from the URI
                        String path = FileUtils.getPath(this, imageUri);
                        Log.d("Multiple File Selected", path);

                        arrayList.add(imageUri);
                        filePath = data.getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "File select error", e);
                    }
                }
            } else if(data.getData() != null) {
                //do something with the image (save it to some directory or whatever you need to do with it here)
                final Uri uri = data.getData();
                Log.i(TAG, "Uri = " + uri.toString());
                try {
                    // Get the file path from the URI
                    final String path = FileUtils.getPath(this, uri);
                    Log.d("Single File Selected", path);

                    arrayList.add(uri);
                    filePath = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "File select error", e);
                }
            }
        }
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            }else {
                Toast.makeText(QuotesUploadActivity.this,"Auth went wrong",Toast.LENGTH_LONG).show();
            }
        }
    }
    private boolean askForPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            int hasCallPermission = ContextCompat.checkSelfPermission(QuotesUploadActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                // Ask for permission
                // need to request permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(QuotesUploadActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // explain
                    showMessageOKCancel(
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(QuotesUploadActivity.this,
                                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                            Constant.REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            });
                    // if denied then working here
                } else {
                    // Request for permission
                    ActivityCompat.requestPermissions(QuotesUploadActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constant.REQUEST_CODE_ASK_PERMISSIONS);
                }

                return false;
            } else {
                // permission granted and calling function working
                return true;
            }
        } else {
            return true;
        }
    }
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(QuotesUploadActivity.this);
        final android.support.v7.app.AlertDialog dialog = builder.setMessage("You need to grant access to Read External Storage")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(QuotesUploadActivity.this, android.R.color.holo_blue_light));
                dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(QuotesUploadActivity.this, android.R.color.holo_red_light));
            }
        });

        dialog.show();

    }
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(Objects.requireNonNull(getContentResolver().getType(fileUri))),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
    private void uploadImageToServer(){
        if (InternetConnection.checkConnection(QuotesUploadActivity.this)) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            if (account != null){
                user_name = account.getGivenName();
            }else {
                user_name = "App User";
            }
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://topaapps.com/vadan/Rayan/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //showProgress();

            // create list of file parts (photo, video, ...)
            final List<MultipartBody.Part> parts = new ArrayList<>();

            // create upload service client
            ApiService service = retrofit.create(ApiService.class);

            if (arrayList != null) {
                // create part for file (photo, video, ...)
                for (int i = 0; i < arrayList.size(); i++) {
                    parts.add(prepareFilePart("image"+i, arrayList.get(i)));
                }
            }

            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
            String date = mdformat.format(calendar.getTime());
            // create a map of data to pass along
            RequestBody descriptions = createPartFromString("www.vahanamobileapp.com");
            RequestBody size = createPartFromString(""+parts.size());
            RequestBody status = createPartFromString("False");
            RequestBody dates = createPartFromString(date);
            RequestBody user_names = createPartFromString(user_name);
            RequestBody category = createPartFromString("Fans Quotes");
            RequestBody titles = createPartFromString(title.getText().toString().trim());
            // finally, execute the request
            Call<QuotesResponse> call = service.uploadMultiple(descriptions, size,status,titles,category,dates,user_names, parts);

            final ProgressDialog progress = new ProgressDialog(QuotesUploadActivity.this);
            progress.setTitle(getString(R.string.app_name));
            progress.setMessage("Data is uploading !");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

            call.enqueue(new Callback<QuotesResponse>() {
                @Override
                public void onResponse(@NonNull Call<QuotesResponse> call, @NonNull retrofit2.Response<QuotesResponse> response) {
                    //hideProgress();
                    if(response.isSuccessful()) {
                        /*Toast.makeText(EnglishNewPostAdsActivity.this,
                                "Images successfully uploaded!", Toast.LENGTH_SHORT).show();*/
                        progress.dismiss();
                        final AlertDialog.Builder alert = new AlertDialog.Builder(QuotesUploadActivity.this);
                        alert.setTitle("හිතට වදින වදන්");
                        alert.setMessage("Successfully Uploaded. Your photo will be published after review.");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageView.setImageBitmap(null);
                                title.setText("");
                                Intent intent = new Intent(QuotesUploadActivity.this,QuotesUserProfileActivity.class);
                                intent.putExtra("profile","upload");
                                startActivity(intent);
                                finish();
                            }
                        });
                        alert.create().show();
                        arrayList.clear();

                        title.setText("");
                    } else {
                        Snackbar.make(parentView, R.string.string_some_thing_wrong, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<QuotesResponse> call, @NonNull Throwable t) {
                    //hideProgress();
                    Snackbar.make(parentView, t.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });

        } else {
            //hideProgress();
            Toast.makeText(QuotesUploadActivity.this,
                    R.string.string_internet_connection_not_available, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            Intent intent = new Intent(QuotesUploadActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(QuotesUploadActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
