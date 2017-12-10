package no.stelar7.kotmaw.riotconstant

import java.util.*

enum class Platform
{
    EUW1,
    NA1;

    override fun toString(): String
    {
        return name.toLowerCase(Locale.ENGLISH)
    }

}