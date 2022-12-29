package com.example.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.SwipeItemBinding

class SwipeSongAdapter(
    val layoutId:Int
): BaseSongAdapter<SwipeItemBinding>(layoutId) {

    override fun onBindHolder(binding: SwipeItemBinding, position: Int) {
        val song = songs[position]
        val text = "${song.title} - ${song.subtitle}"
        binding.apply {
            tvPrimary.text = text
            root.setOnClickListener{
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    override var differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

}