plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("org.sonarqube")
    signing // required for maven central
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin")
    id("net.researchgate.release")
}

val javaVersion: String by System.getProperties()
val mavenGroup: String by System.getProperties()
group = mavenGroup

dependencies {
    implementation(platform(libs.bom))

    api("org.springframework.boot:spring-boot-starter")
    api(libs.kotlin.logging)

    api("software.amazon.awssdk:s3")
    api(libs.jaxb.api)
    api("commons-io:commons-io")

    // TEST ---------
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:minio")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion))
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    withType<Wrapper> {
        val gradleReleaseVersion: String by System.getProperties()
        gradleVersion = gradleReleaseVersion
        distributionType = Wrapper.DistributionType.BIN
    }

    withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    dokkaJavadoc {
        outputDirectory.set(layout.buildDirectory.dir("documentation/javadoc"))
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaJavadoc").get().outputDirectory)
    }

    named("afterReleaseBuild") {
        dependsOn(
            "signMavenPublication",
            "publishToMavenLocal",
            "publishToSonatype",
            "closeAndReleaseSonatypeStagingRepository"
        )
    }
}

sonarqube {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.projectKey", "library::spring-kotlin-s3")
        property("sonar.projectName", "spring-kotlin-s3")
        property("sonar.sources", "src/main/kotlin,src/main/resources")
        property("sonar.exclusions", "**/src/gen/**/*")
    }
}

jacoco {
    val jacocoToolVersion: String by System.getProperties()
    toolVersion = jacocoToolVersion
}

nexusPublishing {
    repositories {
        sonatype {
            // needed because default was updated to another server that this project can't use atm
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrhUsername") as String?)
            password.set(project.findProperty("ossrhPassword") as String?)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("spring-kotlin-s3")
                description.set("S3 library for Kotlin with Spring Boot.")
                url.set("https://mbo.dev")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    url.set("https://github.com/mbogner/library-spring-kotlin-s3")
                    connection.set("git@github.com:mbogner/library-spring-kotlin-s3.git")
                    developerConnection.set("git@github.com:mbogner/library-spring-kotlin-s3.git")
                }
                developers {
                    developer {
                        id.set("mbo")
                        name.set("Manuel Bogner")
                        email.set("outrage_breath.0t@icloud.com")
                        organization.set("mbo.dev")
                        organizationUrl.set("https://mbo.dev")
                        timezone.set("Europe/Vienna")
                        roles.set(listOf("developer", "architect"))
                    }
                }
                organization {
                    name.set("mbo.dev")
                    url.set("https://mbo.dev")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}