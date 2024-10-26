package dev.mbo.springkotlins3

import dev.mbo.logging.logger
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(
    value = ["aws.s3.buckets-init"],
    havingValue = "true",
    matchIfMissing = true
)
@Configuration
open class S3BucketInit(
    private val s3BucketAdmin: S3BucketAdministrator,
    @Value("\${aws.s3.buckets:}")
    val bucketsCsv: String,
) {

    private val log = logger()

    @PostConstruct
    fun initBuckets() {
        val buckets = bucketsCsv.split(",").map { it.trim() }.toSet()
        log.info("init buckets {}", buckets)
        s3BucketAdmin.createAllMissing(buckets)
    }

}