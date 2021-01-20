package com.example.musicplayer.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.model.SongAdapter
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
    private lateinit var toast: Toast
    private lateinit var listViewAdapter: SongAdapter
    private lateinit var search: SearchView
    private lateinit var newList: ArrayList<String>

    private var songList: ArrayList<String>? = null
    private var isPlaying: Boolean = false
    private var isLoop: Boolean = false
    private var isShuffle: Boolean = false
    private var currSong: String = ""
    private var currSongPosition: Int = -1
    private var isInitialized: Boolean = false
    private var lastClickTime: Long = SystemClock.elapsedRealtime()

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
        toast = Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 315)

        songList = getPlayList("/sdcard/Music")
        val lv: ListView = root.findViewById(R.id.list_songs)
        listViewAdapter = SongAdapter(requireActivity(), songList!!)
        lv.adapter = listViewAdapter
        initializeSongControl()
        setHasOptionsMenu(true)

        lv.setOnItemClickListener(){ adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)
            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
            currSongPosition = itemIdAtPos.toInt()
            startNewSong(songList!![currSongPosition])
        }
        lv.setOnItemLongClickListener { parent, view, position, id ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.home_popup_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                val itemIdAtPos = parent.getItemIdAtPosition(position)
                val favSongPosition = itemIdAtPos.toInt()
                (activity as MainActivity).addToFavorites(songList!![favSongPosition])
                true
            }
            popup.show()
            true
        }
        return root
    }

    private fun startNewSong(item: Any?) {
        if (currSong == item){
            toast.setText("The selected song is already playing")
            toast.show()
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
        return (1 until songList?.size as Int).random()
    }

    private fun playNextSong(i: Int) {
        listViewAdapter.notifyDataSetChanged()
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
                toast.setText("Please select a song")
                toast.show()
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
            songList?.clear()
            songList?.addAll(newList)
            search.clearFocus()
            search.setQuery("", true)
            if (SystemClock.elapsedRealtime() - lastClickTime < 400) {
                toast.setText("Woah there! Not so fast pal!")
                toast.show()
            }
            else if(songList?.size!! <= 0){
                toast.setText("You have no songs on your phone.")
                toast.show()
            }
            else if(isShuffle){
                playNextSong(randomInt())
            }else if (isLoop){
                loop.callOnClick()
                playNextSong(1)
            }else{
                playNextSong(1)
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }

        buttonPrevious.setOnClickListener {
            songList?.clear()
            songList?.addAll(newList)
            search.clearFocus()
            search.setQuery("", true)
            if (SystemClock.elapsedRealtime() - lastClickTime < 400) {
                toast.setText("Woah there! Not so fast pal!")
                toast.show()
            }
            else if(songList?.size!! <= 0){
                toast.setText("You have no songs on your phone.")
                toast.show()
            }
            else if(isShuffle){
                playNextSong(randomInt())
            }else if (isLoop){
                if(currSong == "") playNextSong(0)
                else {loop.callOnClick()
                playNextSong(-1)}
            }else{
                if(currSong == "") playNextSong(0)
                else playNextSong(-1)
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                if ((activity as MainActivity).getIsDarkMode()){
                    loop.setColorFilter(Color.argb(255, 255, 255, 255))
                }else{
                    loop.setColorFilter(Color.argb(255, 0, 0, 0))

                }
                isLoop = false
            }else{
                loop.setColorFilter(Color.argb(255, 0, 204, 255))
                isLoop = true
                if(isShuffle) shuffle.callOnClick()
            }
        }

        shuffle.setOnClickListener {
            if(isShuffle){
                if ((activity as MainActivity).getIsDarkMode()){
                    shuffle.setColorFilter(Color.argb(255, 255, 255, 255))
                }else{
                    shuffle.setColorFilter(Color.argb(255, 0, 0, 0))

                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (mp != null && isPlaying) {
            play.callOnClick()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.sort_by_song)
        {
            val sortedList = ArrayList(songList!!.sortedBy {
                it.removeSuffix(".mp3").split("-")[1].trim()
            })
            songList?.clear()
            songList?.addAll(sortedList)
            newList.clear()
            newList.addAll(sortedList)
            listViewAdapter.notifyDataSetChanged()
        }
        else if(item.itemId == R.id.sort_by_artist)
        {
            val sortedList = ArrayList(songList!!.sortedBy {
                it.removeSuffix(".mp3").split("-")[0].trim()
            })
            songList?.clear()
            songList?.addAll(sortedList)
            newList.clear()
            newList.addAll(sortedList)
            listViewAdapter.notifyDataSetChanged()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        newList = ArrayList()
        songList?.let { newList.addAll(it) }
        search = menu.findItem(R.id.menu_search).actionView as SearchView
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search.clearFocus()
                menu.findItem(R.id.menu_search).collapseActionView()
                songList?.clear()
                songList?.addAll(newList)
                val filteredList: ArrayList<String> = ArrayList()
                for (song in songList!!)
                {
                    if(query.toString().toLowerCase() in song.toString().toLowerCase())
                    {
                        filteredList.add(song)
                    }
                }
                songList?.clear()
                songList?.addAll(filteredList)
                listViewAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                songList?.clear()
                songList?.addAll(newList)
                val filteredList: ArrayList<String> = ArrayList()
                for (song in songList!!)
                {
                    if(newText.toString().toLowerCase() in song.toString().toLowerCase())
                    {
                        filteredList.add(song)
                    }
                }
                songList?.clear()
                songList?.addAll(filteredList)
                listViewAdapter.notifyDataSetChanged()
                return true
            }

        })
    }

   /* override fun onPause() {
        super.onPause()
        if (mp != null) {
            play.callOnClick()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mp != null) {
            play.callOnClick()
        }
    }*/
}

