package com.nodoubles.app

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.nodoubles.app.App.Globals.isAdmin
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)
        isAdmin = false

        btn_find_tourney.setOnClickListener {
            startAnon()
        }

        btn_admin.setOnClickListener {
            createSignInIntent()
        }

    }

    private fun startAnon() {
        clearLoggedInUsers()
        isAdmin = false
        startActivity(Intent(applicationContext, FindTourneyActivity::class.java))
        finish()
    }

    private fun clearLoggedInUsers() {
        //Log out firebase users
        signOut()
    }

    private fun createSignInIntent() {
        // Choose authentication providers
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.d("LOGIN", "SUCCESS")
                startActivity(Intent(this, FindTourneyActivity::class.java))
                finish()
            } else {
                Log.d("LOGIN", "FAIL")
                Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // What do we do after signing out?
                }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }

    override fun onBackPressed() {
        signOut()
        finish()
        moveTaskToBack(true)
    }

}

