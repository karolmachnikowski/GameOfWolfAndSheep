package com.example.karol.gameofwolfandsheep.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel

const val NO_DEVICES_PAIRED = "No devices have been paired"
const val NO_DEVICES_FOUND = "No devices have been found"

class ConnectViewModel : ViewModel() {

    private var discoveredDevices = ObservableArrayList<String>()
    private var pairedDevices: ArrayList<String>
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    init {
        pairedDevices = arrayListOf<String>().apply {
            bluetoothAdapter.bondedDevices.forEach {
                this.add((it.name + "\n" + it.address))
            }
            if (this.size == 0) {
                this.add(NO_DEVICES_PAIRED)
            }
        }
    }

    fun getDiscoveredDevices() = discoveredDevices

    fun getPairedDevices() = pairedDevices

    fun onReceiveBroadcast(intent: Intent) {
        val action = intent.action

        if (BluetoothDevice.ACTION_FOUND == action) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                val deviceInfo = if (device.name != null) device.name + "\n" + device.address
                else device.address
                if (!discoveredDevices.contains(deviceInfo))
                    discoveredDevices.add(deviceInfo)
            }

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            if (discoveredDevices.size == 0) {
                discoveredDevices.add(NO_DEVICES_FOUND)
            }

        }
    }

    fun startDiscovery() {
        cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    fun cancelDiscovery() {
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }
}