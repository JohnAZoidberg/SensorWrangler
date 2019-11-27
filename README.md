# SensorWrangler

- Read data from fitness tracking sensors
- Nice user interface to easily select and connect to devices
- Log data to file or database
  - CSV
- API for third-party programs to read sensor data
  - CSV (using live log)
- Flexible visualization of sensor data
  - Pause, continue and jump-to-now time travelling
  - Charts
    - Line chart
    - Scatter chart
    - Bar chart
- Combine measurements of sensors
  - Average of multiple measurements
- Extensibility: Easily add new Sensors and Charts

## Building
Currently there are no Maven or Gradle build files available. The project has to be imported using IntelliJ IDEA and built from there.

Before building, the following dependencies have to be installed:

- [`com.fasterxml.jackson.core:jackson-databind:2.10.1`](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.10.1)
- [`commons-io:commons-io:2.6`](https://mvnrepository.com/artifact/commons-io/commons-io/2.6)

The project targets JDK11 and Kotlin language version 1.3 with experimental features, so kotlinc has to be passed the flags `-Xuse-experimental=kotlin.ExperimentalStdlibApi`.

## Running the jar
1. Get it from the [releases page](https://github.com/JohnAZoidberg/SensorWrangler/releases)
2. Download JDK11 from [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot)
3. Download JavaFX11 LTS from [Gluon](https://gluonhq.com/products/javafx/)
4. Extract both to the directories `jdk-11` and `javafx-sdk-11` respectively
5. Run `jdk-11\bin\java.exe -p javafx-sdk-11\lib --add-modules javafx.controls -jar SensorWrangler.jar`

## Supported Devices

### Using ANTUSB-m transceiver
#### Garmin HRM 3-SS
With [openant](https://github.com/Tigge/openant/blob/master/examples/heart_rate_monitor.py) modified, so it writes the data to a file. Instructions coming soon.

#### Garmin Bike Speed Sensor and Cadence Sensor
Coming soon.
