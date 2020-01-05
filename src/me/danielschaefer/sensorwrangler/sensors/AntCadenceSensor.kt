package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannelId
import be.glever.ant.constants.AntPlusDeviceType
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.speedcadence.CadenceChannel
import be.glever.antplus.speedcadence.datapage.AbstractSpeedCadenceDataPage
import be.glever.antplus.speedcadence.datapage.SpeedCadenceDataPageRegistry
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.random.Random

class AntCadenceSensor : AntSpeedCadenceSensor<CadenceChannel>(){
    override val title: String = "AntCadenceSensor" + Random.nextInt(0, 100)

    private val cadenceMeasurement = Measurement(this, 0, Measurement.Unit.RPM).apply{
        description = "Cadence " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(cadenceMeasurement)

    override val deviceType = AntPlusDeviceType.Cadence

    private var prevCadenceRevCount = 0
    private var firstCadenceRevCount = 0
    private var prevCadenceEventTime: Long = 0

    override fun createChannel(usbDevice: AntUsbDevice, channelId: AntChannelId): CadenceChannel {
        // Reset everything to 0 on connect
        // TODO: Find a better place to reset them to zero
        prevCadenceRevCount = 0
        firstCadenceRevCount = 0
        prevCadenceEventTime = 0

        return CadenceChannel(usbDevice, channelId.deviceNumber)
    }

    override val registry = SpeedCadenceDataPageRegistry()

    override fun handleSpeedCadenceDataPage(dataPage: AbstractAntPlusDataPage) {
        when (dataPage) {
            is AbstractSpeedCadenceDataPage ->
                calcCadence(dataPage)
        }
    }

    private fun calcCadence(dataPage: AbstractSpeedCadenceDataPage) {
        val curCadenceRevCount = dataPage.cumulativeRevolutions
        val speedEventTime = dataPage.eventTime

        if (firstCadenceRevCount == 0)
            firstCadenceRevCount = curCadenceRevCount

        // Skip this, if we get the same measurement as last time
        if (prevCadenceRevCount == curCadenceRevCount)
            return

        // Can only calculate speed, if we've actually moved yet
        val cadence: Double = if (prevCadenceEventTime == 0L) 0.0 else calculateCadence(
            prevCadenceRevCount,
            curCadenceRevCount,
            prevCadenceEventTime,
            speedEventTime
        )

        println("The crank is being rotated at $cadence RPM.")
        Platform.runLater {
            cadenceMeasurement.addDataPoint(cadence)
        }

        prevCadenceRevCount = curCadenceRevCount
        prevCadenceEventTime = speedEventTime
    }

    private fun calculateCadence(
        prevRevCount: Int,
        curRevCount: Int,
        prevTime: Long,
        curTime: Long
    ): Double {
        val timeDiff = curTime - prevTime.toDouble()
        val revDiff = curRevCount - prevRevCount
        return 1000 * 60 * (revDiff / timeDiff)
    }

}
