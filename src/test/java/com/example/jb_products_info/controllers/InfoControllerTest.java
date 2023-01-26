package com.example.jb_products_info.controllers;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.ProductInfoDTO;
import com.example.jb_products_info.dto.SystemStatusDTO;
import com.example.jb_products_info.services.InfoService;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InfoController.class)
class InfoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    InfoService infoService;

    @Test
    void whenGetStatus_thenStatusProvided() throws Exception {
        LocalDateTime now = LocalDateTime.of(2023, 1, 15, 10, 20, 30);
        Set<String> productCodes = Set.of("CODE", "FOR", "TEST");

        SystemStatusDTO status = SystemStatusDTO.builder()
                .lastUpdated(now)
                .serverTimezone(ZoneId.systemDefault())
                .countProducts(productCodes.size())
                .productCodes(productCodes)
                .countBuilds(700)
                .buildsDownloaded(100)
                .buildsDownloading(5)
                .buildsDownloadQueue(545)
                .buildsWithoutLinuxVersion(50)
                .build();
        given(infoService.getStatus()).willReturn(status);

        mockMvc.perform(get("/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated", is(now.toString())))
                .andExpect(jsonPath("$.serverTimezone", is(ZoneId.systemDefault().toString())))
                .andExpect(jsonPath("$.countProducts", is(productCodes.size())))
                .andExpect(jsonPath("$.productCodes", hasSize(3)))
                .andExpect(jsonPath("$.countBuilds", is(700)))
                .andExpect(jsonPath("$.downloadedBuilds", is(100)))
                .andExpect(jsonPath("$.buildsDownloading", is(5)))
                .andExpect(jsonPath("$.buildsDownloadQueue", is(545)))
                .andExpect(jsonPath("$.buildsWithoutLinuxVersion", is(50)));

        verify(infoService, VerificationModeFactory.times(1)).getStatus();
        reset(infoService);
    }

    @Test
    void whenGetBuildInfosByWrongCode_thenNotFound() throws Exception {
        given(infoService.getBuildInfosByCode(anyString())).willReturn(List.of());

        mockMvc.perform(get("/CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfosByCode("CODE");
        reset(infoService);
    }

    @Test
    void whenGetBuildInfoByWrongCodeAndBuild_thenNotFound() throws Exception {
        given(infoService.getBuildInfo(anyString(), anyString())).willReturn(null);

        mockMvc.perform(get("/CODE/testBuild")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfo("CODE", "testBuild");
        reset(infoService);
    }

    @Test
    void whenGetBuildInfosByCorrectCode_thenBuildWithNullInfo() throws Exception {
        BuildInfoDTO build = new BuildInfoDTO();
        String buildNumber = "test-build-number";
        build.setBuildNumber(buildNumber);

        List<BuildInfoDTO> buildInfos = List.of(build);
        given(infoService.getBuildInfosByCode(anyString())).willReturn(buildInfos);

        mockMvc.perform(get("/CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].build-number", is(buildNumber)))
                .andExpect(jsonPath("$[0].product-info", nullValue()));

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfosByCode("CODE");
        reset(infoService);
    }

    @Test
    void whenGetBuildInfosByCorrectCode_thenListProvided() throws Exception {
        ProductInfoDTO productInfo = new ProductInfoDTO();
        String productName = "Test Product";
        productInfo.setName(productName);
        String productCode = "CODE";
        productInfo.setProductCode(productCode);

        BuildInfoDTO build = new BuildInfoDTO();
        String buildNumber = "test-build-number";
        build.setBuildNumber(buildNumber);
        build.setProductInfo(productInfo);

        List<BuildInfoDTO> buildInfos = List.of(build);
        given(infoService.getBuildInfosByCode(anyString())).willReturn(buildInfos);

        mockMvc.perform(get("/CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].build-number", is(buildNumber)))
                .andExpect(jsonPath("$[0].product-info.name", is(productName)))
                .andExpect(jsonPath("$[0].product-info.productCode", is(productCode)));

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfosByCode(productCode);
        reset(infoService);
    }

    @Test
    void whenGetBuildInfoByCorrectCode_thenBuildWithNullInfo() throws Exception {
        BuildInfoDTO build = new BuildInfoDTO();
        String buildNumber = "test-build-number";
        build.setBuildNumber(buildNumber);

        given(infoService.getBuildInfo(anyString(), anyString())).willReturn(build);

        mockMvc.perform(get("/CODE/test-build-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.build-number", is(buildNumber)))
                .andExpect(jsonPath("$.product-info", nullValue()));

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfo("CODE", "test-build-number");
        reset(infoService);
    }

    @Test
    void whenGetBuildInfoByCorrectCode_thenListProvided() throws Exception {
        ProductInfoDTO productInfo = new ProductInfoDTO();
        String productName = "Test Product";
        productInfo.setName(productName);
        String productCode = "CODE";
        productInfo.setProductCode(productCode);

        BuildInfoDTO build = new BuildInfoDTO();
        String buildNumber = "test-build-number";
        build.setBuildNumber(buildNumber);
        build.setProductInfo(productInfo);

        given(infoService.getBuildInfo(anyString(), anyString())).willReturn(build);

        mockMvc.perform(get("/CODE/test-build-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.build-number", is(buildNumber)))
                .andExpect(jsonPath("$.product-info.name", is(productName)))
                .andExpect(jsonPath("$.product-info.productCode", is(productCode)));

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfo(productCode, buildNumber);
        reset(infoService);
    }
}
