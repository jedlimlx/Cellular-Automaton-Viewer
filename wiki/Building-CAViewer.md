### CAViewer's Dependencies (automatically installed)
* JavaFX for GUI
* ControlsFX for additional controls
* JUnit for Unit Tests
* javatuples for tuples
* Json-Java for reading and writing to *.json files
* Jackson for serialising and deserialising objects to *.json files

## Compiling from Source
CAViewer v2 uses Gradle as a build system.

To run the application without the executables, you will first need to install the JDK. <br>
Note that there is no need to install anything other than the JDK.

Ensure your JDK version is least 11. However, I would recommend 14 and above.

### Running the application
1. Run `gradlew build`
2. Run `gradlew run`

This is useful if you are modifying CAViewer.

### Cross-platform fat jar
1. Run `gradlew build --build-file build2.gradle`.

The jar will be generated in build/libs. 
This jar will be able to run on Linux, Mac and Windows provided that JRE / JDK 11 or higher are installed.

### Platform-dependent exectuable
1. Run `gradlew build`. <br>
2. Run `gradlew jpackage` <br>

Note that this requires JDK 14 or higher. <br>
Afterwards, the exectuable will be generated in build/jpackage.

#### Generating an installer
Changing *skipInstaller* from *false* to *true* in [build.gradle](../build.gradle) will generate an installer instead. <br>
On Windows, this will requires that you have the [WiX Toolset](https://wixtoolset.org/) installed.