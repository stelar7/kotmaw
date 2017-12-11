package no.stelar7.kotmaw.util

inline fun <T: Any> T?.notNull(f: (it: T) -> Unit)
{
    if (this != null)
    {
        f(this)
    }
}
