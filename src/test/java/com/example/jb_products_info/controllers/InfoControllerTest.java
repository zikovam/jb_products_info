package com.example.jb_products_info.controllers;

import com.example.jb_products_info.dto.BuildInfoDTO;
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
                .downloadedBuilds(100)
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
    void whenGetBuildInfosByCorrectCode_thenListProvided() throws Exception {
        BuildInfoDTO build = new BuildInfoDTO(); //TODO: finalize entity
        List<BuildInfoDTO> buildInfos = List.of(build);
        given(infoService.getBuildInfosByCode(anyString())).willReturn(buildInfos);

        mockMvc.perform(get("/CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        //TODO: check finalized entity

        verify(infoService, VerificationModeFactory.times(1)).getBuildInfosByCode("CODE");
        reset(infoService);
    }
}
