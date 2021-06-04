import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.0"
    kotlin("jvm") version "1.5.0"
}

group = "org.caviewer"
version = "2.0"

application {
    mainClass.set("application.Main")
    mainModule.set("CAViewer")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javatuples:javatuples:1.2")
    implementation ("org.controlsfx:controlsfx:11.0.2") {
        exclude("org.openjfx")
    }

    implementation("org.json:json:20200518")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")
    implementation("info.picocli:picocli:4.5.1")
    implementation("org.testng:testng:7.1.0")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    testImplementation("org.testfx:testfx-junit5:4.0.16-alpha")
    testImplementation("org.testfx:openjfx-monocle:jdk-11+26") // For Java 11

    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:mac")
}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
    test {
        java {
            srcDirs("src/test/kotlin")
        }
        resources {
            srcDirs("src/test/resources")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    val compileJava: JavaCompile by tasks
    destinationDir = compileJava.destinationDir

    kotlinOptions.jvmTarget = "11"
}

tasks.withType<JavaCompile> {
    dependsOn(":compileKotlin")
    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
        //inputs.property("moduleName", ext["moduleName"])
        doFirst {
            val compileJava: JavaCompile by tasks
            compileJava.options.compilerArgs = listOf(
                // include Gradle dependencies as modules
                "--module-path", sourceSets.main.get().compileClasspath.asPath
            )

            sourceSets.main.get().compileClasspath = files()
        }
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "CAViewer"

    manifest {
        attributes["Implementation-Title"] = "CAViewer"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "application.Main"
    }

    from(configurations.compile.get().map({ if (it.isDirectory) it else zipTree(it) }))
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))

    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
