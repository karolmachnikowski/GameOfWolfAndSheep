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

class Game {

    var cells = ObservableArrayMap<String, Int>()
    var winner = MutableLiveData<Player>()
    var chosenPawn: String? = null
    var currentPlayer = Player.WOLF
    private var wolfPosition = Pair(LAST_ROW, FIRST_COLUMN)

    init {
        cells[SHEEP_ONE_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_TWO_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_THREE_STARTING_POSITION] = CellValue.Sheep.value
        cells[SHEEP_FOUR_STARTING_POSITION] = CellValue.Sheep.value
        cells[WOLF_STARTING_POSITION] = CellValue.Wolf.value
    }


    fun showPossibleSheepMoves(row: Int, column: Int) {
        clearPossibleMoves()
        if (row == LAST_ROW) return
        chosenPawn = StringUtil.stringFromNumbers(row, column)
        if (column > FIRST_COLUMN && cells[StringUtil.stringFromNumbers(row + 1, column - 1)] == CellValue.Empty.value)
            cells[StringUtil.stringFromNumbers(row + 1, column - 1)] = CellValue.PossibleMove.value
        if (column < LAST_COLUMN && cells[StringUtil.stringFromNumbers(row + 1, column + 1)] == CellValue.Empty.value)
            cells[StringUtil.stringFromNumbers(row + 1, column + 1)] = CellValue.PossibleMove.value
    }

    fun showPossibleWolfMoves(row: Int, column: Int) {
        clearPossibleMoves()
        if (row == FIRST_ROW) return
        chosenPawn = StringUtil.stringFromNumbers(row, column)

        if (hasWolfSouthWestMovePossible(row, column))
            cells[StringUtil.stringFromNumbers(row + 1, column - 1)] = CellValue.PossibleMove.value

        if (hasWolfNorthWestMovePossible(row, column))
            cells[StringUtil.stringFromNumbers(row - 1, column - 1)] = CellValue.PossibleMove.value

        if (hasWolfNortEastMovePossible(row, column))
            cells[StringUtil.stringFromNumbers(row - 1, column + 1)] = CellValue.PossibleMove.value

        if (hasWolfSouthEastMovePossible(row, column))
            cells[StringUtil.stringFromNumbers(row + 1, column + 1)] = CellValue.PossibleMove.value
    }

    private fun hasWolfSouthWestMovePossible(row: Int, column: Int) =
        column > FIRST_COLUMN && row < LAST_ROW && cells[StringUtil.stringFromNumbers(
            row + 1,
            column - 1
        )] == CellValue.Empty.value

    private fun hasWolfNorthWestMovePossible(row: Int, column: Int) =
        column > FIRST_COLUMN && cells[StringUtil.stringFromNumbers(row - 1, column - 1)] == CellValue.Empty.value

    private fun hasWolfNortEastMovePossible(row: Int, column: Int) =
        column < LAST_COLUMN && cells[StringUtil.stringFromNumbers(row - 1, column + 1)] == CellValue.Empty.value

    private fun hasWolfSouthEastMovePossible(row: Int, column: Int) =
        column < LAST_COLUMN && row < LAST_ROW && cells[StringUtil.stringFromNumbers(
            row + 1,
            column + 1
        )] == CellValue.Empty.value

    private fun hasWolfAnyMovePossible(row: Int, column: Int) =
        hasWolfSouthWestMovePossible(row, column) || hasWolfNorthWestMovePossible(row, column) ||
                hasWolfNortEastMovePossible(row, column) || hasWolfSouthEastMovePossible(row, column)

    internal fun makeMove(row: Int, column: Int) {
        clearPossibleMoves()
        val pawn = cells[chosenPawn]
        cells[StringUtil.stringFromNumbers(row, column)] = pawn
        cells.remove(chosenPawn)

        when (pawn) {
            CellValue.Wolf.value ->
                if (row == 0) handleWolfWonCase()
                else {
                    wolfPosition = Pair(row, column)
                    currentPlayer = Player.SHEEP
                }
            CellValue.Sheep.value ->
                if (!hasWolfAnyMovePossible(wolfPosition.first, wolfPosition.second)) handleSheepWonCase()
                else currentPlayer = Player.WOLF
        }
    }

    private fun clearPossibleMoves() {
        val filteredCells = cells.filterValues { it != CellValue.PossibleMove.value }
        cells.clear()
        cells.putAll(filteredCells)
    }

    private fun handleWolfWonCase() {
        winner.value = Player.WOLF
    }

    private fun handleSheepWonCase() {
        winner.value = Player.SHEEP
    }
}