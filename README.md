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
  - ANT+ sensors
  - Any other sensor via a custom bridge that offers
    - TCP socket
    - CSV line streaming
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
    - Table chart
    - Bar Distribution chart
    - Pie Distribution chart
- Combine measurements of sensors
  - Average of multiple measurements
- Extensibility: Easily add new Sensors, Charts and Recorders
  - Sensor: Just create a new subtype of the `Sensor` class and it to the `supportedSensors` property of your `Settings` instance
  - Recorder: Just create a new subtype of the `Recorder` class and it to the `supportedRecorders` property of your `Settings` instance
  - Create a new subtype of `Chart` and implement the UI for adding it

## Building

The project targets JDK11 and Kotlin language version 1.3.

### Building with Bazel

Install:

- Bazel 4.2 (or higher)
- OpenJDK11

If you have Nix installed, you can get a shell with the dependencies like this:

```
nix-shell -p gnumake openjdk bazel_4
```

Run (on Windows run the `.exe` files):

```sh
# Commandline
bazel build //:Cli \
  && ./bazel-bin/Cli

# GUI (Needs JavaFX installed)
bazel build //:Gui \
  && ./bazel-bin/Gui

# GUI (JavaFX bundled into the JAR)
bazel build //:Gui_deploy.jar \
  && ./bazel-bin/Gui_deploy.jar
```

I seem to have trouble getting Bazel on Windows to find my Java installation.
Alternatively you can just use a JDK provided by Bazel:

```sh
bazel build //:Gui --java_runtime_version=remotejdk_11
bazel-bin/Gui.exe
```

Build with warnings as error:

```sh
bazel build --extra_toolchains='//:werror_toolchain' //:Gui
```

## Running the jar

1. Get it from the [Releases](https://github.com/JohnAZoidberg/SensorWrangler/releases) or [Actions](https://github.com/JohnAZoidberg/SensorWrangler/actions) page
2. Download JDK11 from [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot)
3. Run `java -cp Gui_deploy.jar`

## Supported Devices and other Sensors

### Using ANT USB-m transceiver
Multiple ANT+ device profiles are supported. Any device implementing a supported device profile is supported:

- Supported
  - Heart Rate
  - Speed
  - Cadence
  - Power
  - Fitness Equipment Control
- Unsupported
  - Combined Speed and Cadence
  - ...

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

Recorders can capture values from the sensor and make them available outside of
SensorWrangler.

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

For example you can create a socket recorder at port 8080 and connect to it via telnet:

```sh
telnet localhost 8080
```

and it will start printing the measured values in the same format as the CSV recorder.

## Charts
See the above GIF for examples.

- Angle chart
- Line chart
- Scatter chart
- Bar chart
- Table chart
  - Shows numerical values of multiple measurements
    - Current value
    - Minum value
    - Maximum value
    - Average value
    - Average of last N seconds
- Distribution chart
  - Shows the distribution between left and right values
  - Measurement value is of range 0 - 100, which signifies percentage of "right"
  - Visualization types
    - Pie chart
    - Bar chart

### Customizable grid
The charts are displayed in a grid, which is 2x2 in size, by default.
The number of rows and columns can be changed in the Settings window but it will only change when saving the configuration and restarting the program.

## Developing

View the dependency graph between the packages in this project by running the following bazel query.

Notes:

- Needs graphviz (dot) installed
- Use cquery because it respects the current platform (Operating System)
- Remove the filter to show 3rd party dependencies
- SVG can be viewed in a browser

```Sh
bazel cquery  --notool_deps --noimplicit_deps 'filter("^//.*:[a-zA-Z_]+$", deps(//:Gui))' --output graph | dot -Tsvg > deps.svg
```
