package com.mrklie.yangtao.hanzigrid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mrklie.yangtao.R
import com.mrklie.yangtao.hanzidetail.HanziDetailActivity
import com.mrklie.yangtao.persistence.Hanzi
import com.mrklie.yangtao.util.getColorForHanzi
import kotlinx.android.synthetic.main.activity_hanzi_detail.*
import kotlinx.android.synthetic.main.hanzi_grid_item.view.*

class HanziGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(hanzi: Hanzi) {
        itemView.hanzi_tile_character.setTextColor(itemView.context.getColor(getColorForHanzi(hanzi.pinyin)))
        itemView.hanzi_tile_character.text = hanzi.character
        itemView.hanzi_tile_pinyin.text = hanzi.pinyin

        if (hanzi.scanned) {
            itemView.hanzi_detail_scanned.visibility = View.VISIBLE
        } else {
            itemView.hanzi_detail_scanned.visibility = View.VISIBLE
        }

        itemView.setOnClickListener {
            it.context.startActivity(HanziDetailActivity.newIntent(it.context, hanzi.character))
        }
    }
}