package me.danielschaefer.sensorwrangler.javafx.popups.test

import me.danielschaefer.sensorwrangler.javafx.App
import java.io.*
import java.nio.file.*
import kotlin.io.path.createDirectory
//import org.json.*


class DataPointsExporter {
    //val configFile = getConfigPath()
    //private val configReader: Reader = FileReader(configFile)
    //val jsonReader: JSONObject = JSONObject(getConfigPath())
    //val number = jsonReader.getInt("index")
    val path = "C:\\Users\\Daniel\\Desktop\\Programmieren\\Projekte\\SensorWrangler\\data\\Measurement-1"
    fun extractDataPoints(){
       /* if (!getDataPath().isDirectory) {
            getDataPath().toPath().createDirectory()
        }
        val newIndex = number + 1
        jsonReader.put("index", "$newIndex")*/
     for (sensor in App.instance.wrangler.sensors){
         val dataFile = Paths.get(path,sensor.title+".txt").toFile()
             dataFile.createNewFile()
         val writer: Writer = FileWriter(dataFile,true);
         for (measurement in sensor.measurements){
             for (dataPoint in measurement.dataPoints){
                    writer.write("${dataPoint.value}\n");
                 writer.flush()
             }
         }
         writer.close()
     }
        /*val configWriter: Writer = FileWriter(configFile,true);
        incrementConfigNumber(configWriter)
        configWriter.close()*/
 }
/*
    fun getConfigPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "config.json").toFile()
    }

    fun getDataPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "Measurement$number").toFile()
    }



    fun incrementConfigNumber(writer: Writer){

    }
    fun closeWriter(){
      configReader.close()
    }*/

}


