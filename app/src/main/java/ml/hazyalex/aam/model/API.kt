package ml.hazyalex.aam.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object API {
    private val client: OkHttpClient = OkHttpClient()
    val jsonParser: Json = Json(JsonConfiguration(ignoreUnknownKeys = true))

    /**
     * GET https://api.jikan.moe/v3/season
     */
    fun getAnimeCurrentSeason(callback: Callback) {
        val request: Request = Request.Builder()
            .url("https://api.jikan.moe/v3/season")
            .build()

        client.newCall(request).enqueue(callback)
    }

    /**
     * GET https://api.jikan.moe/v3/search/anime
     *
     * NOTE: MyAnimeList only processes queries with a minimum of 3 letters
     */
    fun search(parameters: Map<String, String>, callback: Callback) {
        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("api.jikan.moe")
            .addPathSegment("v3")
            .addPathSegment("search")
            .addPathSegment("anime")

        parameters.forEach { urlBuilder.addQueryParameter(it.key, it.value) }

        val request: Request = Request.Builder()
            .url(urlBuilder.build())
            .build()

        client.newCall(request).enqueue(callback)
    }

    /**
     * GET https://api.jikan.moe/v3/anime/{animeID}
     *
     * @param animeID MyAnimeListID of the anime you're trying to get details of
     */
    fun getAnimeDetails(animeID: Long, callback: Callback) {
        val request: Request = Request.Builder()
            .url("https://api.jikan.moe/v3/anime/$animeID")
            .build()

        client.newCall(request).enqueue(callback)
    }
}
