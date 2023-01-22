package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
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

        if (storedProducts.size() != parsedProducts.size()) {
            parsedProducts.removeAll(storedProducts);
            logger.info("found {} new products", parsedProducts.size());

            buildsToDownload.addAll(parsedProducts.stream()
                    .map(Product::getBuilds)
                    .flatMap(Collection::stream)
                    .toList());
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
        logger.info("found {} new builds", buildsToDownload.size());
        productRepository.saveAllAndFlush(storedProducts);

        return buildsToDownload;
    }

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
