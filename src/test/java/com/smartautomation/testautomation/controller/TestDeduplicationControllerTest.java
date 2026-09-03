package com.smartautomation.testautomation.controller;

import com.smartautomation.testautomation.service.LlmService;
import com.smartautomation.testautomation.service.NodeBridgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class TestDeduplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmService llmService;

    @MockBean
    private NodeBridgeService nodeBridgeService;

    @Test
    public void testValiderNonRedondance_AvecDoublons() throws Exception {
        String codeTest = """
                test('cas nominal - succes', () => { expect(1).toBe(1); });
                test('cas nominal - succes', () => { expect(1).toBe(1); });
                """;

        String jsonBody = """
                {
                    "codeTest": %s
                }
                """.formatted(objectToJsonString(codeTest));

        mockMvc.perform(post("/valider-non-redondance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTests").value(2))
                .andExpect(jsonPath("$.testsUniques").value(1))
                .andExpect(jsonPath("$.doublonsDetectes").value(1))
                .andExpect(jsonPath("$.estNonRedondant").value(false))
                .andExpect(jsonPath("$.titresDoublons[0]").value("cas nominal - succes"));
    }

    @Test
    public void testExtraireTitresTests() throws Exception {
        String codeTest = """
                test('cas nominal - premier', () => { expect(1).toBe(1); });
                it('cas d erreur - second', () => { expect(0).toBe(1); });
                """;

        String jsonBody = """
                {
                    "codeTest": %s
                }
                """.formatted(objectToJsonString(codeTest));

        mockMvc.perform(post("/extraire-titres-tests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titres[0]").value("cas nominal - premier"))
                .andExpect(jsonPath("$.titres[1]").value("cas d erreur - second"));
    }

    private String objectToJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }
}
