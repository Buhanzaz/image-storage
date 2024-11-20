package com.blockBox.imageStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final static int MAX_LENGTH_IMAGE = 10485760;
    private final S3AsyncClient s3AsyncClient;

    @PostMapping()
    Flux<String> uploadImages(@RequestPart("file") Flux<FilePart> fileParts) {
        return fileParts
                .flatMap(this::compressImage) // Process each file
                // TODO File extension and size validation needed
                .flatMap(this::uploadToStorage); // upload each file
    }


    private Mono<String> uploadToStorage(byte[] bytes) {
        String bucketName = "photo";
        String objectKey = UUID.randomUUID() + ".jpeg";

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("image/jpeg")
                .build();

        return Mono.fromFuture(() -> s3AsyncClient.putObject(objectRequest, AsyncRequestBody.fromBytes(bytes)))
                .map(response -> "Image uploaded to " + bucketName + "/" + objectKey + "\n");
    }


    private Mono<byte[]> compressImage(FilePart filePart) {
        Flux<DataBuffer> content = filePart.content();

        return DataBufferUtils
                .join(content)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(dataBuffer -> {
                            try {
                                byte[] originalBytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(originalBytes);

                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(originalBytes);
                                BufferedImage originalImage = ImageIO.read(byteArrayInputStream);

                                ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
                                ImageIO.write(originalImage, "jpeg", compressedOutputStream);

                                byte[] compressedBytes = compressedOutputStream.toByteArray();

                                return Mono.just(compressedBytes);
                            } catch (Exception e) {
                                return Mono.error(new RuntimeException("Error during image compression", e));
                            } finally {
                                DataBufferUtils.release(dataBuffer);
                            }
                        }
                );
    }
}
