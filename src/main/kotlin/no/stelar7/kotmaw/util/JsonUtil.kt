package no.stelar7.kotmaw.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class JsonUtil
{
    companion object
    {
        val gson: Gson by lazy {
            GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        }

        fun toJson(obj: Any): String
        {
            return gson.toJson(obj)
        }


        inline fun <reified T: Any> fromJson(data: String?): T?
        {
            return gson.fromJson(data, T::class.java)
        }
    }
}