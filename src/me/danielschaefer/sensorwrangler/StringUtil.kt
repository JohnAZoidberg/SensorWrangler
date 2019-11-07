package me.danielschaefer.sensorwrangler


object StringUtil {
    @JvmStatic
    fun yesNo(bool: Boolean): String {
        return if (bool) "Yes" else "No"
    }
}