package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.example.jb_products_info.services.tasks.BuildDownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BuildDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(BuildDownloadService.class);

    @Value("${filename.to.get}")
    private String filenameToSearch;
    private final BuildRepository buildRepository;
    private final BuildDownloadTask buildDownloadTask;

    public BuildDownloadService(BuildRepository buildRepository,
                                BuildDownloadTask buildDownloadTask) {
        this.buildRepository = buildRepository;
        this.buildDownloadTask = buildDownloadTask;
    }

    public void downloadMultipleBuildsForProductAndExtractFile(List<Build> builds) throws IOException {
        Map<String, Build> buildToBuildNumberMap = builds.stream()
                .collect(Collectors.toMap(Build::getBuildNumber, Function.identity()));

        List<CompletableFuture<String>> buildInfos = new ArrayList<>();
        for (Build build : builds) {
            if (build.getDownloadUrl() != null && build.getProductInfoJsonData() == null) {
                buildInfos.add(
                        buildDownloadTask.downloadBuildAndExtractFileAsync(
                                build.getDownloadUrl(), filenameToSearch, build.getBuildNumber()));
            }
        }

        buildInfos.forEach(future -> future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error(throwable.getMessage());
                //TODO: think about reapplying failed tasks
            } else {
                //Not the most elegant solution
                //We know that json file starts with '{'
                //We also added buildNumber before this symbol
                String buildNumber = result.substring(0, result.indexOf("{"));
                logger.info("Information from build {} collected", buildNumber);

                buildToBuildNumberMap.get(buildNumber).setProductInfoJsonData(result.substring(result.indexOf("{")));
                buildRepository.saveAndFlush(buildToBuildNumberMap.get(buildNumber));
            }
        }));
    }
}
