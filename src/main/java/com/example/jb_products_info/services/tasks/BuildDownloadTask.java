package com.example.jb_products_info.services.tasks;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

@Service
public class BuildDownloadTask {
    private static final Logger logger = LoggerFactory.getLogger(BuildDownloadTask.class);

    @Async
    public CompletableFuture<String> downloadBuildAndExtractFileAsync(String url,
                                                                      String targetFile,
                                                                      String buildNumber) throws IOException {
        return CompletableFuture.completedFuture(downloadBuildAndExtractFile(url, targetFile, buildNumber));
    }

    public String downloadBuildAndExtractFile(String url,
                                                     String targetFile,
                                                     String buildNumber) throws IOException {
        URL fileSource = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) fileSource.openConnection();
        GZIPInputStream gzipInputStream = new GZIPInputStream(urlConnection.getInputStream());
        TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream);
        TarArchiveEntry entry;

        long fileSize = urlConnection.getContentLength();
        logger.info("downloading from: {}, Size of the file: {}", url, fileSize);

        StringBuilder targetFileData = new StringBuilder(buildNumber);

        while ((entry = tarInputStream.getNextTarEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(targetFile)) {
                logger.info("Found {} file", targetFile);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[(int) entry.getSize()];
                int len;
                while ((len = tarInputStream.read(buffer, 0, (int) entry.getSize())) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                targetFileData.append(outputStream);
                outputStream.close();

                logger.info("File {} was extracted successfully", targetFile);
                break;
            }
//            TODO: think about possibility to track progress.
//                  Problem: uncompressed archive on the fly, has greater size than original
        }
        logger.info("red {} bytes", tarInputStream.getBytesRead());

        urlConnection.disconnect();
        gzipInputStream.close();
        tarInputStream.close();

        return targetFileData.toString();
    }
}
