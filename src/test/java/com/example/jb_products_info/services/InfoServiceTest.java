package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfoServiceTest {

    @Mock
    ThreadPoolTaskExecutor executor;
    @Mock
    BuildService buildService;
    @InjectMocks
    InfoService infoService;

    private Build build1;
    private Build build2;
    private Product product1;

    @BeforeEach
    void setUp() {
        build1 = Build.builder()
                .buildNumber("testBuildNumber1")
                .version("testVersion1")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .updatedDateTime(LocalDateTime.of(2023, 1, 18, 10, 20, 30))
                .downloadUrl("testUrl1")
                .build();
        build2 = Build.builder()
                .buildNumber("testBuildNumber2")
                .version("testVersion2")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .updatedDateTime(LocalDateTime.of(2023, 1, 17, 10, 20, 30))
                .downloadUrl("testUrl2")
                .build();

        product1 = Product.builder()
                .code("CODE1")
                .name("Test name 1")
                .builds(new ArrayList<>(Arrays.asList(build1, build2)))
                .build();

        ReflectionTestUtils.setField(infoService, "codesWithoutLinuxBuilds", List.of("CODE1"));
    }

    @Test
    void testGetBuildInfosByCode_allOk(){
        when(buildService.findAllByProductCode("CODE1"))
                .thenReturn(new ArrayList<>(Arrays.asList(build1, build2)));
        when(buildService.convertToBuildInfoDTO(build1)).thenReturn(new BuildInfoDTO());
        when(buildService.convertToBuildInfoDTO(build2)).thenReturn(new BuildInfoDTO());

        List<BuildInfoDTO> buildInfos = infoService.getBuildInfosByCode("CODE1");

        assertEquals(2, buildInfos.size());
        verify(buildService, times(1)).findAllByProductCode("CODE1");
        verify(buildService, times(2)).convertToBuildInfoDTO(any());
    }

    @Test
    void testGetBuildInfosByCode_wrongCode(){
        when(buildService.findAllByProductCode("CODE1"))
                .thenReturn(new ArrayList<>());

        List<BuildInfoDTO> buildInfos = infoService.getBuildInfosByCode("CODE1");

        assertEquals(0, buildInfos.size());
        verify(buildService, times(1)).findAllByProductCode("CODE1");
    }

    @Test
    void testGetBuildInfo_allOk(){
        when(buildService.findFirstByProductCodeAndBuildNumberStartsWith("CODE1", "testBuild"))
                .thenReturn(build1);
        when(buildService.convertToBuildInfoDTO(build1)).thenReturn(new BuildInfoDTO());

        BuildInfoDTO buildInfo = infoService.getBuildInfo("CODE1", "testBuild");

        assertNotNull(buildInfo);
        verify(buildService, times(1))
                .findFirstByProductCodeAndBuildNumberStartsWith("CODE1", "testBuild");
        verify(buildService, times(1)).convertToBuildInfoDTO(any());
    }

    @Test
    void testGetBuildInfo_wrongCode(){
        when(buildService.findFirstByProductCodeAndBuildNumberStartsWith("CODE1", "testBuild"))
                .thenReturn(null);

        BuildInfoDTO buildInfo = infoService.getBuildInfo("CODE1", "testBuild");

        assertNull(buildInfo);
        verify(buildService, times(1))
                .findFirstByProductCodeAndBuildNumberStartsWith("CODE1", "testBuild");
    }

    @Test
    void testGetStatus_allOk(){
        build1.setProduct(product1);
        build2.setProduct(product1);

        when(buildService.findAll()).thenReturn(new ArrayList<>(Arrays.asList(build1, build2)));
        when(executor.getActiveCount()).thenReturn(5);
        when(executor.getQueueSize()).thenReturn(100);


        SystemStatusDTO status = infoService.getStatus();

        assertEquals(1, status.getCountProducts());
        assertEquals(2, status.getCountBuilds());
        assertEquals(ZoneId.systemDefault(), status.getServerTimezone());
        assertEquals(Set.of("CODE1"), status.getProductCodes());
        assertEquals(0, status.getBuildsDownloaded());
        assertEquals(5, status.getBuildsDownloading());
        assertEquals(100, status.getBuildsDownloadQueue());
        assertEquals(2, status.getBuildsWithoutLinuxVersion());
        assertEquals(build1.getUpdatedDateTime(), status.getDatabaseLastUpdated());


        verify(buildService, times(1)).findAll();
        verify(executor, times(1)).getActiveCount();
        verify(executor, times(1)).getQueueSize();
    }
}
