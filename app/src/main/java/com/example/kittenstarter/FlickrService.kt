package com.example.kittenstarter

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/*
 https://www.flickr.com/services/rest/?
    method=flickr.photos.search&
    api_key=1694c8371b676e2b1cf9000245f9b1f2&
    text=kubachi&
    format=json&
    nojsoncallback=1&
    api_sig=55e281fcddefbcff602f41ec0b035702
 */

interface FlickrService {
    @GET("/services/rest/")
    fun search(
        @Query("method")         method: String,
        @Query("api_key")        key: String,
        @Query("text")           text: String,
        @Query("format")         format: String,
        @Query("nojsoncallback") nojsoncallback: Int,
        @Query("page")           page: Int
    ) : Call<Result>
}