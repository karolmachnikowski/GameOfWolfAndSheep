package com.example.karol.gameofwolfandsheep.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LaunchViewModel : ViewModel() {
    var gameStarted = MutableLiveData<Boolean>()

    fun startGame() {
        gameStarted.value = true
    }
}
