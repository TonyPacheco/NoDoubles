package com.nodoubles.app.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.nodoubles.app.*
import com.nodoubles.app.Models.Tourney
import com.squareup.picasso.Picasso
import java.util.*
import android.text.InputType
import com.nodoubles.app.App.Globals.auth
import com.nodoubles.app.App.Globals.isLoggedIn
import kotlinx.android.synthetic.main.tournament.view.*


@Suppress("DEPRECATION")
class TournamentListAdapter(private val context: Context, private val tourneys: ArrayList<Tourney>) : RecyclerView.Adapter<TournamentListAdapter.CustomViewHolder>() {
    private val res: Resources = context.resources

    override fun getItemCount(): Int {
        return tourneys.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.tournament, parent, false)
        return CustomViewHolder(v)
    }

    override fun onBindViewHolder(h: CustomViewHolder, i: Int) {
        val tourney = tourneys[i]
        h.title.text = tourney.name
        h.lock.visibility = if(tourney.privacy != Tourney.PRIVACY_PASSWRD) View.GONE else View.VISIBLE
        h.root.setOnClickListener {
            if(tourney.privacy == Tourney.PRIVACY_PASSWRD){
                /* Tournament requires password */
                if(isLoggedIn() && tourney.organizers.contains(auth.currentUser?.uid)){
                    openTourney(tourney)
                } else {
                    val alert = AlertDialog.Builder(context)
                    alert.setTitle("Password")
                    val pass = EditText(context)
                    pass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    alert.setView(pass)
                    alert.setPositiveButton("Enter") { _,_ ->
                        if(pass.text.toString() == tourney.password){
                            openTourney(tourney)
                        }
                    }
                    alert.setNegativeButton("Back")  { d,_ ->
                        d.dismiss()
                    }
                    alert.show()
                }
            } else {
                openTourney(tourney)
            }
        }
        val url = tourney.photoURL
        if (url != null && url != "")
            Picasso.get()
                    .load(url)
                    .placeholder(res.getDrawable(R.drawable.ic_sword_cross))
                    .error(res.getDrawable(R.drawable.ic_sword_cross))
                    .into(h.photo)
        else
            h.photo.setImageDrawable(context.resources.getDrawable(R.drawable.ic_sword_cross))
    }

    private fun openTourney(tourney: Tourney) {
        App.Globals.TourneyID = tourney.id
        App.Globals.tourney = tourney
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putInt("tourneyID", tourney.id)
        editor.apply()
        App.Globals.isAdmin = App.Globals.isLoggedIn() && tourney.organizers.contains(App.Globals.auth.currentUser!!.uid)
        val intent = Intent(context, ViewRosterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    inner class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var root: View = v.rootView
        var title: TextView = v.title
        var photo: ImageView = v.tourney_photo
        var lock: ImageView = v.lock
    }

}
