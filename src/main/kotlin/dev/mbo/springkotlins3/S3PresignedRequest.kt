package dev.mbo.springkotlins3

import software.amazon.awssdk.http.SdkHttpMethod
import java.net.URL
import java.time.Instant

data class S3PresignedRequest(
    val method: SdkHttpMethod,
    val url: URL,
    val expiration: Instant,
)