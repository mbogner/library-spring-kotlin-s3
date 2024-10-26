package dev.mbo.springkotlins3

import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.model.S3Exception

object S3Caller {

    fun <T> s3Exec(call: () -> T): T {
        return try {
            call.invoke()
        } catch (exc: S3Exception) {
            // The call was transmitted successfully, but Amazon S3 couldn't process it, so it returned an error response.
            throw S3ClientException("Unprocessable request", exc)
        } catch (exc: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client couldn't parse the response from Amazon S3.
            throw S3ClientException("Client request failed", exc)
        }
    }

}