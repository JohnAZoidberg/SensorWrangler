package me.danielschaefer.sensorwrangler

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


object StringUtil {
    @JvmStatic
    fun yesNo(bool: Boolean): String {
        return if (bool) "Yes" else "No"
    }

    @JvmStatic
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    @JvmStatic
    fun formatDate(timestamp: Number): String {
        return formatDate(timestamp.toLong())
    }

    @JvmStatic
    fun formatDate(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.of("GMT+1"))  // TODO: Think about how to deal with TZs
            .format(formatter)
    }
}
