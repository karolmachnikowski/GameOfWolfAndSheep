package com.example.karol.gameofwolfandsheep

import android.graphics.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.example.karol.gameofwolfandsheep.model.Board.CellValue

@BindingAdapter("render")
fun render(view: AppCompatImageView, value: CellValue?) {
    view.background.clearColorFilter()
    view.setImageDrawable(null)
    when(value){
        CellValue.Wolf -> view.setImageResource(CellValue.Wolf.resId!!)
        CellValue.Sheep -> view.setImageResource(CellValue.Sheep.resId!!)
        CellValue.PossibleMove -> view.background.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.OVERLAY)
    }
}

//@BindingAdapter("resId", "filter")
//fun render(view: AppCompatImageView, resId: Int?, filter: Boolean) {
//    view.background.clearColorFilter()
//    view.setImageDrawable(null)
//    when {
//        resId != null -> view.setImageResource(resId)
//        filter -> view.background.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.OVERLAY)
//    }
//}
//
