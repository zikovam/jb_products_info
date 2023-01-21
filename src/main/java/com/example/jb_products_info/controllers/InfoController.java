package com.example.jb_products_info.controllers;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
import com.example.jb_products_info.exceptions.BuildNotFoundException;
import com.example.jb_products_info.services.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/")
public class InfoController {
    Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final InfoService infoService;

    @Autowired
    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping
    public void statusForHuman() {
        //TODO: implement
    }

    @GetMapping("status")
    public SystemStatusDTO status() {
        logger.info("Providing general status of the system");
        return infoService.getStatus();
    }

    @GetMapping("{productCode}")
    public List<BuildInfoDTO> buildInfosForCode(@PathVariable String productCode) {
        logger.info("Providing info for product code: {}", productCode);
        List<BuildInfoDTO> buildInfoList = infoService.getBuildInfosByCode(productCode);
        if (buildInfoList.isEmpty()) {
            throw new BuildNotFoundException();
        }
        return buildInfoList;
    }

    @GetMapping("{productCode}/{buildNumber}")
    public BuildInfoDTO buildInfo(@PathVariable String productCode,
                                  @PathVariable String buildNumber) {
        logger.info("Providing info for product code: {} and build: {}", productCode, buildNumber);
        BuildInfoDTO buildInfo = infoService.getBuildInfo(productCode, buildNumber);
        if (buildInfo == null) {
            throw new BuildNotFoundException();
        }
        return buildInfo;
    }
}
