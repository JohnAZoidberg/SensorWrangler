package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.common.datapage.registry.AbstractDataPageRegistry
import be.glever.antplus.power.PowerChannel
import be.glever.antplus.power.datapage.PowerDataPageRegistry
import be.glever.antplus.power.datapage.main.PowerDataPage10PowerOnly
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
    private val powerDistributionMeasurment = Measurement(this, 0, Measurement.Unit.PERCENTAGE).apply {
        description = "Power distribution" + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(powerMeasurement, cadenceMeasurement, powerDistributionMeasurment)

    override val registry: AbstractDataPageRegistry = PowerDataPageRegistry()

    override fun handleDevSpecificMessage(antMessage: AntMessage?) {
        if (antMessage !is BroadcastDataMessage)
            return

        val payLoad = antMessage.payLoad
        removeToggleBit(payLoad)

        val dataPage = registry.constructDataPage(payLoad)
        if (dataPage is PowerDataPage10PowerOnly) {
            powerMeasurement.addDataPoint(dataPage.instantaneousPower)
            cadenceMeasurement.addDataPoint(dataPage.instantaneousCadence.toDouble())

            if (dataPage.pedalPowerDistribution.percentage != 127)
                powerDistributionMeasurment.addDataPoint(dataPage.pedalPowerDistribution.percentage.toDouble())
        }
    }

    override fun createChannel(device: AntUsbDevice): PowerChannel {
        return PowerChannel(device)
    }

    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }
}
