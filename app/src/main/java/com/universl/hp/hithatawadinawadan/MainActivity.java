package com.universl.hp.hithatawadinawadan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.Constant;
import com.universl.hp.hithatawadinawadan.fragment.FansFragment;
import com.universl.hp.hithatawadinawadan.fragment.HomeFragment;
import com.universl.hp.hithatawadinawadan.fragment.OtherFragment;
import com.universl.hp.hithatawadinawadan.fragment.RomanticFragment;
import com.universl.hp.hithatawadinawadan.fragment.SuccessFragment;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    public ArrayList<QuotesResponse> quotesResponses,
            otherQuotesResponses,romanticQuotesResponses,successQuotesResponses,fansQuotesResponse
            ,quotesResponseArrayList,quotesResponses_photo,getFansQuotesResponse;
    private ProgressDialog progress;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ArrayList<String> image_path;
    private final static String APP_TITLE = "හිතට වදින වදන්";
    private final static String APP_PACKAGE_NAME = "com.universl.hp.myapplication";
    private final static int DAYS_UNTIL_PROMPT = 0;
    private final static int LAUNCH_UNTIL_PROMPT = 0;
    @Override
    protected void onPause() {
        super.onPause();
        if ((progress != null) && progress.isShowing())
            progress.dismiss();
        progress = null;
    }
    private static void app_launched(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("rate_app",0);
        if(sharedPreferences.getBoolean("don't show again",false)){
            return;
        }
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        long launch_count = sharedPreferences.getLong("launch_count",0) + 1;
        editor.putLong("launch_count",launch_count);

        Long date_first_launch = sharedPreferences.getLong("date_first_launch",0);

        if (date_first_launch == 0){
            date_first_launch = System.currentTimeMillis();
            editor.putLong("date_first_launch",date_first_launch);
        }
        if (launch_count >= LAUNCH_UNTIL_PROMPT){
            if (System.currentTimeMillis() >= date_first_launch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)){
                showRateDialog(context,editor);
            }
        }
        editor.commit();
    }

    private static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        Dialog dialog = new Dialog(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String message = "If you enjoy using "+ APP_TITLE
                +", Please take a moment to rate the app. "
                +"Thank you for your support!";

        builder.setMessage(message)
                .setTitle("Rate "+APP_TITLE )
                .setIcon(context.getApplicationInfo().icon)
                .setCancelable(false)
                .setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("don't show again",true);
                        editor.commit();
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id="+APP_PACKAGE_NAME)));
                        }catch (ActivityNotFoundException e){
                            Toast.makeText(context,"You have pressed Rate Now Button",Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }
                }).setNeutralButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"You have pressed Later button",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }).setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editor != null){
                    editor.putBoolean("don't show again",true);
                    editor.commit();
                }
                Toast.makeText(context,"You have pressed No, Thanks Button",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app_launched(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        //get_details();
        lordProduct();
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        FloatingActionButton share = findViewById(R.id.share);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UploadQuotesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("quotes",getFansQuotesResponse);
                bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);
                intent.putExtra("profile",bundle);
                startActivity(intent);
                finish();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(MainActivity.this,"Share successful!",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this,"Share cancel!",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setQuote("# හිතට වදින වදන්")
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.universl.hp.hithatawadinawadan"))
                        .build();
                if (ShareDialog.canShow(ShareLinkContent.class)){
                    shareDialog.show(linkContent);
                }
            }
        });
    }
    private boolean loadFragment(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout,fragment)
                    .commit();
            return true;
        }
        return false;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        Bundle bundle;

        switch (menuItem.getItemId()){
            case R.id.navigation_home:
                fragment = new HomeFragment();
                bundle = new Bundle();
                bundle.putSerializable("quotes",quotesResponses);
                /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);*/
                fragment.setArguments(bundle);
                break;
            case R.id.navigation_romantic:
                fragment = new RomanticFragment();
                bundle = new Bundle();
                bundle.putSerializable("quotes",romanticQuotesResponses);
                /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);*/
                fragment.setArguments(bundle);
                break;
            case R.id.navigation_success:
                fragment = new SuccessFragment();
                bundle = new Bundle();
                bundle.putSerializable("quotes",successQuotesResponses);
                /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);*/
                fragment.setArguments(bundle);
                break;
            case R.id.navigation_other:
                fragment = new OtherFragment();
                bundle = new Bundle();
                bundle.putSerializable("quotes",otherQuotesResponses);
                /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);*/
                fragment.setArguments(bundle);
                break;
            case R.id.navigation_fans:
                fragment = new FansFragment();
                bundle = new Bundle();
                bundle.putSerializable("quotes",fansQuotesResponse);
                /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
                bundle.putStringArrayList("image_path",image_path);*/
                fragment.setArguments(bundle);
                break;
        }
        return loadFragment(fragment);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.power) {
            logout();
            return true;
        }
        if (id == R.id.profile){
            Intent intent = new Intent(MainActivity.this,UserProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("quotes",getFansQuotesResponse);
            /*bundle.putSerializable("favorite_quotes",quotesResponses_photo);
            bundle.putStringArrayList("image_path",image_path);*/
            intent.putExtra("profile",bundle);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        System.exit(0);
    }

    private void get_details(){

        progress = new ProgressDialog(MainActivity.this);

        @SuppressLint("StaticFieldLeak")
        class Network extends AsyncTask<String,Void,String> {
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
                    HttpGet httpget = new HttpGet(Constant.GET_QUOTES_URL);
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
                quotesResponses = new ArrayList<>();
                otherQuotesResponses = new ArrayList<>();
                romanticQuotesResponses = new ArrayList<>();
                successQuotesResponses = new ArrayList<>();
                fansQuotesResponse = new ArrayList<>();
                quotesResponseArrayList = new ArrayList<>();
                quotesResponses = new Gson().fromJson(s, new TypeToken<List<QuotesResponse>>() {
                }.getType());
                class Network_System extends AsyncTask<String,Void,String>{

                    @SuppressLint("WrongThread")
                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            HttpClient client = new DefaultHttpClient();
                            HttpGet httpget = new HttpGet(Constant.GET_FAVORITE_QUOTES_URL);
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
                        if ((progress != null) && progress.isShowing()) {
                            progress.dismiss();
                        }
                        image_path = new ArrayList<>();
                        quotesResponses_photo = new ArrayList<>();
                        quotesResponses_photo = new Gson().fromJson(s, new TypeToken<List<QuotesResponse>>() {
                        }.getType());

                        for (int i = 0; i < quotesResponses_photo.size(); i++){
                            image_path.add(quotesResponses_photo.get(i).photo);
                        }

                        for (int i = 0; i < quotesResponses.size();i++){
                            if (quotesResponses.get(i).status.equalsIgnoreCase("true") && !quotesResponses.get(i).category.equalsIgnoreCase("Fans Quotes")){
                                quotesResponseArrayList.add(quotesResponses.get(i));
                            }
                            if (quotesResponses.get(i).category.equalsIgnoreCase("Other Quotes") && quotesResponses.get(i).status.equalsIgnoreCase("True")){
                                otherQuotesResponses.add(quotesResponses.get(i));
                            }
                            else if (quotesResponses.get(i).category.equalsIgnoreCase("Success Quotes") && quotesResponses.get(i).status.equalsIgnoreCase("True")){
                                successQuotesResponses.add(quotesResponses.get(i));
                            }
                            else if (quotesResponses.get(i).category.equalsIgnoreCase("Romantic Quotes") && quotesResponses.get(i).status.equalsIgnoreCase("True")){
                                romanticQuotesResponses.add(quotesResponses.get(i));
                            }
                            else if (quotesResponses.get(i).status.equalsIgnoreCase("True")
                                    && quotesResponses.get(i).category.equalsIgnoreCase("Fans Quotes")){
                                fansQuotesResponse.add(quotesResponses.get(i));
                            }
                        }

                        Fragment fragment = new HomeFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("quotes",quotesResponseArrayList);
                        /*bundle.putStringArrayList("image_path",image_path);
                        bundle.putSerializable("favorite_quotes",quotesResponses_photo);*/
                        fragment.setArguments(bundle);
                        loadFragment(fragment);
                    }
                }
                new Network_System().execute();
            }
        }
        new Network().execute();
    }
    private void lordProduct(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONArray product = new JSONArray(response);
                            quotesResponses = new ArrayList<>();
                            otherQuotesResponses = new ArrayList<>();
                            romanticQuotesResponses = new ArrayList<>();
                            successQuotesResponses = new ArrayList<>();
                            fansQuotesResponse = new ArrayList<>();
                            getFansQuotesResponse = new ArrayList<>();
                            StringRequest request = new StringRequest(Request.Method.GET,
                                    Constant.GET_FAVORITE_QUOTES_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONArray favorite_product = new JSONArray(response);
                                                quotesResponses_photo = new ArrayList<>();
                                                image_path = new ArrayList<>();

                                                for (int i = 0;i < favorite_product.length(); i++){
                                                    JSONObject productObject = favorite_product.getJSONObject(i);
                                                    String user_id = productObject.getString("user_id");
                                                    String photo = productObject.getString("photo");

                                                    QuotesResponse quotes = new QuotesResponse(photo,user_id);
                                                    quotesResponses_photo.add(quotes);
                                                    image_path.add(photo);
                                                }

                                                for (int i = 0; i < product.length();i++){
                                                    JSONObject productObject = product.getJSONObject(i);
                                                    if (productObject.getString("status").
                                                            equalsIgnoreCase("true") &&
                                                            productObject.getString("category").
                                                                    equalsIgnoreCase("Success Quotes")){
                                                        String category = productObject.getString("category");
                                                        String title = productObject.getString("title");
                                                        String date = productObject.getString("date");
                                                        String photo = productObject.getString("photo");
                                                        String user_name = productObject.getString("user_name");
                                                        String status = productObject.getString("status");

                                                        QuotesResponse quotes = new QuotesResponse(
                                                                category,title,date,photo,user_name,status
                                                        );
                                                        successQuotesResponses.add(quotes);
                                                    }
                                                    if (productObject.getString("status").
                                                            equalsIgnoreCase("true")
                                                            && productObject.getString("category").
                                                            equalsIgnoreCase("Romantic Quotes")){
                                                        String category = productObject.getString("category");
                                                        String title = productObject.getString("title");
                                                        String date = productObject.getString("date");
                                                        String photo = productObject.getString("photo");
                                                        String user_name = productObject.getString("user_name");
                                                        String status = productObject.getString("status");

                                                        QuotesResponse quotes = new QuotesResponse(
                                                                category,title,date,photo,user_name,status
                                                        );
                                                        romanticQuotesResponses.add(quotes);
                                                    }
                                                    if (productObject.getString("status").
                                                            equalsIgnoreCase("true")
                                                            && productObject.getString("category").
                                                            equalsIgnoreCase("Other Quotes")){
                                                        String category = productObject.getString("category");
                                                        String title = productObject.getString("title");
                                                        String date = productObject.getString("date");
                                                        String photo = productObject.getString("photo");
                                                        String user_name = productObject.getString("user_name");
                                                        String status = productObject.getString("status");

                                                        QuotesResponse quotes = new QuotesResponse(
                                                                category,title,date,photo,user_name,status
                                                        );
                                                        otherQuotesResponses.add(quotes);
                                                    }
                                                    if (productObject.getString("status").
                                                            equalsIgnoreCase("true")
                                                            && productObject.getString("category").
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
                                                        fansQuotesResponse.add(quotes);
                                                    }if(productObject.getString("category").
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
                                                        getFansQuotesResponse.add(quotes);
                                                    }
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

                                                        QuotesResponse quotes = new QuotesResponse(
                                                                category,title,date,photo,user_name,status
                                                        );
                                                        quotesResponses.add(quotes);
                                                    }
                                                }
                                                Fragment fragment = new HomeFragment();
                                                Bundle bundle = new Bundle();

                                                bundle.putSerializable("quotes",quotesResponses);
                                                /*bundle.putStringArrayList("image_path",image_path);
                                                bundle.putSerializable("favorite_quotes",quotesResponses_photo);*/
                                                fragment.setArguments(bundle);
                                                loadFragment(fragment);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                            });
                            Volley.newRequestQueue(MainActivity.this).add(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
        });
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
