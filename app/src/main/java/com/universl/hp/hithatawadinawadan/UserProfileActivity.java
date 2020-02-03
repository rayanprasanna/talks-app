package com.universl.hp.hithatawadinawadan;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.universl.hp.hithatawadinawadan.Adapter.FansQuotesAdapter;
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private List<QuotesResponse> fansQuotesResponses,fansQuotesResponsesPhoto;
    private ArrayList<String> image_path;
    private FansQuotesAdapter quotesAdapter;
    private ProgressDialog progress;
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
        setContentView(R.layout.activity_user_profile);

        fansQuotesResponses = new ArrayList<>();
        fansQuotesResponsesPhoto = new ArrayList<>();
        image_path = new ArrayList<>();
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progress = new ProgressDialog(UserProfileActivity.this);
        ListView listView = findViewById(R.id.quotes_list);
        final SearchView searchView = findViewById(R.id.search);
        loadFansProduct();
        Bundle bundle = getIntent().getBundleExtra("profile");

        List<QuotesResponse> responses = (List<QuotesResponse>) bundle.getSerializable("quotes");
        if (responses != null) {
            responses.size();
        }
        quotesAdapter = new FansQuotesAdapter(UserProfileActivity.this, (List<QuotesResponse>) bundle.getSerializable("quotes"),
                (List<QuotesResponse>) bundle.getSerializable("favorite_quotes"),bundle.getStringArrayList("image_path"));


        listView.setAdapter(quotesAdapter);
        searchView.setOnQueryTextListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(fansQuotesResponses.get(position).title,true);
            }
        });
    }
    private void loadFansProduct(){
        @SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<Void,Void,Void>{

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
            protected Void doInBackground(Void... voids) {
                StringRequest request = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    final JSONArray fansProduct = new JSONArray(response);
                                    for (int i = 0;i < fansProduct.length(); i++){
                                        JSONObject productObject = fansProduct.getJSONObject(i);
                                        if (productObject.getString("category").
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
                                            fansQuotesResponses.add(quotes);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfileActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                Volley.newRequestQueue(UserProfileActivity.this).add(request);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_FAVORITE_QUOTES_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    final JSONArray product = new JSONArray(response);
                                    product.length();
                                    for (int i = 0;i < product.length(); i++){
                                        JSONObject productObject = product.getJSONObject(i);
                                        String user_id = productObject.getString("user_id");
                                        String photo = productObject.getString("photo");

                                        QuotesResponse quotes = new QuotesResponse(photo,user_id);
                                        fansQuotesResponsesPhoto.add(quotes);
                                        image_path.add(photo);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfileActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                Volley.newRequestQueue(UserProfileActivity.this).add(stringRequest);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progress.dismiss();
            }
        }new Network().execute();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            Intent intent = new Intent(UserProfileActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UserProfileActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        quotesAdapter.filter(newText);
        return false;
    }
}
