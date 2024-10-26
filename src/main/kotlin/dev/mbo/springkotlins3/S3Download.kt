package dev.mbo.springkotlins3

import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import java.io.InputStream

data class S3Download(
    val metadata: HeadObjectResponse? = null,
    val tagging: GetObjectTaggingResponse? = null,
    val content: InputStream? = null,
)