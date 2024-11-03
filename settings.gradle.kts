rootProject.name = "spring-kotlin-s3"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        val kotlinVersion: String by System.getProperties()
        val sonarqubeVersion: String by System.getProperties()
        val dokkaVersion: String by System.getProperties()

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        kotlin("plugin.noarg") version kotlinVersion
        id("org.sonarqube") version sonarqubeVersion
        id("org.jetbrains.dokka") version dokkaVersion

        val nexusPublishPluginVersion: String by System.getProperties()
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishPluginVersion

        val gradleReleasePluginVersion: String by System.getProperties()
        id("net.researchgate.release") version gradleReleasePluginVersion
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val bomVersion: String by System.getProperties()
            version("bom", bomVersion)
            library("bom", "dev.mbo", "spring-boot-bom").versionRef("bom")

            val kotlinLoggingVersion: String by System.getProperties()
            version("kotlin-logging", kotlinLoggingVersion)
            library("kotlin-logging", "dev.mbo", "kotlin-logging")
                .versionRef("kotlin-logging")

            val jaxbApiVersion: String by System.getProperties()
            version("jaxb-api", jaxbApiVersion)
            library("jaxb-api", "javax.xml.bind", "jaxb-api").versionRef("jaxb-api")
        }
    }
}