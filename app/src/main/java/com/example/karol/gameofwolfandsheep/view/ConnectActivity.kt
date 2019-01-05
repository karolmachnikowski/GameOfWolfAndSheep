package com.example.karol.gameofwolfandsheep.view

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.viewmodel.ConnectViewModel
import kotlinx.android.synthetic.main.connect_activity.*

const val EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS_KEY"
const val MAC_ADDRESS_LENGTH = 17

class ConnectActivity : AppCompatActivity() {

    private lateinit var viewModel: ConnectViewModel

    private lateinit var pairedDevicesRecyclerView: RecyclerView
    private lateinit var pairedDevicesAdapter: RecyclerView.Adapter<*>
    private lateinit var pairedDevicesViewManager: RecyclerView.LayoutManager
    private lateinit var discoveredDevicesRecyclerView: RecyclerView
    private lateinit var discoveredDevicesAdapter: RecyclerView.Adapter<*>
    private lateinit var discoveredDevicesViewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.connect_activity)
        setupViewModel()
        setupObservers()

        setResult(Activity.RESULT_CANCELED)

        pairedDevicesAdapter = DevicesListAdapter(viewModel.getPairedDevices(), onClickHandler)
        pairedDevicesViewManager = LinearLayoutManager(this)

        discoveredDevicesAdapter = DevicesListAdapter(viewModel.getDiscoveredDevices(), onClickHandler)
        discoveredDevicesViewManager = LinearLayoutManager(this)

        pairedDevicesRecyclerView = findViewById<RecyclerView>(R.id.paired_devices).apply {
            layoutManager = pairedDevicesViewManager
            adapter = pairedDevicesAdapter
        }
        discoveredDevicesRecyclerView = findViewById<RecyclerView>(R.id.discovered_devices).apply {
            layoutManager = discoveredDevicesViewManager
            adapter = discoveredDevicesAdapter
        }

        button_scan.setOnClickListener { v ->
            doDiscovery()
            v.visibility = View.GONE
        }

        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(broadcastReveiver, filter)

        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(broadcastReveiver, filter)

        if (viewModel.getPairedDevices().size > 0) {
            title_paired_devices.visibility = View.VISIBLE
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConnectViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.getDiscoveredDevices().addOnListChangedCallback(onDiscoveredDevicesChangedCallback)
        viewModel.getPairedDevices().addOnListChangedCallback(onPairedDevicesChangedCallback)
    }

    private fun doDiscovery() {
        setTitle(R.string.scanning)
        title_discovered_devices.visibility = View.VISIBLE

        viewModel.startDiscovery()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePairedDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelDiscovery()

        this.unregisterReceiver(broadcastReveiver)
    }

    private val onClickHandler = object : DevicesListAdapter.DevicesAdapterOnClickHandler {
        override fun onClick(view: View) {
            viewModel.cancelDiscovery()

            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - MAC_ADDRESS_LENGTH)

            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private val broadcastReveiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setTitle(R.string.select_device)
            }

            viewModel.onReceiveBroadcast(intent)
        }
    }

    private val onDiscoveredDevicesChangedCallback = object :
        ObservableList.OnListChangedCallback<ObservableArrayList<String>>() {
        override fun onItemRangeInserted(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
            discoveredDevicesAdapter.notifyDataSetChanged()
        }

        override fun onChanged(sender: ObservableArrayList<String>?) {
        }

        override fun onItemRangeRemoved(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
        }

        override fun onItemRangeMoved(
            sender: ObservableArrayList<String>?,
            fromPosition: Int, toPosition: Int, itemCount: Int
        ) {
        }

        override fun onItemRangeChanged(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
        }
    }

    private val onPairedDevicesChangedCallback = object :
        ObservableList.OnListChangedCallback<ObservableArrayList<String>>() {
        override fun onItemRangeInserted(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
            pairedDevicesAdapter.notifyDataSetChanged()
        }

        override fun onChanged(sender: ObservableArrayList<String>?) {
        }

        override fun onItemRangeRemoved(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
        }

        override fun onItemRangeMoved(
            sender: ObservableArrayList<String>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
        }

        override fun onItemRangeChanged(sender: ObservableArrayList<String>?, positionStart: Int, itemCount: Int) {
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, ConnectActivity::class.java)
    }
}