package dev.mbo.springkotlins3

class S3ClientException(
    message: String? = null,
    throwable: Throwable? = null
) : RuntimeException(message, throwable)