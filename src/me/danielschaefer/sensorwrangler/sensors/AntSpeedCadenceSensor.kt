package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.channel.AntChannel
import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.speedcadence.datapage.background.SpeedCadenceDataPage2ManufacturerInformation
import be.glever.antplus.speedcadence.datapage.background.SpeedCadenceDataPage3ProductInformation

abstract class AntSpeedCadenceSensor<T : AntChannel> : AntPlusSensor<T>() {

    override fun handleDevSpecificMessage(antMessage: AntMessage?) {
        if (antMessage is BroadcastDataMessage) {
            val payLoad = antMessage.payLoad
            removeToggleBit(payLoad)

            val dataPage = registry.constructDataPage(payLoad)

            when (dataPage) {
                is SpeedCadenceDataPage2ManufacturerInformation ->
                    if (manufacturerIdProperty.value == null) {
                        manufacturerIdProperty.value = dataPage.manufacturerId
                        // Assign it again, to re-fire any listener. Because:
                        // The model name can only be determined once the manufacturer ID has been determined
                        modelNumberProperty.value = modelNumberProperty.value
                    }
                is SpeedCadenceDataPage3ProductInformation ->
                    if (modelNumberProperty.value == null)
                        modelNumberProperty.value = dataPage.modelNumber.toInt()
            }

            handleSpeedCadenceDataPage(dataPage)
        }
    }
    abstract fun handleSpeedCadenceDataPage(dataPage: AbstractAntPlusDataPage)

    private fun removeToggleBit(payload: ByteArray) {
        payload[0] = (127 and payload[0].toInt()).toByte()
    }

}
