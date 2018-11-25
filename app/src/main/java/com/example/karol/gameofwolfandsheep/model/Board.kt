package com.example.karol.gameofwolfandsheep.model

import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.MutableLiveData
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

    var cells = ObservableArrayMap<String, Int>()
    var winner = MutableLiveData<Player>()
    var currentPlayer = MutableLiveData<Player>().apply { value = Player.Wolf }
    private var chosenPawn: String? = null
    private var wolfPosition = Pair(LAST_ROW, FIRST_COLUMN)

    init {
        setDefaultCells()
    }

    private fun setDefaultCells() {
        cells[SHEEP_ONE_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_TWO_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_THREE_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_FOUR_STARTING_POSITION] = CellValue.Sheep.value
        cells[WOLF_STARTING_POSITION] = CellValue.Wolf.value
    }

    fun restartBoard() {
        cells.clear()
        setDefaultCells()
        currentPlayer.value = Player.Wolf
        chosenPawn = null
        wolfPosition = Pair(LAST_ROW, FIRST_COLUMN)
    }

    fun hasSheepSouthWestMovePossible(row: Int, column: Int) =
        column > FIRST_COLUMN && getCell(row + 1, column - 1) == CellValue.Empty.value

    fun hasSheepSouthEastMovePossible(row: Int, column: Int) =
        column < LAST_COLUMN && getCell(row + 1, column + 1) == CellValue.Empty.value

    fun hasWolfSouthWestMovePossible() =
        wolfPosition.second > FIRST_COLUMN && wolfPosition.first < LAST_ROW
                && getCell(wolfPosition.first + 1, wolfPosition.second - 1) == CellValue.Empty.value

    fun hasWolfNorthWestMovePossible() =
        wolfPosition.second > FIRST_COLUMN
                && getCell(wolfPosition.first - 1, wolfPosition.second - 1) == CellValue.Empty.value

    fun hasWolfNortEastMovePossible() =
        wolfPosition.second < LAST_COLUMN
                && getCell(wolfPosition.first - 1, wolfPosition.second + 1) == CellValue.Empty.value

    fun hasWolfSouthEastMovePossible() =
        wolfPosition.second < LAST_COLUMN && wolfPosition.first < LAST_ROW
                && getCell(wolfPosition.first + 1, wolfPosition.second + 1) == CellValue.Empty.value

    fun hasWolfAnyMovePossible() =
        hasWolfSouthWestMovePossible() || hasWolfNorthWestMovePossible() ||
                hasWolfNortEastMovePossible() || hasWolfSouthEastMovePossible()

    fun handleWolfWonCase() {
        winner.value = Player.Wolf
    }

    fun handleSheepWonCase() {
        winner.value = Player.Sheep
    }

    fun setCell(row: Int, column: Int, value: Int?) {
        cells[StringUtil.stringFromNumbers(row, column)] = value
    }

    fun setCellAsPossibleMove(row: Int, column: Int) {
        setCell(row, column, CellValue.PossibleMove.value)
    }

    fun setCell(key: String, value: Int?) {
        cells[key] = value
    }

    fun getCell(row: Int, column: Int) = cells[StringUtil.stringFromNumbers(row, column)]

    fun getCell(key: String) = cells[key]

    fun setCellAsEmpty(key: String) {
        cells.remove(key)
    }

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

    fun setCurrentPlayer(player: Player){
        currentPlayer.value = player
    }

    fun getCurrentPlayer() = currentPlayer.value
}