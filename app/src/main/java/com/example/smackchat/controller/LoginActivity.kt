package com.example.smackchat.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.support.AuthService
import com.example.smackchat.support.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.support.UserDataService
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlin.math.log

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }


    fun loginBtnClick (view: View) {
        //errorToast("btn clicked")
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        hideKeyboard()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.loginUser(email, password) { loginSuccess ->
                if (loginSuccess) {
                    AuthService.findUserByMail(this) { findSuccess ->
                        if (findSuccess) {
                            val loginIntent = Intent(this, MainActivity::class.java)
                            startActivity(loginIntent)
                            finish()
                        } else { errorToast("something went wrong | finduser") }
                    }
                } else { errorToast("something went wrong | login") }
            }
        } else { errorToast("Please enter all credentials")}
    }


    fun toSignupBtnClick(view: View){
        val toSignupIntent = Intent(this, SignupActivity::class.java)
        startActivity(toSignupIntent)
        finish()
    }

    fun errorToast(text:String){
        Toast.makeText(this, text,
            Toast.LENGTH_LONG).show()

    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}