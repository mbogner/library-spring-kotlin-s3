package dev.mbo.springkotlins3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [TestApplication::class])
class S3BucketAdministratorTest @Autowired constructor(
    private val s3BucketAdministrator: S3BucketAdministrator,
    @Value("\${aws.s3.buckets:}")
    val bucketsCsv: String,
) {

    private val existingBucketNames: List<String> = bucketsCsv.split(",").toList()

    @Test
    fun contextLoads() {
        assertThat(s3BucketAdministrator).isNotNull
    }

    @Test
    fun listBuckets() {
        val buckets = s3BucketAdministrator.listBucketNames()
        assertThat(buckets).hasSize(existingBucketNames.size)
        assertThat(buckets).containsAll(existingBucketNames)
    }

    @Test
    fun createListDelete() {
        val bucketName = UUID.randomUUID().toString()
        test(bucketName, existingBucketNames.size, false)
        s3BucketAdministrator.createBucket(bucketName)
        test(bucketName, existingBucketNames.size + 1, true)
        s3BucketAdministrator.deleteBucket(bucketName)
        test(bucketName, existingBucketNames.size, false)
    }

    fun test(bucketName: String, size: Int, included: Boolean) {
        val buckets = s3BucketAdministrator.listBucketNames()
        assertThat(buckets).hasSize(size)
        if (included) {
            assertThat(buckets).contains(bucketName)
        } else {
            assertThat(buckets).doesNotContain(bucketName)

        }
    }

}