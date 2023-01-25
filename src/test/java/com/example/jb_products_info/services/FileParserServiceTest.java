package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
class FileParserServiceTest {
    @InjectMocks
    FileParserService fileParserService;

    @Mock
    private Product mockProduct;

    @BeforeEach
    void init() {
        try {
            Files.deleteIfExists(Path.of(Constants.UPDATE_XML_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenCollectProductsWithoutFile_thenFileNotFoundException() {
        Assertions.assertThrows(FileNotFoundException.class,
                () -> fileParserService.collectProducts(),
                "FileNotFoundException was expected");
    }
}
