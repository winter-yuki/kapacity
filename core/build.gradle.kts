import org.gradle.jvm.tasks.Jar
import java.io.File

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.0")
    compileOnly("io.arrow-kt:arrow-meta:1.5.0-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.arrow-kt:arrow-meta:1.5.0-SNAPSHOT")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Jar> {
    val classpath = sourceSets.main.get().compileClasspath
    val sources = classpath.find {
        File("arrow-kt/arrow-meta").toString() in it.absolutePath
    }!!
    from(zipTree(sources))
}
