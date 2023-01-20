package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.ProductInfoDTO;
import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class InfoService {
    final Logger logger = LoggerFactory.getLogger(InfoService.class);

    private final BuildRepository buildRepository;
    private final ThreadPoolTaskExecutor executor;

    public InfoService(BuildRepository buildRepository,
                       @Qualifier("taskExecutor") Executor executor) {
        this.buildRepository = buildRepository;
        this.executor = (ThreadPoolTaskExecutor)executor;
    }

    public List<BuildInfoDTO> getBuildInfoList(String productCode) {
        List<Build> builds = buildRepository.findAllByProductCode(productCode);
        return builds.stream()
                .map(InfoService::convertToBuildInfoDTO)
                .collect(Collectors.toList());
    }

    public BuildInfoDTO getBuildInfoBy(String productCode,
                                       String buildNumber) {
        Build build = buildRepository.findFirstByProductCodeAndBuildNumberStartsWith(productCode, buildNumber);
        return build == null ? null : convertToBuildInfoDTO(build);
    }

    public void getStatus(){
        logger.info("currently executing {} threads of {}. In queue: {}",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getQueueSize());
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
