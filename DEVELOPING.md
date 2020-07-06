## Developing

With the new inclusion of Gradle replacing ant (bye bye 2014) this brings some more modern features. Running any of the commands will automatically install the Gradle wrapper, no other install needed. This uses Gradle 6.5.1 and Java 11.

### Run The Demo

```
./gradlew run
```

### Javadoc generation

```
./gradlew javadoc
```

### Building Jar

```
./gradlew shadowJar
```

### Publish To Maven Local

This allows you to use JSyn as a maven/gradle repository on your machine.

```
./gradlew publishToMavenLocal
```

It can be used via

```groovy
repositories {
    // ...
    mavenLocal()
}

dependencies {
    // ...
    implementation 'com.jsyn:jsyn:17.0.0-SNAPSHOT'
}
```

