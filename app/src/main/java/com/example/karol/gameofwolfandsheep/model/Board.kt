package com.example.karol.gameofwolfandsheep.model

import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.MutableLiveData
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.utils.StringUtil


const val SHEEP_ONE_STARTING_POSITION = "01"
const val SHEEP_TWO_STARTING_POSITION = "03"
const val SHEEP_THREE_STARTING_POSITION = "05"
const val SHEEP_FOUR_STARTING_POSITION = "07"
const val WOLF_STARTING_POSITION = "70"
const val FIRST_ROW = 0
const val LAST_ROW = 7
const val FIRST_COLUMN = 0
const val LAST_COLUMN = 7

class Board {

    private var cells = ObservableArrayMap<String, CellValue>()
    private var winner = MutableLiveData<Player>()
    private var currentPlayer = MutableLiveData<Player>().apply { value = Player.Wolf }
    private var chosenPawn: String? = null
    private var wolfPosition = Pair(LAST_ROW, FIRST_COLUMN)

    init {
        setDefaultCells()
    }

    private fun setDefaultCells() {
        cells[SHEEP_ONE_STARTING_POSITION] = CellValue.Sheep
        cells[SHEEP_TWO_STARTING_POSITION] = CellValue.Sheep
        cells[SHEEP_THREE_STARTING_POSITION] = CellValue.Sheep
        cells[SHEEP_FOUR_STARTING_POSITION] = CellValue.Sheep
        cells[WOLF_STARTING_POSITION] = CellValue.Wolf
    }

    fun restartBoard() {
        cells.clear()
        setDefaultCells()
        currentPlayer.value = Player.Wolf
        chosenPawn = null
        wolfPosition = Pair(LAST_ROW, FIRST_COLUMN)
    }

    fun hasSheepSouthWestMovePossible(row: Int, column: Int) =
        column > FIRST_COLUMN && getCell(row + 1, column - 1) == CellValue.Empty

    fun hasSheepSouthEastMovePossible(row: Int, column: Int) =
        column < LAST_COLUMN && getCell(row + 1, column + 1) == CellValue.Empty

    fun hasWolfSouthWestMovePossible() =
        wolfPosition.second > FIRST_COLUMN && wolfPosition.first < LAST_ROW
                && getCell(wolfPosition.first + 1, wolfPosition.second - 1) == CellValue.Empty

    fun hasWolfNorthWestMovePossible() =
        wolfPosition.second > FIRST_COLUMN
                && getCell(wolfPosition.first - 1, wolfPosition.second - 1) == CellValue.Empty

    fun hasWolfNortEastMovePossible() =
        wolfPosition.second < LAST_COLUMN
                && getCell(wolfPosition.first - 1, wolfPosition.second + 1) == CellValue.Empty

    fun hasWolfSouthEastMovePossible() =
        wolfPosition.second < LAST_COLUMN && wolfPosition.first < LAST_ROW
                && getCell(wolfPosition.first + 1, wolfPosition.second + 1) == CellValue.Empty

    fun hasWolfAnyMovePossible() =
        hasWolfSouthWestMovePossible() || hasWolfNorthWestMovePossible() ||
                hasWolfNortEastMovePossible() || hasWolfSouthEastMovePossible()

    fun handleWolfWonCase() {
        winner.value = Player.Wolf
    }

    fun handleSheepWonCase() {
        winner.value = Player.Sheep
    }

    fun getCells() = cells

    fun setCell(row: Int, column: Int, value: CellValue) {
        cells[StringUtil.stringFromNumbers(row, column)] = value
    }

    fun setCellAsPossibleMove(row: Int, column: Int) {
        setCell(row, column, CellValue.PossibleMove)
    }

    fun setCell(key: String, value: CellValue) {
        cells[key] = value
    }

    fun getCell(row: Int, column: Int) = cells[StringUtil.stringFromNumbers(row, column)] ?: CellValue.Empty

    fun getCell(key: String) = cells[key] ?: CellValue.Empty

    fun setCellAsEmpty(key: String) {
        cells.remove(key)
    }

    fun getWinner() = winner

    fun setCurrentPlayer(player: Player){
        currentPlayer.value = player
    }

    fun getCurrentPlayer() = currentPlayer

    fun setChosenPawn(row: Int, column: Int) {
        chosenPawn = StringUtil.stringFromNumbers(row, column)
    }

    fun setChosenPawnCellAsEmpty() {
        cells.remove(chosenPawn)
        chosenPawn = null
    }

    fun getChosenPawn() = chosenPawn

    fun setWolfPosition(row: Int, column: Int) {
        wolfPosition = Pair(row, column)
    }

    fun getWolfPosition() = wolfPosition

    enum class CellValue(val value: Int?, val resId: Int?, val filter: Boolean) {
        Empty(null, null, false),
        Wolf(1, R.drawable.wolf, false),
        Sheep(2, R.drawable.sheep, false),
        PossibleMove(3, null, true)
    }

    enum class Player {
        Wolf,
        Sheep
    }
}