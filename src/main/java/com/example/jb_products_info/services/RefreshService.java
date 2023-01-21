package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.example.jb_products_info.utils.Constants.UPDATE_FILE_PATH;

@Service
public class RefreshService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshService.class);

    @Value("${download.updates.url}")
    public String jetbrainsUpdateFileUrl;

    private final BuildDownloadService buildDownloadService;
    private final FileParserService fileParserService;
    private final ProductRepository productRepository;

    public RefreshService(BuildDownloadService buildDownloadService,
                          FileParserService fileParserService,
                          ProductRepository productRepository) {
        this.buildDownloadService = buildDownloadService;
        this.fileParserService = fileParserService;
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void collectDataAfterStartup() {
        try {
            downloadFileByUrl();
            List<Product> products = fileParserService.collectProducts();
            upsertAll(products);
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
            List<Build> builds = upsertAll(products);
            buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(builds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void continueDownloading() {
        List<Product> storedProducts = productRepository.findAll();

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
        //TODO: check is file the same we already got;

        logger.info("downloading update.xml from URL: {}", jetbrainsUpdateFileUrl);

        URL url = new URL(jetbrainsUpdateFileUrl);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(UPDATE_FILE_PATH)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("download was finished. file location: {}", UPDATE_FILE_PATH);
            }
        }
    }

    /**
     * @param parsedProducts
     * @return list of new builds, needed to be processed
     */
    @Transactional
    public List<Build> upsertAll(List<Product> parsedProducts) {

        List<Product> storedProducts = productRepository.findAll();

        List<Build> buildsToDownload = new ArrayList<>();

        if (storedProducts.size() != parsedProducts.size()) {
            System.out.println(parsedProducts.removeAll(storedProducts)); //TODO: deal with it
            System.out.println(parsedProducts.size());

            buildsToDownload.addAll(parsedProducts.stream().map(Product::getBuilds).flatMap(Collection::stream).toList());

            productRepository.saveAllAndFlush(parsedProducts);
        }

        // updating every stored product with new builds we found
        for (Product product : storedProducts) {
            List<Build> storedBuilds = product.getBuilds();

            Optional<Product> parsedProduct = parsedProducts.stream()
                    .filter(prod -> prod.getCode().equals(product.getCode()))
                    .findFirst();

            if (parsedProduct.isPresent()) {
                List<Build> parsedBuilds = parsedProduct.get().getBuilds();

                parsedBuilds.removeAll(storedBuilds);
                parsedBuilds.forEach(build -> build.setProduct(product));

                buildsToDownload.addAll(parsedBuilds);

                storedBuilds.addAll(parsedBuilds);
            }
        }
        productRepository.saveAllAndFlush(storedProducts);

        return buildsToDownload;
    }
}
