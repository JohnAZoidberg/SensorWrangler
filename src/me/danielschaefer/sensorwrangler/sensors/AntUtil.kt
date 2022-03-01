package me.danielschaefer.sensorwrangler.sensors

object AntUtil {
    fun getManufacturerName(manufacturerId: Int?): String {
        return when (manufacturerId) {
            0x01 -> "Garmin"
            0x41 -> "Mio Global (Physical Enterprises)"
            else -> "Unknown"
        }
    }

    fun getModelName(manufacturerId: Int?, modelNumber: Int?): String {
        return when (manufacturerId) {
            // Garmin
            0x01 -> when (modelNumber) {
                0x07 -> "HRM 3-SS"
                0x09 -> "Speed Sensor"
                0x0A -> "Cadence Sensor"
                0x565 -> "Vector (CP)"
                else -> "Unknown"
            }
            // Mio
            0x41 -> when (modelNumber) {
                0x03 -> "FUSE"
                else -> "Unknown"
            }
            else -> "Unknown"
        }
    }
}
