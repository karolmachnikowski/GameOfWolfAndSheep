package com.example.karol.gameofwolfandsheep.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.karol.gameofwolfandsheep.model.BluetoothDevices
import com.example.karol.gameofwolfandsheep.utils.BluetoothService

class ConnectViewModel : ViewModel() {

    private val devices = BluetoothDevices()
    private var wasDiscoveryStarted = false

    init {
        devices.updatePairedDevices()
    }

    fun getDiscoveredDevices() = devices.getDiscoveredDevices()

    fun getPairedDevices() = devices.getPairedDevices()

    fun updatePairedDevices() {
        devices.updatePairedDevices()
    }

    fun onReceiveBroadcast(intent: Intent) {
        val action = intent.action

        if (BluetoothDevice.ACTION_FOUND == action) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            devices.addDiscoveredDevice(device)

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            if (getDiscoveredDevices().size == 0 && wasDiscoveryStarted) {
                devices.setNoDevicesDiscoveredMessage()
            }
        }
    }

    fun startDiscovery() {
        BluetoothService.startDiscovery()
        wasDiscoveryStarted = true
    }

    fun cancelDiscovery() {
        BluetoothService.cancelDiscovery()
    }
}