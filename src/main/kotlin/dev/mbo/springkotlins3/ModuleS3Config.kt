package dev.mbo.springkotlins3

import dev.mbo.logging.logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@ConditionalOnProperty(
    value = ["aws.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Configuration
@ComponentScan(basePackageClasses = [ModuleS3Config::class])
open class ModuleS3Config(
    @Value("\${aws.credentials.access-key}")
    val accessKey: String,
    @Value("\${aws.credentials.secret-key}")
    val secretKey: String,
    @Value("\${aws.s3.region}")
    val awsRegion: String,
    @Value("\${aws.s3.endpoint}")
    val endpoint: String,
    @Value("\${aws.s3.custom:false}")
    val customConfigEnabled: Boolean,
) {

    private val log = logger()

    @Bean
    internal open fun amazonS3(
        @Qualifier("aws") endpointUri: URI,
        region: Region,
        credentialsProvider: StaticCredentialsProvider
    ): S3Client {
        log.info(
            "configure s3 to use custom config: {} (region={}, endpoint={})",
            customConfigEnabled, region, endpointUri
        )
        return S3Client.builder()
            .region(region)
            .endpointOverride(endpointUri)
            .credentialsProvider(credentialsProvider)
            .forcePathStyle(true) // we don't have any DNS setup for this - without this it tries <bucket>.<tld>
            .build()
    }

    @Bean
    internal open fun presigner(
        @Qualifier("aws") endpointUri: URI,
        region: Region,
        credentialsProvider: StaticCredentialsProvider
    ): S3Presigner {
        return S3Presigner.builder()
            .region(region)
            .endpointOverride(endpointUri)
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)  // Enable path-style access
                    .build()
            )
            .build()
    }

    @Bean
    @Qualifier("aws")
    @ConditionalOnMissingBean
    internal open fun awsEndpointUri(): URI {
        return URI.create(endpoint)
    }

    @Bean
    internal open fun awsRegion(): Region {
        return Region.of(awsRegion)
    }

    @Bean
    @ConditionalOnMissingBean
    internal open fun awsCredentialsProvider(): StaticCredentialsProvider {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
    }

}
