package com.newsportal.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Value("${app.minio.public-url}")
    private String publicUrl;

    /**
     * Upload a file and return its public URL.
     */
    public String upload(MultipartFile file, String folder) {
        ensureBucketExists();

        String extension = getExtension(file.getOriginalFilename());
        String objectKey = buildObjectKey(folder, extension);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File uploaded to MinIO: {}/{}", bucket, objectKey);
            return publicUrl + "/" + objectKey;
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    /**
     * Delete a file by its object key.
     */
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.info("File deleted from MinIO: {}/{}", bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}/{}", bucket, objectKey, e);
        }
    }

    /**
     * Generate a presigned URL for temporary private access (e.g., documents).
     */
    public String generatePresignedUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL", e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to ensure MinIO bucket exists", e);
        }
    }

    private String buildObjectKey(String folder, String extension) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("%s/%d/%02d/%s%s",
                folder, now.getYear(), now.getMonthValue(),
                UUID.randomUUID(), extension);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
