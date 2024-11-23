package com.looter.rall.data.api.models

import androidx.core.text.parseAsHtml
import org.json.JSONObject

//"media": {
//    "reddit_video": {
//        "bitrate_kbps": 2400,
//        "fallback_url": "https://v.redd.it/rwvldua6d0nc1/DASH_720.mp4?source=fallback",
//        "has_audio": true,
//        "height": 794,
//        "width": 720,
//        "scrubber_media_url": "https://v.redd.it/rwvldua6d0nc1/DASH_96.mp4",
//        "dash_url": "https://v.redd.it/rwvldua6d0nc1/DASHPlaylist.mpd?a=1712481048%2CMDBkNDE2MGM5YzVkMWM5YTFmMzRiMzBiNmFmODFjMTJmY2IyOTNmOTE1NzBiYzc2OGU4Njk5MzFkYjY0MjEwZA%3D%3D&v=1&f=sd",
//        "duration": 6,
//        "hls_url": "https://v.redd.it/rwvldua6d0nc1/HLSPlaylist.m3u8?a=1712481048%2CZjI5YjQwNmEzZmY0M2IyYzkzNjFkMjE1YTNjNTc3NWU2MTg4Y2VlMDk3ODBmYjY0NTJhMjcwOWUwNzBkMjU2MA%3D%3D&v=1&f=sd",
//        "is_gif": false,
//        "transcoding_status": "completed"
//    }
//}
data class RedditVideoJson(
    private val redditVideo: JSONObject
) {
    val hlsUrl get() = redditVideo.getString("hls_url").parseAsHtml().toString()
    val fallbackUrl get() = redditVideo.getString("fallback_url")
    val dashUrl get() = redditVideo.getString("dash_url")
    val height get() = redditVideo.getInt("height")
    val width get() = redditVideo.getInt("width")
}