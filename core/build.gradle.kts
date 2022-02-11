plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.0")
    compileOnly("io.arrow-kt:arrow-meta:1.5.0-SNAPSHOT")
}
