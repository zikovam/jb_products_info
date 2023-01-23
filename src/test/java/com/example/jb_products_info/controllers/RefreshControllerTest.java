package com.example.jb_products_info.controllers;

import com.example.jb_products_info.services.RefreshService;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RefreshController.class)
class RefreshControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    RefreshService refreshService;

    @Test
    void whenRefreshInformation_thenOkResponse() throws Exception{
        mockMvc.perform(get("/refresh"))
                .andExpect(status().isOk());

        verify(refreshService, VerificationModeFactory.times(1)).updateData();
        reset(refreshService);
    }

    @Test
    void whenRefreshInformationForProduct_thenOkResponse() throws Exception{
        mockMvc.perform(get("/refresh/CODE"))
                .andExpect(status().isOk());

        verify(refreshService, VerificationModeFactory.times(1)).updateData("CODE");
        reset(refreshService);
    }
}
