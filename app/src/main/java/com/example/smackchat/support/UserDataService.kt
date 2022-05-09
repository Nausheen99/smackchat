package com.example.smackchat.support

import com.example.smackchat.controller.App

object UserDataService {
    var id = ""
    var avatarName = ""
    //var avatarColor = ""
    var email = ""
    var name = ""

    fun logout(){
        id = ""
        avatarName = ""
        email = ""
        name = ""
        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false

    }
}