package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannelId
import be.glever.ant.constants.AntPlusDeviceType
import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.hrm.HRMChannel
import be.glever.antplus.hrm.datapage.HrmDataPageRegistry
import be.glever.antplus.hrm.datapage.background.HrmDataPage2ManufacturerInformation
import be.glever.antplus.hrm.datapage.background.HrmDataPage3ProductInformation
import be.glever.antplus.hrm.datapage.main.HrmDataPage4PreviousHeartBeatEvent
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.data.Measurement
import kotlin.random.Random

class AntHeartRateSensor : AntPlusSensor<HRMChannel>() {
    override val registry = HrmDataPageRegistry()

    override val title: String = "AntHeartRateSensor" + Random.nextInt(0, 100)

    private val measurement = Measurement(this, 0, Measurement.Unit.BPM).apply {
        description = "Heartrate " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(measurement)

    override val deviceType = AntPlusDeviceType.HRM

    override fun handleDevSpecificMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)
            val dataPage: AbstractAntPlusDataPage = registry.constructDataPage(payLoad)

            when (dataPage) {
                is HrmDataPage2ManufacturerInformation ->
                    if (manufacturerIdProperty.value == null) {
                        manufacturerIdProperty.value = dataPage.manufacturerId
                        // Assign it again, to re-fire any listener. Because:
                        // The model name can only be determined once the manufacturer ID has been determined
                        modelNumberProperty.value = modelNumberProperty.value
                    }
                is HrmDataPage3ProductInformation ->
                    if (modelNumberProperty.value == null)
                        modelNumberProperty.value = dataPage.modelNumber.toInt()
                is HrmDataPage4PreviousHeartBeatEvent ->
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

    override fun createChannel(usbDevice: AntUsbDevice, channelId: AntChannelId): HRMChannel {
        return HRMChannel(usbDevice, channelId.deviceNumber)
    }
}
