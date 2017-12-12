package no.stelar7.kotmaw.riotconstant

import java.util.*

class Platform
{
    enum class Service
    {
        BR1,
        EUN1,
        EUW1,
        JP1,
        KR,
        LA1,
        LA2,
        NA1,
        OC1,
        TR1,
        RU,
        PBE1;

        override fun toString(): String
        {
            return name.toLowerCase(Locale.ENGLISH)
        }
    }

    enum class Region
    {

        AMERICAS,
        EUROPE,
        ASIA;

        override fun toString(): String
        {
            return name.toLowerCase(Locale.ENGLISH)
        }
    }

}