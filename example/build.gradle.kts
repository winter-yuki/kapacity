import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks.withType<KotlinCompile>().all {
    dependsOn(":core:jar")
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xplugin=$rootDir/core/build/libs/core-1.0-SNAPSHOT.jar",
            "-P",
            "plugin:arrow.meta.plugin.compiler:generatedSrcOutputDir=${buildDir}"
        )
    }
}
