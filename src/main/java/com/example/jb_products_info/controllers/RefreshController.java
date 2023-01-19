package com.example.jb_products_info.controllers;

import com.example.jb_products_info.services.RefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/refresh")
public class RefreshController {
    final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    private final RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @GetMapping
    public void refreshInformation() {
        logger.info("Refreshing all information");
        refreshService.updateData();
    }

    @GetMapping("/{productCode}")
    public void refreshForProduct(@PathVariable String productCode) {
        //TODO: implement
    }
}
