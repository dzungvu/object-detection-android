package com.luke.objectdetection.data

import com.google.gson.annotations.SerializedName

data class SearchResponse (
    @SerializedName("results")
    val results: List<SearchItemResponse> = emptyList()
) {
    data class SearchItemResponse(
        @SerializedName("title")
        val title: String = "",
        @SerializedName("price")
        val price: String = "",
        @SerializedName("product_url")
        val prodUrl: String = "",
        @SerializedName("image_url")
        val imgUrl: String = "",
    )
}