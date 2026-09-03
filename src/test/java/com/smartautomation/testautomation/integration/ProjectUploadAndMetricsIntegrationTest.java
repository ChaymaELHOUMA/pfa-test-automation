package com.smartautomation.testautomation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.CoverageResult;
import com.smartautomation.testautomation.model.MutationResult;
import com.smartautomation.testautomation.service.ProjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectUploadAndMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    private String lastProjectId;

    @AfterEach
    public void tearDown() throws Exception {
        if (lastProjectId != null) {
            Path workspace = Paths.get(scriptsDir, "workspaces", lastProjectId);
            if (Files.exists(workspace)) {
                Files.walk(workspace)
                     .sorted(Comparator.reverseOrder())
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             throw new UncheckedIOException(e);
                         }
                     });
            }
        }
    }

    @Test
    public void testUploadProjectAndMetrics_NoSilentFallback() throws Exception {
        // 1. Create a fake zip
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("app.js"));
            zos.write("console.log('hello');".getBytes());
            zos.closeEntry();
        }
        
        MockMultipartFile file = new MockMultipartFile("file", "fake.zip", "application/zip", baos.toByteArray());

        // 2. Upload
        MvcResult uploadResult = mockMvc.perform(multipart("/upload-projet")
                .file(file)
                .param("pointEntree", "app.js")
                .param("nomProjet", "Fake Project"))
                .andExpect(status().isOk())
                .andReturn();

        ProjectService.ProjectMetadata meta = objectMapper.readValue(uploadResult.getResponse().getContentAsString(), ProjectService.ProjectMetadata.class);
        lastProjectId = meta.idProjet();
        assertNotNull(lastProjectId);

        // 3. Verify files are generated
        Path workspace = Paths.get(scriptsDir, "workspaces", lastProjectId);
        assertTrue(Files.exists(workspace.resolve("jest.config.json")), "jest.config.json should be generated");
        assertTrue(Files.exists(workspace.resolve("stryker.config.json")), "stryker.config.json should be generated");

        // 4. Test coverage fallback prevention
        // We haven't run actual tests, so coverage-summary.json won't exist.
        // It SHOULD throw an internal server error (500) rather than falling back to projet-reference.
        MvcResult covResult = mockMvc.perform(get("/api/coverage/" + lastProjectId))
                .andExpect(status().isInternalServerError())
                .andReturn();
                
        CoverageResult coverage = objectMapper.readValue(covResult.getResponse().getContentAsString(), CoverageResult.class);
        assertFalse(coverage.isSucces());
        assertTrue(coverage.getMessageErreur().contains(lastProjectId) || coverage.getMessageErreur().contains("non produit"));
        assertFalse(coverage.getMessageErreur().contains("projet-reference")); // Should NOT talk about projet-reference

        // 5. Test mutation fallback prevention
        MvcResult mutResult = mockMvc.perform(get("/api/mutation/" + lastProjectId))
                .andExpect(status().isInternalServerError())
                .andReturn();

        MutationResult mutation = objectMapper.readValue(mutResult.getResponse().getContentAsString(), MutationResult.class);
        assertFalse(mutation.isSucces());
        assertTrue(mutation.getMessageErreur().contains(lastProjectId) || mutation.getMessageErreur().contains("non produit"));
        assertFalse(mutation.getMessageErreur().contains("projet-reference"));
    }
}
