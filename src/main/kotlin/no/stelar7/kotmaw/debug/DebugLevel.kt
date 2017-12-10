package no.stelar7.kotmaw.debug

enum class DebugLevel
{
    NONE,
    BASIC,
    EXTENDED,
    ALL;

    fun printIf(level: DebugLevel, text: String)
    {
        if (level <= this)
        {
            println("DEBUG LOGGER: $text")
        }
    }
}