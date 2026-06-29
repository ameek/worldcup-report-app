package com.fifa.reporting.worldcup_report_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorldCupE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getAllWorldCupsReturnsSeededData() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].year", containsInAnyOrder(2022, 2018, 2014)));
    }

    @Test
    void getWorldCupByYearReturns2022Champion() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups/year/2022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2022))
                .andExpect(jsonPath("$.champion").value("Argentina"))
                .andExpect(jsonPath("$.hostCountry").value("Qatar"));
    }

    @Test
    void getWorldCupsByChampionReturnsArgentinaTitles() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups/champion/Argentina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year").value(2022));
    }

    @Test
    void getWorldCupsByHostCountryReturnsBrazil2014() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups/host/Brazil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year").value(2014));
    }

    @Test
    void getWorldCupByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void generateTeamReportReturnsPdf() throws Exception {
        mockMvc.perform(get("/api/v1/reports/team/Argentina")
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(result -> {
                    byte[] body = result.getResponse().getContentAsByteArray();
                    if (body.length < 100) {
                        throw new AssertionError("Expected non-empty PDF, got " + body.length + " bytes");
                    }
                });
    }

    @Test
    void generateTeamReportForUnknownTeamReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/reports/team/UnknownTeam"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateTravelApprovalReportReturnsPdf() throws Exception {
        assertTravelApprovalPdf("/api/v1/travel-approval/report");
    }

    @Test
    void generateTravelApprovalReportV2ReturnsPdf() throws Exception {
        assertTravelApprovalPdf("/api/v1/travel-approval/report/v2");
    }

    private void assertTravelApprovalPdf(String url) throws Exception {
        mockMvc.perform(get(url).accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(result -> {
                    byte[] body = result.getResponse().getContentAsByteArray();
                    if (body.length < 100) {
                        throw new AssertionError("Expected non-empty PDF, got " + body.length + " bytes");
                    }
                });
    }
}
