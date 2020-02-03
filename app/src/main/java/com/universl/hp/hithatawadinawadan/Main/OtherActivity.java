package com.universl.hp.hithatawadinawadan.Main;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.hp.hithatawadinawadan.Main.adapter.OtherAdapter;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesUploadActivity;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesUserProfileActivity;
import com.universl.hp.hithatawadinawadan.Main.util.AppController;
import com.universl.hp.hithatawadinawadan.Main.util.MyApplication;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtherActivity extends AppCompatActivity {

    private ProgressDialog progress;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ArrayList<String> image_path;
    ListView listView;
    SearchView searchView;
    OtherAdapter otherAdapter;
    List<QuotesResponse> quotesResponseList,quotesResponseList_photo;
    private DatabaseReference databaseQuotes;
    private AdView adView;

    @Override
    protected void onPause() {
        super.onPause();
        if ((progress != null) && progress.isShowing())
            progress.dismiss();
        progress = null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        /*ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color='#000000'>Other Quotes</font>"));*/
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#000000'>Other Quotes</font>"));
        lordProduct();
        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        listView = findViewById(R.id.quotes_list);
        //searchView = findViewById(R.id.search_title);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        FloatingActionButton share = findViewById(R.id.share);

        quotesResponseList_photo = new ArrayList<>();
        image_path = new ArrayList<>();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherActivity.this,QuotesUploadActivity.class);
                startActivity(intent);
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, "# Hithata Wadina Vadan");
                share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.universl.hp.hithatawadinawadan");

                startActivity(Intent.createChooser(share, "Share link!"));
            }
        });
        otherAdapter = new OtherAdapter(OtherActivity.this,quotesResponseList,quotesResponseList_photo,image_path);
        listView.setAdapter(otherAdapter);

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
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_home:
                        Intent home_intent = new Intent(OtherActivity.this,HomeActivity.class);
                        startActivity(home_intent);
                        finish();
                        return true;
                    case R.id.navigation_romantic:
                        Intent romantic_intent = new Intent(OtherActivity.this,RomanticActivity.class);
                        startActivity(romantic_intent);
                        finish();
                        return true;
                    case R.id.navigation_success:
                        Intent success_intent = new Intent(OtherActivity.this,SuccessActivity.class);
                        startActivity(success_intent);
                        finish();
                        return true;
                    case R.id.navigation_other:
                        Intent other_intent = new Intent(OtherActivity.this,OtherActivity.class);
                        startActivity(other_intent);
                        finish();
                        return true;
                    case R.id.navigation_fans:
                        Intent fans_intent = new Intent(OtherActivity.this,FansActivity.class);
                        startActivity(fans_intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
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

    @Override
    protected void onStart() {
        super.onStart();
        databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quotesResponseList_photo.clear();
                image_path.clear();
                //quotesResponseList.clear();
                for (DataSnapshot quotesSnapshot : dataSnapshot.getChildren()){
                    QuotesResponse quotes = quotesSnapshot.getValue(QuotesResponse.class);

                    quotesResponseList_photo.add(quotes);
                }
                for (int i = 0; i < quotesResponseList_photo.size(); i++){
                    image_path.add(quotesResponseList_photo.get(i).photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.power) {
            logout();
            return true;
        }
        if (id == R.id.profile){
            Intent intent = new Intent(OtherActivity.this,QuotesUserProfileActivity.class);
            intent.putExtra("profile","uploaded");
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
    private void lordProduct(){
        quotesResponseList = new ArrayList<>();
        /*StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONArray product = new JSONArray(response);
                            for (int i = 0; i < product.length(); i++){
                                JSONObject productObject = product.getJSONObject(i);
                                if (productObject.getString("status")
                                        .equalsIgnoreCase("true") &&
                                        productObject.getString("category").
                                                equalsIgnoreCase("Other Quotes")){
                                    String category = productObject.getString("category");
                                    String title = productObject.getString("title");
                                    String date = productObject.getString("date");
                                    String photo = productObject.getString("photo");
                                    String user_name = productObject.getString("user_name");
                                    String status = productObject.getString("status");

                                    QuotesResponse quotes = new QuotesResponse(category,title,date,photo,user_name,status);
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
                        Toast.makeText(OtherActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
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
                                    && items.get(i).category.equalsIgnoreCase("Other Quotes")){
                                quotesResponseList.add(items.get(i));
                            }
                        }

                        // refreshing recycler view
                        otherAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().getRequestQueue().getCache().remove(com.universl.hp.hithatawadinawadan.Main.util.Constant.GET_QUOTES_URL);
        AppController.getInstance().addToRequestQueue(request);
        deleteCache(OtherActivity.this);
    }
    private static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
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

        otherAdapter = new OtherAdapter(OtherActivity.this,filtered_output,quotesResponseList_photo,image_path);
        listView.setAdapter(otherAdapter);
    }
}
