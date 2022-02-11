import org.gradle.jvm.tasks.Jar
import java.io.File

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.0")
    compileOnly("io.arrow-kt:arrow-meta:1.5.0-SNAPSHOT")
}

tasks.withType<Jar> {
    val classpath = sourceSets.main.get().compileClasspath
    val sources = classpath.find {
        File("arrow-kt/arrow-meta").toString() in it.absolutePath
    }!!
    from(zipTree(sources))
}
