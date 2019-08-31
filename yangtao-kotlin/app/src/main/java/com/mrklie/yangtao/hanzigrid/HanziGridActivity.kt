package com.mrklie.yangtao.hanzigrid

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.mrklie.yangtao.ar.ArActivity
import kotlinx.android.synthetic.main.activity_hanzi_grid.*




class HanziGridActivity : AppCompatActivity() {

    private val MIN_OPENGL_VERSION = 3.0
    private val TAG = HanziGridActivity::class.java.getSimpleName()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(com.mrklie.yangtao.R.layout.activity_hanzi_grid)
        setSupportActionBar(toolbar)
        initHanziGrid()

        fab.setOnClickListener { _ ->
            startActivity(ArActivity.newIntent(applicationContext))
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
            com.mrklie.yangtao.R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initHanziGrid() {
        hanzi_grid_recycler_view.layoutManager = GridLayoutManager(this,4)

        val hanziGridRecyclerAdapter = HanziGridRecyclerAdapter()
        hanzi_grid_recycler_view.adapter = hanziGridRecyclerAdapter
        hanziGridRecyclerAdapter.setHanziList(generateDummyData())
    }

    private fun generateDummyData(): List<HanziGridModel> {
        val result = mutableListOf<HanziGridModel>()
        result.add(HanziGridModel("一", "yī"))
        result.add(HanziGridModel("乙", "yǐ"))
        result.add(HanziGridModel("二", "èr"))
        result.add(HanziGridModel("十", "shí"))
        result.add(HanziGridModel("丁", "dīng"))
        result.add(HanziGridModel("厂", "chǎng"))
        result.add(HanziGridModel("七", "qī"))
        result.add(HanziGridModel("卜", "bo"))
        result.add(HanziGridModel("八", "bā"))
        result.add(HanziGridModel("人", "rén"))
        result.add(HanziGridModel("入", "rù"))
        result.add(HanziGridModel("儿", "ér"))
        result.add(HanziGridModel("匕", "bǐ"))
        result.add(HanziGridModel("几", "jǐ"))
        result.add(HanziGridModel("九", "jiǔ"))
        result.add(HanziGridModel("刁", "diāo"))
        result.add(HanziGridModel("了", "le"))
        result.add(HanziGridModel("刀", "dāo"))
        result.add(HanziGridModel("力", "lì"))
        result.add(HanziGridModel("乃", "nǎi"))
        result.add(HanziGridModel("又", "yòu"))
        result.add(HanziGridModel("三", "sān"))
        result.add(HanziGridModel("干", "gàn"))
        result.add(HanziGridModel("于", "yú"))
        result.add(HanziGridModel("亏", "kuī"))
        result.add(HanziGridModel("工", "gōng"))
        result.add(HanziGridModel("土", "tǔ"))
        result.add(HanziGridModel("士", "shì"))
        result.add(HanziGridModel("才", "cái"))
        result.add(HanziGridModel("下", "xià"))
        result.add(HanziGridModel("寸", "cùn"))
        result.add(HanziGridModel("大", "dà"))
        result.add(HanziGridModel("丈", "zhàng"))
        result.add(HanziGridModel("与", "yǔ"))
        result.add(HanziGridModel("万", "wàn"))
        result.add(HanziGridModel("上", "shàng"))
        result.add(HanziGridModel("小", "xiǎo"))
        result.add(HanziGridModel("口", "kǒu"))
        result.add(HanziGridModel("山", "shān"))
        result.add(HanziGridModel("巾", "jīn"))
        result.add(HanziGridModel("千", "qiān"))
        result.add(HanziGridModel("乞", "qǐ"))
        result.add(HanziGridModel("川", "chuān"))
        result.add(HanziGridModel("亿", "yì"))
        result.add(HanziGridModel("个", "gè"))
        result.add(HanziGridModel("夕", "xī"))
        result.add(HanziGridModel("久", "jiǔ"))
        result.add(HanziGridModel("么", "me"))
        result.add(HanziGridModel("勺", "sháo"))
        result.add(HanziGridModel("凡", "fán"))
        result.add(HanziGridModel("丸", "wán"))
        result.add(HanziGridModel("及", "jí"))
        result.add(HanziGridModel("广", "guǎng"))
        result.add(HanziGridModel("亡", "wáng"))
        result.add(HanziGridModel("门", "mén"))
        result.add(HanziGridModel("丫", "yā"))
        result.add(HanziGridModel("义", "yì"))
        result.add(HanziGridModel("之", "zhī"))
        result.add(HanziGridModel("尸", "shī"))
        result.add(HanziGridModel("己", "jǐ"))
        result.add(HanziGridModel("已", "yǐ"))
        result.add(HanziGridModel("巳", "sì"))
        result.add(HanziGridModel("弓", "gōng"))
        result.add(HanziGridModel("子", "zi"))
        result.add(HanziGridModel("卫", "wèi"))
        result.add(HanziGridModel("也", "yě"))
        result.add(HanziGridModel("女", "nǚ"))
        result.add(HanziGridModel("刃", "rèn"))
        result.add(HanziGridModel("飞", "fēi"))
        result.add(HanziGridModel("习", "xí"))
        result.add(HanziGridModel("叉", "chā"))
        result.add(HanziGridModel("马", "mǎ"))
        result.add(HanziGridModel("乡", "xiāng"))
        result.add(HanziGridModel("丰", "fēng"))
        result.add(HanziGridModel("王", "wáng"))
        result.add(HanziGridModel("开", "kāi"))
        result.add(HanziGridModel("井", "jǐng"))
        result.add(HanziGridModel("天", "tiān"))
        result.add(HanziGridModel("夫", "fū"))
        result.add(HanziGridModel("元", "yuán"))
        result.add(HanziGridModel("无", "wú"))
        result.add(HanziGridModel("云", "yún"))
        result.add(HanziGridModel("专", "zhuān"))
        result.add(HanziGridModel("丐", "gài"))
        result.add(HanziGridModel("扎", "zhā"))
        result.add(HanziGridModel("艺", "yì"))
        result.add(HanziGridModel("木", "mù"))
        result.add(HanziGridModel("五", "wǔ"))
        result.add(HanziGridModel("支", "zhī"))
        result.add(HanziGridModel("厅", "tīng"))
        result.add(HanziGridModel("不", "bù"))
        result.add(HanziGridModel("犬", "quǎn"))
        result.add(HanziGridModel("太", "tài"))
        result.add(HanziGridModel("区", "qū"))
        result.add(HanziGridModel("历", "lì"))
        result.add(HanziGridModel("歹", "dǎi"))
        result.add(HanziGridModel("友", "yǒu"))
        result.add(HanziGridModel("尤", "yóu"))
        result.add(HanziGridModel("匹", "pǐ"))
        result.add(HanziGridModel("车", "chē"))
        return result
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
}
