package com.example.imageuploader;

import com.google.gson.annotations.SerializedName;

public class ImgRes {
    @SerializedName("image")
    private String Image;


    @SerializedName("response")
    private String Response;

    @SerializedName("nopol")
    private String nopol;

    public String getResponse() {
        return Response;
    }

    public String getNopol() {
        return nopol;
    }
}
