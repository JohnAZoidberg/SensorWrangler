package me.danielschaefer.sensorwrangler.javafx.popups.test

import me.danielschaefer.sensorwrangler.javafx.App
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.io.FileReader
import java.io.Reader
import java.nio.file.Paths

class DataPointsExporter {
    val configFile = getConfigPath()
    private val configWriter: Writer = FileWriter(configFile,false);
    private val configReader: Reader = FileReader(configFile)


    fun extractDataPoints(){
     for (sensor in App.instance.wrangler.sensors){
         val dataFile = Paths.get(getDataPath().toString(),sensor.title.toString()+".txt").toFile()
         val writer: Writer = FileWriter(dataFile,true);
         for (measurement in sensor.measurements){
             for (dataPoint in measurement.dataPoints){
                    writer.write("$dataPoint");
                 writer.flush()
             }
         }
         writer.close()
     }
 }

    fun getConfigPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "config.txt").toFile()
    }

    fun getDataPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "Measurement" + getConfigNumber().toString()).toFile()
    }

    fun getConfigNumber(): Int {
        val number = configReader.read().toInt()
        configWriter.write(number+1)
        configWriter.flush()
        return number
    }
    fun closeWriter(){
      configWriter.close()
      configReader.close()
    }

}