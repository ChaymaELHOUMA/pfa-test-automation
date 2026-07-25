package com.smartautomation.testautomation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartautomation.testautomation.model.NodeExecutionResult;
import com.smartautomation.testautomation.service.NodeBridgeService;

@RestController
public class BridgeTestController {

    private final NodeBridgeService nodeBridgeService;

    public BridgeTestController(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

   
    @GetMapping("/test-bridge")
    public ResponseEntity<NodeExecutionResult> testBridge() {
        NodeExecutionResult result = nodeBridgeService.runScript("hello.js");
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok(result);
    }

    
    @GetMapping("/test-bridge/erreur")
    public ResponseEntity<NodeExecutionResult> testBridgeErreur(@RequestParam String scenario) {
        String script = switch (scenario) {
            case "inexistant" -> "ne-existe-pas.js";
            case "erreur" -> "erreur.js";
            case "timeout" -> "boucle-infinie.js";
            default -> "hello.js";
        };

        NodeExecutionResult result = nodeBridgeService.runScript(script);
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(result);
    }
}

