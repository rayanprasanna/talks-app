package com.universl.hp.hithatawadinawadan.Main.response;

import com.google.gson.annotations.SerializedName;

public class QuotesResponse {
    @SerializedName("category")
    public String category;

    @SerializedName("title")
    public String title;

    @SerializedName("date")
    public String date;

    @SerializedName("photo")
    public String photo;

    @SerializedName("user_name")
    public String user_name;

    @SerializedName("status")
    public String status;

    @SerializedName("user_id")
    public String user_id;

    public QuotesResponse() {
    }

    public QuotesResponse(String user_id, String photo) {
        this.photo = photo;
        this.user_id = user_id;
    }

    public QuotesResponse(String category, String title, String date, String photo, String user_name, String status) {
        this.category = category;
        this.title = title;
        this.date = date;
        this.photo = photo;
        this.user_name = user_name;
        this.status = status;
    }
}
