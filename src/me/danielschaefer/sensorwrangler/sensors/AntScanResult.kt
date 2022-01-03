package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannelId

data class AntScanResult(val channelId: AntChannelId, var manufacturerId: Int? = null, var modelNumber: Int? = null) : ScanResult {
    val deviceTypeName
        get() = channelId.deviceType.toString()

    override fun toString(): String {
        if (manufacturerId != null)
            return "$deviceTypeName (ID: ${channelId.intDeviceNumber}) - ${AntUtil.getModelName(manufacturerId, modelNumber)} by ${AntUtil.getManufacturerName(manufacturerId)})"
        else
            return "$deviceTypeName (ID: ${channelId.intDeviceNumber})"
    }
}
