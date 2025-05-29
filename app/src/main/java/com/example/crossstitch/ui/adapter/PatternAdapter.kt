package com.example.crossstitch.ui.adapter

import android.icu.text.Transliterator.Position
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= listPattern.size || position >= listProgress.size) return
        var p = listPattern?.get(position)
        var g = listProgress?.get(position)
        var viewHolder: PatternHolder = holder as PatternHolder
        viewHolder.binding.name.setText(p?.name)
        viewHolder.binding.name1.setText(p?.name)
        var converter = Converter()
        viewHolder.binding.image.setImageBitmap(p?.image?.let { converter.byteArrayToBitmap(it) })
        viewHolder.binding.itemPattern.setOnClickListener(View.OnClickListener {
            Irv.onClickItem(viewHolder.adapterPosition)
        })
        viewHolder.binding.progressBar.progress = (g?.completedCells!!)
        viewHolder.binding.mistake.setText("Mistake: ${g.mistake}")
        viewHolder.binding.completedCells.setText("Completed: ${g.completedCells/180}%")
        if (!listState[position]) {
            viewHolder.binding.front.visibility = View.GONE
            viewHolder.binding.behind.visibility = View.VISIBLE
        } else {
            viewHolder.binding.front.visibility = View.VISIBLE
            viewHolder.binding.behind.visibility = View.GONE
        }

    }
    fun rotate(position: Int){
        var current = listState.get(position)
        listState[position] = !current
    }


    inner class PatternHolder(var binding: LineItemPatternBinding):ViewHolder(binding.root){

    }
}