package me.danielschaefer.sensorwrangler.recording

import me.danielschaefer.sensorwrangler.Measurement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DataPoint: Table() {
    // TODO: Probably the first 3 cols should be the primary key
    val timestamp = varchar("timestamp", 50).primaryKey()
    val sensor = varchar("sensor", 100)
    val measurement = varchar("measurement", 100)
    val value = double("value")
}

class DatabaseRecorder(connection: String, username: String, password: String): Recorder {
    init {
        Database.connect("jdbc:postgresql://$connection", driver = "org.postgresql.Driver",
            user = username, password = password)
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