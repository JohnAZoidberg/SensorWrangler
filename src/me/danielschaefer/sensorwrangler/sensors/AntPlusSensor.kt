package me.danielschaefer.sensorwrangler.sensors

import be.glever.ant.AntException
import be.glever.ant.channel.AntChannel
import be.glever.ant.channel.AntChannelId
import be.glever.ant.channel.BackgroundScanningChannel
import be.glever.ant.constants.AntPlusDeviceType
import be.glever.ant.message.AntMessage
import be.glever.ant.message.data.BroadcastDataMessage
import be.glever.ant.usb.AntUsbDevice
import be.glever.ant.usb.AntUsbDeviceFactory
import be.glever.ant.util.ByteUtils
import be.glever.antplus.common.datapage.AbstractAntPlusDataPage
import be.glever.antplus.common.datapage.DataPage80ManufacturersInformation
import be.glever.antplus.common.datapage.registry.AbstractDataPageRegistry
import be.glever.antplus.common.datapage.registry.ComprehensiveDataPageRegistry
import be.glever.antplus.hrm.datapage.background.HrmDataPage2ManufacturerInformation
import be.glever.antplus.hrm.datapage.background.HrmDataPage3ProductInformation
import be.glever.antplus.speedcadence.datapage.background.SpeedCadenceDataPage2ManufacturerInformation
import be.glever.antplus.speedcadence.datapage.background.SpeedCadenceDataPage3ProductInformation
import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.ObservableList
import java.lang.Thread.sleep
import kotlin.concurrent.thread

abstract class AntPlusSensor<T : AntChannel> : Scannable<AntScanResult>, Sensor() {
    protected var manufacturerIdProperty = ReadOnlyObjectWrapper<Int?>(null)
    protected var modelNumberProperty = ReadOnlyObjectWrapper<Int?>(null)
    protected abstract val registry: AbstractDataPageRegistry
    var channelId: AntChannelId? = null
        private set
    private var antUsbDevice: AntUsbDevice? = null
    private var channelNumber: Int? = null

    private var scanningProperty = ReadOnlyBooleanWrapper(false)
    protected val scanRegistry = ComprehensiveDataPageRegistry()

    abstract val deviceType: AntPlusDeviceType

    val manufacturerId
        get() = manufacturerIdProperty.readOnlyProperty

    val modelNumber
        get() = modelNumberProperty.readOnlyProperty

    private fun handleScanMessage(foundChannels: ObservableList<AntScanResult>, antMessage: AntMessage) {
        // Don't process new events if we've aborted the scan
        if (!scanningProperty.value)
            return

        var foundChannelId: AntChannelId? = null
        var foundModelNumber: Int? = null
        var foundManufacturerId: Int? = null
        var dataPage: AbstractAntPlusDataPage? = null

        if (antMessage is BroadcastDataMessage) {
            val msg = antMessage as BroadcastDataMessage
            val ext = msg.extendedData
            if (ext is AntChannelId) {
                foundChannelId = ext
            }

            val payLoad = antMessage.payLoad
            // TODO: Determine when exactly the toggle bit needs to be removed
            // removeToggleBit(payLoad)
            dataPage = scanRegistry.constructDataPage(payLoad)
        }

        // Message does not have extended data or extended data does not include channel ID
        // Therefore we cannot identify which device it belongs to
        // Shouldn't really ever occur.
        if (foundChannelId == null)
            return

        if (dataPage is DataPage80ManufacturersInformation) {
            foundModelNumber = dataPage.modelNumber
            foundManufacturerId = dataPage.manufacturerId
        }

        if (dataPage is SpeedCadenceDataPage2ManufacturerInformation) {
            foundManufacturerId = dataPage.manufacturerId
        }
        if (dataPage is SpeedCadenceDataPage3ProductInformation) {
            foundModelNumber = ByteUtils.toInt(dataPage.modelNumber)
        }

        if (dataPage is HrmDataPage3ProductInformation) {
            foundModelNumber = ByteUtils.toInt(dataPage.modelNumber)
        }
        if (dataPage is HrmDataPage2ManufacturerInformation) {
            foundManufacturerId = dataPage.manufacturerId
        }

        if (!foundChannels.any { it.channelId == foundChannelId }) {
            println("Found device at channel: $foundChannelId")
            val scanResult = AntScanResult(
                foundChannelId,
                manufacturerId = foundManufacturerId,
                modelNumber = foundModelNumber
            )
            Platform.runLater {
                // TODO: Should probably lock the list before modifying it
                foundChannels.add(scanResult)
                foundChannels.setAll(foundChannels.sorted())
            }
        } else {
            // Add new manufacturer and model number to already found
            foundChannels.find { it.channelId == foundChannelId }?.let { previouslyFound ->
                var changed = false

                if (previouslyFound.manufacturerId == null) {
                    previouslyFound.manufacturerId = foundManufacturerId
                    changed = true
                }

                if (previouslyFound.modelNumber == null) {
                    previouslyFound.modelNumber = foundModelNumber
                    changed = true
                }

                // Don't update all of the list if nothing has changed
                if (changed)
                    return@let

                Platform.runLater {
                    // TODO: Should probably lock the list before modifying it
                    // TODO: Better way of refreshing the UI than removing and adding all elements
                    foundChannels.remove(previouslyFound)
                    foundChannels.add(previouslyFound)
                    foundChannels.setAll(foundChannels.sorted())
                    println("Replaced with $previouslyFound")
                }
            }
        }
    }

