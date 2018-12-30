package com.example.karol.gameofwolfandsheep.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.karol.gameofwolfandsheep.R

class DevicesListAdapter(private val devices: ArrayList<String>, private val onClickHandler: DevicesAdapterOnClickHandler) :
    RecyclerView.Adapter<DevicesListAdapter.DeviceViewHolder>() {

    interface DevicesAdapterOnClickHandler {
        fun onClick(view: View)
    }

    inner class DeviceViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView), View.OnClickListener {
        override fun onClick(view: View) {
            onClickHandler.onClick(view)
        }

        init {
            textView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListAdapter.DeviceViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_list_element, parent, false) as TextView

        return DeviceViewHolder(textView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.textView.text = devices[position]
    }

    override fun getItemCount() = devices.size
}