package com.example.karol.gameofwolfandsheep.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.karol.gameofwolfandsheep.model.Game

class GameViewModel : ViewModel() {

    private val game = Game()

    fun onClickedCellAt(row: Int, column: Int) {
        game.onInteractionWithCell(row, column)
    }

    fun getWinner() = game.getWinner()

    fun getCurrentPlayer() = game.getCurrentPlayer()

    fun getCells() = game.getCells()

    fun getBluetoothState() = game.getBluetoothState()

    fun getBluetoothFailure() = game.getBluetoothFailure()

    fun getBluetoothPlayer() = game.getBluetoothPlayer()

    fun getBluetoothDevice() = game.getBluetoothDevice()

    fun restartGame() {
        game.restartGame()
    }

    fun isBluetoothEnabled() = game.isBluetoothEnabled()

    fun ensureDiscoverable(context: Context) {
        game.ensureDiscoverable(context)
    }

    fun connectToDevice(address: String) {
        game.connectToDevice(address)
    }

    fun listenForConnections() {
        game.listenForConnections()
    }

    fun stopGameBluetoothThreads() {
        game.stopBluetoothThreads()
    }
}
