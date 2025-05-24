package com.example.crossstitch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.databinding.LineItemPatternBinding
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.ui.adapter.irv.IPatternRv

class PatternAdapter(var Irv:IPatternRv) :RecyclerView.Adapter<ViewHolder>() {
     var listPattern: List<PatternData>? = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = LineItemPatternBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatternHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPattern?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var p = listPattern?.get(position)
        var viewHolder: PatternHolder = holder as PatternHolder
        viewHolder.binding.name.setText(p?.name)
        var converter = Converter()
        viewHolder.binding.image.setImageBitmap(p?.image?.let { converter.byteArrayToBitmap(it) })
        viewHolder.binding.itemPattern.setOnClickListener(View.OnClickListener {
            Irv.onClickItem(viewHolder.adapterPosition)
        })
    }

    class PatternHolder(var binding: LineItemPatternBinding):ViewHolder(binding.root){

    }
}