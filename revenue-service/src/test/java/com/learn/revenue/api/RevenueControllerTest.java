package com.learn.revenue.api;

import com.learn.common.auth.JwtService;
import com.learn.common.error.GlobalExceptionHandler;
import com.learn.revenue.service.RevenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevenueController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueService revenueService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void rejectsARevenueBodyWithNoAmount() throws Exception {
        mockMvc.perform(post("/api/revenues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "source": "web",
                                  "category": "ads",
                                  "currency": "USD",
                                  "recordedOn": "2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value(400))
                .andExpect(jsonPath("$.responseMessage").value("Validation failed"))
                .andExpect(jsonPath("$.responseBody.amount").exists());
    }
}
