package com.company.birthday.controller;

import com.company.birthday.dto.request.BirthdayConfigRequest;
import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.service.BirthdayConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BirthdayConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class BirthdayConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BirthdayConfigService birthdayConfigService;

    @Test
    void getConfigReturnsJson() throws Exception {
        when(birthdayConfigService.getBirthdayConfig()).thenReturn(new BirthdayConfigResponse(true, 8, 15));

        mockMvc.perform(get("/config/birthday"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.hour").value(8))
                .andExpect(jsonPath("$.minute").value(15));
    }

    @Test
    void postConfigPersistsAndReturnsJson() throws Exception {
        BirthdayConfigResponse response = new BirthdayConfigResponse(false, 10, 45);
        when(birthdayConfigService.saveBirthdayConfig(any(BirthdayConfigRequest.class))).thenReturn(response);

        BirthdayConfigRequest request = new BirthdayConfigRequest();
        request.setEnabled(false);
        request.setHour(10);
        request.setMinute(45);

        mockMvc.perform(post("/config/birthday")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.hour").value(10))
                .andExpect(jsonPath("$.minute").value(45));

        verify(birthdayConfigService).saveBirthdayConfig(any(BirthdayConfigRequest.class));
    }
}


