package dev.mbo.springkotlins3

import java.nio.charset.StandardCharsets

data class S3FileMeta(
    val metadata: Map<String, String> = emptyMap(),
    val tags: Map<String, String> = emptyMap(),
    val encoding: String = StandardCharsets.UTF_8.toString(),
)
