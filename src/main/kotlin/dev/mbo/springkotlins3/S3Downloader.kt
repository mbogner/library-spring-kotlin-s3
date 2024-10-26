package dev.mbo.springkotlins3

import dev.mbo.logging.logger
import dev.mbo.springkotlins3.S3Caller.s3Exec
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
class S3Downloader(
    private val s3: S3Client,
    private val presigner: S3Presigner,
) {

    private val log = logger()

    fun download(
        id: S3FileId,
        downloadMetadata: Boolean = false,
        downloadTagging: Boolean = false,
        download: S3Download? = null
    ): S3Download {
        val objectFuture = downloadObject(id)
        val metaFuture = if (downloadMetadata) {
            downloadMetadata(id)
        } else {
            CompletableFuture.completedFuture(null)
        }
        val taggingFuture = if (downloadTagging) {
            downloadTagging(id)
        } else {
            CompletableFuture.completedFuture(null)
        }

        CompletableFuture.allOf(objectFuture, metaFuture, taggingFuture).join()
        log.debug("full downloads for s3 {} completed", id)

        val dlMetadata = if (downloadMetadata) {
            metaFuture.get()
        } else {
            download?.metadata // use existing value if provided
        }
        val dlTagging = if (downloadTagging) {
            taggingFuture.get()
        } else {
            download?.tagging // use existing value if provided
        }

        return S3Download(
            content = objectFuture.get(),
            metadata = dlMetadata,
            tagging = dlTagging
        )
    }

    fun downloadMeta(id: S3FileId): S3Download {
        val metaFuture = downloadMetadata(id)
        val taggingFuture = downloadTagging(id)

        CompletableFuture.allOf(metaFuture, taggingFuture).join()
        log.debug("meta downloads for s3 {} completed", id)

        return S3Download(
            metadata = metaFuture.get(),
            tagging = taggingFuture.get(),
        )
    }

    fun presignedGetUrl(id: S3FileId, validForSeconds: Long = 600L): S3PresignedRequest {
        val rq = GetObjectRequest.builder().bucket(id.bucketName).key(id.key).build()
        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(validForSeconds))
            .getObjectRequest(rq)
            .build()
        val presignedRequest = presigner.presignGetObject(presignRequest)
        log.debug("created presigned url for {} (valid for {}s)", id, validForSeconds)
        return S3PresignedRequest(
            method = presignedRequest.httpRequest().method(),
            url = presignedRequest.url(),
            expiration = presignedRequest.expiration()
        )
    }

    protected fun downloadObject(id: S3FileId): CompletableFuture<ResponseInputStream<GetObjectResponse>> {
        return s3Exec {
            log.debug("downloading s3 object {}", id)
            val rq = GetObjectRequest.builder().bucket(id.bucketName).key(id.key).build()
            CompletableFuture.completedFuture(s3.getObject(rq))
        }
    }

    protected fun downloadMetadata(id: S3FileId): CompletableFuture<HeadObjectResponse> {
        return s3Exec {
            log.debug("downloading s3 metadata {}", id)
            val rq = HeadObjectRequest.builder().bucket(id.bucketName).key(id.key).build()
            CompletableFuture.completedFuture(s3.headObject(rq))
        }
    }

    protected fun downloadTagging(id: S3FileId): CompletableFuture<GetObjectTaggingResponse> {
        return s3Exec {
            log.debug("downloading s3 tagging {}", id)
            val rq = GetObjectTaggingRequest.builder().bucket(id.bucketName).key(id.key).build()
            CompletableFuture.completedFuture(s3.getObjectTagging(rq))
        }
    }

}