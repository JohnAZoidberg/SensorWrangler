package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.antplus.fec.FecChannel
import be.glever.antplus.fec.datapage.FecDataPageRegistry
import be.glever.antplus.fec.datapage.main.FecDataPage16GeneralFeData
import be.glever.antplus.fec.datapage.main.FecDataPage25Bike
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.random.Random

class AntStationaryBike : AntPlusSensor<FecChannel>(){
    override val title: String = "AntPlusStationaryBike" + Random.nextInt(0, 100)

    private val powerMeasurement = Measurement(this, 0, Measurement.Unit.WATT).apply{
        description = "Power " + Random.nextInt(0, 100)
    }
    private val cadenceMeasurement = Measurement(this, 0, Measurement.Unit.RPM).apply{
        description = "Cadence " + Random.nextInt(0, 100)
    }
    private val speedMeasurement = Measurement(this, 0, Measurement.Unit.METER_PER_SECOND).apply{
        description = "Speed " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(powerMeasurement, cadenceMeasurement, speedMeasurement)

    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }

    override fun createChannel(device: AntUsbDevice): FecChannel {
        return FecChannel(device)
    }

    override val registry = FecDataPageRegistry()

    override fun handleDevSpecificMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)

            val dataPage = registry.constructDataPage(payLoad)
            if (dataPage is FecDataPage25Bike) {
                powerMeasurement.addDataPoint(dataPage.instantaneousPower.toDouble())
                cadenceMeasurement.addDataPoint(dataPage.instantaneousCadence.toDouble())
            }
            if (dataPage is FecDataPage16GeneralFeData) {
                // TODO: Which measurements are useful?
                //val distanceTraveledEnabled: Boolean = fecDataPage.isDistanceTraveledEnabled()
                //val isVirtualSpeed: Boolean = fecDataPage.isVirtualSpeed()
                //val heartRateSource: Optional<HeartRateDataSource> = fecDataPage.getHeartRateSource()
                //val distance: Int = fecDataPage.getDistanceTravelled()
                //val time: Int = fecDataPage.getElapsedTime()
                //val equipmentType: Optional<EquipmentType> = fecDataPage.getEquipmentType()
                //val fecState: Optional<FecState> = fecDataPage.getFecState()
                //val heartRate: Int = fecDataPage.getHeartRate()
                //val lapToggle: Boolean = fecDataPage.getLapToggle()
                speedMeasurement.addDataPoint(dataPage.speed)
            }
        }
    }

}
