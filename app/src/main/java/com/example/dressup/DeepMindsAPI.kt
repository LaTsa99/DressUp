package com.example.dressupinterface

import com.example.dressup.BikiniResult
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DeepMindsAPI {

    companion object{
        const val MULTIPART_FORM_DATA = "multipart/form-data"
        const val PHOTO_MULTIPART_KEY_IMG = "image"
    }

    @Multipart
    @POST("/predict")
    fun getResult(@Part image: MultipartBody.Part) : Call<BikiniResult>
}

