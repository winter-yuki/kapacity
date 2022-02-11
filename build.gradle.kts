import java.net.URI

plugins {
    kotlin("jvm") version "1.5.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    group = "me.yukio"
    version = "1.0-SNAPSHOT"
}
