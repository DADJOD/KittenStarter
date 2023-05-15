package com.example.kittenstarter

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/*
https://api.flickr.com/services/rest/?
    method=flickr.photos.search&
    api_key=7a8da45e81bb8153e93030525b30595b&
    text=moscow&
    format=json&
    nojsoncallback=1&
    api_sig=a5864e3aa8a33a12296974308d04c96a
 */


interface FlickrService {
//    @GET("/services/rest/")
//    fun search(
//        @Query("method") method: String?,
//        @Query("api_key") key: String?,
//        @Query("text") text: String?,
//        @Query("format") format: String?,
//        @Query("nojsoncallback") nojsoncallback: Int,
//        @Query("page") page: Int
//    ): Call<Result?>?
}