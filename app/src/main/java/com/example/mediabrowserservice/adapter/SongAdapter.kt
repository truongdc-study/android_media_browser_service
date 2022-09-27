package com.example.mediabrowserservice.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediabrowserservice.R
import com.example.mediabrowserservice.model.Song
import com.example.mediabrowserservice.utils.setAlphaAnimation

class SongAdapter (
    val callback : Callback
    ): RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private val listSong = mutableListOf<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) =
        holder.onBind(listSong[position])

    override fun getItemCount() = listSong.size

    fun setListSong(list: List<Song>) {
        listSong.apply {
            clear()
            addAll(list)
        }
        notifyDataSetChanged()
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSong = view.findViewById<ImageView>(R.id.imgItemSong)
        val tvTitleSong = view.findViewById<TextView>(R.id.tvItemTitle)
        val tvSubTitleSong = view.findViewById<TextView>(R.id.tvItemSubTitle)
        val viewParentSong = view.findViewById<ViewGroup>(R.id.viewParentItemSong)
        fun onBind(itemSong: Song) {
            /**
             * Use Glide library to load Image Song
             */
            imgSong.setBackgroundResource(R.drawable.ic_music)
            tvTitleSong.text = itemSong.title
            tvSubTitleSong.text = itemSong.subtitle
            viewParentSong.setOnClickListener {
                 it.setAlphaAnimation()
                callback.onClickSong(itemSong)
            }
        }
    }

    interface Callback {
        fun onClickSong(song : Song)
    }
}
