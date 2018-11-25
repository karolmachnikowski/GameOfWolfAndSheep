package com.example.karol.gameofwolfandsheep.model

import com.example.karol.gameofwolfandsheep.R

enum class CellValue(val value: Int?, val resId: Int?) {
    Empty(null, null),
    Wolf(1, R.drawable.wolf),
    Sheep(2, R.drawable.sheep),
    PossibleMove(5, null)
}