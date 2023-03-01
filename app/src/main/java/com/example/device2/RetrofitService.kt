package com.example.myapplication

import com.example.device2.endpoint
import com.example.device2.url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {
    val Endpoint = Retrofit.Builder().baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create()).build()
        .create(endpoint::class.java)
}