package com.nodoubles.app

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.nodoubles.app.App.Globals.auth
import com.nodoubles.app.App.Globals.ctx
import com.nodoubles.app.App.Globals.db
import com.nodoubles.app.App.Globals.instance
import com.nodoubles.app.Models.Tourney

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(ctx())
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
    }


    object Globals {
        lateinit var auth: FirebaseAuth
        lateinit var db: FirebaseDatabase
        lateinit var instance: App

        fun ctx(): Context {
            return instance.applicationContext
        }

        fun isLoggedIn() : Boolean {
            return auth.currentUser != null
        }

        var TourneyID: Int = 0
        var tourney: Tourney? = null
        var isAdmin = false
        const val CSV_REQUEST_INT = 42
        const val READ_SUCCESSFUL = 1
        const val HEADER_MISMATCH = -1
        const val CSV_PARSE_ERROR = -2
    }
}
