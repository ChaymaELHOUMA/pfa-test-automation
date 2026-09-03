package com.smartautomation.testautomation.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartautomation.testautomation.model.NodeExecutionResult;

@Service
public class NodeBridgeService {

    private static final Logger log = LoggerFactory.getLogger(NodeBridgeService.class);

    @Value("${node.bridge.timeout-seconds:30}")
    private long timeoutSeconds;

    @Value("${node.bridge.scripts-dir:node-runner}")
    private String scriptsDir;

    public NodeExecutionResult runScript(String scriptName, String... args) {
        return runScriptAtPath(scriptName, args);
    }

    public NodeExecutionResult runScriptAtPath(String scriptPath, String... args) {
        return runScriptAtPathWithTimeout(scriptPath, timeoutSeconds, args);
    }

    public NodeExecutionResult runScriptAtPathWithTimeout(String scriptPath, long customTimeoutSec, String... args) {
        List<String> command = new ArrayList<>();
        command.add("node");
        command.add(scriptPath);
        command.addAll(Arrays.asList(args));

        log.debug("Starting Node process: {}", command);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new java.io.File(scriptsDir));
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            process = pb.start();

            StringBuilder stdoutBuffer = new StringBuilder();
            StringBuilder stderrBuffer = new StringBuilder();

            Thread stdoutThread = lireFluxDeFaconAsynchrone(process.getInputStream(), stdoutBuffer, "stdout");
            Thread stderrThread = lireFluxDeFaconAsynchrone(process.getErrorStream(), stderrBuffer, "stderr");

            boolean finishedInTime = process.waitFor(customTimeoutSec, TimeUnit.SECONDS);

            if (!finishedInTime) {
                log.warn("Timeout exceeded ({}s) for script {}, forcibly terminating process", customTimeoutSec, scriptPath);
                process.destroyForcibly();
                stdoutThread.join(2000);
                stderrThread.join(2000);
                return NodeExecutionResult.echecTimeout(stdoutBuffer.toString(), stderrBuffer.toString());
            }

            stdoutThread.join(2000);
            stderrThread.join(2000);

            int exitCode = process.exitValue();
            String stdout = stdoutBuffer.toString();
            String stderr = stderrBuffer.toString();

            if (exitCode != 0) {
                log.warn("Script {} terminated with code {}: {}", scriptPath, exitCode, stderr);
                return NodeExecutionResult.echecExitCode(stdout, stderr, exitCode);
            }

            return NodeExecutionResult.ok(stdout, stderr, exitCode);

        } catch (IOException e) {
            log.error("Failed to start Node process for script {}", scriptPath, e);
            return NodeExecutionResult.echecTechnique("Process startup error: " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            log.error("Node process interrupted for script {}", scriptPath, e);
            return NodeExecutionResult.echecTechnique("Process interrupted: " + e.getMessage());
        }
    }

    public NodeExecutionResult runNpxCommand(long timeoutSec, String... args) {
        List<String> command = new ArrayList<>();
        command.add("npx");
        command.addAll(Arrays.asList(args));

        log.debug("Starting npx command: {}", command);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new java.io.File(scriptsDir));
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            process = pb.start();

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread outThread = lireFluxDeFaconAsynchrone(process.getInputStream(), stdout, "stdout");
            Thread errThread = lireFluxDeFaconAsynchrone(process.getErrorStream(), stderr, "stderr");

            boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("Timeout exceeded ({}s) for npx command {}, forcibly terminating process", timeoutSec, Arrays.toString(args));
                process.destroyForcibly();
                outThread.join(2000);
                errThread.join(2000);
                return NodeExecutionResult.echecTimeout(stdout.toString(), stderr.toString());
            }

            outThread.join(2000);
            errThread.join(2000);

            int exitCode = process.exitValue();
            String outStr = stdout.toString();
            String errStr = stderr.toString();

            if (exitCode != 0) {
                log.warn("npx command {} terminated with code {}: {}", Arrays.toString(args), exitCode, errStr);
                return NodeExecutionResult.echecExitCode(outStr, errStr, exitCode);
            }

            return NodeExecutionResult.ok(outStr, errStr, exitCode);

        } catch (IOException e) {
            log.error("Failed to start npx command {}", Arrays.toString(args), e);
            return NodeExecutionResult.echecTechnique("npx process startup error: " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            log.error("npx command interrupted {}", Arrays.toString(args), e);
            return NodeExecutionResult.echecTechnique("Process interrupted: " + e.getMessage());
        }
    }

    private Thread lireFluxDeFaconAsynchrone(InputStream stream, StringBuilder target, String streamName) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    target.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                log.debug("End of stream reading {} ({})", streamName, e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
