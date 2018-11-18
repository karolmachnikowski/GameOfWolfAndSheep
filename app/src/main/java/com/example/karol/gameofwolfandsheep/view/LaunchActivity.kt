package com.example.karol.gameofwolfandsheep.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.databinding.LaunchActivityBinding
import com.example.karol.gameofwolfandsheep.viewmodel.LaunchViewModel

class LaunchActivity : AppCompatActivity() {

    private lateinit var viewModel: LaunchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_activity)
        viewModel = ViewModelProviders.of(this).get(LaunchViewModel::class.java)
        setupBinding()
        setupObserver()

    }

    private fun setupBinding(){
        val activityBinding = DataBindingUtil.setContentView<LaunchActivityBinding>(this, R.layout.launch_activity)
        activityBinding.launchViewModel = viewModel
    }

    private fun setupObserver(){
        viewModel.gameStarted.observe(this, Observer {
                gameStarted -> if(gameStarted) startActivity(GameActivity.intent(this))
        })
    }

    companion object {
        fun intent(context: Context) = Intent(context, LaunchActivity::class.java)
    }
}
