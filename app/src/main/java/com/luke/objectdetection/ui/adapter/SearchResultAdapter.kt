package com.luke.objectdetection.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luke.objectdetection.data.SearchResponse
import com.luke.objectdetection.databinding.ItemSearchResultBinding
import com.luke.objectdetection.utils.RecyclerItemClickListener

class SearchResultAdapter() :
    RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    private val searchResults: ArrayList<SearchResponse.SearchItemResponse> = arrayListOf()
    private var itemClickListener: RecyclerItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val viewBinding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(viewBinding = viewBinding, listener = itemClickListener)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.bind(searchResult)
    }

    fun updateSearchResults(searchResults: List<SearchResponse.SearchItemResponse>) {
        this.searchResults.clear()
        this.searchResults.addAll(searchResults)
        notifyDataSetChanged()
    }

    fun setItemClickListener(itemClickListener: RecyclerItemClickListener) {
        this.itemClickListener = itemClickListener
    }


    inner class SearchResultViewHolder(
        private val viewBinding: ItemSearchResultBinding,
        listener: RecyclerItemClickListener?
    ) :
        RecyclerView.ViewHolder(viewBinding.root) {

        init {
            viewBinding.root.setOnClickListener {
                listener?.onItemClickListener(
                    searchResults[absoluteAdapterPosition],
                    absoluteAdapterPosition,
                    viewBinding.root.id
                )
            }
        }

        fun bind(searchResultItem: SearchResponse.SearchItemResponse) {
            with(searchResultItem) {
                // Set the title of the product
                viewBinding.tvTitle.text = title
                // Set the price of the product
                viewBinding.tvPrice.text = price
                // Set the image of the product
                Glide.with(viewBinding.root)
                    .load(imgUrl)
                    .into(viewBinding.ivProduct)
            }
        }
    }
}