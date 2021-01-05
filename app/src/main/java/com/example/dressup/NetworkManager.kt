package com.example.dressup

import android.os.Handler
import android.util.Log
import com.example.dressupinterface.DeepMindsAPI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.concurrent.TimeUnit

object NetworkManager {

    var deepMindsAPI: DeepMindsAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cryptic-journey-45592.herokuapp.com/")
            .client(OkHttpClient.Builder().connectTimeout(100, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        deepMindsAPI = retrofit.create(DeepMindsAPI::class.java)
    }

    fun getResult(file: File, onSuccess: (BikiniResult) -> Unit, onError: (Throwable) -> Unit){
        val requestFile = file.asRequestBody(DeepMindsAPI.MULTIPART_FORM_DATA.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(DeepMindsAPI.PHOTO_MULTIPART_KEY_IMG, file.name, requestFile)

        val uploadImageRequest = deepMindsAPI.getResult(body)
        runCallOnBackgroundThread(uploadImageRequest, onSuccess, onError)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val handler = Handler()
        Thread {
            try {
                val response = call.execute().body()!!
                Log.d("ITTEN", response.toString())
                handler.post { onSuccess(response) }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }.start()
    }
}