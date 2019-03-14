package com.nodoubles.app.Models

class Tourney() {
     var id: Int = 0
     var name: String = ""
     var photoURL: String? = null
     val organizers: ArrayList<String> = ArrayList()
     var admin: String = ""

    init {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
    }

    constructor(name: String, creator: String, photoURL: String) : this() {
        this.name = name
        this.organizers.add(creator)
        admin = creator
        this.photoURL = photoURL
    }

}