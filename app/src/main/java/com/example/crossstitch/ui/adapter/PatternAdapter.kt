package com.example.crossstitch.ui.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.crossstitch.base.FlipAnimation
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.databinding.LineItemPatternBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.ui.adapter.irv.IPatternRv

class PatternAdapter(var Irv:IPatternRv, var listPattern: List<PatternData>,var listProgress: List<GameProgress>, var listState: MutableList<Boolean>) :RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = LineItemPatternBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatternHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPattern?.size ?: 0
    }

    // Trong phương thức onBindViewHolder của PatternAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= listPattern.size || position >= listProgress.size) return
        var p = listPattern?.get(position)
        var g = listProgress?.get(position)
        var viewHolder: PatternHolder = holder as PatternHolder
        holder.binding.itemPattern.setOnClickListener {
            Irv.onClickItem(viewHolder.adapterPosition)
        }
        viewHolder.binding.name.setText(p?.name)
        var converter = Converter()
        viewHolder.binding.image.setImageBitmap(p?.image?.let { converter.byteArrayToBitmap(it) })

        viewHolder.binding.itemPattern.setOnClickListener(View.OnClickListener {
            Irv.onClickItem(viewHolder.adapterPosition)
        })
    }




    inner class PatternHolder(var binding: LineItemPatternBinding):ViewHolder(binding.root){

    }


}