package com.universl.hp.hithatawadinawadan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.universl.hp.hithatawadinawadan.Main.HomeActivity;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    List<QuotesResponse> quotesResponseList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        product();
        /*int SPLASH_DISPLAY_LENGTH = 4000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);*/
    }
    private void product(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("StaticFieldLeak")
                class Network extends AsyncTask<Void,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {

                        quotesResponseList = new ArrayList<>();
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            final JSONArray product = new JSONArray(response);
                                            for (int i = 0; i < product.length(); i++){
                                                JSONObject productObject = product.getJSONObject(i);
                                                if (productObject.getString("status")
                                                        .equalsIgnoreCase("true") &&
                                                        !productObject.getString("category").
                                                                equalsIgnoreCase("Fans Quotes")){
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
                                        Toast.makeText(SplashActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                new Network().execute();
            }
        }, 4000);
    }
}
