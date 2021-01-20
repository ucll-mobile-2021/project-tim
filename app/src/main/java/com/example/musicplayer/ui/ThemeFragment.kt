package com.example.musicplayer.ui

import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R
import java.io.File

class ThemeFragment : Fragment() {

    private lateinit var myswitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_theme, container, false)
        myswitch = root.findViewById(R.id.switch_theme)
        if ((activity as MainActivity).isChecked()){
            myswitch.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            (activity as MainActivity).setDarkMode(true)
        }
        myswitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                (activity as MainActivity).setDarkMode(true)

            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                (activity as MainActivity).setDarkMode(false)
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
        menu.findItem(R.id.sort_by_artist).isVisible = false
        menu.findItem(R.id.sort_by_song).isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        val favoritesFile = File("/sdcard/Music/darkmode.txt")
        favoritesFile.writeText(myswitch.isChecked.toString())
    }

}