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

import java.io.IOException;
import java.util.List;

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
    private final DataSource dataSource;

    public RefreshService(BuildDownloadService buildDownloadService,
                          FileParserService fileParserService,
                          ProductService productService,
                          ThreadPoolTaskExecutor executor,
                          DataSource dataSource) {
        this.buildDownloadService = buildDownloadService;
        this.fileParserService = fileParserService;
        this.productService = productService;
        this.executor = executor;
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void collectDataAfterStartup() {
        dataSource.downloadFileByUrl(jetbrainsUpdateFileUrl, UPDATE_XML_FILE_PATH);
        List<Product> products = fileParserService.collectProducts();
        productService.upsertAll(products);
        continueDownloading();
    }

    @Scheduled(cron = "@hourly")
    public void updateData() throws IOException {
        dataSource.downloadFileByUrl(jetbrainsUpdateFileUrl, UPDATE_XML_FILE_PATH);
        List<Product> products = fileParserService.collectProducts();
        List<Build> builds = productService.upsertAll(products);

        //if downloading queue is already empty, reapplying all builds
        if (executor.getQueueSize() + executor.getActiveCount() == 0) {
            continueDownloading();
        }

        buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
    }

    public void updateData(String productCode) throws IOException {
        dataSource.downloadFileByUrl(jetbrainsUpdateFileUrl, UPDATE_XML_FILE_PATH);
        Product product = fileParserService.collectProduct(productCode);
        if (product != null) {
            List<Build> builds = productService.upsert(product);
            buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
        }
    }

    public void continueDownloading() {
        List<Product> storedProducts = productService.findAll();
        logger.info("continuing build download");

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
}
