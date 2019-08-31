package com.mrklie.yangtao.hanzigrid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.hanzi_grid_item.view.*

class HanziGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(hanzi: HanziGridModel) {
        itemView.hanzi_tile_character.text = hanzi.character
        itemView.hanzi_tile_pinyin.text = hanzi.pinyin
    }
}