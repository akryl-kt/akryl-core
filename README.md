# akryl

Kotlin type-safe wrapper for React.JS.

## Installation

To use akryl you need to create Kotlin.JS project with [kotlin-frontend-plugin](https://github.com/Kotlin/kotlin-frontend-plugin). 
You can use [hello-world](https://github.com/akryl-kt/akryl-examples/tree/master/hello-world) example as a template 
or build your own configuration. 

### Gradle

Add jcenter repository:

```gradle
repositories {
    jcenter()
}
```

Add dependency:

```gradle
dependencies {
    compile "io.akryl:akryl-core:<version>"
}
```

Add npm packages:

```gradle
kotlinFrontend {
    npm {
        dependency("react", "16.8.6")
        dependency("react-dom", "16.8.6")
    }
}
```

Run webpack server in continuous mode:

```bash
./gradlew -t run
```
