package com.example.karol.gameofwolfandsheep.utils

object StringUtil {

    fun stringFromNumbers(vararg numbers : Int) : String {
        val builder = StringBuilder()
        numbers.forEach { builder.append(it) }
        return builder.toString()
    }
}