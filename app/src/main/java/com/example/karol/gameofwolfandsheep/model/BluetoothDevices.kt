package com.example.karol.gameofwolfandsheep.model

import android.bluetooth.BluetoothDevice
import androidx.databinding.ObservableArrayList
import com.example.karol.gameofwolfandsheep.utils.BluetoothService

const val NO_DEVICES_PAIRED = "No devices have been paired"
const val NO_DEVICES_FOUND = "No devices have been found"

class BluetoothDevices {

    private var discoveredDevices = ObservableArrayList<String>()
    private var pairedDevices = ObservableArrayList<String>()

    fun getDiscoveredDevices() = discoveredDevices

    fun getPairedDevices() = pairedDevices

    fun addDiscoveredDevice(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            val deviceInfo = if (device.name != null) device.name + "\n" + device.address else device.address
            if (!discoveredDevices.contains(deviceInfo)) {
                discoveredDevices.add(deviceInfo)
            }
        }
    }

    fun updatePairedDevices() {
        pairedDevices.clear()
        BluetoothService.getPairedDevices().forEach {
            pairedDevices.add((it.name + "\n" + it.address))
        }
        if (pairedDevices.size == 0) {
            pairedDevices.add(NO_DEVICES_PAIRED)
        }
    }

    fun setNoDevicesDiscoveredMessage() {
        discoveredDevices.add(NO_DEVICES_FOUND)
    }
}