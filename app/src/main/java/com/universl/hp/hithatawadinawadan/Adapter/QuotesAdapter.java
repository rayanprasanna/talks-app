package com.universl.hp.hithatawadinawadan.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Util.DetailsActivity;
import com.universl.hp.hithatawadinawadan.Util.GlideApp;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuotesAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<QuotesResponse> quotesResponses,otherQuotesResponses,romanticQuotesResponses,successQuotesResponses,fansQuotesResponse,allQuotesResponses;
    private List<QuotesResponse> image_quotesResponses;
    private ArrayList<String> image_pathList;
    private ArrayList<QuotesResponse> quotesResponseArrayList;
    private String androidId;
    private ArrayList<String> image_path,getImage_path;
    private DatabaseReference databaseQuotes;

    public QuotesAdapter() {
    }

    public void setQuotesResponses(List<QuotesResponse> quotesResponses) {
        this.quotesResponses = quotesResponses;
    }

    public QuotesAdapter(Context context, List<QuotesResponse> quotesResponses) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
    }

    @SuppressLint("HardwareIds")
    public QuotesAdapter(Context context, List<QuotesResponse> quotesResponses,List<QuotesResponse> image_quotesResponses, ArrayList<String> image_pathList) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.image_pathList = image_pathList;
        this.image_quotesResponses = image_quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        getImage_path = new ArrayList<>();
        otherQuotesResponses = new ArrayList<>();
    }

    public class ViewHolder{
        TextView title,publish_date,count_of_favorite,review;
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
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.quotes_list, null);
            // Locate the TextViews in listView_item.xml
            databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
            viewHolder.review = convertView.findViewById(R.id.review);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.publish_date = convertView.findViewById(R.id.date);
            viewHolder.image = convertView.findViewById(R.id.quotes_image);
            viewHolder.count_of_favorite = convertView.findViewById(R.id.count_of_favorite);
            viewHolder.favorite = convertView.findViewById(R.id.favorite);
            viewHolder.review.setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(image_pathList,quotesResponses.get(position).photo)));
        viewHolder.isClickFavoriteButton = false;
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
        // Set the results into TextViews
        viewHolder.title.setText(quotesResponses.get(position).title);
        GlideApp.with(context.getApplicationContext()).load(quotesResponses.get(position).photo).fitCenter().into(viewHolder.image);
        viewHolder.publish_date.setText(quotesResponses.get(position).date);

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
                    try {
                        uploadFavorite(quotesResponses.get(position).photo);
                        viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(getImage_path,quotesResponses.get(position).photo)));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    /*@SuppressLint("StaticFieldLeak")
                    class Network extends AsyncTask<Void,Void,Void>{

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

                                                int SPLASH_DISPLAY_LENGTH = 1000;
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(context,MainActivity.class);
                                                        ((Activity)context).startActivity(intent);
                                                        ((Activity)context).finish();
                                                    }
                                                }, SPLASH_DISPLAY_LENGTH);
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
                    new Network().execute();*/
                }else {
                    viewHolder.isClickFavoriteButton = false;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
                }
            }
        });
        return convertView;
    }

    private void uploadFavorite(final String image_path) throws MalformedURLException {
        String id = databaseQuotes.push().getKey();
        com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse response =
                new com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse(id,image_path);
        assert id != null;
        databaseQuotes.child(id).setValue(response);

        databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot quotesSnapShot : dataSnapshot.getChildren()){
                    QuotesResponse quotes = quotesSnapShot.getValue(QuotesResponse.class);
                    otherQuotesResponses.add(quotes);
                }
                for (int i = 0; i < otherQuotesResponses.size(); i++){
                    getImage_path.add(otherQuotesResponses.get(i).photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Toast.makeText(context,"Favorite Added",Toast.LENGTH_LONG).show();
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
