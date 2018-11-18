package com.example.karol.gameofwolfandsheep

import android.graphics.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.example.karol.gameofwolfandsheep.model.CellValue

@BindingAdapter("render")
fun render(view: AppCompatImageView, value: Int?) {
    view.background.clearColorFilter()
    view.setImageDrawable(null)
    when(value){
        CellValue.Wolf.value -> view.setImageResource(CellValue.Wolf.resId!!)
        CellValue.Sheep.value -> view.setImageResource(CellValue.Sheep.resId!!)
        CellValue.PossibleMove.value -> view.background.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.OVERLAY)
    }
}

