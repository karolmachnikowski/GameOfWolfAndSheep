package com.example.karol.gameofwolfandsheep.view

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.viewmodel.LaunchViewModel

class LaunchFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = LaunchFragment()
    }

    private lateinit var viewModel: LaunchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.launch_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LaunchViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
