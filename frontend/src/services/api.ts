import axios from 'axios';
import {
  AstAnalysisOutcome,
  CoverageResult,
  MultiTypeTestResult,
  MutationResult,
  PipelineResult,
  ProjectMetadata,
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
  // Projets
  listerProjets: async (): Promise<ProjectMetadata[]> => {
    if (FORCE_MOCK_MODE) return [];
    try {
      const res = await client().get<ProjectMetadata[]>('/api/projets');
      return res.data;
    } catch {
      return [];
    }
  },

  uploadProjet: async (file: File, pointEntree: string, repositoryFile?: string, serviceFile?: string, nomProjet?: string): Promise<ProjectMetadata> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('pointEntree', pointEntree);
    if (repositoryFile) formData.append('repositoryFile', repositoryFile);
    if (serviceFile) formData.append('serviceFile', serviceFile);
    if (nomProjet) formData.append('nomProjet', nomProjet);

    const res = await axios.post<ProjectMetadata>(`${BASE_URL}/upload-projet`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    });
    return res.data;
  },

  // 1. Analyse AST
  analyserAst: async (chemin: string, idProjet?: string): Promise<AstAnalysisOutcome> => {
    if (FORCE_MOCK_MODE) return MOCK_AST_OUTCOME;
    try {
      const res = await client().get<AstAnalysisOutcome>(`/analyser-ast`, { params: { chemin, idProjet } });
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
    testsExistants?: string[],
    idProjet?: string
  ): Promise<{ prompt?: string; erreur?: string }> => {
    if (FORCE_MOCK_MODE) {
      return {
        prompt: `// Prompt généré pour ${nomUnite} en mode [${mode.toUpperCase()}]\n// Source: ${chemin}\n\nAnalyses l'unité "${nomUnite}" et génère une suite de tests Jest complète.\nRespecte les cas nominaux, limites et d'erreur.`
      };
    }
    try {
      const params: any = { chemin, nomUnite, mode, idProjet };
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
  genererEtExecuterTest: async (
    chemin: string,
    nomUnite: string,
    mode: 'structure' | 'brut' | 'integration' = 'structure',
    idProjet?: string
  ): Promise<PipelineResult> => {
    if (FORCE_MOCK_MODE) return MOCK_PIPELINE_RESULT;
    try {
      const res = await client().post<PipelineResult>('/generer-et-executer-test', { chemin, nomUnite, mode, idProjet });
      return res.data;
    } catch (err: any) {
      console.warn('API Call /generer-et-executer-test failed:', err);
      return MOCK_PIPELINE_RESULT;
    }
  },

  // 4. Comparaison de Prompts
  comparerPrompts: async (chemin: string, nomUnite: string, idProjet?: string): Promise<PromptComparisonResult> => {
    if (FORCE_MOCK_MODE) return MOCK_PROMPT_COMPARISON;
    try {
      const res = await client().get<PromptComparisonResult>('/comparer-prompts', { params: { chemin, nomUnite, idProjet } });
      return res.data;
    } catch {
      return MOCK_PROMPT_COMPARISON;
    }
  },

  // 5. Exécution de toute la suite (Multi-types)
  executerSuiteTests: async (idProjet?: string): Promise<MultiTypeTestResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MULTI_TYPE_TEST_RESULT;
    try {
      const res = await client().post<MultiTypeTestResult>('/executer-suite-tests', null, { params: { idProjet } });
      return res.data;
    } catch {
      return MOCK_MULTI_TYPE_TEST_RESULT;
    }
  },

  // Nouvelle méthode : Génération Multi-Type
  genererSuiteMultiType: async (chemin: string, idProjet?: string): Promise<MultiTypeTestResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MULTI_TYPE_TEST_RESULT;
    try {
      const res = await client().post<MultiTypeTestResult>('/generer-suite-multi-type', null, { params: { chemin, idProjet } });
      return res.data;
    } catch (err: any) {
      console.warn('API Call /generer-suite-multi-type failed:', err);
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
  mesurerCouvertureProjet: async (idProjet?: string): Promise<CoverageResult> => {
    if (FORCE_MOCK_MODE) return MOCK_COVERAGE_RESULT;
    try {
      const target = idProjet ? `/api/coverage/${idProjet}` : '/api/coverage/projet-reference';
      const res = await client().get<CoverageResult>(target);
      return res.data;
    } catch {
      return MOCK_COVERAGE_RESULT;
    }
  },

  // 8. Mutation testing Stryker
  obtenirMutationProjetReference: async (idProjet?: string): Promise<MutationResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MUTATION_RESULT;
    try {
      const target = idProjet ? `/api/mutation/${idProjet}` : '/api/mutation/projet-reference';
      const res = await client().get<MutationResult>(target, { timeout: 310000 });
      return res.data;
    } catch {
      return MOCK_MUTATION_RESULT;
    }
  }
};
