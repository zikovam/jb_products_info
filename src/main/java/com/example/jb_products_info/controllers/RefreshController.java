package com.example.jb_products_info.controllers;

import com.example.jb_products_info.services.RefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.example.jb_products_info.utils.Constants.ERROR_FIND_UPDATE_XML;
import static com.example.jb_products_info.utils.Constants.ERROR_SAVE_UPDATE_XML;

@RestController
@RequestMapping(value = "/refresh")
public class RefreshController {
    final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    private final RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @GetMapping
    public ResponseEntity<String> refreshInformation() {
        logger.info("Refreshing all information");
        try {
            refreshService.updateData();
            return new ResponseEntity<>("Data updated successfully.", HttpStatus.OK);
        } catch (FileNotFoundException e) {
            logger.error(ERROR_FIND_UPDATE_XML);
            return new ResponseEntity<>(ERROR_FIND_UPDATE_XML, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            logger.error(ERROR_SAVE_UPDATE_XML);
            return new ResponseEntity<>(ERROR_SAVE_UPDATE_XML, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<String> refreshForProduct(@PathVariable String productCode) {
        logger.info("Refreshing information about {} product", productCode);
        try {
            refreshService.updateData(productCode);
            return new ResponseEntity<>("Data updated successfully for product " + productCode, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            logger.error(ERROR_FIND_UPDATE_XML);
            return new ResponseEntity<>(ERROR_FIND_UPDATE_XML, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            logger.error(ERROR_SAVE_UPDATE_XML);
            return new ResponseEntity<>(ERROR_SAVE_UPDATE_XML, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
