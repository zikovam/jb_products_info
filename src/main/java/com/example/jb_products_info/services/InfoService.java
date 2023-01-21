package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.ProductInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final BuildRepository buildRepository;
    private final ThreadPoolTaskExecutor executor;

    public InfoService(BuildRepository buildRepository,
                       @Qualifier("taskExecutor") Executor executor) {
        this.buildRepository = buildRepository;
        this.executor = (ThreadPoolTaskExecutor) executor;
    }

    public List<BuildInfoDTO> getBuildInfosByCode(String productCode) {
        List<Build> builds = buildRepository.findAllByProductCode(productCode);
        return builds.stream()
                .map(InfoService::convertToBuildInfoDTO)
                .toList();
    }

    public BuildInfoDTO getBuildInfo(String productCode,
                                     String buildNumber) {
        Build build = buildRepository.findFirstByProductCodeAndBuildNumberStartsWith(productCode, buildNumber);
        return build == null ? null : convertToBuildInfoDTO(build);
    }

    @Transactional
    public SystemStatusDTO getStatus() {
        List<Build> builds = buildRepository.findAll();

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
                .downloadedBuilds((int) builds.stream()
                        .filter(build -> build.getProductInfoJsonData() != null)
                        .count())
                .buildsDownloading(executor.getActiveCount())
                .buildsDownloadQueue(executor.getQueueSize())
                .buildsWithoutLinuxVersion(buildsWithoutLinuxVersion)
                .lastUpdated(builds.stream()
                        .map(Build::getUpdatedDateTime)
                        .max(Comparator.naturalOrder())
                        .orElse(LocalDateTime.MIN))
                .build();
        logger.info("Current status of the system: {}", currentStatus);
        return currentStatus;
    }


    private static BuildInfoDTO convertToBuildInfoDTO(Build build) {
        String buildNumber = build.getBuildNumber();
        if (build.getProductInfoJsonData() != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                ProductInfoDTO productInfoDTO = mapper.readValue(build.getProductInfoJsonData(), ProductInfoDTO.class);
                return new BuildInfoDTO(buildNumber, productInfoDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BuildInfoDTO(buildNumber, null);
    }
}
