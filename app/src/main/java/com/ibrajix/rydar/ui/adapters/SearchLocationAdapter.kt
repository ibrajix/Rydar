package com.ibrajix.rydar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibrajix.rydar.databinding.RcvLytLocationResultBinding
import com.ibrajix.rydar.response.Candidate

class SearchLocationAdapter(private val onClickListener: OnLocationItemClickListener) : ListAdapter<Candidate, SearchLocationAdapter.SearchLocationViewHolder>(
    SearchLocationDiffCallback()
) {


    class SearchLocationViewHolder private constructor(private val binding: RcvLytLocationResultBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Candidate){
            /*binding.model = item
            binding.executePendingBindings()*/
        }

        companion object{
            fun from(parent: ViewGroup) : SearchLocationViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RcvLytLocationResultBinding.inflate(layoutInflater, parent, false)
                return SearchLocationViewHolder(binding)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLocationViewHolder {
        return SearchLocationViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SearchLocationViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
        holder.itemView.setOnClickListener {
            if (item != null) {
                onClickListener.onClickNews(item)
            }
        }
    }

    class OnLocationItemClickListener(val clickListener: (searchLocation: Candidate) -> Unit){
        fun onClickNews(searchLocation: Candidate) = clickListener(searchLocation)
    }


    class SearchLocationDiffCallback : DiffUtil.ItemCallback<Candidate>() {

        override fun areItemsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem.geometry == newItem.geometry
        }

        override fun areContentsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem == newItem
        }

    }

}