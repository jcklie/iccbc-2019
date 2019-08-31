package com.mrklie.yangtao.hanzigrid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrklie.yangtao.R

class HanziGridRecyclerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listOfHanzi = listOf<HanziModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HanziGridViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.hanzi_grid_item, parent, false))
    }

    override fun getItemCount(): Int = listOfHanzi.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val hanziGridViewHolder = viewHolder as HanziGridViewHolder
        hanziGridViewHolder.bindView(listOfHanzi[position])
    }

    fun setHanziList(aListOfHanzi: List<HanziModel>) {
        listOfHanzi = aListOfHanzi
        notifyDataSetChanged()
    }
}