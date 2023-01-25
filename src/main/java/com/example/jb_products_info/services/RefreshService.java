package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import static com.example.jb_products_info.utils.Constants.ERROR_FIND_UPDATE_XML;
import static com.example.jb_products_info.utils.Constants.ERROR_PARSING_UPDATE_XML;
import static com.example.jb_products_info.utils.Constants.ERROR_SAVE_UPDATE_XML;
import static com.example.jb_products_info.utils.Constants.UPDATE_XML_FILE_PATH;

@Service
public class RefreshService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshService.class);

    @Value("${download.updates.url}")
    public String jetbrainsUpdateFileUrl;

    private final BuildDownloadService buildDownloadService;
    private final FileParserService fileParserService;
    private final ProductService productService;
    private final ThreadPoolTaskExecutor executor;

    public RefreshService(BuildDownloadService buildDownloadService,
                          FileParserService fileParserService,
                          ProductService productService,
                          ThreadPoolTaskExecutor executor) {
        this.buildDownloadService = buildDownloadService;
        this.fileParserService = fileParserService;
        this.productService = productService;
        this.executor = executor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void collectDataAfterStartup() {
        try {
            downloadFileByUrl();
            List<Product> products = fileParserService.collectProducts();
            productService.upsertAll(products);
            continueDownloading();
        } catch (FileNotFoundException e) {
            logger.error(ERROR_FIND_UPDATE_XML);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ERROR_FIND_UPDATE_XML, e);
        } catch (IOException e) {
            logger.error(ERROR_SAVE_UPDATE_XML);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, ERROR_SAVE_UPDATE_XML, e);
        } catch (XMLStreamException e) {
            logger.error(ERROR_PARSING_UPDATE_XML);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PARSING_UPDATE_XML, e);
        }
    }

    @Scheduled(cron = "@hourly")
    public void updateData() throws IOException, XMLStreamException {
        downloadFileByUrl();
        List<Product> products = fileParserService.collectProducts();
        List<Build> builds = productService.upsertAll(products);

        //if downloading queue is already empty, reapplying all builds
        if (executor.getQueueSize() + executor.getActiveCount() == 0) {
            continueDownloading();
        }

        buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
    }

    public void updateData(String productCode) throws IOException, XMLStreamException {
        downloadFileByUrl();
        Product product = fileParserService.collectProduct(productCode);
        if (product != null) {
            List<Build> builds = productService.upsert(product);
            buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
        }
    }

    public void continueDownloading() {
        List<Product> storedProducts = productService.findAll();

        for (Product product : storedProducts) {
            try {
                buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(product.getBuilds());
            } catch (IOException e) {
                logger.error("Problem with build downloading");
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Problem with build downloading", e);
            }
        }
    }

    public void downloadFileByUrl() throws IOException {
        logger.info("downloading update.xml from URL: {}", jetbrainsUpdateFileUrl);

        URL url = new URL(jetbrainsUpdateFileUrl);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(UPDATE_XML_FILE_PATH)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("download was finished. file location: {}", UPDATE_XML_FILE_PATH);
            }
        }
    }
}
