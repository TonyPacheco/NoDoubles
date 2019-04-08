package com.nodoubles.app.Adapters

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nodoubles.app.App
import com.nodoubles.app.Models.Tourney
import com.nodoubles.app.R
import com.nodoubles.app.ViewRosterActivity
import com.squareup.picasso.Picasso
import java.util.*

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
        h.root.setOnClickListener {
            App.Globals.TourneyID = tourney.id
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putInt("tourneyID", tourney.id)
            editor.apply()
            App.Globals.isAdmin = App.Globals.isLoggedIn() && tourney.organizers.contains(App.Globals.auth.currentUser!!.uid)
            val intent = Intent(context, ViewRosterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
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

    inner class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var root: View = v.rootView
        var title: TextView = v.findViewById(R.id.title)
        var photo: ImageView = v.findViewById(R.id.tourney_photo)
    }

}
