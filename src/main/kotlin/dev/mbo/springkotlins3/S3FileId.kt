package dev.mbo.springkotlins3

import java.util.UUID

class S3FileId(
    val bucketName: String,
    val key: String = UUID.randomUUID().toString(),
)