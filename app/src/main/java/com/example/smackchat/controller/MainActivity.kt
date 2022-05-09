package com.example.smackchat.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smackchat.R
import com.example.smackchat.Adapter.MessageRecyclerAdapter
import com.example.smackchat.Model.Channel
import com.example.smackchat.Model.Message
import com.example.smackchat.support.*
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_channel.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    lateinit var messageAdapter: MessageRecyclerAdapter
    lateinit var channelAdapter: ArrayAdapter<Channel>
    val socket = IO.socket(SOCKET_URL)
    var selectedChannel : Channel? = null

    private fun setAdapters() {
        messageAdapter = MessageRecyclerAdapter(this, MessageService.messages)
        msgRecycler.adapter = messageAdapter

        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
        val layoutManager = LinearLayoutManager(this)
        msgRecycler.layoutManager = layoutManager

    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)

        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

       /*
            LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))
        */

        if (App.prefs.isLoggedIn) {
            navUsername.text = UserDataService.name

            println("user ${UserDataService.name} nav $navUsername")
            val resourceId =
                resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
            navUserImage.setImageResource(resourceId)
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByMail(this){}
        }

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)



        setAdapters()


        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }


    }

    private val onNewChannel = Emitter.Listener {args ->

        runOnUiThread{
            val channelName = args[0] as String
            val channelDescription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDescription, channelId)
            MessageService.channels.add(newChannel)
            channelAdapter.notifyDataSetChanged()
            println(newChannel.name)

        }
    }

    private val onNewMessage = Emitter.Listener { args->

        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String
                if (channelId == selectedChannel?.id) {
                    val msgBody = args[0] as String

                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    //val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(msgBody, userName, channelId, userAvatar, id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    msgRecycler.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun toLoginBtnClick(view: View) {
        val toLoginIntent = Intent(this, LoginActivity::class.java)
        startActivity(toLoginIntent)
    }

    fun logoutBtnClick(view: View) {
        UserDataService.logout()
        navUserImage.setImageResource(R.drawable.dark4)
        val outToIn = Intent(this, LoginActivity::class.java)
        startActivity(outToIn)
    }

    fun sendMsgBtnClick(view: View){
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId,
                UserDataService.name, UserDataService.avatarName)
            messageTextField.text.clear()
            hideKeyboard()
        }
    }
    fun addChannelBtnClick(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel, null)

            builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val channelN = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val channelD = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)
                    val channelName = channelN.text.toString()
                    val channelDesc = channelD.text.toString()

                    socket.emit("newChannel", channelName, channelDesc)
                }
                .setNegativeButton("Cancel") { _, _ ->

                    hideKeyboard()
                }
                .show()
        }
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun updateWithChannel(){
        currentChatName.text = selectedChannel?.name
        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if (complete) {
                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0) {
                        msgRecycler.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                navUsername.text = UserDataService.name
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                    packageName)
                navUserImage.setImageResource(resourceId)

                MessageService.getChannels{ complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }
}