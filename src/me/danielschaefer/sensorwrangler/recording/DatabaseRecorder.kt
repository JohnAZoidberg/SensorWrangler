package me.danielschaefer.sensorwrangler.recording

import me.danielschaefer.sensorwrangler.Measurement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DataPoint: Table() {
    // TODO: Probably the first 3 cols should be the primary key
    val timestamp = varchar("timestamp", 50).primaryKey()
    val sensor = varchar("sensor", 100)
    val measurement = varchar("measurement", 100)
    val value = double("value")
}

/**
 * @param connection
 * @param username can be empty
 * @param password can be empty
 * @param driver
 */
class DatabaseRecorder(connection: String, username: String, password: String, driver: String): Recorder {
    init {
        Database.connect("jdbc:$connection", driver = driver, user = username, password = password)
        // Tell SQLite3 to enforce transactional integrity by serializing writes from different connections
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    override fun recordValue(timestamp: String, measurement: Measurement, value: Double) {
        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            SchemaUtils.create (DataPoint)

            DataPoint.insert {
                it[DataPoint.timestamp] = timestamp
                it[sensor] = measurement.sensor.title
                it[DataPoint.measurement] = measurement.description ?: ""
                it[DataPoint.value] = value
            } get DataPoint.timestamp
        }
    }

    override fun close() {
        // TODO: There doesn't seem to be any way to close the connection using org.jetbrains.exposed...
    }
}
