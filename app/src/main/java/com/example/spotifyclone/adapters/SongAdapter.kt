package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.FragmentHomeBinding
import com.example.spotifyclone.databinding.ListItemBinding
import com.google.android.material.textview.MaterialTextView
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
): BaseSongAdapter<ListItemBinding>(R.layout.list_item) {

    override var differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

    override fun onBindHolder(binding: ListItemBinding, position: Int) {
        val song = songs[position]

        binding.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            glide.load(song.imageUrl).into(ivItemImage)

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }

        }
    }


}