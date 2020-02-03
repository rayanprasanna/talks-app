package com.universl.hp.hithatawadinawadan.Main.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesDetailsActivity;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesUploadActivity;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesUserProfileActivity;
import com.universl.hp.hithatawadinawadan.Main.util.AppController;
import com.universl.hp.hithatawadinawadan.Main.util.MyApplication;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Main.util.Constant;
import com.universl.hp.hithatawadinawadan.Util.GlideApp;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ProfileAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<QuotesResponse> quotesResponses;
    private List<QuotesResponse> image_quotesResponses;
    private ArrayList<String> image_pathList;
    private ArrayList<QuotesResponse> quotesResponseArrayList;
    private String androidId;
    private DatabaseReference databaseQuotes;

    public ProfileAdapter() {
    }

    public ProfileAdapter(Context context, List<QuotesResponse> quotesResponses) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
    }

    @SuppressLint("HardwareIds")
    public ProfileAdapter(Context context, List<QuotesResponse> quotesResponses, List<QuotesResponse> image_quotesResponses, ArrayList<String> image_pathList) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.image_pathList = image_pathList;
        this.image_quotesResponses = image_quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public class ViewHolder{
        TextView title,publish_date,count_of_favorite,review,like;
        ImageView image,favorite,delete;
        Boolean isClickFavoriteButton;
        LinearLayout quotes_lay;
    }

    @Override
    public int getCount() {
        return quotesResponses.size();
    }

    @Override
    public Object getItem(int position) {
        return quotesResponses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.quotes_list, null);
            // Locate the TextViews in listView_item.xml

            databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
            viewHolder.delete = convertView.findViewById(R.id.delete);
            viewHolder.review = convertView.findViewById(R.id.review);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.like = convertView.findViewById(R.id.like);
            viewHolder.publish_date = convertView.findViewById(R.id.date);
            viewHolder.image = convertView.findViewById(R.id.quotes_image);
            viewHolder.count_of_favorite = convertView.findViewById(R.id.count_of_favorite);
            viewHolder.favorite = convertView.findViewById(R.id.favorite);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.quotes_lay = convertView.findViewById(R.id.quotes_lay);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (quotesResponses.get(position).status.equalsIgnoreCase("False")){
            String test = quotesResponses.get(position).photo;
            System.out.println(test);
            GlideApp
                    .with(context.getApplicationContext())
                    .load(quotesResponses.get(position).photo)
                    .fitCenter()
                    .into(viewHolder.image);
            viewHolder.image.setAlpha(225);
            viewHolder.title.setText(quotesResponses.get(position).title);
            viewHolder.publish_date.setText(quotesResponses.get(position).date);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.like.setVisibility(View.VISIBLE);
            viewHolder.favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setVisibility(View.VISIBLE);
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateQuotes(quotesResponses.get(position).photo);
                    quotesResponses.remove(position);
                    viewHolder.delete.setVisibility(View.GONE);
                    viewHolder.review.setVisibility(View.GONE);
                    viewHolder.image.setVisibility(View.GONE);
                    viewHolder.like.setVisibility(View.GONE);
                    viewHolder.count_of_favorite.setVisibility(View.GONE);
                    viewHolder.favorite.setVisibility(View.GONE);
                    viewHolder.publish_date.setVisibility(View.GONE);
                    viewHolder.title.setVisibility(View.GONE);
                    /*viewHolder.quotes_lay.setVisibility(View.GONE);*/
                }
            });
        }else {
            viewHolder.title.setText(quotesResponses.get(position).title);
            viewHolder.publish_date.setText(quotesResponses.get(position).date);
            GlideApp
                    .with(context.getApplicationContext())
                    .load(quotesResponses.get(position).photo)
                    .fitCenter()
                    .into(viewHolder.image);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.like.setVisibility(View.VISIBLE);
            viewHolder.favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(image_pathList,quotesResponses.get(position).photo)));
        }
        /*if (!image_pathList.isEmpty()){
            for (int j = 0; j < quotesResponses.size(); j++){
                for (int k = 0; k < image_quotesResponses.size(); k++ ){
                    if (quotesResponses.get(j).photo.equals(image_quotesResponses.get(k).photo)
                            && androidId.equalsIgnoreCase(image_quotesResponses.get(k).user_id)){
                        viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                        viewHolder.isClickFavoriteButton = true;
                    }
                }
            }
        }
        if (Collections.frequency(image_pathList,quotesResponses.get(position).photo) == 0){
            viewHolder.isClickFavoriteButton = false;
            viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
        }*/
        viewHolder.isClickFavoriteButton = false;
        // Set the results into TextViews
        viewHolder.title.setText(quotesResponses.get(position).title);
        GlideApp.with(context.getApplicationContext()).load(quotesResponses.get(position).photo).fitCenter().into(viewHolder.image);
        viewHolder.publish_date.setText(quotesResponses.get(position).date);
        viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewHolder.isClickFavoriteButton){
                    viewHolder.isClickFavoriteButton = true;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                    uploadFavorite(quotesResponses.get(position).photo);
                    viewHolder.count_of_favorite.setText(String.valueOf(Integer.parseInt(viewHolder.count_of_favorite.getText().toString()) + 1));
                }else {
                    viewHolder.isClickFavoriteButton = false;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
                }
            }
        });
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,QuotesDetailsActivity.class);
                intent.putExtra("count",viewHolder.count_of_favorite.getText());
                intent.putExtra("quotes_image",quotesResponses.get(position).photo);
                intent.putExtra("quotes_user_name",quotesResponses.get(position).user_name);
                (context).startActivity(intent);
                ((Activity)context).finish();
            }
        });

        return convertView;
    }
    private void updateQuotes(final String image_path) {
        //String upload_id = UUID.randomUUID().toString();
        /*try {
            new MultipartUploadRequest(context,upload_id,Constant.DELETE_QUOTES_URL)
                    .addParameter("photo",image_path)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/

        /*SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, Constant.DELETE_QUOTES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("හිතට වදින වදන්");
                        alert.setMessage("Successfully Delete!");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                *//*imageView.setImageBitmap(null);
                                title.setText("");
                                Intent intent = new Intent(QuotesUploadActivity.this,QuotesUserProfileActivity.class);
                                intent.putExtra("profile","upload");
                                startActivity(intent);
                                finish();*//*
                            }
                        });
                        alert.create().show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        simpleMultiPartRequest.addStringParam("photo",image_path);*/
        /*String tag_json_obj = "json_obj_req";

        String url = Constant.DELETE_QUOTES_URL;

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("හිතට වදින වදන්");
                        alert.setMessage("Successfully Delete!");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alert.create().show();
                        pDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,error.getMessage(),Toast.LENGTH_LONG).show();
                pDialog.hide();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("photo", image_path);
                return params;
            }

        };

// Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);*/
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.DELETE_QUOTES_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setIcon(R.mipmap.ic_icon);
                        alert.setTitle("Selfie Guru");
                        alert.setMessage("Successfully Delete !");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, QuotesUserProfileActivity.class);
                                intent.putExtra("profile","upload");
                                context.startActivity(intent);
                                ((Activity)context).finish();
                            }
                        });
                        alert.create().show();
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("photo", image_path);
                return params;
            }
        };
        //queue.add(postRequest);
        AppController.getInstance().getRequestQueue().getCache().remove(com.universl.hp.hithatawadinawadan.Main.util.Constant.GET_QUOTES_URL);
        AppController.getInstance().addToRequestQueue(postRequest);
        deleteCache(context);
        //AppController.getInstance().addToRequestQueue(simpleMultiPartRequest);
    }

    private void uploadFavorite(String image_path){
        String id = databaseQuotes.push().getKey();
        com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse response =
                new com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse(id,image_path);
        assert id != null;
        databaseQuotes.child(id).setValue(response);
    }

    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        quotesResponses.clear();
        if (charText.length() == 0) {
            quotesResponses.addAll(quotesResponseArrayList);
        } else {
            for (QuotesResponse response : quotesResponseArrayList) {
                if (response.title.toLowerCase(Locale.getDefault()).contains(charText)) {
                    quotesResponses.add(response);
                }
            }
        }
        notifyDataSetChanged();
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
}
