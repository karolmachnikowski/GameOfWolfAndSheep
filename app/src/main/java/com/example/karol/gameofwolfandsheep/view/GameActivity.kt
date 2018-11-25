package com.example.karol.gameofwolfandsheep.view

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.karol.gameofwolfandsheep.R
import com.example.karol.gameofwolfandsheep.databinding.GameActivityBinding
import com.example.karol.gameofwolfandsheep.model.BluetoothFailure
import com.example.karol.gameofwolfandsheep.model.Player
import com.example.karol.gameofwolfandsheep.utils.*
import com.example.karol.gameofwolfandsheep.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.game_activity.*

const val REQUEST_CONNECT_DEVICE = 1
const val REQUEST_ENABLE_BT_TO_CONNECT = 2
const val REQUEST_ENABLE_BT_TO_ALLOW_CONNECT = 3

class GameActivity : AppCompatActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)
        setupViewModelAndBinding()
        setupObservers()
    }

    private fun setupViewModelAndBinding() {
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
        val activityBinding = DataBindingUtil.setContentView<GameActivityBinding>(this, R.layout.game_activity)
        activityBinding.gameViewModel = viewModel
    }

    private fun setupObservers() {
        viewModel.winner.observe(this, Observer { winner ->
            showGameEndedDialog(winner)
        })

        viewModel.bluetoothStatus.observe(this, Observer { appStatus ->
            this.invalidateOptionsMenu()
            bluetooth_state_text_view.text =
                    when (appStatus) {
                        STATE_CONNECTED_AS_CLIENT, STATE_CONNECTED_AS_SERVER ->
                            getString(R.string.connected_state_title, viewModel.bluetoothDevice)
                        STATE_CONNECTING -> getString(R.string.connecting_state_title)
                        STATE_LISTENING -> getString(R.string.listening_state_title)
                        else -> getString(R.string.none_state_title)
                    }
        })

        viewModel.bluetoothPlayer.observe(this, Observer { bluetoothPlayer ->
            if (bluetoothPlayer != null) {
                playing_as_text_view.text = getString(R.string.playing_as, bluetoothPlayer.toString())
            } else {
                playing_as_text_view.text = null
            }
        })

        viewModel.bluetoothFailure.observe(this, Observer { failure ->
            val msg = when (failure) {
                BluetoothFailure.LISTENING_LOST -> getString(R.string.listening_lost)
                BluetoothFailure.CONNECTION_LOST -> getString(R.string.connection_lost)
                BluetoothFailure.CONNECTION_FAILED -> getString(R.string.connection_failed)
                else -> null
            }
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.currentPlayer.observe(this, Observer { player ->
            current_player_text_view.text = getString(R.string.player_turn, player.toString())
        })
    }

    private fun showGameEndedDialog(player: Player) {
        val alertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.game_ended_dialog_title)
            .setMessage(getString(R.string.has_won, player.toString()))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                viewModel.restartGame()
            }
            .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun askForBuetoothEnabling(requestCode: Int) {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, requestCode)
    }

    private fun showBluetoothNeededToast() {
        Toast.makeText(this, getString(R.string.bluetooth_needed), Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val address = data?.extras?.getString(EXTRA_DEVICE_ADDRESS)
                    viewModel.connectToDevice(address)
                }
            }
            REQUEST_ENABLE_BT_TO_CONNECT -> {
                if (resultCode == Activity.RESULT_OK) {
                    this.startActivityForResult(ConnectActivity.intent(this), REQUEST_CONNECT_DEVICE)
                } else {
                    showBluetoothNeededToast()
                }
            }
            REQUEST_ENABLE_BT_TO_ALLOW_CONNECT -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.listenForConnections()
                } else {
                    showBluetoothNeededToast()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.game_activity_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val allowToConnectMenuItem = menu?.findItem(R.id.allow_to_connect)
        val connectMenuItem = menu?.findItem(R.id.connect)
        val disconnectMenuItem = menu?.findItem(R.id.disconnet)
        when (viewModel.getBluetoothState()) {
            STATE_NONE -> {
                allowToConnectMenuItem?.setTitle(R.string.allow_to_connect_menu_item)
            }
            STATE_LISTENING -> {
                allowToConnectMenuItem?.setTitle(R.string.disallow_to_connect_menu_item)
            }
            else -> {
                connectMenuItem?.isVisible = false
                allowToConnectMenuItem?.isVisible = false
                disconnectMenuItem?.isVisible = true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.connect -> {
                if(!viewModel.isBluetoothEnabled()){
                    askForBuetoothEnabling(REQUEST_ENABLE_BT_TO_CONNECT)
                } else {
                    this.startActivityForResult(ConnectActivity.intent(this), REQUEST_CONNECT_DEVICE)
                }
                return true
            }
            R.id.disconnet -> {
                viewModel.stopBluetoothUtil()
                return true
            }
            R.id.allow_to_connect -> {
                when (viewModel.getBluetoothState()) {
                    STATE_LISTENING -> viewModel.stopBluetoothUtil()
                    else -> {
                        if(!viewModel.isBluetoothEnabled()){
                            askForBuetoothEnabling(REQUEST_ENABLE_BT_TO_ALLOW_CONNECT)
                        } else {
                            viewModel.listenForConnections()
                        }
                    }
                }
                return true
            }
            R.id.discoverable -> {
                viewModel.ensureDiscoverable(this)
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopBluetoothUtil()
    }
}