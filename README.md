# SensorWrangler

![Preview](https://danielschaefer.me/SensorWrangler/0.1.0.gif)

## Features
- Free software (GPLv2 licensed)
- Read data from fitness tracking sensors
- Nice user interface to easily select and connect to devices
- Log data to file or database
  - CSV
  - JDBC database (only postgres driver yet)
- API for third-party programs to read sensor data
  - Just implement the `Recorder` interface and call `addRecorder()` on your instance of `SensorWrangler`
- Flexible visualization of sensor data
  - Pause, continue and jump-to-now time travelling
  - Charts
    - Line chart
    - Scatter chart
    - Bar chart
- Combine measurements of sensors
  - Average of multiple measurements
- Extensibility: Easily add new Sensors, Charts and Recorders
  - Sensor: Just create a new subtype of the `Sensor` class and it to the `supportedSensors` property of your `Settings` instance
  - Recorder: Just create a new subtype of the `Recorder` class and it to the `supportedRecorders` property of your `Settings` instance
  - Create a new subtype of `Chart` and implement the UI for adding it

## Building
Currently there are no Maven or Gradle build files available. The project has to be imported using IntelliJ IDEA and built from there.

Before building, the following dependencies have to be installed:

- Saving the configuration
  - [`com.fasterxml.jackson.core:jackson-databind:2.10.1`](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.10.1)
- *Tail*ing a file
  - [`commons-io:commons-io:2.6`](https://mvnrepository.com/artifact/commons-io/commons-io/2.6)
- Recording to databases
  - `exposed-core-0.18.1`
  - `exposed-jdbc-0.18.1`
  - `joda-time:joda-time:2.5` (runtime dependency of `exposed`)
  - `org.postgresql:postgresql:42.2.5`
- Connecting to ANT+ sensors
  - [j-antplus](https://github.com/glever/j-antplus)
- Logging (also run-time dependency of some other dependencies)
  - `org.slf4j:slf4j-api:1.7.11`

The project targets JDK11 and Kotlin language version 1.3.

## Running the jar
1. Get it from the [releases page](https://github.com/JohnAZoidberg/SensorWrangler/releases)
2. Download JDK11 from [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot)
3. Download JavaFX11 LTS from [Gluon](https://gluonhq.com/products/javafx/)
4. Extract both to the directories `jdk-11` and `javafx-sdk-11` respectively
5. Run `jdk-11\bin\java.exe -p javafx-sdk-11\lib --add-modules javafx.controls -jar SensorWrangler.jar`

## Supported Devices and other Sensors

### Using ANTUSB-m transceiver
#### Garmin HRM 3-SS
With the AntPlusSensor. It's configure to directly connect to the HRM (heart rate monitor).

With [openant](https://github.com/Tigge/openant/blob/master/examples/heart_rate_monitor.py) modified, so it writes the data to a file.
This file can be used by the FileSensor.

#### Garmin Bike Speed Sensor and Cadence Sensor
Coming soon to the AntPlusSensor.
First, support for their data pages has to be implemented in `j-antplus`. Watch the progess [here](https://github.com/glever/j-antplus/issues/1).

### Virtual Sensors

If you don't have any physical sensors yet, you can still try out the
visualization functionality by using the random sensors:

- RandomSensor: Spits out random values in range
- RandomWalkSensor: Spits out random values in range but doesn't jump more than stepSize

If your physical sensors are not yet supported, you can write a program to take
your sensor data make it available using one of the following virtual sensors:

#### FileSensor
SensorWrangler reads from the file line by line. If a sensor writes a new line,
it will immediately be added to the list of data points.

At `tools/heartrate.py` there is an example implementation of such a
sensor, which writes random values to the specified file.

#### SocketSensor
SensorWrangler connects to *hostname* at *port* and adds a new data point for
each new line sent over the socket.

At `tools/socket-sensor.py` there is an example implementation of such a
sensor, which opens up a port on localhost and generates random values.

## Recorders

### CsvRecorder
Record to a CSV formatted file. Can be `tail -f`ed

### DatabaseRecorder
Connect to a database via JDBC (currently only PostgreSQL)

If your database is running on `localhost` at port `5432` in database `wrangler`, your connection string is: `[::1]:5432/wrangler`

### Socket recorder
Opens a TCP socket and writes CSV formatted lines to client sockets.

## Charts
See the above GIF for examples.

- Line chart
- Scatter chart
- Bar chart
