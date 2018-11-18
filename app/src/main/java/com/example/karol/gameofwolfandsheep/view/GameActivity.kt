package com.example.karol.gameofwolfandsheep.view

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.databinding.GameActivityBinding
import com.example.karol.gameofwolfandsheep.model.Player
import com.example.karol.gameofwolfandsheep.viewmodel.GameViewModel

class GameActivity : AppCompatActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)
        setupBinding()
        setupObserver()
    }

    private fun setupBinding(){
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
        val activityBinding = DataBindingUtil.setContentView<GameActivityBinding>(this, R.layout.game_activity)
        activityBinding.gameViewModel = viewModel
        viewModel.init()
    }

    private fun setupObserver(){
        viewModel.getWinner().observe(this, Observer {
                player -> showDialog(player)
        })
    }

    private fun showDialog(player: Player){
        val alertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Game has ended")
            .setPositiveButton(player.toString() + " player has won!") { _, _ ->this.startActivity(LaunchActivity.intent(this)) }
            .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    companion object {
        fun intent(context: Context) = Intent(context, GameActivity::class.java)
    }
}