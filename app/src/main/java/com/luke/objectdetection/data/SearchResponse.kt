package com.luke.objectdetection.data

import com.google.gson.annotations.SerializedName

data class SearchResponse (
    @SerializedName("data")
    val results: List<SearchItemResponse> = emptyList()
) {
    data class SearchItemResponse(
        @SerializedName("title")
        val title: String = "",
        @SerializedName("price")
        val price: String = "",
        @SerializedName("href_value")
        val prodUrl: String = "",
        @SerializedName("img")
        val imgUrl: String = "",
    )
}