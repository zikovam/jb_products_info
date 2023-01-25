package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    ProductRepository productRepository;
    @InjectMocks
    ProductService productService;

    private Build build1;
    private Build build2;
    private Build build3;
    private Build build4;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        build1 = Build.builder()
                .buildNumber("testBuildNumber1")
                .version("testVersion1")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl1")
                .build();
        build2 = Build.builder()
                .buildNumber("testBuildNumber2")
                .version("testVersion2")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl2")
                .build();
        build3 = Build.builder()
                .buildNumber("testBuildNumber3")
                .version("testVersion3")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl3")
                .build();
        build4 = Build.builder()
                .buildNumber("testBuildNumber4")
                .version("testVersion4")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl4")
                .build();

        product1 = Product.builder()
                .code("CODE1")
                .name("Test name 1")
                .builds(new ArrayList<>(Arrays.asList(build1, build2)))
                .build();
        product2 = Product.builder()
                .code("CODE2")
                .name("Test name 2")
                .builds(new ArrayList<>(Arrays.asList(build3)))
                .build();
        product3 = Product.builder()
                .code("CODE3")
                .name("Test name 3")
                .builds(new ArrayList<>(Arrays.asList(build4)))
                .build();
    }

    @Test
    void testUpsertAll_newProducts() {
        List<Product> storedProducts = new ArrayList<>(Arrays.asList(product1));
        List<Product> parsedProducts = new ArrayList<>(Arrays.asList(product1, product2));

        when(productRepository.findAll()).thenReturn(storedProducts);
        when(productRepository.saveAllAndFlush(storedProducts)).thenReturn(storedProducts);

        List<Build> buildsToDownload = productService.upsertAll(parsedProducts);

        assertEquals(1, buildsToDownload.size());
        assertEquals(build3, buildsToDownload.get(0));
        verify(productRepository, times(1)).findAll();
        verify(productRepository, times(1)).saveAllAndFlush(storedProducts);
    }

    @Test
    void testUpsertAll_newBuilds() {
        Product product2Alt = Product.builder()
                .code("CODE2")
                .name("Test name 2")
                .builds(new ArrayList<>(Arrays.asList(build2)))
                .build();
        List<Product> storedProducts = new ArrayList<>(Arrays.asList(product1, product2));
        List<Product> parsedProducts = new ArrayList<>(Arrays.asList(product1, product2Alt));

        when(productRepository.findAll()).thenReturn(storedProducts);
        when(productRepository.saveAllAndFlush(storedProducts)).thenReturn(storedProducts);

        List<Build> buildsToDownload = productService.upsertAll(parsedProducts);

        assertEquals(1, buildsToDownload.size());
        assertEquals(build2, buildsToDownload.get(0));
        verify(productRepository, times(1)).saveAllAndFlush(storedProducts);
    }

    @Test
    void testUpsertAll_oneNewProductAndOneRemoved() {
        List<Product> storedProducts = new ArrayList<>(Arrays.asList(product1, product2));
        List<Product> parsedProducts = new ArrayList<>(Arrays.asList(product1, product3));

        when(productRepository.findAll()).thenReturn(storedProducts);
        when(productRepository.saveAllAndFlush(storedProducts)).thenReturn(storedProducts);

        List<Build> buildsToDownload = productService.upsertAll(parsedProducts);

        assertEquals(1, buildsToDownload.size());
        assertEquals(build4, buildsToDownload.get(0));
        verify(productRepository, times(1)).saveAllAndFlush(storedProducts);
    }

    @Test
    void testUpsert_newProduct(){
        Product parsedProduct = product1;

        when(productRepository.findByCode(parsedProduct.getCode())).thenReturn(null);
        when(productRepository.saveAndFlush(parsedProduct)).thenReturn(parsedProduct);

        List<Build> buildsToDownload = productService.upsert(parsedProduct);

        assertEquals(2, buildsToDownload.size());
        assertEquals(build1, buildsToDownload.get(0));
        assertEquals(build2, buildsToDownload.get(1));
        verify(productRepository, times(1)).findByCode(parsedProduct.getCode());
        verify(productRepository, times(1)).saveAndFlush(parsedProduct);
    }

    @Test
    void testUpsert_newBuilds(){
        Product storedProduct = product1;

        Product product1Alt = Product.builder()
                .code("CODE1")
                .name("Test name 1")
                .builds(new ArrayList<>(Arrays.asList(build2, build3)))
                .build();

        when(productRepository.findByCode(product1Alt.getCode())).thenReturn(storedProduct);
        when(productRepository.saveAndFlush(storedProduct)).thenReturn(storedProduct);

        List<Build> buildsToDownload = productService.upsert(product1Alt);

        assertEquals(1, buildsToDownload.size());
        assertEquals(build3, buildsToDownload.get(0));
        verify(productRepository, times(1)).findByCode(product1Alt.getCode());
        verify(productRepository, times(1)).saveAndFlush(storedProduct);
    }
}
