import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.21.1"
    kotlin("jvm") version "1.5.0"
}

group = "me.jedli"
version = "2.0"

application {
    mainClass.set("application.Main")
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


jlink {
    val os = org.gradle.internal.os.OperatingSystem.current()
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    
    launcher {
        name = "application.Main"
    }

    jpackage {
        outputDir = "jpackage"
        imageName = "CAViewer"
        skipInstaller = true
        imageOptions = if (os.isWindows) listOf("--win-console", "--icon", "src/main/resources/icon/PulsarIcon.ico")
        else listOf("--icon", "src/main/resources/icon/PulsarIcon.png")
        installerName = "CAViewer"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}