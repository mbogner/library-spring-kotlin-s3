package dev.mbo.springkotlins3

import org.apache.hc.client5.http.HttpResponseException
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpStatus
import org.apache.hc.core5.http.io.HttpClientResponseHandler
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [TestApplication::class])
class S3UploaderDownloaderTest @Autowired constructor(
    private val s3Uploader: S3Uploader,
    private val s3Downloader: S3Downloader,
) {

    companion object {
        private const val BUCKET_NAME = "test1"
    }

    @Test
    fun contextLoads() {
        assertThat(s3Uploader).isNotNull
        assertThat(s3Downloader).isNotNull
    }

    @Test
    fun upAndDownload() {
        val id = S3FileId(bucketName = BUCKET_NAME)
        save(id, filePath = "src/test/resources/application.yml")
        read(id)
        val url = presignedUrl(id)
        downloadFileWithHttpClient(url)
        save(id, filePath = "src/test/resources/test.yml")
        delete(id)
    }

    private fun read(id: S3FileId) {
        // read metadata
        val downloadMeta: S3Download = s3Downloader.downloadMeta(id)
        assertThat(downloadMeta).isNotNull
        assertThat(downloadMeta.metadata).isNotNull
        assertThat(downloadMeta.tagging).isNotNull
        assertThat(downloadMeta.content).isNull()

        // read full
        var downloadFull: S3Download = s3Downloader.download(id, downloadMetadata = true, downloadTagging = true)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNotNull
        assertThat(downloadFull.tagging).isNotNull
        assertThat(downloadFull.content).isNotNull

        downloadFull = s3Downloader.download(id, downloadMetadata = false, downloadTagging = true)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNull()
        assertThat(downloadFull.tagging).isNotNull
        assertThat(downloadFull.content).isNotNull

        downloadFull = s3Downloader.download(id, downloadMetadata = true, downloadTagging = false)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNotNull
        assertThat(downloadFull.tagging).isNull()
        assertThat(downloadFull.content).isNotNull

        downloadFull = s3Downloader.download(id, downloadMetadata = false, downloadTagging = false)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNull()
        assertThat(downloadFull.tagging).isNull()
        assertThat(downloadFull.content).isNotNull

        downloadFull = s3Downloader.download(id)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNull()
        assertThat(downloadFull.tagging).isNull()
        assertThat(downloadFull.content).isNotNull

        downloadFull = s3Downloader.download(id, download = downloadMeta)
        assertThat(downloadFull).isNotNull
        assertThat(downloadFull.metadata).isNotNull
        assertThat(downloadFull.tagging).isNotNull
        assertThat(downloadFull.content).isNotNull
    }

    private fun presignedUrl(id: S3FileId): S3PresignedRequest {
        val presignedUrl = s3Downloader.presignedGetUrl(id)
        assertThat(presignedUrl).isNotNull
        assertThat(presignedUrl.method).isNotNull
        assertThat(presignedUrl.url).isNotNull
        assertThat(presignedUrl.expiration).isNotNull
        return presignedUrl
    }

    private fun save(id: S3FileId, filePath: String, contentType: String = "text/x-yaml") {
        s3Uploader.uploadFile(
            id = id, file = File(filePath), contentType = contentType
        )
    }

    private fun delete(id: S3FileId) {
        // delete
        s3Uploader.delete(id)
        var exc = assertThrows(S3ClientException::class.java) {
            s3Downloader.downloadMeta(id)
        }
        assertThat(exc).hasCauseInstanceOf(NoSuchKeyException::class.java)
        exc = assertThrows(S3ClientException::class.java) {
            s3Downloader.download(id)
        }
        assertThat(exc).hasCauseInstanceOf(NoSuchKeyException::class.java)
        // we can delete also unknown keys
        s3Uploader.delete(id)
        s3Uploader.delete(S3FileId(bucketName = BUCKET_NAME))
        // but not from unknown buckets
        exc = assertThrows(S3ClientException::class.java) {
            s3Uploader.delete(S3FileId(bucketName = UUID.randomUUID().toString()))
        }
        assertThat(exc).hasCauseInstanceOf(NoSuchBucketException::class.java)
    }

    private fun downloadFileWithHttpClient(presignedRequest: S3PresignedRequest) {
        val httpGet = HttpGet(presignedRequest.url.toURI())
        val responseHandler = HttpClientResponseHandler { response ->
            if (response.code == HttpStatus.SC_OK) {
                response.entity?.let {
                    ByteArrayOutputStream().use { bout ->
                        bout.write(EntityUtils.toByteArray(it))
                        bout.close()
                    }
                }
            } else {
                throw HttpResponseException(response.code, "Failed to download file: ${response.reasonPhrase}")
            }
        }
        HttpClients.createDefault().use { client ->
            client.execute(httpGet, responseHandler)
        }
    }

}