# spring-kotlin-s3

S3 library for Kotlin with Spring Boot.

## Release

see https://github.com/mbogner/spring-boot-bom for more details.

For example this prepares a release of 1.0.0 and a development version of 1.0.1-SNAPSHOT.
Do NOT run it with the sample versions. This needs to be updated on every run.

```shell
./gradlew release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.0.0 -Prelease.newVersion=1.0.1-SNAPSHOT
```

You can skip `-Prelease.releaseVersion=1.0.0 -Prelease.newVersion=1.0.1-SNAPSHOT` to release the snapshot and
autoincrement.

### Build

Local:
```shell
./gradlew clean signMavenPublication publishToMavenLocal
```

see `~/.m2/repository/dev/mbo/spring-kotlin-s3` for the created content

Release:
```shell
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

All in One:
```shell
./gradlew clean signMavenPublication publishToMavenLocal publishToSonatype closeAndReleaseSonatypeStagingRepository
```

By running this you don't need to use the web interface to close and release the library.

see https://s01.oss.sonatype.org/content/groups/public/dev/mbo/

### Web Process

https://s01.oss.sonatype.org