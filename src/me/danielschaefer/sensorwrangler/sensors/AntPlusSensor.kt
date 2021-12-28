package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannel
import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.ant.usb.AntUsbDeviceFactory
import be.glever.antplus.common.datapage.DataPage80ManufacturersInformation
import be.glever.antplus.common.datapage.registry.AbstractDataPageRegistry
import javafx.beans.property.ReadOnlyObjectWrapper
import kotlin.concurrent.thread

abstract class AntPlusSensor<T : AntChannel> : Sensor() {
    protected var manufacturerIdProperty = ReadOnlyObjectWrapper<Int?>(null)
    protected var modelNumberProperty = ReadOnlyObjectWrapper<Int?>(null)
    protected abstract val registry: AbstractDataPageRegistry

    val manufacturerId
        get() = manufacturerIdProperty.readOnlyProperty

    val modelNumber
        get() = modelNumberProperty.readOnlyProperty

    override fun specificConnect() {
        thread(start = true) {
            val availableDevices = AntUsbDeviceFactory.getAvailableAntDevices()
            val antDevice = availableDevices.first()

            if (antDevice == null) {
                super.disconnect("No devices found")
                return@thread
            } else {
                // TODO: Wrap in try-catch and disconnect
                antDevice.use { device ->
                    // TODO: use catches all exceptions and discards them - we want them logged!
                    if (!device.isInitialized) {
                        device.initialize()
                        device.closeAllChannels() // Otherwise channels stay open on usb dongle even if program shuts down
                    }

                    createChannel(device).events.doOnNext { handleMessage(it) }.subscribe()
                    System.`in`.read()  // TODO: Use better method to sleep thread indefinitely
                }
            }
        }
    }

    override fun specificDisconnect(reason: String?) {
        // TODO: Is there a way to disconnect?
    }

    /**
     * Create an instance of the generic channel class
     *
     * This is necessary because we cannot instantiate it in this class, since generic types are erased at run-time.
     */
    protected abstract fun createChannel(device: AntUsbDevice): T

    protected abstract fun handleDevSpecificMessage(antMessage: AntMessage?)

    private fun handleMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            when (val dataPage = registry.constructDataPage(antMessage.payLoad)) {
                is DataPage80ManufacturersInformation -> {
                    manufacturerIdProperty.value = dataPage.manufacturerId
                    modelNumberProperty.value = dataPage.modelNumber
                }
            }
        }

        handleDevSpecificMessage(antMessage)
    }

    fun getManufacturerName(): String {
        return when (manufacturerId.value) {
            1-> "Garmin"
            65 -> "Mio Global (Physical Enterprises)"
            else -> "Unknown"
        }
    }

    fun getModelName(): String {
        val unknown = "Unknown"
        return when (manufacturerId.value) {
            1 -> when (modelNumber.value) {
                7 -> "HRM 3-SS"
                9 -> "Speed Sensor"
                10 -> "Cadence Sensor"
                else -> unknown
            }
            65 -> when (modelNumber.value) {
                3 -> "FUSE"
                else -> unknown
            }
            else -> unknown
        }
    }
}
