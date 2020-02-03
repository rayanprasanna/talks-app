package com.universl.hp.hithatawadinawadan.Main.service;

import com.universl.hp.hithatawadinawadan.Main.response.QuotesResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("insert_quotes.php")
    Call<QuotesResponse> uploadMultiple(
            @Part("description") RequestBody description,
            @Part("size") RequestBody size,
            @Part("status") RequestBody status,
            @Part("title") RequestBody title,
            @Part("category")RequestBody category,
            @Part("date")RequestBody date,
            @Part("user_name")RequestBody user_name,
            @Part List<MultipartBody.Part> files);
}
