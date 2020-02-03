package com.universl.hp.hithatawadinawadan.Main.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;
import com.universl.hp.hithatawadinawadan.Main.sub_activity.QuotesDetailsActivity;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Util.GlideApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SuccessAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<QuotesResponse> quotesResponses;
    private List<QuotesResponse> image_quotesResponses;
    private ArrayList<String> image_pathList;
    private ArrayList<QuotesResponse> quotesResponseArrayList;
    private String androidId;
    private DatabaseReference databaseQuotes;

    public SuccessAdapter() {
    }

    public SuccessAdapter(Context context, List<QuotesResponse> quotesResponses) {
        this.context = context;
        this.quotesResponses = quotesResponses;
        this.quotesResponseArrayList = new ArrayList<>();
        quotesResponseArrayList.addAll(quotesResponses);
        layoutInflater = LayoutInflater.from(context);
    }

    @SuppressLint("HardwareIds")
    public SuccessAdapter(Context context, List<QuotesResponse> quotesResponses, List<QuotesResponse> image_quotesResponses, ArrayList<String> image_pathList) {
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
                intent.putExtra("activity","success");
                intent.putExtra("count",viewHolder.count_of_favorite.getText());
                intent.putExtra("quotes_image",quotesResponses.get(position).photo);
                intent.putExtra("quotes_user_name",quotesResponses.get(position).user_name);
                ((Activity)context).startActivity(intent);
                ((Activity)context).finish();
            }
        });
        return convertView;
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
}
