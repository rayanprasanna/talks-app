package com.universl.hp.hithatawadinawadan.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.Constant;
import com.universl.hp.hithatawadinawadan.Util.DetailsActivity;
import com.universl.hp.hithatawadinawadan.Util.GlideApp;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FansQuotesAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<QuotesResponse> quotesResponses;
    private List<QuotesResponse> image_quotesResponses;
    private ArrayList<String> image_pathList;
    private ArrayList<QuotesResponse> quotesResponseArrayList;
    private String androidId;
    private ArrayList<String> image_path;
    private String user_name;

    public FansQuotesAdapter() {
    }

    public FansQuotesAdapter(Context context, List<QuotesResponse> quotesResponses) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
    }

    @SuppressLint("HardwareIds")
    public FansQuotesAdapter(Context context, List<QuotesResponse> quotesResponses,List<QuotesResponse> image_quotesResponses, ArrayList<String> image_pathList) {
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
        TextView title,publish_date,count_of_favorite,like,textView;
        ImageView image,favorite;
        Boolean isClickFavoriteButton;
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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null){
            user_name = account.getGivenName();
        }
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.quotes_list, null);
            // Locate the TextViews in listView_item.xml
            viewHolder.textView = convertView.findViewById(R.id.review);
            viewHolder.like = convertView.findViewById(R.id.like);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.publish_date = convertView.findViewById(R.id.date);
            viewHolder.image = convertView.findViewById(R.id.quotes_image);
            viewHolder.count_of_favorite = convertView.findViewById(R.id.count_of_favorite);
            viewHolder.favorite = convertView.findViewById(R.id.favorite);

            convertView.setTag(viewHolder);
            viewHolder.textView.setVisibility(View.GONE);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (quotesResponses.get(position).status.equalsIgnoreCase("False")){
            viewHolder.title.setText(quotesResponses.get(position).title);
            viewHolder.publish_date.setText(quotesResponses.get(position).date);
            GlideApp
                    .with(context.getApplicationContext())
                    .load(quotesResponses.get(position).photo)
                    .fitCenter()
                    .into(viewHolder.image);
            viewHolder.image.setAlpha(100);
            viewHolder.textView.setPaintFlags(viewHolder.textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            viewHolder.textView.setVisibility(View.VISIBLE);
            viewHolder.like.setVisibility(View.GONE);
            viewHolder.favorite.setVisibility(View.GONE);
            viewHolder.count_of_favorite.setVisibility(View.GONE);
            viewHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.image.setAlpha(255);
                    viewHolder.textView.setVisibility(View.GONE);
                    viewHolder.like.setVisibility(View.VISIBLE);
                    viewHolder.favorite.setVisibility(View.VISIBLE);
                    viewHolder.count_of_favorite.setVisibility(View.VISIBLE);
                    try {
                        updateQuotes(quotesResponses.get(position).photo);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
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
            viewHolder.image.setAlpha(255);
            viewHolder.textView.setVisibility(View.GONE);
            viewHolder.like.setVisibility(View.VISIBLE);
            viewHolder.favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setVisibility(View.VISIBLE);
        }
        viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(image_pathList,quotesResponses.get(position).photo)));
        if (!image_pathList.isEmpty()){
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
        }
        // Set the results into TextViews

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,DetailsActivity.class);
                intent.putExtra("quotes_image",quotesResponses.get(position).photo);
                intent.putExtra("quotes_user_name",quotesResponses.get(position).user_name);
                ((Activity)context).startActivity(intent);
                ((Activity)context).finish();
            }
        });

        viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewHolder.isClickFavoriteButton){
                    viewHolder.isClickFavoriteButton = true;

                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                    @SuppressLint("StaticFieldLeak")
                    class Network extends AsyncTask<Void,Void,Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                uploadFavorite(quotesResponses.get(position).photo);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                    Constant.GET_FAVORITE_QUOTES_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONArray favorite_product = new JSONArray(response);
                                                image_path = new ArrayList<>();

                                                for (int i = 0;i < favorite_product.length(); i++){
                                                    JSONObject productObject = favorite_product.getJSONObject(i);
                                                    //String user_id = productObject.getString("user_id");
                                                    String photo = productObject.getString("photo");

                                                    image_path.add(photo);
                                                }
                                                viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(image_path,quotesResponses.get(position).photo)));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(context,error.getMessage(),Toast.LENGTH_LONG).show();
                                        }

                                    });
                            Volley.newRequestQueue(context).add(stringRequest);
                        }

                    }
                    new Network().execute();
                }else {
                    viewHolder.isClickFavoriteButton = false;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
                }
            }
        });
        return convertView;
    }
    private void updateQuotes(String image_path) throws MalformedURLException{
        String upload_id = UUID.randomUUID().toString();
        new MultipartUploadRequest(context,upload_id,Constant.DELETE_QUOTES_URL)
                .addParameter("photo",image_path)
                .setNotificationConfig(new UploadNotificationConfig())
                .setMaxRetries(2)
                .startUpload();
    }
    private void uploadFavorite(String image_path) throws MalformedURLException {
        String upload_id = UUID.randomUUID().toString();
        new MultipartUploadRequest(context,upload_id,Constant.UPLOAD_FAVORITE_QUOTES_URL)
                .addParameter("photo",image_path)
                .addParameter("user_id",androidId)
                .setNotificationConfig(new UploadNotificationConfig())
                .setMaxRetries(2)
                .startUpload();

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
}
