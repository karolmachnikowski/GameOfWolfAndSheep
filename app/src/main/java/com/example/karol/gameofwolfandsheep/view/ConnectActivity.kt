package com.example.karol.gameofwolfandsheep.view

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.karol.gameofwolfandsheep.R
import kotlinx.android.synthetic.main.activity_device_list.*

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADRESS_KEY"

class ConnectActivity : AppCompatActivity() {

    private var mBtAdapter: BluetoothAdapter? = null

    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.connect_activity)
        setResult(Activity.RESULT_CANCELED)

        button_scan.setOnClickListener { v ->
            doDiscovery()
            v.visibility = View.GONE
        }

        val pairedDevicesArrayAdapter = ArrayAdapter<String>(this, R.layout.device_list_element)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_list_element)

        paired_devices.adapter = pairedDevicesArrayAdapter
        paired_devices.onItemClickListener = mDeviceClickListener

        new_devices.adapter = mNewDevicesArrayAdapter
        new_devices.onItemClickListener = mDeviceClickListener

        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        val pairedDevices = mBtAdapter!!.bondedDevices

        if (pairedDevices.size > 0) {
            title_paired_devices.visibility = View.VISIBLE
            pairedDevices
                .forEach { pairedDevicesArrayAdapter.add(it.name + "\n" + it.address) }

        } else {
            val noDevices = resources.getText(R.string.none_paired).toString()
            pairedDevicesArrayAdapter.add(noDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }

        this.unregisterReceiver(mReceiver)
    }

    private fun doDiscovery() {
        setTitle(R.string.scanning)

        title_new_devices.visibility = View.VISIBLE

        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }

        mBtAdapter!!.startDiscovery()
    }

    private val mDeviceClickListener = AdapterView.OnItemClickListener { _, v, _, _ ->
        mBtAdapter!!.cancelDiscovery()

        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add(
                        if(device.name != null) device.name + "\n" + device.address
                        else device.address
                    )
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setTitle(R.string.select_device)
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = resources.getText(R.string.none_found).toString()
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, ConnectActivity::class.java)
    }

}