package com.example.localstackpoc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

/**
 * Service layer for S3 operations: upload, download, and list files.
 * Uses the same S3Client bean regardless of environment (LocalStack or real AWS).
 */
@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client,
                     @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Uploads a file to the configured S3 bucket.
     *
     * @param key         the object key (typically the filename)
     * @param content     the file content as a byte array
     * @param contentType the MIME type of the file
     * @return the object key that was stored
     */
    public String uploadFile(String key, byte[] content, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content)
        );
        log.info("Uploaded file '{}' to bucket '{}'", key, bucketName);
        return key;
    }

    /**
     * Downloads a file from the configured S3 bucket.
     *
     * @param key the object key to download
     * @return the file content as a byte array
     */
    public byte[] downloadFile(String key) {
        byte[] data = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                ResponseTransformer.toBytes()
        ).asByteArray();
        log.info("Downloaded file '{}' from bucket '{}' ({} bytes)", key, bucketName, data.length);
        return data;
    }

    /**
     * Lists all object keys in the configured S3 bucket.
     *
     * @return list of object keys
     */
    public List<String> listFiles() {
        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .build()
        );
        List<String> keys = response.contents().stream()
                .map(S3Object::key)
                .toList();
        log.info("Listed {} files in bucket '{}'", keys.size(), bucketName);
        return keys;
    }
}
