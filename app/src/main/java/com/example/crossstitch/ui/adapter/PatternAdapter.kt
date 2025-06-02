package com.example.crossstitch.ui.adapter

import android.icu.text.Transliterator.Position
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.crossstitch.converter.Converter
import com.example.crossstitch.converter.ConverterPixel
import com.example.crossstitch.databinding.LineItemPatternBinding
import com.example.crossstitch.model.entity.GameProgress
import com.example.crossstitch.model.entity.PatternData
import com.example.crossstitch.ui.adapter.irv.IPatternRv
import com.google.android.material.snackbar.Snackbar

class PatternAdapter(var Irv:IPatternRv, var listPattern: List<PatternData>,var listProgress: List<GameProgress>, var listState: MutableList<Boolean>) :RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = LineItemPatternBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatternHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPattern?.size ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
//        viewHolder.binding.mistake.setText("Mistake: ${g.mistake}")
//        viewHolder.binding.completedCells.setText("Completed: ${g.completedCells/180}%")
        viewHolder.binding.btnDownload.tooltipText = when (g.completedCells/180){
            100 -> null
            else -> "Finish 100% to unlock the final image. Current progress: ${g.completedCells/180}%."
        }
        viewHolder.binding.btnDownload.setOnClickListener {
            it.performLongClick()
            if (g.completedCells/180 == 100){
                var converterP = ConverterPixel()
                Irv.onDownloadClicked(converterP.colorMatrixToBitmap(p!!.gridColor))
            }
        }

        viewHolder.binding.btnReset.setOnClickListener {
            showConfirmationDialog(
                it.context,
                "Reset",
                "Are you sure you want to reset this pattern?",
                onConfirmed = {
                    // Gọi hàm xử lý reset ở đây
                    Toast.makeText(it.context, "Reset confirmed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        viewHolder.binding.btnFill.setOnClickListener {
            showConfirmationDialog(
                it.context,
                "Fill",
                "Are you sure you want to auto-fill the pattern?",
                onConfirmed = {
                    // Gọi hàm xử lý fill ở đây
                    Toast.makeText(it.context, "Fill confirmed", Toast.LENGTH_SHORT).show()
                }
            )
        }

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

    private fun showConfirmationDialog(
        context: android.content.Context,
        title: String,
        message: String,
        onConfirmed: () -> Unit
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}