package no.stelar7.kotmaw.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

object JsonUtil
{
    val gson: Gson by lazy {
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    }

    fun toJson(obj: Any): String
    {
        return gson.toJson(obj)
    }


    inline fun <reified T: Any> fromJson(data: String): T?
    {
        if (data.startsWith("{\"status\":{")) return null

        val internal = if (data.startsWith('"')) "{\"value\"=$data}" else data
        return gson.fromJson(internal, T::class.java)
    }

    inline fun <reified T: Any> fromJson(data: JsonObject): T
    {
        return gson.fromJson(data, T::class.java)
    }
}
