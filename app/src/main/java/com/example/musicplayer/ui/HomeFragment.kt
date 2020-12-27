package com.example.musicplayer.ui

import android.graphics.Color
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
import com.example.musicplayer.model.SongAdapter
import org.w3c.dom.Text
import java.io.File

class HomeFragment : Fragment() {

    private var mp: MediaPlayer? = null
    private lateinit var play: ImageButton
    private lateinit var loop: ImageButton
    private lateinit var shuffle: ImageButton
    private lateinit var seekbar: SeekBar
    private lateinit var buttonNext: ImageButton
    private lateinit var buttonPrevious: ImageButton
    private lateinit var totalTime: TextView
    private lateinit var currentTime: TextView
    private lateinit var currentSong: TextView

    private var songList: ArrayList<String>? = null
    private var isPlaying: Boolean = false
    private var isLoop: Boolean = false
    private var isShuffle: Boolean = false
    private var currSong: String = ""
    private var currSongPosition: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        play = root.findViewById(R.id.img_btn_play)
        loop = root.findViewById(R.id.img_btn_loop)
        shuffle = root.findViewById(R.id.img_btn_shuffle)
        seekbar = root.findViewById(R.id.sb_song_bar)
        buttonNext = root.findViewById(R.id.img_btn_next)
        buttonPrevious = root.findViewById(R.id.img_btn_previous)
        totalTime = root.findViewById(R.id.tv_total_time)
        currentTime = root.findViewById(R.id.tv_current_time)
        currentSong = root.findViewById(R.id.tv_currently_playing)

        songList = getPlayList("/sdcard/Music")
        val lv: ListView = root.findViewById(R.id.list_songs)
        val listViewAdapter = SongAdapter(requireActivity(), songList!!)
        lv.adapter = listViewAdapter
        initializeSongControl()

        lv.setOnItemClickListener(){ adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)
            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
            currSongPosition = itemIdAtPos.toInt()
            startNewSong(songList!![currSongPosition])
        }
        return root
    }

    private fun startNewSong(item: Any?) {
        if (currSong == item){
            Toast.makeText(requireContext(), "Already playing ${item.toString().removeSuffix(".mp3")}", Toast.LENGTH_SHORT).show()
        }else{
            if (mp !== null) {
                isPlaying = true
                play.callOnClick()
            }
            val songPath: String = "/sdcard/Music/" + item
            currentSong.text = item.toString().removeSuffix(".mp3")
            mp = MediaPlayer.create(requireContext(), Uri.parse(songPath))
            mp?.setOnCompletionListener {
                if(isLoop){
                    currSong = ""
                    playNextSong(0)
                }else if (isShuffle){
                    playNextSong(randomInt())
                }else{
                    playNextSong(1)
                }
            }
            play.callOnClick()
            currSong = item.toString()
            initialiseSeekBar()
        }
    }

    private fun randomInt(): Int {
        return (0 until songList?.size as Int).random()
    }

    private fun playNextSong(i: Int) {
        if(currSongPosition + i > songList?.size as Int - 1)
        {
            currSongPosition += i - (songList?.size as Int)
        }
        else if (currSongPosition + i < 0)
        {
            currSongPosition += i + (songList?.size as Int)
        }
        else
        {
            currSongPosition += i
        }
        startNewSong(songList!![currSongPosition])
    }

    private fun initializeSongControl() {
        play.setOnClickListener {
            if (mp == null){
                Toast.makeText(requireContext(), "Please select a song", Toast.LENGTH_SHORT).show()
            }else{
                if (!isPlaying){
                    mp?.start()
                    play.setImageResource(R.drawable.ic_pause)
                }else{
                    mp?.pause()
                    play.setImageResource(R.drawable.ic_play)
                }
                isPlaying = !isPlaying
            }
        }

        buttonNext.setOnClickListener {
            if(isShuffle){
                playNextSong(randomInt())
            }else if (isLoop){
                loop.callOnClick()
                playNextSong(1)
            }else{
                playNextSong(1)
            }
        }

        buttonPrevious.setOnClickListener {
            if(isShuffle){
                playNextSong(randomInt())
            }else if (isLoop){
                loop.callOnClick()
                playNextSong(-1)
            }else{
                playNextSong(-1)
            }
        }

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mp?.seekTo(progress)
                    currentTime.text = getTimeString(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        loop.setOnClickListener {
            if(isLoop){
                loop.setColorFilter(Color.argb(255, 255, 255, 255))
                isLoop = false
            }else{
                loop.setColorFilter(Color.argb(255, 0, 204, 255))
                isLoop = true
                if(isShuffle) shuffle.callOnClick()
            }
        }

        shuffle.setOnClickListener {
            if(isShuffle){
                shuffle.setColorFilter(Color.argb(255, 255, 255, 255))
                isShuffle = false
            }else{
                shuffle.setColorFilter(Color.argb(255, 0, 204, 255))
                isShuffle = true
                if(isLoop) loop.callOnClick()
            }
        }
    }


    private fun initialiseSeekBar() {
        seekbar.max = mp!!.duration
        totalTime.text = getTimeString(mp!!.duration)
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    seekbar.progress = mp!!.currentPosition
                    currentTime.text = getTimeString(mp!!.currentPosition)
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    seekbar.progress = 0
                }
            }
        }, 0)
    }

    private fun getPlayList(rootPath: String): ArrayList<String> {
        val fileList: ArrayList<String> = ArrayList()
        val rootFolder = File(rootPath)
        val files: Array<File> = rootFolder.listFiles() //here you will get NPE if directory doesn't contains  any file,handle it like this.
        for (file in files) {
            if (file.name.endsWith(".mp3")) {
                val song = file.name
                fileList.add(song)
            }
        }
        return fileList
    }

    private fun getTimeString(millis: Int): String {
        val buf = StringBuffer()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds))
        return buf.toString()
    }

}