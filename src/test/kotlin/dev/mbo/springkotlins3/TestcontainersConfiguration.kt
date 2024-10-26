package dev.mbo.springkotlins3

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import java.net.URI

@TestConfiguration(proxyBeanMethods = false)
open class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    internal open fun minioContainer(): MinIOContainer {
        return MinIOContainer(DockerImageName.parse("minio/minio:latest"))
    }

    @Bean
    internal open fun awsCredentialsProvider(
        minIOContainer: MinIOContainer,
    ): StaticCredentialsProvider {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                minIOContainer.userName, minIOContainer.password
            )
        )
    }

    @Bean
    @Qualifier("aws")
    internal open fun awsEndpointUri(
        minIOContainer: MinIOContainer,
    ): URI {
        return URI.create(minIOContainer.s3URL)
    }
}
