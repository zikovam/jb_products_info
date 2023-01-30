package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
import com.example.jb_products_info.entities.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class InfoService {
    final Logger logger = LoggerFactory.getLogger(InfoService.class);

    @Value("${codes.without.linux}")
    private List<String> codesWithoutLinuxBuilds;
    private final ThreadPoolTaskExecutor executor;
    private final BuildService buildService;

    public InfoService(@Qualifier("taskExecutor") Executor executor,
                       BuildService buildService) {
        this.executor = (ThreadPoolTaskExecutor) executor;
        this.buildService = buildService;
    }

    public List<BuildInfoDTO> getBuildInfosByCode(String productCode) {
        List<Build> builds = buildService.findAllByProductCode(productCode);
        return builds.stream()
                .map(buildService::convertToBuildInfoDTO)
                .toList();
    }

    public BuildInfoDTO getBuildInfo(String productCode,
                                     String buildNumber) {
        Build build = buildService.findFirstByProductCodeAndBuildNumberStartsWith(productCode, buildNumber);
        return build == null ? null : buildService.convertToBuildInfoDTO(build);
    }

    public SystemStatusDTO getStatus() {
        List<Build> builds = buildService.findAll();

        Set<String> productCodes = builds.stream()
                .map(build -> build.getProduct().getCode())
                .collect(Collectors.toSet());

        Integer buildsWithoutLinuxVersion = (int) builds.stream()
                .map(build -> build.getProduct().getCode())
                .filter(code -> codesWithoutLinuxBuilds.contains(code))
                .count();

        SystemStatusDTO currentStatus = SystemStatusDTO.builder()
                .countProducts(productCodes.size())
                .countBuilds(builds.size())
                .serverTimezone(ZoneId.systemDefault())
                .productCodes(productCodes)
                .buildsDownloaded((int) builds.stream()
                        .filter(build -> build.getProductInfoJsonData() != null)
                        .count())
                .buildsDownloading(executor.getActiveCount())
                .buildsDownloadQueue(executor.getQueueSize())
                .buildsWithoutLinuxVersion(buildsWithoutLinuxVersion)
                .databaseLastUpdated(builds.stream()
                        .map(Build::getUpdatedDateTime)
                        .max(Comparator.naturalOrder())
                        .orElse(LocalDateTime.MIN))
                .build();
        logger.info("Current status of the system: {}", currentStatus);
        return currentStatus;
    }
}
