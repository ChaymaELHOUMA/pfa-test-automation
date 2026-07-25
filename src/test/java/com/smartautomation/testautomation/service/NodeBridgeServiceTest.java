package com.smartautomation.testautomation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.smartautomation.testautomation.model.NodeExecutionResult;


@SpringBootTest
@TestPropertySource(properties = {
        "node.bridge.timeout-seconds=3"
})
class NodeBridgeServiceTest {

    @Autowired
    private NodeBridgeService nodeBridgeService;

    @Test
    void cas_nominal_doit_retourner_success_true() {
        NodeExecutionResult result = nodeBridgeService.runScript("hello.js");

        assertTrue(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertFalse(result.isTimedOut());
        assertTrue(result.getStdout().contains("Node repond bien a Java"));
    }

    @Test
    void script_en_erreur_doit_retourner_success_false_avec_stderr() {
        NodeExecutionResult result = nodeBridgeService.runScript("erreur.js");

        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertFalse(result.isTimedOut());
        assertTrue(result.getStderr().contains("Erreur simulee"));
    }

    @Test
    void script_qui_boucle_doit_declencher_le_timeout() {
        NodeExecutionResult result = nodeBridgeService.runScript("boucle-infinie.js");

        assertFalse(result.isSuccess());
        assertTrue(result.isTimedOut());
        assertEquals(-1, result.getExitCode());
    }

    @Test
    void script_inexistant_doit_retourner_une_erreur_technique_sans_exception() {
        NodeExecutionResult result = nodeBridgeService.runScript("ce-fichier-n-existe-pas.js");

        assertFalse(result.isSuccess());
        assertFalse(result.isTimedOut());
        assertNotNull(result.getStderr());
    }
}
