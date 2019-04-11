package com.nodoubles.app.Models

class Tourney() {

    companion object {
        const val SCORE_TYPE_STANDARD_WEIGHTING = 0
        const val SCORE_TYPE_REGION_BASED_SCORE = 1
        const val STANDARD_SCHEME_ID = "4_3_2_1_3_1_-1_-2"
        const val PRIVACY_OPEN = 0
        const val PRIVACY_PRIVATE = 1
        const val PRIVACY_PASSWRD = 2
    }

    var id: Int = 0
    var name: String = ""
    var photoURL: String? = null
    val organizers: ArrayList<String> = ArrayList()
    var admin: String = ""
    var privacy: Int = PRIVACY_OPEN
    var password: String = ""

    var scoreType: Int = SCORE_TYPE_STANDARD_WEIGHTING
    var scoreSchemeId: String = STANDARD_SCHEME_ID
    var trackUserHits = false

    init {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
    }

    constructor(name: String, creator: String, photoURL: String, scoreType: Int, privacy: Int, pw:String="") : this() {
        this.name = name
        this.organizers.add(creator)
        this.photoURL = photoURL
        this.scoreType = scoreType
        this.privacy = privacy
        this.password = pw
        admin = creator
    }

    constructor(name: String, creator: String, photoURL: String, scoreType: Int,
                scheme: String, trackHits: Boolean, privacy: Int, pw:String="") :
            this(name, creator, photoURL, scoreType, privacy, pw){
        scoreSchemeId = scheme
        trackUserHits = trackHits
    }

}