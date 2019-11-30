package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDeviceFactory
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.hrm.HRMChannel
import be.glever.antplus.hrm.datapage.HrmDataPageRegistry
import be.glever.antplus.hrm.datapage.main.HrmDataPage4PreviousHeartBeatEvent
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.concurrent.thread
import kotlin.random.Random

class AntPlusSensor : Sensor() {
    private val registry = HrmDataPageRegistry()

    override val title: String = "AntPlusSensor" + Random.nextInt(0, 100)
    private val measurement = Measurement(this, 0, Measurement.Unit.METER).apply{
        description = "HRM " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(measurement)

    override fun connect() {
        if (connected)
            return

        thread(start = true) {
            val availableDevices = AntUsbDeviceFactory.getAvailableAntDevices()
            val antDevice = availableDevices.first()
            // TODO: Maybe filter by serialNumber { it.serialNumber contentEquals serialNumber.encodeToByteArray() }

            if (antDevice == null) {
                super.disconnect("No devices found")
            } else {
                antDevice.use { device ->
                    device.initialize()
                    device.closeAllChannels() // Otherwise channels stay open on usb dongle even if program shuts down
                    HRMChannel(device).events.doOnNext { handleMessage(it) }.subscribe()
                    System.`in`.read()  // QUESTION: Why is this necessary?
                }
            }
        }

        super.connect()
    }

    private fun handleMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)
            val dataPage: AbstractAntPlusDataPage = registry.constructDataPage(payLoad)

            if (dataPage is HrmDataPage4PreviousHeartBeatEvent)
                measurement.addDataPoint(dataPage.computedHeartRateInBpm.toDouble())
        }
    }

    /**
     * Non-legacy devices swap the first bit of the pageNumber every 4 messages.
     * For the moment not taking the legacy HRM devices into account.
     *
     * @param payload
     */
    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }
}
