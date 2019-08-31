package com.mrklie.yangtao.hanzigrid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mrklie.yangtao.R
import com.mrklie.yangtao.hanzidetail.HanziDetailActivity
import kotlinx.android.synthetic.main.hanzi_grid_item.view.*

class HanziGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(hanzi: HanziModel) {
        itemView.hanzi_tile_character.text = hanzi.character
        itemView.hanzi_tile_pinyin.text = hanzi.pinyin

        if (hanzi.scanned) {
            itemView.cardview.setBackgroundResource(R.drawable.frame_scanned)
        } else {
            itemView.cardview.setBackgroundResource(R.drawable.frame_unscanned)
        }

        itemView.setOnClickListener {
            it.context.startActivity(HanziDetailActivity.newIntent(it.context, hanzi))
        }
    }
}