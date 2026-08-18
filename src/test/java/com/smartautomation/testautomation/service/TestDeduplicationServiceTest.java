package com.smartautomation.testautomation.service;

import com.smartautomation.testautomation.model.TestDeduplicationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestDeduplicationServiceTest {

    private TestDeduplicationService service;

    @BeforeEach
    void setUp() {
        service = new TestDeduplicationService();
    }

    @Test
    void extraireTitresTestsDoitExtraireTousLesTitres() {
        String code = """
                describe('Tests Utilisateur', () => {
                    test('cas nominal - obtenir utilisateur', () => {
                        expect(1).toBe(1);
                    });

                    it('cas limite - id 0', () => {
                        expect(0).toBe(0);
                    });
                });
                """;

        List<String> titres = service.extraireTitresTests(code);
        assertEquals(2, titres.size());
        assertEquals("cas nominal - obtenir utilisateur", titres.get(0));
        assertEquals("cas limite - id 0", titres.get(1));
    }

    @Test
    void analyserEtDedupliquerSansDoublons() {
        String code = """
                test('cas nominal - succes', () => {
                    expect(200).toBe(200);
                });
                test('cas erreur - 400 invalide', () => {
                    expect(400).toBe(400);
                });
                """;

        TestDeduplicationResult result = service.analyserEtDedupliquer(code);

        assertTrue(result.isEstNonRedondant());
        assertEquals(2, result.getTotalTests());
        assertEquals(2, result.getTestsUniques());
        assertEquals(0, result.getDoublonsDetectes());
        assertTrue(result.getTitresDoublons().isEmpty());
    }

    @Test
    void analyserEtDedupliquerAvecDoublonTitreIdentique() {
        String code = """
                test('cas nominal - succes', () => {
                    const res = 1 + 1;
                    expect(res).toBe(2);
                });
                test('cas nominal - succes', () => {
                    const diff = 2 - 0;
                    expect(diff).toBe(2);
                });
                """;

        TestDeduplicationResult result = service.analyserEtDedupliquer(code);

        assertFalse(result.isEstNonRedondant());
        assertEquals(2, result.getTotalTests());
        assertEquals(1, result.getTestsUniques());
        assertEquals(1, result.getDoublonsDetectes());
        assertEquals(1, result.getTitresDoublons().size());
        assertEquals("cas nominal - succes", result.getTitresDoublons().get(0));

        // Le code dédupliqué ne doit contenir qu'un seul bloc test('cas nominal - succes')
        List<String> titresApres = service.extraireTitresTests(result.getCodeDeduplique());
        assertEquals(1, titresApres.size());
    }

    @Test
    void analyserEtDedupliquerAvecCorpsIdentique() {
        String code = """
                test('cas nominal - premier', () => {
                    const data = { ok: true };
                    expect(data.ok).toBe(true);
                });
                test('cas nominal - second', () => {
                    const data = { ok: true };
                    expect(data.ok).toBe(true);
                });
                """;

        TestDeduplicationResult result = service.analyserEtDedupliquer(code);

        assertFalse(result.isEstNonRedondant());
        assertEquals(2, result.getTotalTests());
        assertEquals(1, result.getTestsUniques());
        assertEquals(1, result.getDoublonsDetectes());
    }
}
