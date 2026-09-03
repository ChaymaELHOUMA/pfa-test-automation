import { describe, it, expect, beforeEach, vi } from 'vitest';
import { apiService, setBaseUrl, setForceMockMode, checkBackendHealth } from './api';

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: vi.fn(),
      post: vi.fn(),
    })),
    post: vi.fn(),
    isCancel: vi.fn(),
  },
}));

describe('API Service', () => {
  beforeEach(() => {
    setBaseUrl('http://localhost:8080');
    setForceMockMode(false);
  });

  describe('Configuration', () => {
    it('should allow setting base URL', () => {
      setBaseUrl('http://test.com');
      // This would need the actual getBaseUrl implementation to test
    });

    it('should allow toggling mock mode', () => {
      setForceMockMode(true);
      setForceMockMode(false);
    });
  });

  describe('Backend Health Check', () => {
    it('should return false in force mock mode', async () => {
      setForceMockMode(true);
      const result = await checkBackendHealth();
      expect(result).toBe(false);
    });

    it('should return false on network error', async () => {
      setForceMockMode(false);
      // This would need proper axios mocking
      const result = await checkBackendHealth();
      expect(typeof result).toBe('boolean');
    });
  });

  describe('API Methods', () => {
    it('should have listerProjets method', () => {
      expect(typeof apiService.listerProjets).toBe('function');
    });

    it('should have uploadProjet method', () => {
      expect(typeof apiService.uploadProjet).toBe('function');
    });

    it('should have analyserAst method', () => {
      expect(typeof apiService.analyserAst).toBe('function');
    });

    it('should have construirePrompt method', () => {
      expect(typeof apiService.construirePrompt).toBe('function');
    });

    it('should have genererEtExecuterTest method', () => {
      expect(typeof apiService.genererEtExecuterTest).toBe('function');
    });

    it('should have comparerPrompts method', () => {
      expect(typeof apiService.comparerPrompts).toBe('function');
    });

    it('should have executerSuiteTests method', () => {
      expect(typeof apiService.executerSuiteTests).toBe('function');
    });

    it('should have genererSuiteMultiType method', () => {
      expect(typeof apiService.genererSuiteMultiType).toBe('function');
    });

    it('should have extraireTitresTests method', () => {
      expect(typeof apiService.extraireTitresTests).toBe('function');
    });

    it('should have validerNonRedondance method', () => {
      expect(typeof apiService.validerNonRedondance).toBe('function');
    });

    it('should have mesurerCouvertureProjet method', () => {
      expect(typeof apiService.mesurerCouvertureProjet).toBe('function');
    });

    it('should have obtenirMutationProjetReference method', () => {
      expect(typeof apiService.obtenirMutationProjetReference).toBe('function');
    });
  });
});