package com.example.musicplayer.model

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.musicplayer.R

class SongAdapter(private val context: Activity, private val songs: ArrayList<String>)
    : ArrayAdapter<String>(context, R.layout.fragment_list, songs) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.fragment_song, null, true)

        val titleText = rowView.findViewById(R.id.tv_song_title) as TextView
        val artistText = rowView.findViewById(R.id.tv_song_artist) as TextView

        titleText.text = songs[position].removeSuffix(".mp3").split("-")[1].trim()
        artistText.text = songs[position].removeSuffix(".mp3").split("-")[0].trim()

        return rowView
    }
}