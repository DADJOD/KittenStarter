package com.example.kittenstarter

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Result {
    @SerializedName("photos")
    @Expose
    var photos: Photos? = null

    @SerializedName("stat")
    @Expose
    var stat: String? = null
}