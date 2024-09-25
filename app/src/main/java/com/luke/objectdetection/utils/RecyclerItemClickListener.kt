package com.luke.objectdetection.utils

import com.luke.objectdetection.data.SearchResponse

interface RecyclerItemClickListener {
    fun onItemClickListener(searchItem: SearchResponse.SearchItemResponse, position: Int, viewId: Int)
}