# Kapacity

Kotlin compiler plugin that adds `shallowSize` extension property to all data classes.

## Getting started

### Usage

Kotlin 1.5.0 is supported.

1. Download `.jar` from release assets.
2. Add compiler option in `build.gradle.kts`:

```kotlin
tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xplugin=/path/to/core-1.0-SNAPSHOT.jar",
            "-P",
            "plugin:arrow.meta.plugin.compiler:generatedSrcOutputDir=${buildDir}"
        )
    }
}
```

### Development

```bash
$ git clone git@github.com:winter-yuki/kapacity.git
$ cd kapacity
$ ./gradlew :example:run # Run example
$ ./gradlew :core:test # Run tests
```
