package com.example.mediabrowserservice.api

import android.net.Uri
import com.example.mediabrowserservice.api.ConstantAPI.METHOD_GET
import com.example.mediabrowserservice.api.ConstantAPI.READ_TIME_OUT
import com.example.mediabrowserservice.model.Song
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object ConstantAPI {
    const val METHOD_GET = "GET"
    const val READ_TIME_OUT = 3000
}
object SongAPI {

    const val URL_SONG = "https://62a4325a47e6e400638de307.mockapi.io/songFromFirebase"

    fun getAPiSong(URL: String ) : MutableList<Song> {
        val builtURI: Uri = Uri.parse(URL).buildUpon()
            .build()
        val requestURL = URL(builtURI.toString())
        val connection = requestURL.openConnection() as HttpURLConnection
        connection.run {
            requestMethod = METHOD_GET
            readTimeout = READ_TIME_OUT
            connectTimeout = READ_TIME_OUT
            connect()
        }
        val data =  connection.inputStream.bufferedReader().readText()
        val jsonArray = JSONArray(data)
        val listSong = mutableListOf<Song>()
        for (i in 0 until jsonArray.length()){
            val jsonQuotes = jsonArray.optJSONObject(i)
            listSong.add(Song(
                mediaId = jsonQuotes.optString("mediaId"),
                title = jsonQuotes.optString("title"),
                subtitle = jsonQuotes.optString("subtitle"),
                songUrl = jsonQuotes.optString("songUrl"),
                imageUrl = jsonQuotes.optString("imageUrl")
            ))
        }
        return listSong
    }
}
