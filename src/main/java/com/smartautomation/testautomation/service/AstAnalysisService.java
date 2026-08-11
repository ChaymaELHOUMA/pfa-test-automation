package com.smartautomation.testautomation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautomation.testautomation.model.AstAnalysisOutcome;
import com.smartautomation.testautomation.model.AstAnalysisResult;
import com.smartautomation.testautomation.model.NodeExecutionResult;


@Service
public class AstAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AstAnalysisService.class);

    private final NodeBridgeService nodeBridgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AstAnalysisService(NodeBridgeService nodeBridgeService) {
        this.nodeBridgeService = nodeBridgeService;
    }

    public AstAnalysisOutcome analyser(String cheminFichier) {
        log.info("Analyse AST du fichier : {}", cheminFichier);

        NodeExecutionResult resultatProcess = nodeBridgeService.runScript("analyze.js", cheminFichier);

        if (resultatProcess.isTimedOut()) {
            log.warn("Timeout lors de l'analyse AST de {}", cheminFichier);
            return AstAnalysisOutcome.echec("Timeout depasse lors de l'analyse AST");
        }

        if (!resultatProcess.isSuccess()) {
            log.warn("Echec du script analyze.js pour {} : {}", cheminFichier, resultatProcess.getStderr());
            return AstAnalysisOutcome.echec("Erreur analyze.js : " + tronquer(resultatProcess.getStderr()));
        }

        String stdout = resultatProcess.getStdout();
        if (stdout == null || stdout.isBlank()) {
            log.error("Aucune sortie JSON recue de analyze.js pour {}", cheminFichier);
            return AstAnalysisOutcome.echec("analyze.js n'a produit aucune sortie exploitable");
        }

        try {
            AstAnalysisResult resultat = objectMapper.readValue(stdout, AstAnalysisResult.class);
            log.info("Analyse AST reussie pour {} : {} unite(s) testable(s) trouvee(s)",
                    cheminFichier, resultat.getUnites() != null ? resultat.getUnites().size() : 0);
            return AstAnalysisOutcome.succes(resultat);

        } catch (JsonProcessingException e) {
            log.error("Impossible de parser le JSON produit par analyze.js pour {}", cheminFichier, e);
            return AstAnalysisOutcome.echec("Sortie de analyze.js illisible (JSON invalide) : " + tronquer(stdout));
        }
    }

    private String tronquer(String texte) {
        if (texte == null) return "";
        return texte.length() > 500 ? texte.substring(0, 500) + "..." : texte;
    }
}
