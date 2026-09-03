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
    void nominal_case_should_return_success_true() {
        NodeExecutionResult result = nodeBridgeService.runScript("hello.js");

        assertTrue(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertFalse(result.isTimedOut());
        assertTrue(result.getStdout().contains("Node repond bien a Java"));
    }

    @Test
    void error_script_should_return_success_false_with_stderr() {
        NodeExecutionResult result = nodeBridgeService.runScript("erreur.js");

        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertFalse(result.isTimedOut());
        assertTrue(result.getStderr().contains("Erreur simulee"));
    }

    @Test
    void infinite_loop_script_should_trigger_timeout() {
        NodeExecutionResult result = nodeBridgeService.runScript("boucle-infinie.js");

        assertFalse(result.isSuccess());
        assertTrue(result.isTimedOut());
        assertEquals(-1, result.getExitCode());
    }

    @Test
    void non_existent_script_should_return_technical_error_without_exception() {
        NodeExecutionResult result = nodeBridgeService.runScript("ce-fichier-n-existe-pas.js");

        assertFalse(result.isSuccess());
        assertFalse(result.isTimedOut());
        assertNotNull(result.getStderr());
    }
}
