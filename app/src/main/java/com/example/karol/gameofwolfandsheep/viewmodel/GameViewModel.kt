package com.example.karol.gameofwolfandsheep.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.karol.gameofwolfandsheep.model.*
import com.example.karol.gameofwolfandsheep.utils.*

class GameViewModel : ViewModel() {

    val board = Board()
    val cells = board.cells
    val winner = board.winner
    val currentPlayer = board.currentPlayer
    val bluetoothStatus = MutableLiveData<Int>().apply { value = STATE_NONE }
    val bluetoothFailure = MutableLiveData<BluetoothFailure>()
    var bluetoothPlayer = MutableLiveData<Player>()
    var bluetoothDevice: String? = null

    init {
        BluetoothGameUtil.setHandler(getHandler())
    }

    fun restartGame() {
        board.restartBoard()
    }

    fun onClickedCellAt(row: Int, column: Int) {
        when (board.getCell(row, column)) {
            CellValue.Sheep.value ->
                if (board.getCurrentPlayer() == Player.Sheep
                    && (bluetoothPlayer.value == null || bluetoothPlayer.value == Player.Sheep)
                )
                    chooseSheepAndShowPossibleMoves(row, column)
            CellValue.Wolf.value ->
                if (board.getCurrentPlayer() == Player.Wolf
                    && (bluetoothPlayer.value == null || bluetoothPlayer.value == Player.Wolf)
                ) chooseWolfAndShowPossibleMoves()
            CellValue.PossibleMove.value -> moveToChosenCell(row, column)
        }
    }

    fun isBluetoothEnabled() = BluetoothGameUtil.isBluetoothEnabled()

    fun getBluetoothState() = BluetoothGameUtil.getState()

    fun ensureDiscoverable(context: Context) {
        BluetoothGameUtil.ensureDiscoverable(context)
    }

    fun connectToDevice(address: String?) {
        BluetoothGameUtil.connect(address)
    }

    fun listenForConnections() {
        BluetoothGameUtil.listenForConnections()
    }

    fun stopBluetoothUtil() {
        BluetoothGameUtil.stop()
    }

    private fun chooseSheepAndShowPossibleMoves(row: Int, column: Int) {
        clearPossibleMoves()
        board.setChosenPawn(row, column)

        if (board.hasSheepSouthWestMovePossible(row, column))
            board.setCell(row + 1, column - 1, CellValue.PossibleMove.value)
        if (board.hasSheepSouthEastMovePossible(row, column))
            board.setCell(row + 1, column + 1, CellValue.PossibleMove.value)
    }

    private fun chooseWolfAndShowPossibleMoves() {
        clearPossibleMoves()
        val wolfPosition = board.getWolfPosition()
        board.setChosenPawn(wolfPosition.first, wolfPosition.second)

        if (board.hasWolfSouthWestMovePossible())
            board.setCellAsPossibleMove(wolfPosition.first + 1, wolfPosition.second - 1)

        if (board.hasWolfNorthWestMovePossible())
            board.setCellAsPossibleMove(wolfPosition.first - 1, wolfPosition.second - 1)

        if (board.hasWolfNortEastMovePossible())
            board.setCellAsPossibleMove(wolfPosition.first - 1, wolfPosition.second + 1)

        if (board.hasWolfSouthEastMovePossible())
            board.setCellAsPossibleMove(wolfPosition.first + 1, wolfPosition.second + 1)
    }

    private fun moveToChosenCell(row: Int, column: Int) {
        clearPossibleMoves()
        val pawn = board.getChosenPawn()!!
        val pawnValue = board.getCell(pawn)
        board.setCell(row, column, pawnValue)

        when (pawnValue) {
            CellValue.Wolf.value ->
                if (row == FIRST_ROW) board.handleWolfWonCase()
                else {
                    board.setWolfPosition(row, column)
                    board.setCurrentPlayer(Player.Sheep)
                }
            CellValue.Sheep.value ->
                if (!board.hasWolfAnyMovePossible()) board.handleSheepWonCase()
                else board.setCurrentPlayer(Player.Wolf)
        }

        val msg = pawn + StringUtil.stringFromNumbers(row, column)
        BluetoothGameUtil.write(msg.toByteArray())
        board.setChosenPawnCellAsEmpty()
    }

    private fun clearPossibleMoves() {
        val filteredCells = cells.filterValues { it != CellValue.PossibleMove.value }
        cells.clear()
        cells.putAll(filteredCells)
    }

    private fun getHandler() =
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_STATE_CHANGE -> {
                        handleMessageStateChange(msg)
                    }
                    MESSAGE_READ -> {
                        handleMessageRead(msg)
                    }
                    MESSAGE_DEVICE_NAME -> {
                        bluetoothDevice = msg.data.getString(DEVICE_NAME)
                    }
                    MESSAGE_BT_TURNED_OF_WHILE_LISTENING -> {
                        bluetoothFailure.value = BluetoothFailure.LISTENING_LOST
                    }
                    MESSAGE_CONNECTION_LOST -> {
                        bluetoothFailure.value = BluetoothFailure.CONNECTION_LOST
                        board.restartBoard()
                    }
                    MESSAGE_CONNECTION_FAILED -> {
                        bluetoothFailure.value = BluetoothFailure.CONNECTION_FAILED
                    }
                }
            }
        }

    private fun handleMessageStateChange(msg: Message){
        bluetoothStatus.value = msg.arg1
        when (msg.arg1) {
            STATE_CONNECTED_AS_SERVER -> {
                bluetoothPlayer.value = Player.Sheep
                board.restartBoard()
            }
            STATE_CONNECTED_AS_CLIENT -> {
                bluetoothPlayer.value = Player.Wolf
                board.restartBoard()
            }
            STATE_NONE, STATE_LISTENING, STATE_CONNECTING -> {
                bluetoothPlayer.value = null
                bluetoothDevice = null
            }
        }
    }

    private fun handleMessageRead(msg: Message){
        val readBuf = msg.obj as ByteArray
        val readMessage = String(readBuf, 0, msg.arg1)
        val fromCellKey = readMessage.substring(0, 2)
        val toCellKey = readMessage.substring(2, 4)
        val value = board.getCell(fromCellKey)
        board.setCellAsEmpty(fromCellKey)
        board.setCell(toCellKey, value)

        if (value == CellValue.Wolf.value)
            board.setWolfPosition(toCellKey.substring(0, 1).toInt(), toCellKey.substring(1).toInt())

        if (board.getWolfPosition().first == FIRST_ROW)
            board.handleWolfWonCase()
        else if (!board.hasWolfAnyMovePossible()) board.handleSheepWonCase()

        board.setCurrentPlayer(bluetoothPlayer.value!!)
    }
}
