import axios from 'axios';
import {
  AstAnalysisOutcome,
  CoverageResult,
  MultiTypeTestResult,
  MutationResult,
  PipelineResult,
  PromptComparisonResult,
  TestDeduplicationResult
} from '../types/api';
import {
  MOCK_AST_OUTCOME,
  MOCK_COVERAGE_RESULT,
  MOCK_DEDUPLICATION_RESULT,
  MOCK_MULTI_TYPE_TEST_RESULT,
  MOCK_MUTATION_RESULT,
  MOCK_PIPELINE_RESULT,
  MOCK_PROMPT_COMPARISON
} from './mockData';

let BASE_URL = 'http://localhost:8080';
let FORCE_MOCK_MODE = false;

export const getBaseUrl = () => BASE_URL;
export const setBaseUrl = (url: string) => { BASE_URL = url; };

export const isForceMockMode = () => FORCE_MOCK_MODE;
export const setForceMockMode = (mock: boolean) => { FORCE_MOCK_MODE = mock; };

const client = () => axios.create({
  baseURL: BASE_URL,
  timeout: 45000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const checkBackendHealth = async (): Promise<boolean> => {
  if (FORCE_MOCK_MODE) return false;
  try {
    const res = await client().get('/analyser-ast?chemin=projet-reference/routes-utilisateurs.js', { timeout: 3000 });
    return res.status === 200 || res.status === 422;
  } catch {
    return false;
  }
};

export const apiService = {
  // 1. Analyse AST
  analyserAst: async (chemin: string): Promise<AstAnalysisOutcome> => {
    if (FORCE_MOCK_MODE) return MOCK_AST_OUTCOME;
    try {
      const res = await client().get<AstAnalysisOutcome>(`/analyser-ast`, { params: { chemin } });
      return res.data;
    } catch (err: any) {
      console.warn('API Call failed, falling back to mock data:', err);
      return MOCK_AST_OUTCOME;
    }
  },

  // 2. Construction de Prompt
  construirePrompt: async (
    chemin: string,
    nomUnite: string,
    mode: 'structure' | 'brut' | 'integration' = 'structure',
    testsExistants?: string[]
  ): Promise<{ prompt?: string; erreur?: string }> => {
    if (FORCE_MOCK_MODE) {
      return {
        prompt: `// Prompt généré pour ${nomUnite} en mode [${mode.toUpperCase()}]\n// Source: ${chemin}\n\nAnalyses l'unité "${nomUnite}" et génère une suite de tests Jest complète.\nRespecte les cas nominaux, limites et d'erreur.`
      };
    }
    try {
      const params: any = { chemin, nomUnite, mode };
      if (testsExistants && testsExistants.length > 0) {
        params.testsExistants = testsExistants.join(',');
      }
      const res = await client().get('/construire-prompt', { params });
      return res.data;
    } catch (err: any) {
      console.warn('API Call /construire-prompt failed, falling back to mock:', err);
      return {
        prompt: `// Prompt de démonstration pour ${nomUnite} (${mode})\n// Analyse AST de ${chemin}\n\nGenère des tests unitaires/intégration Jest pour l'unité ${nomUnite}.`
      };
    }
  },

  // 3. Génération & Exécution Ciblée
  genererEtExecuterTest: async (nomFonction: string, codeSource: string): Promise<PipelineResult> => {
    if (FORCE_MOCK_MODE) return MOCK_PIPELINE_RESULT;
    try {
      const res = await client().post<PipelineResult>('/generer-et-executer-test', { nomFonction, codeSource });
      return res.data;
    } catch (err: any) {
      console.warn('API Call /generer-et-executer-test failed:', err);
      return MOCK_PIPELINE_RESULT;
    }
  },

  // 4. Comparaison de Prompts
  comparerPrompts: async (chemin: string, nomUnite: string): Promise<PromptComparisonResult> => {
    if (FORCE_MOCK_MODE) return MOCK_PROMPT_COMPARISON;
    try {
      const res = await client().get<PromptComparisonResult>('/comparer-prompts', { params: { chemin, nomUnite } });
      return res.data;
    } catch {
      return MOCK_PROMPT_COMPARISON;
    }
  },

  // 5. Exécution de toute la suite (Multi-types)
  executerSuiteTests: async (): Promise<MultiTypeTestResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MULTI_TYPE_TEST_RESULT;
    try {
      const res = await client().post<MultiTypeTestResult>('/executer-suite-tests');
      return res.data;
    } catch {
      return MOCK_MULTI_TYPE_TEST_RESULT;
    }
  },

  // 6. Extraction de titres de tests & Validation de non-redondance
  extraireTitresTests: async (codeTest: string): Promise<{ titres: string[] }> => {
    if (FORCE_MOCK_MODE) return { titres: ['cas nominal - creation utilisateur valide', 'cas erreur - email invalide'] };
    try {
      const res = await client().post<{ titres: string[] }>('/extraire-titres-tests', { codeTest });
      return res.data;
    } catch {
      return { titres: ['cas nominal - creation utilisateur valide', 'cas erreur - email invalide'] };
    }
  },

  validerNonRedondance: async (codeTest: string): Promise<TestDeduplicationResult> => {
    if (FORCE_MOCK_MODE) return MOCK_DEDUPLICATION_RESULT;
    try {
      const res = await client().post<TestDeduplicationResult>('/valider-non-redondance', { codeTest });
      return res.data;
    } catch {
      return MOCK_DEDUPLICATION_RESULT;
    }
  },

  // 7. Couverture nyc / Istanbul
  mesurerCouvertureProjet: async (): Promise<CoverageResult> => {
    if (FORCE_MOCK_MODE) return MOCK_COVERAGE_RESULT;
    try {
      const res = await client().get<CoverageResult>('/api/coverage/projet-reference');
      return res.data;
    } catch {
      return MOCK_COVERAGE_RESULT;
    }
  },

  // 8. Mutation testing Stryker
  obtenirMutationProjetReference: async (): Promise<MutationResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MUTATION_RESULT;
    try {
      const res = await client().get<MutationResult>('/api/mutation/projet-reference');
      return res.data;
    } catch {
      return MOCK_MUTATION_RESULT;
    }
  }
};
