package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.example.jb_products_info.services.tasks.BuildDownloadTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuildDownloadServiceTest {
    public static final String TEST_FILE_NAME = "testFile";
    public static final String BUILD_INFO_JSON = """
                {
                  "name": "DataGrip",
                  "version": "2022.3.3",
                  "buildNumber": "223.8617.3",
                  "productCode": "DB",
                  "dataDirectoryName": "DataGrip2022.3",
                  "svgIconPath": "bin/datagrip.svg",
                  "productVendor": "JetBrains"
                }
                """;
    public static final String BUILD_VERSION = "223.8617.3";
    @Mock
    BuildRepository buildRepository;
    @Mock
    BuildDownloadTask buildDownloadTask;
    @InjectMocks
    BuildDownloadService buildDownloadService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(buildDownloadService, "filenameToSearch", TEST_FILE_NAME);
    }

    @Test
    void testDownloadMultipleBuildsForProductAndExtractFile_() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> future.complete(BUILD_VERSION + BUILD_INFO_JSON)).start();
        Build build = Build.builder()
                .downloadUrl("testDownloadUrl")
                .buildNumber(BUILD_VERSION)
                .build();

        when(buildDownloadTask
                .downloadBuildAndExtractFileAsync(build.getDownloadUrl(), TEST_FILE_NAME, build.getBuildNumber()))
                .thenReturn(future);

        buildDownloadService.downloadMultipleBuildsForProductAndExtractFile(List.of(build));

        Build buildToSave = Build.builder()
                .downloadUrl("testDownloadUrl")
                .buildNumber(BUILD_VERSION)
                .productInfoJsonData(BUILD_INFO_JSON)
                .build();

        verify(buildDownloadTask, times(1))
                .downloadBuildAndExtractFileAsync(build.getDownloadUrl(), TEST_FILE_NAME, build.getBuildNumber());
        verify(buildRepository, times(1)).saveAndFlush(buildToSave);
    }
}
