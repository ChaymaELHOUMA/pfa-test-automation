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

        log.debug("Lancement du process Node : {}", command);

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

            boolean termineATemps = process.waitFor(customTimeoutSec, TimeUnit.SECONDS);

            if (!termineATemps) {
                log.warn("Timeout depasse ({}s) pour le script {}, destruction forcee du process", customTimeoutSec, scriptPath);
                process.destroyForcibly();
                stdoutThread.interrupt();
                stderrThread.interrupt();
                return NodeExecutionResult.echecTimeout();
            }

            stdoutThread.join(2000);
            stderrThread.join(2000);

            int exitCode = process.exitValue();
            String stdout = stdoutBuffer.toString();
            String stderr = stderrBuffer.toString();

            if (exitCode != 0) {
                log.warn("Le script {} s'est termine avec le code {} : {}", scriptPath, exitCode, stderr);
                return NodeExecutionResult.echecExitCode(stdout, stderr, exitCode);
            }

            return NodeExecutionResult.ok(stdout, stderr, exitCode);

        } catch (IOException e) {
            log.error("Impossible de lancer le process Node pour le script {}", scriptPath, e);
            return NodeExecutionResult.echecTechnique("Erreur de lancement du process : " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            log.error("Process Node interrompu pour le script {}", scriptPath, e);
            return NodeExecutionResult.echecTechnique("Process interrompu : " + e.getMessage());
        }
    }

    public NodeExecutionResult runNpxCommand(long timeoutSec, String... args) {
        List<String> command = new ArrayList<>();
        command.add("npx");
        command.addAll(Arrays.asList(args));

        log.debug("Lancement de la commande npx : {}", command);

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
                log.warn("Timeout depasse ({}s) pour la commande npx {}, destruction forcee du process", timeoutSec, Arrays.toString(args));
                process.destroyForcibly();
                outThread.interrupt();
                errThread.interrupt();
                return NodeExecutionResult.echecTimeout();
            }

            outThread.join(2000);
            errThread.join(2000);

            int exitCode = process.exitValue();
            String outStr = stdout.toString();
            String errStr = stderr.toString();

            if (exitCode != 0) {
                log.warn("La commande npx {} s'est terminee avec le code {} : {}", Arrays.toString(args), exitCode, errStr);
                return NodeExecutionResult.echecExitCode(outStr, errStr, exitCode);
            }

            return NodeExecutionResult.ok(outStr, errStr, exitCode);

        } catch (IOException e) {
            log.error("Impossible de lancer la commande npx {}", Arrays.toString(args), e);
            return NodeExecutionResult.echecTechnique("Erreur de lancement du process npx : " + e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            log.error("Commande npx interrompue {}", Arrays.toString(args), e);
            return NodeExecutionResult.echecTechnique("Process interrompu : " + e.getMessage());
        }
    }

    private Thread lireFluxDeFaconAsynchrone(InputStream stream, StringBuilder cible, String nomFlux) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    cible.append(ligne).append(System.lineSeparator());
                }
            } catch (IOException e) {
                log.debug("Fin de lecture du flux {} ({})", nomFlux, e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
