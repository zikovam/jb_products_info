package com.example.jb_products_info.services;

import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {
    @Mock
    BuildDownloadService buildDownloadService;
    @Mock
    FileParserService fileParserService;
    @Mock
    ProductService productService;
    @Mock
    ThreadPoolTaskExecutor executor;
    @Mock
    DataSource dataSource;

    @InjectMocks
    RefreshService refreshService;

    private Build build2;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        Build build1 = Build.builder()
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
        Build build3 = Build.builder()
                .buildNumber("testBuildNumber3")
                .version("testVersion3")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl3")
                .build();

        product1 = Product.builder()
                .code("CODE1")
                .name("Test name 1")
                .builds(new ArrayList<>(Arrays.asList(build1, build2)))
                .build();
        product2 = Product.builder()
                .code("CODE2")
                .name("Test name 2")
                .builds(new ArrayList<>(Collections.singletonList(build3)))
                .build();

        ReflectionTestUtils.setField(refreshService, "jetbrainsUpdateFileUrl", "testUrl");
    }

    @Test
    void testCollectDataAfterStartup_allOk() throws Exception {
        List<Product> list = new ArrayList<>(Arrays.asList(product1, product2));
        when(fileParserService.collectProducts()).thenReturn(list);
        when(productService.findAll()).thenReturn(list);

        refreshService.collectDataAfterStartup();

        verify(dataSource, times(1)).downloadFileByUrl(anyString(), anyString());
        verify(fileParserService, times(1)).collectProducts();
        verify(productService, times(1)).upsertAll(list);
        verify(productService, times(1)).findAll();
        verify(buildDownloadService, times(2)).downloadMultipleBuildsForProductAndExtractFile(any());
    }

    @Test
    void testUpdateData_allOk() throws Exception {
        List<Product> list = new ArrayList<>(Arrays.asList(product1, product2));
        when(fileParserService.collectProducts()).thenReturn(list);
        List<Build> builds = Collections.singletonList(build2);
        when(productService.upsertAll(list)).thenReturn(builds);
        when(executor.getQueueSize()).thenReturn(100);

        refreshService.updateData();

        verify(dataSource, times(1)).downloadFileByUrl(anyString(), anyString());
        verify(fileParserService, times(1)).collectProducts();
        verify(productService, times(1)).upsertAll(list);
        verify(buildDownloadService, times(1)).downloadMultipleBuildsForProductAndExtractFile(builds);
    }

    @Test
    void testUpdateDataByCode_allOk() throws Exception {
        when(fileParserService.collectProduct("CODE1")).thenReturn(product1);
        List<Build> builds = Collections.singletonList(build2);
        when(productService.upsert(product1)).thenReturn(builds);

        refreshService.updateData("CODE1");

        verify(dataSource, times(1)).downloadFileByUrl(anyString(), anyString());
        verify(fileParserService, times(1)).collectProduct("CODE1");
        verify(productService, times(1)).upsert(product1);
        verify(buildDownloadService, times(1)).downloadMultipleBuildsForProductAndExtractFile(builds);
    }

    @Test
    void testUpdateDataByCode_notFound() throws Exception {
        when(fileParserService.collectProduct("CODE2")).thenReturn(null);

        refreshService.updateData("CODE2");

        verify(dataSource, times(1)).downloadFileByUrl(anyString(), anyString());
        verify(fileParserService, times(1)).collectProduct("CODE2");
    }

    @Test
    void testContinueDownloading_allOk() throws Exception {
        List<Product> list = new ArrayList<>(Arrays.asList(product1, product2));
        when(productService.findAll()).thenReturn(list);

        refreshService.continueDownloading();

        verify(productService, times(1)).findAll();
        verify(buildDownloadService, times(2)).downloadMultipleBuildsForProductAndExtractFile(any());
    }

    @Test
    void testContinueDownloading_downloadError() throws Exception {
        List<Product> list = new ArrayList<>(Arrays.asList(product1, product2));
        when(productService.findAll()).thenReturn(list);
        doThrow(new IOException()).when(buildDownloadService).downloadMultipleBuildsForProductAndExtractFile(any());

        assertThrows(
                ResponseStatusException.class,
                () -> refreshService.continueDownloading(),
                "Expected continueDownloading() to throw ResponseStatusException"
        );

        verify(productService, times(1)).findAll();
        verify(buildDownloadService, times(1)).downloadMultipleBuildsForProductAndExtractFile(any());
    }
}
