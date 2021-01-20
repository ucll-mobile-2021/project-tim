package com.example.musicplayer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musicplayer.ui.HomeFragment
import com.google.android.material.navigation.NavigationView
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var favoritesList: ArrayList<String> = arrayListOf<String>()
    private lateinit var toast: Toast
    private var isDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
        createNeededFiles()
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 285)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        loadFavorites()

        if (isChecked()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            this.setDarkMode(true)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_home, R.id.nav_theme, R.id.nav_playlist, R.id.nav_favorites
                ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun createNeededFiles() {
        var fileDarkMode = File("/sdcard/Music/darkmode.txt")
        var fileFavorites = File("/sdcard/Music/favorites.txt")
        fileDarkMode.createNewFile()
        fileFavorites.createNewFile()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }


    fun addToFavorites(string: String) {
        if(string in favoritesList){
            toast.setText("This song is already in your favorites")
            toast.show()
        }else{
            favoritesList.add(string)
            toast.setText("This song has been added to your favorites")
            toast.show()
        }
    }

    fun removeFromFavorites(string: String) {
        if(string in favoritesList){
            favoritesList.remove(string)
        }
    }

    private fun loadFavorites(){
        val favoritesFile = File("/sdcard/Music/favorites.txt")
        for (text in favoritesFile.readLines()){
            favoritesList.add(text)
        }
    }

    fun getFavorites(): ArrayList<String> {
        return favoritesList
    }

    fun setDarkMode(darkmode: Boolean) {
        this.isDarkMode = darkmode
    }

    fun getIsDarkMode() : Boolean {
        return isDarkMode
    }

    override fun onPause() {
        super.onPause()
        val favoritesFile = File("/sdcard/Music/favorites.txt")
        favoritesFile.writeText("")
        for (string in favoritesList)
        {
            favoritesFile.appendText(string + "\n")
        }

    }

    fun isChecked() : Boolean{
        val favoritesFile = File("/sdcard/Music/darkmode.txt")
        if (favoritesFile.readLines().size !== 0){
            val string = favoritesFile.readLines()[0]
            return (string == "true")
        }else{
            return false
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}