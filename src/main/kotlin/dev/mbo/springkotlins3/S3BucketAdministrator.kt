package dev.mbo.springkotlins3

import dev.mbo.logging.logger
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Bucket
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest

@Component
class S3BucketAdministrator(
    private val s3: S3Client,
) {

    private val log = logger()

    fun createBucket(bucketName: String) {
        S3Caller.s3Exec {
            log.debug("create bucket: {}", bucketName)
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
            log.debug("created bucket: {}", bucketName)
        }
    }

    fun deleteBucket(bucketName: String) {
        S3Caller.s3Exec {
            log.debug("delete bucket (if exists): {}", bucketName)
            s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build())
            log.debug("deleted bucket (if exists): {}", bucketName)
        }
    }

    fun listBuckets(): List<Bucket> {
        return S3Caller.s3Exec {
            log.debug("list all buckets")
            val buckets = s3.listBuckets().buckets()
            log.debug("existing buckets: {}", buckets)
            buckets
        }
    }

    fun listBucketNames(): List<String> {
        return listBuckets().map { it.name() }
    }

    fun createAllMissing(buckets: Set<String>) {
        log.debug("create missing buckets from list: {}", buckets)
        listBuckets().map { it.name() }.let {
            buckets.forEach { bucket ->
                if (it.contains(bucket)) {
                    log.debug("bucket {} already exists", bucket)
                } else {
                    createBucket(bucket)
                }
            }
        }
    }

}