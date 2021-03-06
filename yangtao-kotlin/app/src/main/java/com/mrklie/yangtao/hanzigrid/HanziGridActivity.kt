package com.mrklie.yangtao.hanzigrid

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.mrklie.yangtao.BuildConfig
import com.mrklie.yangtao.ar.ArActivity
import com.mrklie.yangtao.persistence.AppDatabase
import com.mrklie.yangtao.persistence.Hanzi
import com.mrklie.yangtao.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_hanzi_grid.*
import org.jetbrains.anko.activityUiThread
import org.jetbrains.anko.doAsync
import org.opencv.android.OpenCVLoader


class HanziGridActivity : AppCompatActivity() {

    private val MIN_OPENGL_VERSION = 3.0
    private val TAG = HanziGridActivity::class.java.getSimpleName()
    private val RECORD_REQUEST_CODE = 101

    lateinit var hanziGridRecyclerAdapter: HanziGridRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(com.mrklie.yangtao.R.layout.activity_hanzi_grid)
        setSupportActionBar(toolbar)

        // Initialize grid view
        hanzi_grid_recycler_view.layoutManager = GridLayoutManager(this,4)
        hanziGridRecyclerAdapter = HanziGridRecyclerAdapter()
        hanzi_grid_recycler_view.adapter = hanziGridRecyclerAdapter

        doAsync {
            checkFirstRun()

            val characters = AppDatabase.getDatabase(applicationContext).hanziDao().selectAll()
            activityUiThread  {
                initHanziGrid(characters)
            }
        }

        fab.setOnClickListener {
            startActivity(ArActivity.newIntent(applicationContext))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Needs camera permission to run!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.mrklie.yangtao.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            com.mrklie.yangtao.R.id.action_settings -> {
                startActivity(SettingsActivity.newIntent(applicationContext))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Could not load OpenCV", Toast.LENGTH_SHORT).show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                RECORD_REQUEST_CODE)
        }
    }

    private fun initHanziGrid(characters: List<Hanzi>) {
        hanziGridRecyclerAdapter.setHanziList(characters)
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    fun checkFirstRun() {
        val PREFS_NAME = "YangtaoSharedPreferences"
        val PREF_VERSION_CODE_KEY = "version_code"
        val DOESNT_EXIST = -1

        // Get current version code
        val currentVersionCode = BuildConfig.VERSION_CODE

        // Get saved version code
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST)

        // Check for first run or upgrade
        when {
            currentVersionCode == savedVersionCode -> // This is just a normal run
                return
            savedVersionCode == DOESNT_EXIST -> {
                prepopulateDatabase()
            }
            currentVersionCode > savedVersionCode -> {
                // TODO This is an upgrade
            }
            // Update the shared preferences with the current version code
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply()
    }

    private fun prepopulateDatabase()  {
        val result = mutableListOf<Hanzi>()
        assets.open("character_data.tsv").bufferedReader().useLines {
            for (line in it ) {
                val s = line.split("\t").toMutableList()
                while (s.size < 11) s.add("")

                val character = s[0]
                val traditional = s[0]
                val pinyin = s[2]
                val pinyinNumbered = s[3]
                val definition = s[4]
                val decomposition = s[5]
                val origin = s[6]
                val phonetic = s[7]
                val semantic = s[8]
                val mnemonic = s[9]
                val etymology = s[10]

                val hanzi = Hanzi(character, traditional, pinyin, pinyinNumbered, definition,
                    decomposition, origin, phonetic, semantic, mnemonic, etymology)
                result.add(hanzi)
            }
        }
        AppDatabase.getDatabase(applicationContext).hanziDao().insertAll(result)
    }
}
