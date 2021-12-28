package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.hrm.HRMChannel
import be.glever.antplus.hrm.datapage.HrmDataPageRegistry
import be.glever.antplus.hrm.datapage.main.HrmDataPage4PreviousHeartBeatEvent
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.random.Random

class AntHeartRateSensor : AntPlusSensor<HRMChannel>() {
    override val registry = HrmDataPageRegistry()

    override val title: String = "AntHeartRateSensor" + Random.nextInt(0, 100)

    private val measurement = Measurement(this, 0, Measurement.Unit.BPM).apply{
        description = "Heartrate " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(measurement)

    override fun handleMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)
            val dataPage: AbstractAntPlusDataPage = registry.constructDataPage(payLoad)

            if (dataPage is HrmDataPage4PreviousHeartBeatEvent) {
                // TODO: Call runLater in UI code, not here
                // UI can only be updated from UI threads
                Platform.runLater {
                    measurement.addDataPoint(dataPage.computedHeartRateInBpm.toDouble())
                }
            }
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

    override fun createChannel(device: AntUsbDevice): HRMChannel {
        return HRMChannel(device)
    }
}
