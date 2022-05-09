package com.example.smackchat.support

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.smackchat.controller.App
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser(email: String, password: String, complete: (Boolean)-> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            println("register $response")
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.prefs.requestQueue.add(registerRequest)
    }

    fun loginUser( email: String, password: String, complete: (Boolean) -> Unit){
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->

            try {
                App.prefs.userEmail = response.getString("user")
                App.prefs.authToken = response.getString("token")
                App.prefs.isLoggedIn = true
                complete(true)
                println("login $response")
                //println(isLoggedIn)

            }
            catch (e: JSONException){
                Log.d("JSON", "Exc:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not login user: $error")
            complete(false)
        }){

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

        }
        App.prefs.requestQueue.add(loginRequest)
    }


    fun createUser(name:String, email: String, avatarName: String, complete: (Boolean) -> Unit){
        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)

        val requestBody = jsonBody.toString()

        val createUserRequest = object : JsonObjectRequest(Method.POST, URL_ADD, null, Response.Listener { response ->
            try{
                println(response)
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.id = response.getString("_id")
                UserDataService.avatarName = response.getString("avatarName")
                complete(true)
            }
            catch(e : JSONException){
                Log.d("JSON", "Exc:" + e.localizedMessage)
                complete(false)

            }

        },Response.ErrorListener { error ->
            Log.d("ERROR", "Could not add user $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers

            }
        }
        App.prefs.requestQueue.add(createUserRequest)

    }

    fun findUserByMail(context: Context, complete: (Boolean) -> Unit){
        val findUserRequest = object : JsonObjectRequest(Method.GET, "$URL_GET_USER${App.prefs.userEmail}", null,
            Response.Listener {response ->
                try{
                    UserDataService.name = response.getString(("name"))
                    UserDataService.email = response.getString("email")
                    UserDataService.avatarName = response.getString("avatarName")
                    UserDataService.id = response.getString("_id")

                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)

                    complete(true)

                }catch (e: JSONException){
                    Log.d("JSON", "Exc:" + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not find user $error")
                complete(false) }){

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers

            }
        }
        App.prefs.requestQueue.add(findUserRequest)
    }
}