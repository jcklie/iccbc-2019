package com.mrklie.yangtao.hanzidetail

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mrklie.yangtao.R
import com.mrklie.yangtao.persistence.AppDatabase
import com.mrklie.yangtao.persistence.Hanzi
import com.mrklie.yangtao.util.getColorForHanzi
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.activity_hanzi_detail.*
import org.jetbrains.anko.doAsync


class HanziDetailActivity : AppCompatActivity() {

    private lateinit var hanzi: Hanzi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hanzi_detail)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val character = intent.getStringExtra(KEY_CHARACTER)!!

        doAsync {
            hanzi = AppDatabase.getDatabase(applicationContext).hanziDao().selectById(character)

            runOnUiThread {
                hanzi_detail_character.text = hanzi.character
                hanzi_detail_character.setTextColor(getColor(getColorForHanzi(hanzi.pinyin)))
                hanzi_detail_pinyin.text = hanzi.pinyin
                hanzi_detail_pinyin.setTextColor(getColor(getColorForHanzi(hanzi.pinyin)))
                hanzi_detail_definition.text = hanzi.definition
                hanzi_detail_decomposition.text = hanzi.decomposition
                hanzi_detail_mnemonic.setText(hanzi.mnemonic)

                val markwon = Markwon.create(applicationContext)
                markwon.setMarkdown(hanzi_detail_etymology, hanzi.etymology)

                if (hanzi.phonetic.isNotEmpty()) {
                    hanzi_detail_phonetic.text = "Phonetic: ${hanzi.phonetic}"
                } else {
                    hanzi_detail_phonetic.visibility = View.GONE
                }

                if (hanzi.semantic.isNotEmpty()) {
                    hanzi_detail_semantic.text = "Semantic: ${hanzi.semantic}"
                } else {
                    hanzi_detail_semantic.visibility = View.GONE
                }

                if (!hanzi.scanned) {
                    hanzi_detail_scanned.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_hanzi_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_play_pronunciation -> { playPronounciation(); true}
            R.id.action_save_hanzi_details -> { saveHanziDetails(); true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun playPronounciation() {
        val uri = Uri.parse("android.resource://$packageName/raw/${hanzi.pinyinNumbered}")
        val mp = MediaPlayer.create(this, uri)
        mp.setOnCompletionListener { releaseInstance() }
        mp.start()
    }

    private fun saveHanziDetails() {
        val newHanzi = hanzi.copy(mnemonic = hanzi_detail_mnemonic.text.toString())
        doAsync {
            AppDatabase.getDatabase(applicationContext).hanziDao().update(newHanzi)
        }
    }

    companion object {
        private val TAG = HanziDetailActivity::class.qualifiedName
        private const val KEY_CHARACTER = "KEY_CHARACTER"

        fun newIntent(context: Context, hanzi: String): Intent {
            val intent = Intent(context, HanziDetailActivity::class.java)
            intent.putExtra(KEY_CHARACTER, hanzi)
            return intent
        }
    }
}
