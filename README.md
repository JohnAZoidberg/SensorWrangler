# SensorWrangler

![Preview](https://danielschaefer.me/SensorWrangler/0.1.0.gif)

## Goals
The aim of this project is to provide a free/libre software solution for using fitness sensors.


To provide that, five features are necessary:

- Get data from sensors (using any protocol)
- Control fitness equipment (like resistance of a bike trainer)
- Provide a simple visualization of the data
- Provide simple transformation abilities (e.g. average same measurements from different sensor)
- Export (live) data to a number of targets (e.g. file, socket, as library, ...)

This project is **not** going to implement a complex featureset like that of [GoldenCheetah](https://www.goldencheetah.org/).

It should be easy, to add new sensors and export targets.

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
  - Cusomizable number of charts
  - Charts
    - Angle chart
    - Line chart
    - Scatter chart
    - Bar chart
    - Current value chart
    - Distribution chart
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

### Using ANT USB-m transceiver
#### Garmin HRM 3-SS
With the AntPlusSensor. It's configure to directly connect to the HRM (heart rate monitor).
When running on Windows, please install the libusb driver using [Zadig](https://zadig.akeo.ie/):

1. Plug ANT USB-m transeiver into USB port
2. Open Zadig
3. Option -> List all devices
4. Select device: `ANT USB-m Stick`
5. Select target driver: `libusb-win32 (v1.2.6.0)`
6. Downgrade driver

With [openant](https://github.com/Tigge/openant/blob/master/examples/heart_rate_monitor.py) modified, so it writes the data to a file.
This file can be used by the FileSensor.

#### Garmin Bike Speed Sensor and Cadence Sensor
Supported with `AntSpeedSensor` and `AntCadenceSensor`.

#### ELITE Suito
Supported with `AntStationaryBike`.

#### Garmin Vector 3
Supported with `AntPowerSensor`.

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
Connect to a database via JDBC.

For PostgreSQL, the connection string is `postgresql://[::1]:5432/wrangler` and the driver is `org.postgresql.Driver`,
if your database is running on `localhost` at port `5432` in database `wrangler`.

For SQLite, the connection string is `sqlite:/var/my.db` and the driver is `org.sqlite.JDBC`,
if your database is located at `/var/my.db`.

The library for connecting to the database is Jetbrains's Exposed.
To use a different database, check the [supported](https://github.com/JetBrains/Exposed#Dialects) DBs and add the required JDBC driver to the classpath before starting SensorWrangler.

### Socket recorder
Opens a TCP socket and writes CSV formatted lines to client sockets.

## Charts
See the above GIF for examples.

- Angle chart
- Line chart
- Scatter chart
- Bar chart
- Current value chart
  - Shows the current values of multiple measurements
- Distribution chart
  - Shows the distribution between left and right values
  - Measurement value is of range 0 - 100, which signifies percentage of "right"

### Customizable grid
The charts are displayed in a grid, which is 2x2 in size, by default.
The number of rows and columns can be changed in the Settings window but it will only change when saving the configuration and restarting the program.
