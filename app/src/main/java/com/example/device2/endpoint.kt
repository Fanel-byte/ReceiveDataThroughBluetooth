package com.example.device2

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


//endpoint c'est service
interface endpoint {
    @GET("getcarpin/{id}")
    suspend fun getcar(@Path("id")id:Int): Response<String>


}