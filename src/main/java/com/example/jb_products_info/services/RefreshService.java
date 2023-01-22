package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import static com.example.jb_products_info.utils.Constants.UPDATE_FILE_PATH;

@Service
public class RefreshService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshService.class);

    @Value("${download.updates.url}")
    public String jetbrainsUpdateFileUrl;

    private final BuildDownloadService buildDownloadService;
    private final FileParserService fileParserService;
    private final ProductService productService;

    public RefreshService(BuildDownloadService buildDownloadService,
                          FileParserService fileParserService,
                          ProductService productService) {
        this.buildDownloadService = buildDownloadService;
        this.fileParserService = fileParserService;
        this.productService = productService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void collectDataAfterStartup() {
        try {
            downloadFileByUrl();
            List<Product> products = fileParserService.collectProducts();
            productService.upsertAll(products);
            continueDownloading();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "@hourly")
    public void updateData() {
        try {
            downloadFileByUrl();
            List<Product> products = fileParserService.collectProducts();
            List<Build> builds = productService.upsertAll(products);
            buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateData(String productCode) {
        try {
            downloadFileByUrl();
            Product product = fileParserService.collectProduct(productCode);
            if (product != null) {
                List<Build> builds = productService.upsert(product);
                buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void continueDownloading() {
        List<Product> storedProducts = productService.findAll();

        for (Product product :
                storedProducts) {
            try { //TODO: deal with it
                buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(product.getBuilds());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void downloadFileByUrl() throws IOException {
        //TODO: check is file the same we already got

        logger.info("downloading update.xml from URL: {}", jetbrainsUpdateFileUrl);

        URL url = new URL(jetbrainsUpdateFileUrl);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(UPDATE_FILE_PATH)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("download was finished. file location: {}", UPDATE_FILE_PATH);
            }
        }
    }
}
