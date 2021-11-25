package com.example.imageuploader;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("api.php")
    Call<ImgRes> uploadImage(
            @Field("image") String image
    );
}
