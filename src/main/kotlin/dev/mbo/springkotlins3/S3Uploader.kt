package dev.mbo.springkotlins3

import dev.mbo.springkotlins3.S3Caller.s3Exec
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.Tag
import software.amazon.awssdk.services.s3.model.Tagging
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

@Service
class S3Uploader(
    private val s3: S3Client
) {

    /**
     * Create or update a file on given coordinates (id).
     */
    fun uploadFromStream(
        id: S3FileId,
        inputStream: InputStream,
        contentType: String,
        contentLength: Long,
        meta: S3FileMeta = S3FileMeta()
    ) {
        s3Exec {
            val tagSet = meta.tags.map { Tag.builder().key(it.key).value(it.value).build() }
            val rq = PutObjectRequest.builder().bucket(id.bucketName).key(id.key).contentType(contentType)
                .contentEncoding(meta.encoding).metadata(meta.metadata)
                .tagging(Tagging.builder().tagSet(tagSet).build()).build()

            s3.putObject(rq, RequestBody.fromInputStream(inputStream, contentLength))
        }
    }

    /**
     * Create or update spring resource.
     */
    fun uploadResource(
        id: S3FileId,
        contentType: String,
        resource: Resource,
        meta: S3FileMeta = S3FileMeta()
    ) {
        resource.inputStream.use {
            val byteArray = it.readBytes()
            val contentLength = byteArray.size.toLong()
            ByteArrayInputStream(byteArray).use { inputStream ->
                uploadFromStream(
                    id = id,
                    contentType = contentType,
                    contentLength = contentLength,
                    inputStream = inputStream,
                    meta = meta
                )
            }
        }
    }

    /**
     * Create or update file. Length is taken from file directly.
     */
    fun uploadFile(
        id: S3FileId,
        file: File,
        contentType: String,
        meta: S3FileMeta = S3FileMeta()
    ) {
        check(file.exists()) {
            throw IllegalArgumentException("file $file does not exist")
        }
        file.inputStream().use { inputStream ->
            uploadFromStream(
                id = id,
                inputStream = inputStream,
                contentType = contentType,
                contentLength = file.length(),
                meta = meta
            )
        }
    }

    /**
     * Remove file with given coordinates.
     */
    fun delete(id: S3FileId) {
        s3Exec {
            val deleteRequest = DeleteObjectRequest.builder().bucket(id.bucketName).key(id.key).build()
            s3.deleteObject(deleteRequest)
        }
    }

}