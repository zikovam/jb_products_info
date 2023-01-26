package com.example.jb_products_info.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@Service
public class DataSourceImpl implements DataSource {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceImpl.class);

    @Override
    public void downloadFileByUrl(String url, String fileLocation) {
        logger.info("downloading update.xml from URL: {}", url);

        try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileLocation)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("download was finished. file location: {}", fileLocation);
            }
        } catch (MalformedURLException e) {
            logger.error("Error with url: {}", url);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error with url", e);
        } catch (IOException e) {
            logger.error("Problem with file downloading and saving");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Problem with file downloading and saving", e);
        }
    }
}
