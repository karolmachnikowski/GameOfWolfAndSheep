package com.example.karol.gameofwolfandsheep.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.karol.gameofwolfandsheep.viewmodel.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

const val SERVICE_NAME = "BluetoothGameUtil"
const val DISCOVERABLE_TIME = 300

const val STATE_NONE = 0
const val STATE_LISTENING = 1
const val STATE_CONNECTING = 2
const val STATE_CONNECTED_AS_SERVER = 3
const val STATE_CONNECTED_AS_CLIENT = 4

const val MESSAGE_STATE_CHANGE = 1
const val MESSAGE_READ = 2
const val MESSAGE_WRITE = 3
const val MESSAGE_DEVICE_NAME = 4
const val MESSAGE_BT_TURNED_OF_WHILE_LISTENING = 5
const val MESSAGE_CONNECTION_FAILED = 6
const val MESSAGE_CONNECTION_LOST = 7
const val MESSAGE_WRITE_FAILED = 8

const val DEVICE_NAME = "DEVICE_NAME_KEY"

object BluetoothGameUtil {

    private val serviceUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private lateinit var handler: Handler
    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var connectionState = STATE_NONE

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    fun ensureDiscoverable(context: Context) {
        if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .apply { putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIME) }
            context.startActivity(discoverableIntent)
        }
    }

    @Synchronized
    private fun handleStateChange() {
        connectionState = getState()

        handler.obtainMessage(MESSAGE_STATE_CHANGE, connectionState, -1).sendToTarget()
    }

    @Synchronized
    fun getState(): Int {
        return connectionState
    }

    @Synchronized
    fun listenForConnections() {
        connectThread?.cancel()
        connectThread = null

        connectedThread?.cancel()
        connectedThread = null

        connectionState = STATE_LISTENING
        acceptThread = AcceptThread()
        acceptThread!!.start()

        handleStateChange()
    }

    @Synchronized
    fun connect(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)
        if (connectionState == STATE_CONNECTING) {
            connectThread?.cancel()
            connectThread = null

        }

        connectedThread?.cancel()
        connectedThread = null

        connectionState = STATE_CONNECTING
        connectThread = ConnectThread(device)
        connectThread!!.start()

        handleStateChange()
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice, isServiceOwner: Boolean) {
        connectThread?.cancel()
        connectThread = null

        connectedThread?.cancel()
        connectedThread = null

        acceptThread?.cancel()
        acceptThread = null

        connectionState = if (isServiceOwner) STATE_CONNECTED_AS_SERVER else STATE_CONNECTED_AS_CLIENT
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()

        val msg = handler.obtainMessage(MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(DEVICE_NAME, device.name)
        msg.data = bundle
        handler.sendMessage(msg)

        handleStateChange()
    }

    @Synchronized
    fun stop() {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread
        }

        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }

        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }

        connectionState = STATE_NONE
        handleStateChange()
    }


    fun write(out: ByteArray) {
        val r: ConnectedThread
        synchronized(this) {
            if (connectionState != STATE_CONNECTED_AS_SERVER && connectionState != STATE_CONNECTED_AS_CLIENT) return
            r = connectedThread!!
        }
        r.write(out)
    }

    private fun listeningLost() {
        handler.obtainMessage(MESSAGE_BT_TURNED_OF_WHILE_LISTENING).let { handler.sendMessage(it) }

        connectionState = STATE_NONE
        handleStateChange()
    }

    private fun connectionFailed() {
        handler.obtainMessage(MESSAGE_CONNECTION_FAILED).let { handler.sendMessage(it) }

        connectionState = STATE_NONE
        handleStateChange()
    }

    private fun connectionLost() {
        handler.obtainMessage(MESSAGE_CONNECTION_LOST).let { handler.sendMessage(it) }

        connectionState = STATE_NONE
        handleStateChange()
    }

    private class AcceptThread : Thread() {

        private var serverSocket: BluetoothServerSocket =
            bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, serviceUUID)

        override fun run() {
            while (connectionState != STATE_CONNECTED_AS_SERVER && connectionState != STATE_CONNECTED_AS_CLIENT) {
                val socket: BluetoothSocket? = try {
                    serverSocket.accept()
                } catch (e: IOException) {
                    Log.e(BluetoothGameUtil::javaClass.name, "Socket's accept() method failed", e)
                    null
                }

                if(!isBluetoothEnabled()){
                    listeningLost()
                    return
                }

                if (socket != null) {
                    synchronized(this) {
                        when (connectionState) {
                            STATE_LISTENING, STATE_CONNECTING ->
                                connected(socket, socket.remoteDevice, true)
                            STATE_NONE, STATE_CONNECTED_AS_SERVER, STATE_CONNECTED_AS_CLIENT ->
                                serverSocket.close()
                            else -> {
                            }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                Log.e(BluetoothGameUtil::javaClass.name, "Could not close the connect socket", e)
            }
        }
    }

    private class ConnectThread(val device: BluetoothDevice) : Thread() {

        private val socket = device.createRfcommSocketToServiceRecord(serviceUUID)

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            try {
                socket.connect()
            } catch (e: IOException) {
                try {
                    socket.close()
                } catch (e2: IOException) {
                    Log.e(BluetoothGameUtil::javaClass.name,
                        "unable to close()$socket during connection failure", e2)
                }
                connectionFailed()
                return
            }

            synchronized(this) {
                connectThread = null
            }

            connected(socket, device, false)
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(BluetoothGameUtil::javaClass.name, "Could not close the client socket", e)
            }
        }
    }

    private class ConnectedThread(private val socket: BluetoothSocket) : Thread() {

        private val inStream: InputStream = socket.inputStream
        private val outStream: OutputStream = socket.outputStream

        override fun run() {
            val buffer = ByteArray(1024)
            var numBytes: Int

            while (connectionState == STATE_CONNECTED_AS_SERVER || connectionState == STATE_CONNECTED_AS_CLIENT) {
                numBytes = try {
                    inStream.read(buffer)
                } catch (e: IOException) {
                    Log.e(BluetoothGameUtil::javaClass.name, "Input stream was disconnected", e)
                    connectionLost()
                    break
                }
                handler.obtainMessage(MESSAGE_READ, numBytes, -1, buffer).sendToTarget()
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outStream.write(buffer)
            } catch (e: IOException) {
                Log.e(BluetoothGameUtil::javaClass.name, "Error occurred when sending data", e)
                handler.obtainMessage(MESSAGE_WRITE_FAILED).let { handler.sendMessage(it) }
                return
            }

            handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(BluetoothGameUtil::javaClass.name, "Could not close the connect socket", e)
            }
        }
    }
}