    override fun scan(foundChannels: ObservableList<AntScanResult>) {
        scanningProperty.value = true
        val availableDevices = AntUsbDeviceFactory.getAvailableAntDevices()
        antUsbDevice = availableDevices.first()

        if (antUsbDevice == null) {
            super.disconnect("No USB ANT transceivers found")
            scanningProperty.value = false
            return
        }

        thread(start = true) {
            // TODO: nse catches all exceptions and discards them - we want them logged!
            antUsbDevice?.let { concreteUsbDevice ->
                if (!concreteUsbDevice.isInitialized) {
                    concreteUsbDevice.initialize()
                    concreteUsbDevice.closeAllChannels() // Otherwise channels stay open on usb dongle even if program shuts down
                }

                val scanChannel = BackgroundScanningChannel(concreteUsbDevice, AntPlusDeviceType.Any)
                scanChannel.events.doOnNext { msg -> handleScanMessage(foundChannels, msg) }.subscribe()
                sleep(10000)
                try {
                    concreteUsbDevice.closeChannel(scanChannel.channelNumber)
                } catch (e: AntException) {
                    // Yeah, whatever
                } finally {
                    scanningProperty.value = false
                }
            }
        }
    }

    override fun getScanStatusProperty(): ReadOnlyBooleanProperty {
        return scanningProperty.readOnlyProperty
    }

    override fun configureScan(config: AntScanResult) {
        channelId = config.channelId
        config.manufacturerId?.let { manufacturerIdProperty.value = it }
        config.modelNumber?.let { modelNumberProperty.value = it }
    }

    override fun specificConnect() {
        thread(start = true) {
            val availableDevices = AntUsbDeviceFactory.getAvailableAntDevices()
            val antUsbDevice = availableDevices.first()

            if (antUsbDevice == null) {
                super.disconnect("No ANT transceiver devices found")
                return@thread
            }

            if (channelId == null) {
                super.disconnect("No channel ID configured")
                return@thread
            }

            channelId?.let { connectChannelId ->
                // TODO: Wrap in try-catch and disconnect
                antUsbDevice.use { device ->
                    // TODO: use catches all exceptions and discards them - we want them logged!
                    if (!device.isInitialized) {
                        device.initialize()
                        // Close previously opened channels
                        // E.g. if the program crashes, the channels are not necessarily closed
                        device.closeAllChannels()
                    }

                    // TODO: Get channelNumber from new channel and assign it to current object
                    createChannel(device, connectChannelId).events.doOnNext { handleMessage(it) }.subscribe()
                    System.`in`.read() // TODO: Use better method to sleep thread indefinitely
                }
            }
        }
    }

    override fun specificDisconnect(reason: String?) {
        channelNumber?.let {
            antUsbDevice?.closeChannel(it.toByte())
        }
        channelNumber = null
    }

    /**
     * Create an instance of the generic channel class
     *
     * This is necessary because we cannot instantiate it in this class, since generic types are erased at run-time.
     */
    protected abstract fun createChannel(usbDevice: AntUsbDevice, channelId: AntChannelId): T

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
        return AntUtil.getManufacturerName(manufacturerId.value)
    }

    fun getModelName(): String {
        return AntUtil.getModelName(manufacturerId.value, modelNumber.value)
    }
}
