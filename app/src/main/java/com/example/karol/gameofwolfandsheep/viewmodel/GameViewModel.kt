package com.example.karol.gameofwolfandsheep.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.karol.gameofwolfandsheep.model.*
import com.example.karol.gameofwolfandsheep.utils.*

class GameViewModel : ViewModel() {

    private val board = Board()
    private val bluetoothStatusObservable = MutableLiveData<Int>().apply { value = STATE_NONE }
    private val bluetoothFailureObservable = MutableLiveData<BluetoothFailureCode>()
    private val bluetoothPlayerObservable = MutableLiveData<String>()
    private val winnerObservable = MutableLiveData<String>()
    private var bluetoothPlayer: Board.Player? = null
    private var bluetoothDevice: String? = null

    init {
        BluetoothGameUtil.setHandler(provideBluetoothMessageHandler())

        board.getWinner().observeForever { player -> winnerObservable.value = player.toString() }
    }

    fun onClickedCellAt(row: Int, column: Int) {
        when (board.getCell(row, column)) {
            Board.CellValue.Sheep ->
                if (board.getCurrentPlayer().value == Board.Player.Sheep
                    && (bluetoothPlayer== null || bluetoothPlayer == Board.Player.Sheep)
                )
                    chooseSheepAndShowPossibleMoves(row, column)
            Board.CellValue.Wolf ->
                if (board.getCurrentPlayer().value == Board.Player.Wolf
                    && (bluetoothPlayer == null || bluetoothPlayer == Board.Player.Wolf)
                ) chooseWolfAndShowPossibleMoves()
            Board.CellValue.PossibleMove -> moveToChosenCell(row, column)
        }
    }

    fun getWinner() = winnerObservable

    fun getCurrentPlayer() = board.getCurrentPlayer()

    fun getCells() = board.getCells()

    fun getBluetoothStatus() = bluetoothStatusObservable

    fun getBluetoothFailure() = bluetoothFailureObservable

    fun getBluetoothPlayer() = bluetoothPlayerObservable

    fun getBluetoothDevice() = bluetoothDevice

    fun restartGame() {
        board.restartBoard()
    }

    fun isBluetoothEnabled() = BluetoothGameUtil.isBluetoothEnabled()

    fun getBluetoothState() = BluetoothGameUtil.getState()

    fun ensureDiscoverable(context: Context) {
        BluetoothGameUtil.ensureDiscoverable(context)
    }

    fun connectToDevice(address: String) {
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
            board.setCell(row + 1, column - 1, Board.CellValue.PossibleMove)
        if (board.hasSheepSouthEastMovePossible(row, column))
            board.setCell(row + 1, column + 1, Board.CellValue.PossibleMove)
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
            Board.CellValue.Wolf ->
                if (row == FIRST_ROW) board.handleWolfWonCase()
                else {
                    board.setWolfPosition(row, column)
                    board.setCurrentPlayer(Board.Player.Sheep)
                }
            Board.CellValue.Sheep ->
                if (!board.hasWolfAnyMovePossible()) board.handleSheepWonCase()
                else board.setCurrentPlayer(Board.Player.Wolf)
        }

        sendBluetoothPawnMovedMessage(pawn, StringUtil.stringFromNumbers(row, column))
        board.setChosenPawnCellAsEmpty()
    }

    private fun clearPossibleMoves() {
        val filteredCells = board.getCells().filterValues { it != Board.CellValue.PossibleMove }
        board.getCells().clear()
        board.getCells().putAll(filteredCells)
    }

    private fun provideBluetoothMessageHandler() =
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_STATE_CHANGE -> {
                        handleBluetoothMessageStateChanged(msg)
                    }
                    MESSAGE_READ -> {
                        handleBluetoothMessageRead(msg)
                    }
                    MESSAGE_DEVICE_NAME -> {
                        bluetoothDevice = msg.data.getString(DEVICE_NAME)
                    }
                    MESSAGE_BT_TURNED_OF_WHILE_LISTENING -> {
                        bluetoothFailureObservable.value = BluetoothFailureCode.LISTENING_LOST
                    }
                    MESSAGE_CONNECTION_LOST -> {
                        bluetoothFailureObservable.value = BluetoothFailureCode.CONNECTION_LOST
                        board.restartBoard()
                    }
                    MESSAGE_CONNECTION_FAILED -> {
                        bluetoothFailureObservable.value = BluetoothFailureCode.CONNECTION_FAILED
                    }
                }
            }
        }

    private fun handleBluetoothMessageStateChanged(msg: Message){
        bluetoothStatusObservable.value = msg.arg1
        when (msg.arg1) {
            STATE_CONNECTED_AS_SERVER -> {
                bluetoothPlayer = Board.Player.Sheep
                bluetoothPlayerObservable.value = bluetoothPlayer.toString()
                board.restartBoard()
            }
            STATE_CONNECTED_AS_CLIENT -> {
                bluetoothPlayer = Board.Player.Wolf
                bluetoothPlayerObservable.value = bluetoothPlayer.toString()
                board.restartBoard()
            }
            STATE_NONE, STATE_LISTENING, STATE_CONNECTING -> {
                bluetoothPlayerObservable.value = null
                bluetoothDevice = null
            }
        }
    }

    private fun handleBluetoothMessageRead(msg: Message){
        val readBuf = msg.obj as ByteArray
        val readMessage = String(readBuf, 0, msg.arg1)
        val fromCellKey = readMessage.substring(0, 2)
        val toCellKey = readMessage.substring(2, 4)
        val value = board.getCell(fromCellKey)
        board.setCellAsEmpty(fromCellKey)
        board.setCell(toCellKey, value)

        if (value == Board.CellValue.Wolf)
            board.setWolfPosition(toCellKey.substring(0, 1).toInt(), toCellKey.substring(1).toInt())

        if (board.getWolfPosition().first == FIRST_ROW)
            board.handleWolfWonCase()
        else if (!board.hasWolfAnyMovePossible()) board.handleSheepWonCase()

        board.setCurrentPlayer(bluetoothPlayer!!)
    }

    private fun sendBluetoothPawnMovedMessage(fromCell: String, toCell: String){
        val msg = fromCell + toCell
        BluetoothGameUtil.write(msg.toByteArray())
    }

    enum class BluetoothFailureCode {
        LISTENING_LOST,
        CONNECTION_LOST,
        CONNECTION_FAILED
    }
}
