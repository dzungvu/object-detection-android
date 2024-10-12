package com.luke.objectdetection.network

import com.luke.objectdetection.data.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("search")
    fun search(@Query("q") query: String): Call<SearchResponse>
}