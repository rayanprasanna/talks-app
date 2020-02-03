package com.universl.hp.hithatawadinawadan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.Constant;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UploadQuotesActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int STORAGE_PERMISSION_CODE = 111;
    private static final int PICK_IMAGE_REQUEST = 1100;
    private ImageView imageView;
    private Uri filePath;
    private EditText title;
    private Context context;
    private Activity activity;
    private RelativeLayout relativeLayout;
    FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mAuth;
    private String TAG = "abc";
    private PopupWindow popupWindow;
    private final int RC_SIGN_IN = 100;
    private String user_name;
    private List<QuotesResponse>quotesResponses,quotesResponses_photo;
    private ArrayList<String> image_path;

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
    
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_quotes);
        requestStoragePermission();
        FloatingActionButton chose_image,chose_send;

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageView = findViewById(R.id.quotes_image);
        title = findViewById(R.id.title);

        chose_image = findViewById(R.id.wallpaper);
        chose_send = findViewById(R.id.send);

        relativeLayout = findViewById(R.id.activity_main);
        context = getApplicationContext();
        activity = UploadQuotesActivity.this;

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
                        Toast.makeText(UploadQuotesActivity.this,"Something went wrong !",Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        chose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Post"),PICK_IMAGE_REQUEST);
            }
        });
        chose_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
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
                            Toast.makeText(UploadQuotesActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

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
        quotesResponses = new ArrayList<>();
        quotesResponses_photo = new ArrayList<>();
        image_path = new ArrayList<>();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null){
            user_name = account.getGivenName();
        }

        @SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<Void,Void,Void>{

            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                String date = mdformat.format(calendar.getTime());
                String path = getPath(filePath);
                String upload_id = UUID.randomUUID().toString();
                try {
                    new MultipartUploadRequest(UploadQuotesActivity.this,upload_id,Constant.UPLOAD_QUOTES_URL)
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

                class Network_2 extends AsyncTask<Void,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        StringRequest request = new StringRequest(Request.Method.GET,
                                Constant.GET_QUOTES_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    final JSONArray product = new JSONArray(response);
                                    StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                            Constant.GET_FAVORITE_QUOTES_URL,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        JSONArray photo_product = new JSONArray(response);
                                                        for (int i = 0;i < photo_product.length(); i++){
                                                            JSONObject productObject = photo_product.getJSONObject(i);
                                                            String user_id = productObject.getString("user_id");
                                                            String photo = productObject.getString("photo");

                                                            QuotesResponse quotes = new QuotesResponse(photo,user_id);
                                                            quotesResponses_photo.add(quotes);
                                                            image_path.add(photo);
                                                        }
                                                        for (int i = 0; i < product.length();i++){
                                                            JSONObject productObject = product.getJSONObject(i);
                                                            if(productObject.getString("category").
                                                                    equalsIgnoreCase("Fans Quotes")){
                                                                String category = productObject.getString("category");
                                                                String title = productObject.getString("title");
                                                                String date = productObject.getString("date");
                                                                String photo = productObject.getString("photo");
                                                                String user_name = productObject.getString("user_name");
                                                                String status = productObject.getString("status");

                                                                QuotesResponse quotes = new QuotesResponse(
                                                                        category,title,date,photo,user_name,status
                                                                );
                                                                quotesResponses.add(quotes);
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                            , new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(UploadQuotesActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Volley.newRequestQueue(UploadQuotesActivity.this).add(stringRequest);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(UploadQuotesActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                        Volley.newRequestQueue(UploadQuotesActivity.this).add(request);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        final AlertDialog.Builder alert = new AlertDialog.Builder(UploadQuotesActivity.this);
                        alert.setTitle("හිතට වදින වදන්");
                        alert.setMessage("Successfully Uploaded!");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                imageView.setImageBitmap(null);
                                title.setText("");
                                quotesResponses.size();
                                Bundle bundle1 = getIntent().getBundleExtra("profile");
                                List<QuotesResponse> quotesResponseList = (List<QuotesResponse>) bundle1.getSerializable("quotes");
                                if (quotesResponseList != null) {
                                    System.out.println("///////////////// " + quotesResponses.size() + " +++++++++++++++++ " + quotesResponseList.size());
                                }
                                Intent intent = new Intent(UploadQuotesActivity.this,UserProfileActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("quotes", (Serializable) quotesResponses);
                                bundle.putSerializable("favorite_quotes", (Serializable) quotesResponses_photo);
                                bundle.putStringArrayList("image_path",image_path);
                                intent.putExtra("profile",bundle);
                                startActivity(intent);
                                finish();
                            }
                        });
                        alert.create().show();
                    }
                }new Network_2().execute();
            }
        }
        new Network().execute();
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
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(UploadQuotesActivity.this,"Permission granted",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(UploadQuotesActivity.this,"Permission not granted",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
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
                Toast.makeText(UploadQuotesActivity.this,"Auth went wrong",Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            Intent intent = new Intent(UploadQuotesActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UploadQuotesActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
