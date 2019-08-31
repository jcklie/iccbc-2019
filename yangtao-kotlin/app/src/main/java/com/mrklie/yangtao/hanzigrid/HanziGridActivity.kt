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

    private fun generateDummyData(): List<HanziModel> {
        val result = mutableListOf<HanziModel>()
        result.add(HanziModel("一", "yī"))
        result.add(HanziModel("乙", "yǐ"))
        result.add(HanziModel("二", "èr"))
        result.add(HanziModel("十", "shí"))
        result.add(HanziModel("丁", "dīng"))
        result.add(HanziModel("厂", "chǎng"))
        result.add(HanziModel("七", "qī"))
        result.add(HanziModel("卜", "bo"))
        result.add(HanziModel("八", "bā"))
        result.add(HanziModel("人", "rén"))
        result.add(HanziModel("入", "rù"))
        result.add(HanziModel("儿", "ér"))
        result.add(HanziModel("匕", "bǐ"))
        result.add(HanziModel("几", "jǐ"))
        result.add(HanziModel("九", "jiǔ"))
        result.add(HanziModel("刁", "diāo"))
        result.add(HanziModel("了", "le"))
        result.add(HanziModel("刀", "dāo"))
        result.add(HanziModel("力", "lì"))
        result.add(HanziModel("乃", "nǎi"))
        result.add(HanziModel("又", "yòu"))
        result.add(HanziModel("三", "sān"))
        result.add(HanziModel("干", "gàn"))
        result.add(HanziModel("于", "yú"))
        result.add(HanziModel("亏", "kuī"))
        result.add(HanziModel("工", "gōng"))
        result.add(HanziModel("土", "tǔ"))
        result.add(HanziModel("士", "shì"))
        result.add(HanziModel("才", "cái"))
        result.add(HanziModel("下", "xià"))
        result.add(HanziModel("寸", "cùn"))
        result.add(HanziModel("大", "dà"))
        result.add(HanziModel("丈", "zhàng"))
        result.add(HanziModel("与", "yǔ"))
        result.add(HanziModel("万", "wàn"))
        result.add(HanziModel("上", "shàng"))
        result.add(HanziModel("小", "xiǎo"))
        result.add(HanziModel("口", "kǒu"))
        result.add(HanziModel("山", "shān"))
        result.add(HanziModel("巾", "jīn"))
        result.add(HanziModel("千", "qiān"))
        result.add(HanziModel("乞", "qǐ"))
        result.add(HanziModel("川", "chuān"))
        result.add(HanziModel("亿", "yì"))
        result.add(HanziModel("个", "gè"))
        result.add(HanziModel("夕", "xī"))
        result.add(HanziModel("久", "jiǔ"))
        result.add(HanziModel("么", "me"))
        result.add(HanziModel("勺", "sháo"))
        result.add(HanziModel("凡", "fán"))
        result.add(HanziModel("丸", "wán"))
        result.add(HanziModel("及", "jí"))
        result.add(HanziModel("广", "guǎng"))
        result.add(HanziModel("亡", "wáng"))
        result.add(HanziModel("门", "mén"))
        result.add(HanziModel("丫", "yā"))
        result.add(HanziModel("义", "yì"))
        result.add(HanziModel("之", "zhī"))
        result.add(HanziModel("尸", "shī"))
        result.add(HanziModel("己", "jǐ"))
        result.add(HanziModel("已", "yǐ"))
        result.add(HanziModel("巳", "sì"))
        result.add(HanziModel("弓", "gōng"))
        result.add(HanziModel("子", "zi"))
        result.add(HanziModel("卫", "wèi"))
        result.add(HanziModel("也", "yě"))
        result.add(HanziModel("女", "nǚ"))
        result.add(HanziModel("刃", "rèn"))
        result.add(HanziModel("飞", "fēi"))
        result.add(HanziModel("习", "xí"))
        result.add(HanziModel("叉", "chā"))
        result.add(HanziModel("马", "mǎ"))
        result.add(HanziModel("乡", "xiāng"))
        result.add(HanziModel("丰", "fēng"))
        result.add(HanziModel("王", "wáng"))
        result.add(HanziModel("开", "kāi"))
        result.add(HanziModel("井", "jǐng"))
        result.add(HanziModel("天", "tiān"))
        result.add(HanziModel("夫", "fū"))
        result.add(HanziModel("元", "yuán"))
        result.add(HanziModel("无", "wú"))
        result.add(HanziModel("云", "yún"))
        result.add(HanziModel("专", "zhuān"))
        result.add(HanziModel("丐", "gài"))
        result.add(HanziModel("扎", "zhā"))
        result.add(HanziModel("艺", "yì"))
        result.add(HanziModel("木", "mù"))
        result.add(HanziModel("五", "wǔ"))
        result.add(HanziModel("支", "zhī"))
        result.add(HanziModel("厅", "tīng"))
        result.add(HanziModel("不", "bù"))
        result.add(HanziModel("犬", "quǎn"))
        result.add(HanziModel("太", "tài"))
        result.add(HanziModel("区", "qū"))
        result.add(HanziModel("历", "lì"))
        result.add(HanziModel("歹", "dǎi"))
        result.add(HanziModel("友", "yǒu"))
        result.add(HanziModel("尤", "yóu"))
        result.add(HanziModel("匹", "pǐ"))
        result.add(HanziModel("车", "chē"))
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
