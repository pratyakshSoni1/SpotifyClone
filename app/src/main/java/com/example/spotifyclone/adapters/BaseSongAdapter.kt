package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.google.android.material.textview.MaterialTextView

abstract class BaseSongAdapter<BINDING:ViewDataBinding>(
    private val layoutId:Int
): RecyclerView.Adapter<BaseSongAdapter.SongViewHolder<BINDING>>() {

    abstract fun onBindHolder(binding:BINDING, position: Int)

    class SongViewHolder<VIEWBINDING:ViewDataBinding>(val binder:VIEWBINDING): RecyclerView.ViewHolder(binder.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder<BINDING> {

        val binder = DataBindingUtil.inflate<BINDING>(
            LayoutInflater.from(parent.context),
            layoutId,
            parent,
            false
        )

        return  SongViewHolder(binder)
    }


    protected val diffCallback = object: DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun onBindViewHolder(holder: SongViewHolder<BINDING>, position: Int) {
        onBindHolder( holder.binder, position )
    }

    protected abstract var differ: AsyncListDiffer<Song>

    var songs:List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    protected var onItemClickListener:((Song)->Unit)? = null

    fun setItemClickListener(listener: (Song) ->Unit ){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

}