package com.smartautomation.testautomation.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.smartautomation.testautomation.exception.TestGenerationException;

@Service
public class TestCodeExtractor {

    private static final Pattern CODE_BLOCK_PATTERN =
            Pattern.compile("```(?:javascript|js)?\\s*([\\s\\S]*?)```");

    
    public String extraireCode(String reponseLlm) {
        if (reponseLlm == null || reponseLlm.isBlank()) {
            throw new TestGenerationException("Reponse du LLM vide, impossible d'extraire un test");
        }

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(reponseLlm);
        if (!matcher.find()) {
            throw new TestGenerationException(
                    "Aucun bloc de code delimite (```javascript ... ```) trouve dans la reponse du LLM");
        }

        String code = matcher.group(1).trim();
        if (code.isEmpty()) {
            throw new TestGenerationException("Le bloc de code extrait est vide");
        }

        return code;
    }
}
