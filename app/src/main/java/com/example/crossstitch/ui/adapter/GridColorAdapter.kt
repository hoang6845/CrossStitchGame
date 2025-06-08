package com.example.crossstitch.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.crossstitch.databinding.ItemSquareBinding
import com.example.crossstitch.ui.adapter.irv.IRv

class GridColorAdapter(
    private var pattern: List<Int> = listOf(),
    private var listSymbols: Array<String>? = null,
    private var iRv:IRv? = null
): RecyclerView.Adapter<ViewHolder>() {

    private var selectedColor :Int = Int.MIN_VALUE
    private var oldSelectedPosition = 0
    fun setSelectedColor(color: Int, newPosition:Int){
        selectedColor = color
        if (newPosition!=oldSelectedPosition){
            notifyItemChanged(oldSelectedPosition)
            oldSelectedPosition = newPosition
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridColorHolder(binding)
    }

    override fun getItemCount(): Int {
        return pattern.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var p = pattern?.get(position)
        var viewHolder = holder as GridColorHolder
        viewHolder.binding.item.setCardBackgroundColor(p!!)
        if (selectedColor == p){
            viewHolder.binding.item.strokeWidth = 8
            viewHolder.binding.item.strokeColor = getInverseColor(selectedColor)
        }else{
            viewHolder.binding.item.strokeWidth = 0
        }
        viewHolder.binding.symbol.setText(listSymbols?.get(viewHolder.adapterPosition))
        viewHolder.binding.item.setOnClickListener {
            iRv?.onClickItem(viewHolder.adapterPosition)
        }

    }

    inner class GridColorHolder(var binding: ItemSquareBinding):ViewHolder(binding.root){

    }

    private fun getInverseColor(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.rgb(255 - r, 255 - g, 255 - b)
    }
}