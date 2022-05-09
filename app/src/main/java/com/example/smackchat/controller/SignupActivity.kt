package com.example.smackchat.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.support.AuthService
import com.example.smackchat.support.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.support.UserDataService
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.navUserImage
import kotlinx.android.synthetic.main.nav_header_main.*

class SignupActivity : AppCompatActivity() {

    var avi = "dark4"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        createSpinner.visibility = View.INVISIBLE
    }

    fun toLoginBtnClick(view: View) {
        val toLoginIntent = Intent(this, LoginActivity::class.java)
        startActivity(toLoginIntent)
    }

    fun changeAviClick(view: View) {
        val random = java.util.Random()
        val theme = random.nextInt(2)
        val aviNum = random.nextInt(28)

        if (theme == 0) {
            avi = "light$aviNum"
        } else {
            avi = "dark$aviNum"
        }
        val resourceId = resources.getIdentifier(avi, "drawable", packageName)
        navUserImage.setImageResource(resourceId)
    }

    fun signupBtnClick(view: View) {
        createWait(true)
        val username = createNameInput.text.toString()
        val email = createEmailInput.text.toString()
        val password = createPasswordInput.text.toString()

        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() ){
            AuthService.registerUser( email, password) { registerSuccess ->
                if (registerSuccess) {
                    AuthService.loginUser( email, password){ loginSuccess ->
                        if(loginSuccess){
                            AuthService.createUser(username,email, avi){createSuccess ->
                                if ( createSuccess )
                                {

                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)



                                    val toMainIntent = Intent(this, MainActivity::class.java)
                                    startActivity(toMainIntent)

                                    println(UserDataService.name)
                                    println(UserDataService.avatarName)

                                    createWait(false)
                                    finish()
                                }else { errorToast("Something went wrong, please try again. | createfail") }
                            }
                        }else{ errorToast("Something went wrong, please try again. | loginfail") }
                    }
                }else{ errorToast("Something went wrong, please try again. | registerfail") }
            }
        }else{ errorToast( "Please enter all required fields.")
            createWait(false) }
    }

    fun errorToast(text:String){
        Toast.makeText(this, text,
            Toast.LENGTH_LONG).show()
        createWait(false)
    }

    fun createWait(enable:Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        }else{
            createSpinner.visibility = View.INVISIBLE
        }
        signupBtn.isEnabled = !enable
        navUserImage.isEnabled = !enable
    }
}