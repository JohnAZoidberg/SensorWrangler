package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannelId
import be.glever.ant.constants.AntPlusDeviceType
import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.common.datapage.registry.AbstractDataPageRegistry
import be.glever.antplus.power.PowerChannel
import be.glever.antplus.power.datapage.PowerDataPageRegistry
import be.glever.antplus.power.datapage.background.PowerDataPage14TorqueBarycenter
import be.glever.antplus.power.datapage.background.PowerDataPage3MeasurementOutput
import be.glever.antplus.power.datapage.background.PowerDataPageE0RightForceAngle
import be.glever.antplus.power.datapage.background.PowerDataPageE1LeftForceAngle
import be.glever.antplus.power.datapage.background.PowerDataPageE2PedalPosition
import be.glever.antplus.power.datapage.main.PowerDataPage10PowerOnly
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.random.Random

class AntPowerSensor : AntPlusSensor<PowerChannel>() {
    override val title: String = "AntPowerSensor" + Random.nextInt(0, 100)

    private val powerMeasurement = Measurement(this, 0, Measurement.Unit.WATT).apply {
        description = "Power " + Random.nextInt(0, 100)
    }
    private val cadenceMeasurement = Measurement(this, 0, Measurement.Unit.RPM).apply {
        description = "Cadence " + Random.nextInt(0, 100)
    }
    private val powerDistributionMeasurement = Measurement(this, 0, Measurement.Unit.PERCENTAGE).apply {
        description = "Power distribution" + Random.nextInt(0, 100)
    }
    private val torqueBarycenterAngleMeasurement = Measurement(this, 0, Measurement.Unit.DEGREE).apply {
        description = "Torque Barycenter Angle" + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(
        powerMeasurement,
        cadenceMeasurement,
        powerDistributionMeasurement,
        torqueBarycenterAngleMeasurement
    )

    override val registry: AbstractDataPageRegistry = PowerDataPageRegistry()

    override val deviceType = AntPlusDeviceType.Power

    override fun handleDevSpecificMessage(antMessage: AntMessage?) {
        if (antMessage !is BroadcastDataMessage)
            return

        val payLoad = antMessage.payLoad
        removeToggleBit(payLoad)

        val dataPage = registry.constructDataPage(payLoad)
        Platform.runLater { addMeasurement(dataPage) }
    }

    private fun addMeasurement(dataPage: AbstractAntPlusDataPage) {
        // TODO: Check which measurements are supported
        when (dataPage) {
            is PowerDataPage3MeasurementOutput -> {
                // TODO: Handle this data page
                dataPage.numberOfDataTypes
                dataPage.dataType
                dataPage.scaledMeasurement
                dataPage.timeStamp
                dataPage.measurement
                dataPage.scaleFactor
            }
            is PowerDataPage14TorqueBarycenter -> {
                torqueBarycenterAngleMeasurement.addDataPoint(dataPage.torqueBarycenterAngle)
            }
            is PowerDataPageE0RightForceAngle -> {
                // TODO: Handle this data page
                dataPage.torque
                dataPage.startAngle
                dataPage.endAngle
                dataPage.startPeakAngle
                dataPage.endPeakAngle
            }
            is PowerDataPageE1LeftForceAngle -> {
                // TODO: Handle this data page
                dataPage.torque
                dataPage.startAngle
                dataPage.endAngle
                dataPage.startPeakAngle
                dataPage.endPeakAngle
            }
            is PowerDataPageE2PedalPosition -> {
                // TODO: Handle this data page
                dataPage.cadence
                dataPage.leftPco
                dataPage.rightPco
                dataPage.riderPosition
            }
            is PowerDataPage10PowerOnly -> {
                powerMeasurement.addDataPoint(dataPage.instantaneousPower.toDouble())
                cadenceMeasurement.addDataPoint(dataPage.instantaneousCadence.toDouble())

                dataPage.pedalPowerDistribution?.let {
                    if (it.percentage != 127)
                        powerDistributionMeasurement.addDataPoint(it.percentage.toDouble())
                }
            }
        }
    }

    override fun createChannel(usbDevice: AntUsbDevice, channelId: AntChannelId): PowerChannel {
        return PowerChannel(usbDevice, channelId.deviceNumber)
    }

    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }
}
