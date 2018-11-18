package com.example.karol.gameofwolfandsheep.viewmodel

import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.karol.gameofwolfandsheep.model.CellValue
import com.example.karol.gameofwolfandsheep.model.Game
import com.example.karol.gameofwolfandsheep.model.Player
import com.example.karol.gameofwolfandsheep.utils.StringUtil.stringFromNumbers


class GameViewModel : ViewModel() {

    lateinit var cells : ObservableArrayMap<String, Int>
    private lateinit var game: Game

    fun init() {
        game = Game()
        cells = game.cells
    }

    fun onClickedCellAt(row: Int, column: Int) {
        when (game.cells[stringFromNumbers(row, column)]) {
            CellValue.Sheep.value -> if (game.currentPlayer == Player.SHEEP) game.showPossibleSheepMoves(row, column)
            CellValue.Wolf.value -> if (game.currentPlayer == Player.WOLF) game.showPossibleWolfMoves(row, column)
            CellValue.PossibleMove.value -> game.makeMove(row, column)
        }
    }

    fun getWinner(): LiveData<Player> {
        return game.winner
    }
}
