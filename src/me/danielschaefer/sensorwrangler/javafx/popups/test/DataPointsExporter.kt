package me.danielschaefer.sensorwrangler.javafx.popups.test

import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.javafx.App
import java.io.File
import java.io.FileWriter
import java.io.Writer
import me.danielschaefer.sensorwrangler.sensors.Sensor

class DataPointsExporter {
    val testFile = "D:\\Programming\\Studienarbeit\\TestDataPoints.txt";
    private val writer: Writer = FileWriter(testFile,true);
    fun extractDataPoints(){
     for (sensor in App.instance.wrangler.sensors){
         for (measurement in sensor.measurements){
             for (dataPoint in measurement.dataPoints){
                    writer.write("$dataPoint");
                 writer.flush()
             }
         }
     }
 }
    fun closeWriter(){
        writer.close()
    }

}