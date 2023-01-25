package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * @param parsedProducts products to be checked
     * @return list of new builds, needed to be processed
     */
    @Transactional
    public List<Build> upsertAll(List<Product> parsedProducts) {

        List<Product> storedProducts = productRepository.findAll();

        List<Build> buildsToDownload = new ArrayList<>();

            parsedProducts.removeAll(storedProducts);
            logger.info("found {} new or updated products", parsedProducts.size());

        // updating every stored product with new builds we found
        for (Product parsedProduct : parsedProducts) {
            List<Build> parsedBuilds = parsedProduct.getBuilds();

            Optional<Product> storedProduct = storedProducts.stream()
                    .filter(prod -> prod.getCode().equals(parsedProduct.getCode()))
                    .findFirst();

            if (storedProduct.isPresent()) {
                List<Build> storedBuilds = storedProduct.get().getBuilds();

                parsedBuilds.removeAll(storedBuilds);
                parsedBuilds.forEach(build -> build.setProduct(storedProduct.get()));

                buildsToDownload.addAll(parsedBuilds);
                storedBuilds.addAll(parsedBuilds);

                logger.info("Product {} is updated with {} new builds",
                        storedProduct.get().getCode(), parsedBuilds.size());
            } else {
                storedProducts.add(parsedProduct);
                buildsToDownload.addAll(parsedProduct.getBuilds());

                logger.info("Product {} is new with {} new builds",
                        parsedProduct.getCode(), parsedBuilds.size());
            }
        }
        productRepository.saveAllAndFlush(storedProducts);

        return buildsToDownload;
    }

    /**
     * @param parsedProduct product to be checked
     * @return list of new builds, needed to be processed
     */
    @Transactional
    public List<Build> upsert(Product parsedProduct){
        Product storedProduct = productRepository.findByCode(parsedProduct.getCode());
        List<Build> buildsToDownload = new ArrayList<>();

        if (storedProduct == null){
            logger.info("found new product {}", parsedProduct.getCode());
            buildsToDownload.addAll(parsedProduct.getBuilds());
            productRepository.saveAndFlush(parsedProduct);
        } else {
            List<Build> storedBuilds = storedProduct.getBuilds();
            List<Build> parsedBuilds = parsedProduct.getBuilds();

            parsedBuilds.removeAll(storedBuilds);
            parsedBuilds.forEach(build -> build.setProduct(storedProduct));

            buildsToDownload.addAll(parsedBuilds);
            storedBuilds.addAll(parsedBuilds);

            productRepository.saveAndFlush(storedProduct);
        }
        logger.info("found {} new builds", buildsToDownload.size());
        return buildsToDownload;
    }

    @Transactional
    public List<Product> findAll(){
        return productRepository.findAll();
    }
}
