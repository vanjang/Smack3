package com.cochipcho.smack3.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Message
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cochipcho.smack3.Model.Channel
import com.cochipcho.smack3.R
import com.cochipcho.smack3.R.layout.add_channel_dialog
import com.cochipcho.smack3.Services.AuthService
import com.cochipcho.smack3.Services.MessageService
import com.cochipcho.smack3.Services.UserDataService
import com.cochipcho.smack3.Utilities.BROADCAST_USER_DATA_CHANGE
import com.cochipcho.smack3.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    var selectedChannel: Channel? = null

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

    }

//    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        socket.connect()
        socket.on("channelCreated", onNewChannel )
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()

        channel_list.setOnItemClickListener { _, _, position, _ ->
            selectedChannel = MessageService.channels[position]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }


        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }
//        hideKeyboard()

//        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))

    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))
        super.onResume()
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        // onReceive() called when it receives a broadcast
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email

                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)

                println("resource id : ${resourceId}")

                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatartColor(UserDataService.avatarColor))
                loginButtonNavHeader.text = "Logout"

                MessageService.getChannels {
                    if (it) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged() // iOS reloadData()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {
        mainChannelLogin.text = "#${selectedChannel?.name}"
        // download messages for channel
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    fun loginButtonNavClicked(view: View) {

        if (App.prefs.isLoggedIn) {
            UserDataService.logout()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun addChannelClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)
                    val channelName = nameTextField.text.toString()
                    val channelDec = descTextField.text.toString()


                    // Create channel with channel name and description
                    socket.emit("newChannel", channelName, channelDec)
                }
                .setNegativeButton("Cancel") { _, _ ->

                    // Cancel and close the dialog

                }
                .show()

        }
    }

    private val onNewChannel = Emitter.Listener {
        runOnUiThread {
            val channelName = it[0] as String
            val channelDescription = it[1] as String
            val channelId = it[2] as String

            val newChannel = Channel(channelName, channelDescription, channelId)
            MessageService.channels.add(newChannel)

            channelAdapter.notifyDataSetChanged()

        }
    }

    private val onNewMessage = Emitter.Listener {
        runOnUiThread {
            val messageBody = it[0] as String
            val channelId =  it[2] as String
            val username = it[3] as String
            val userAvatar = it[4] as String
            val userAvatarColor = it[5] as String
            val id = it[6] as String
            val timestamp = it[7] as String

            val newMessage = com.cochipcho.smack3.Model.Message(messageBody, username, channelId, userAvatar, userAvatarColor, id, timestamp)
            MessageService.messages.add(newMessage)
            println(newMessage.message)
        }
    }

    fun sendMessageButtonClicked(view: View) {
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId, UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

}
