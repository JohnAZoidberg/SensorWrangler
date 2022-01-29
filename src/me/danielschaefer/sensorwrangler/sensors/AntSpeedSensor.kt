package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.speedcadence.SpeedChannel
import be.glever.antplus.speedcadence.datapage.SpeedCadenceDataPageRegistry
import be.glever.antplus.speedcadence.datapage.main.SpeedCadenceDataPage5Motion
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.data.Measurement
import mu.KotlinLogging
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class AntSpeedSensor : AntPlusSensor<SpeedChannel>() {
    override val registry = SpeedCadenceDataPageRegistry()

    override val title: String = "AntSpeedSensor" + Random.nextInt(0, 100)

    private val speedMeasurement = Measurement(this, 0, Measurement.Unit.METER_PER_SECOND).apply {
        description = "Speed " + Random.nextInt(0, 100)
    }
    private val distanceMeasurement = Measurement(this, 0, Measurement.Unit.METER).apply {
        description = "Distance " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(speedMeasurement, distanceMeasurement)

    @JsonProperty("wheelDiameter")
    @SensorProperty(title = "Wheel diameter [in]")
    var wheelDiameter = 28

    private var prevSpeedRevCount = 0
    private var firstSpeedRevCount = 0
    private var prevSpeedEventTime: Long = 0

    /**
     * Non-legacy devices swap the first bit of the pageNumber every 4 messages.
     * For the moment not taking the legacy HRM devices into account.
     *
     * @param payload
     */
    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }

    override fun createChannel(device: AntUsbDevice): SpeedChannel {
        // Reset everything to 0 on connect
        // TODO: Find a better place to reset them to zero
        prevSpeedRevCount = 0
        firstSpeedRevCount = 0
        prevSpeedEventTime = 0

        return SpeedChannel(device)
    }

    override fun handleMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)
            val dataPage = registry.constructDataPage(payLoad)
            // logger.debug { "Received datapage ${dataPage}" }
            if (dataPage is SpeedCadenceDataPage5Motion) {
                calcSpeedDistance(dataPage, wheelDiameter * 2.54)
            }
        }
    }

    private fun calcSpeedDistance(dataPage: SpeedCadenceDataPage5Motion, diameter: Double) {
        val curRevCount = dataPage.cumulativeRevolutions

        if (firstSpeedRevCount == 0)
            firstSpeedRevCount = curRevCount

        // Skip this, if we get the same measurement as last time
        if (firstSpeedRevCount == curRevCount)
            return

        val circumference = Math.PI * diameter
        val speedEventTime = dataPage.eventTime
        val isMoving = dataPage.isMoving

        // Can only calculate speed, if we've actually moved yet
        val speed: Double = if (prevSpeedEventTime == 0L) 0.0 else calculateSpeed(
            circumference,
            prevSpeedRevCount,
            curRevCount,
            prevSpeedEventTime,
            speedEventTime
        )
        val kmhSpeed = speed * 3.6
        val travelledDistance = calculateDistance(circumference, curRevCount, firstSpeedRevCount)

        logger.debug { "The bike is currently ${(if (isMoving) "" else " not")} moving at $kmhSpeed km/h and has travelled $travelledDistance m." }
        Platform.runLater {
            speedMeasurement.addDataPoint(kmhSpeed)
            distanceMeasurement.addDataPoint(travelledDistance)
        }

        prevSpeedRevCount = curRevCount
        prevSpeedEventTime = speedEventTime
    }

    /**
     * Calculate the speed in m/s from a current and a previous measurement
     *
     */
    private fun calculateSpeed(
        circumference: Double,
        prevRevCount: Int,
        curRevCount: Int,
        prevTime: Long,
        curTime: Long
    ): Double {
        val timeDiff = curTime - prevTime.toDouble()
        val revDiff = curRevCount - prevRevCount.toDouble()
        return 1000 * circumference * (revDiff / timeDiff)
    }

    private fun calculateDistance(circumference: Double, curRevCount: Int, firstRevCount: Int): Double {
        return circumference * (curRevCount - firstRevCount)
    }
}
