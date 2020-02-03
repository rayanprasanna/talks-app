package com.universl.hp.hithatawadinawadan.Main.sub_activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.hp.hithatawadinawadan.Main.HomeActivity;
import com.universl.hp.hithatawadinawadan.Main.adapter.ProfileAdapter;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Main.util.AppController;
import com.universl.hp.hithatawadinawadan.Main.util.MyApplication;
import com.universl.hp.hithatawadinawadan.MainActivity;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Util.Constant;

/*import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;*/
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class QuotesUserProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    List<QuotesResponse> quotesResponseList, quotesResponseList_photo,getQuotesResponseList;
    ArrayList<String> image_path;
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
    ProfileAdapter profileAdapter;
    ListView listView;
    SearchView searchView;
    private ProgressDialog progress;
    private DatabaseReference databaseQuotes;
    private AdView adView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes_user_profile);

        lordProduct();
        lordReviewProduct();

        /*ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color='#000000'>Me</font>"));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#000000'>Me</font>"));

        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
        quotesResponseList_photo = new ArrayList<>();
        image_path = new ArrayList<>();

        listView = findViewById(R.id.quotes_list);
        //searchView = findViewById(R.id.search_title);

        relativeLayout = findViewById(R.id.activity_main);
        context = getApplicationContext();
        activity = QuotesUserProfileActivity.this;

        FloatingTextButton user = findViewById(R.id.user_button);
        FloatingTextButton review = findViewById(R.id.review_button);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
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
                        Toast.makeText(QuotesUserProfileActivity.this, "Something went wrong !", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileAdapter = new ProfileAdapter(QuotesUserProfileActivity.this, quotesResponseList, quotesResponseList_photo, image_path);
                listView.setAdapter(profileAdapter);
            }
        });

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileAdapter = new ProfileAdapter(QuotesUserProfileActivity.this, getQuotesResponseList);
                listView.setAdapter(profileAdapter);
            }
        });
        String profile = getIntent().getStringExtra("profile");
        if (profile.equalsIgnoreCase("upload")){
            user.setVisibility(View.VISIBLE);
            review.setVisibility(View.VISIBLE);
            profileAdapter = new ProfileAdapter(QuotesUserProfileActivity.this, getQuotesResponseList);
            listView.setAdapter(profileAdapter);
        }else {
            user.setVisibility(View.VISIBLE);
            review.setVisibility(View.VISIBLE);
            profileAdapter = new ProfileAdapter(QuotesUserProfileActivity.this, quotesResponseList, quotesResponseList_photo, image_path);
            listView.setAdapter(profileAdapter);
        }

        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSearch(newText);
                return false;
            }
        });*/
        initAds();
    }

    //Ads
    private void initAds() {
        adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if ((progress != null) && progress.isShowing())
            progress.dismiss();
        progress = null;
        databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quotesResponseList_photo.clear();
                image_path.clear();
                //quotesResponseList.clear();
                for (DataSnapshot quotesSnapshot : dataSnapshot.getChildren()) {
                    QuotesResponse quotes = quotesSnapshot.getValue(QuotesResponse.class);

                    quotesResponseList_photo.add(quotes);
                }
                for (int i = 0; i < quotesResponseList_photo.size(); i++) {
                    image_path.add(quotesResponseList_photo.get(i).photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @SuppressLint("InflateParams")
    private void open_Popup_window() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = null;
        if (inflater != null) {
            customView = inflater.inflate(R.layout.popup_window, null);
        }

        popupWindow = new PopupWindow(
                customView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
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
                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER_HORIZONTAL, 0, 0);
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
                            Toast.makeText(QuotesUserProfileActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void lordProduct() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            user_name = account.getGivenName();
        }
        progress = new ProgressDialog(QuotesUserProfileActivity.this);
        quotesResponseList = new ArrayList<>();
        /*@SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<String,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setTitle(getString(R.string.app_name));
                progress.setMessage("Data is Downloading !");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(true);
                progress.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(com.universl.hp.hithatawadinawadan.Main.util.Constant.GET_QUOTES_URL);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();

                    return client.execute(httpget, responseHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "Data Download Successfully!";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                quotesResponseList = new ArrayList<>();
                quotesResponseList = new Gson().fromJson(s, new TypeToken<List<QuotesResponse>>() {
                }.getType());
                for (int i = 0; i < quotesResponseList.size();i++){
                    if (quotesResponseList.get(i).status.equalsIgnoreCase("true")
                            && quotesResponseList.get(i).category.equalsIgnoreCase("Fans Quotes")
                            && quotesResponseList.get(i).user_name.equalsIgnoreCase(user_name)){
                        quotesResponseList.remove(i);
                        quotesResponseList.add(quotesResponseList.get(i));
                    }
                }
                if ((progress != null) && progress.isShowing()) {
                    progress.dismiss();
                }
            }

        }new Network().execute();*/
        /*StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONArray product = new JSONArray(response);
                            for (int i = 0; i < product.length(); i++) {
                                JSONObject productObject = product.getJSONObject(i);
                                if (productObject.getString("status")
                                        .equalsIgnoreCase("true") &&
                                        productObject.getString("category").
                                                equalsIgnoreCase("Fans Quotes")
                                        && productObject.getString("user_name").equalsIgnoreCase(user_name)) {
                                    String category = productObject.getString("category");
                                    String title = productObject.getString("title");
                                    String date = productObject.getString("date");
                                    String photo = productObject.getString("photo");
                                    String user_name = productObject.getString("user_name");
                                    String status = productObject.getString("status");

                                    QuotesResponse quotes = new QuotesResponse(category, title, date, photo, user_name, status);
                                    quotesResponseList.add(quotes);
                                }
                            }
                            quotesResponseList.size();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(QuotesUserProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        Volley.newRequestQueue(this).add(stringRequest);*/
        JsonArrayRequest request = new JsonArrayRequest(Constant.GET_QUOTES_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<QuotesResponse> items = new Gson().fromJson(response.toString(), new TypeToken<List<QuotesResponse>>() {
                        }.getType());

                        // adding contacts to contacts list
                        quotesResponseList.clear();
                        for (int i = 0; i < items.size();i++){
                            if (items.get(i).status.equalsIgnoreCase("true")
                                    && items.get(i).category.equalsIgnoreCase("Fans Quotes")
                                    && items.get(i).user_name.equalsIgnoreCase(user_name)){
                                quotesResponseList.add(items.get(i));
                            }
                        }
                        quotesResponseList.size();

                        // refreshing recycler view
                        profileAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void lordReviewProduct() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            user_name = account.getGivenName();
        }
        getQuotesResponseList = new ArrayList<>();
        /*progress = new ProgressDialog(QuotesUserProfileActivity.this);
        quotesResponseList = new ArrayList<>();
        @SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<String,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setTitle(getString(R.string.app_name));
                progress.setMessage("Data is Downloading !");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(true);
                progress.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(com.universl.hp.hithatawadinawadan.Main.util.Constant.GET_QUOTES_URL);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();

                    return client.execute(httpget, responseHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "Data Download Successfully!";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                quotesResponseList = new ArrayList<>();
                quotesResponseList = new Gson().fromJson(s, new TypeToken<List<QuotesResponse>>() {
                }.getType());
                for (int i = 0; i < quotesResponseList.size();i++){
                    if (quotesResponseList.get(i).status.equalsIgnoreCase("false")
                            && quotesResponseList.get(i).category.equalsIgnoreCase("Fans Quotes")
                            && quotesResponseList.get(i).user_name.equalsIgnoreCase(user_name)){
                        quotesResponseList.remove(i);
                        quotesResponseList.add(quotesResponseList.get(i));
                    }
                }
                if ((progress != null) && progress.isShowing()) {
                    progress.dismiss();
                }
            }

        }new Network().execute();*/
        /*StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONArray product = new JSONArray(response);
                            for (int i = 0; i < product.length(); i++) {
                                JSONObject productObject = product.getJSONObject(i);
                                if (productObject.getString("status")
                                        .equalsIgnoreCase("False") &&
                                        productObject.getString("category").
                                                equalsIgnoreCase("Fans Quotes")
                                        && productObject.getString("user_name").equalsIgnoreCase(user_name)) {
                                    String category = productObject.getString("category");
                                    String title = productObject.getString("title");
                                    String date = productObject.getString("date");
                                    String photo = productObject.getString("photo");
                                    String user_name = productObject.getString("user_name");
                                    String status = productObject.getString("status");

                                    QuotesResponse quotes = new QuotesResponse(category, title, date, photo, user_name, status);
                                    getQuotesResponseList.add(quotes);
                                }
                            }
                            getQuotesResponseList.size();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(QuotesUserProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        Volley.newRequestQueue(this).add(stringRequest);*/
        String tag_json_arry = "json_array_req";
        JsonArrayRequest request = new JsonArrayRequest("http://topaapps.com/vadan/Rayan/get_quotes.php",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<QuotesResponse> items = new Gson().fromJson(response.toString(), new TypeToken<List<QuotesResponse>>() {
                        }.getType());

                        // adding contacts to contacts list
                        //getQuotesResponseList.clear();
                        items.size();
                        for (int i = 0; i < items.size();i++){
                            if (items.get(i).status.equalsIgnoreCase("False")
                                    && items.get(i).category.equalsIgnoreCase("Fans Quotes")
                                    && items.get(i).user_name.equalsIgnoreCase(user_name)){
                                getQuotesResponseList.add(items.get(i));
                            }
                        }
                        getQuotesResponseList.size();
                        // refreshing recycler view
                        profileAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })/*{
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 100;
                    final long cacheExpired = 100;
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null){
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null){
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString),cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            protected void deliverResponse(JSONArray response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        }*/;
        //Volley.newRequestQueue(this).add(request);
        AppController.getInstance().getRequestQueue().getCache().remove(com.universl.hp.hithatawadinawadan.Main.util.Constant.GET_QUOTES_URL);
        AppController.getInstance().addToRequestQueue(request, tag_json_arry);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                getSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                getSearch(query);
                return false;
            }
        });
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                Toast.makeText(QuotesUserProfileActivity.this, "Auth went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void getSearch(String query){
        List<QuotesResponse> filtered_output = new ArrayList<>();

        if (searchView != null){
            for (QuotesResponse item : quotesResponseList){
                if (item.title.toLowerCase().startsWith(query.toLowerCase()))
                    filtered_output.add(item);
            }
        }else
            filtered_output = quotesResponseList;

        profileAdapter = new ProfileAdapter(QuotesUserProfileActivity.this,filtered_output,quotesResponseList_photo,image_path);
        listView.setAdapter(profileAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.power) {
            logout();
            return true;
        }
        if (id == R.id.profile){
            Intent intent = new Intent(QuotesUserProfileActivity.this,QuotesUserProfileActivity.class);
            intent.putExtra("profile","uploaded");
            startActivity(intent);
            finish();
        }
        if (id == android.R.id.home){
            Intent intent = new Intent(QuotesUserProfileActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        System.exit(0);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(QuotesUserProfileActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
