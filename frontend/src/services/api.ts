import axios, { AxiosError } from 'axios';
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

// Custom error types
class ApiError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public originalError?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

const client = () => axios.create({
  baseURL: BASE_URL,
  timeout: 45000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Helper function to handle API errors
const handleApiError = (error: any, fallbackData: any, context: string): any => {
  if (FORCE_MOCK_MODE) {
    console.warn(`[API] ${context}: Force mock mode enabled, returning fallback data`);
    return fallbackData;
  }

  if (axios.isCancel(error)) {
    console.warn(`[API] ${context}: Request cancelled`);
    return fallbackData;
  }

  if (error instanceof AxiosError) {
    const statusCode = error.response?.status;
    const errorMessage = error.response?.data?.error || error.message;
    
    console.error(`[API] ${context}: HTTP ${statusCode} - ${errorMessage}`, error);
    
    // Return fallback data for client errors (4xx) and server errors (5xx)
    if (statusCode && statusCode >= 400) {
      console.warn(`[API] ${context}: Returning fallback data due to error`);
      return fallbackData;
    }
  }

  // Network errors or other issues
  console.error(`[API] ${context}: Unexpected error`, error);
  return fallbackData;
};

export const checkBackendHealth = async (): Promise<boolean> => {
  if (FORCE_MOCK_MODE) return false;
  try {
    const res = await client().get('/analyser-ast?chemin=projet-reference/routes-utilisateurs.js', { timeout: 3000 });
    return res.status === 200 || res.status === 422;
  } catch (error) {
    console.warn('[API] Backend health check failed:', error);
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
    } catch (error) {
      return handleApiError(error, [], 'listerProjets');
    }
  },

  uploadProjet: async (file: File, pointEntree: string, repositoryFile?: string, serviceFile?: string, nomProjet?: string): Promise<ProjectMetadata> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('pointEntree', pointEntree);
    if (repositoryFile) formData.append('repositoryFile', repositoryFile);
    if (serviceFile) formData.append('serviceFile', serviceFile);
    if (nomProjet) formData.append('nomProjet', nomProjet);

    try {
      const res = await axios.post<ProjectMetadata>(`${BASE_URL}/upload-projet`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60000
      });
      return res.data;
    } catch (error) {
      throw new ApiError('Failed to upload project', 0, error);
    }
  },

  // 1. Analyse AST
  analyserAst: async (chemin: string, idProjet?: string): Promise<AstAnalysisOutcome> => {
    if (FORCE_MOCK_MODE) return MOCK_AST_OUTCOME;
    try {
      const res = await client().get<AstAnalysisOutcome>(`/analyser-ast`, { params: { chemin, idProjet } });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_AST_OUTCOME, 'analyserAst');
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
    } catch (error) {
      return handleApiError(error, {
        prompt: `// Prompt de démonstration pour ${nomUnite} (${mode})\n// Analyse AST de ${chemin}\n\nGenère des tests unitaires/intégration Jest pour l'unité ${nomUnite}.`,
        erreur: 'API Error - using fallback prompt'
      }, 'construirePrompt');
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
      const res = await client().post<PipelineResult>('/generer-et-executer-test', { chemin, nomUnite, mode, idProjet }, { timeout: 120000 });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_PIPELINE_RESULT, 'genererEtExecuterTest');
    }
  },

  // 4. Comparaison de Prompts
  comparerPrompts: async (chemin: string, nomUnite: string, idProjet?: string): Promise<PromptComparisonResult> => {
    if (FORCE_MOCK_MODE) return MOCK_PROMPT_COMPARISON;
    try {
      const res = await client().get<PromptComparisonResult>('/comparer-prompts', { params: { chemin, nomUnite, idProjet } });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_PROMPT_COMPARISON, 'comparerPrompts');
    }
  },

  // 5. Exécution de toute la suite (Multi-types)
  executerSuiteTests: async (idProjet?: string): Promise<MultiTypeTestResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MULTI_TYPE_TEST_RESULT;
    try {
      const res = await axios.post<MultiTypeTestResult>(`${BASE_URL}/executer-suite-tests`, null, { 
        params: { idProjet },
        timeout: 180000 
      });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_MULTI_TYPE_TEST_RESULT, 'executerSuiteTests');
    }
  },

  // Nouvelle méthode : Génération Multi-Type
  genererSuiteMultiType: async (chemin: string, idProjet?: string): Promise<MultiTypeTestResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MULTI_TYPE_TEST_RESULT;
    try {
      const res = await axios.post<MultiTypeTestResult>(`${BASE_URL}/generer-suite-multi-type`, null, { 
        params: { chemin, idProjet },
        timeout: 180000 
      });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_MULTI_TYPE_TEST_RESULT, 'genererSuiteMultiType');
    }
  },

  // 6. Extraction de titres de tests & Validation de non-redondance
  extraireTitresTests: async (codeTest: string): Promise<{ titres: string[] }> => {
    if (FORCE_MOCK_MODE) return { titres: ['cas nominal - creation utilisateur valide', 'cas erreur - email invalide'] };
    try {
      const res = await client().post<{ titres: string[] }>('/extraire-titres-tests', { codeTest });
      return res.data;
    } catch (error) {
      return handleApiError(error, { titres: ['cas nominal - creation utilisateur valide', 'cas erreur - email invalide'] }, 'extraireTitresTests');
    }
  },

  validerNonRedondance: async (codeTest: string): Promise<TestDeduplicationResult> => {
    if (FORCE_MOCK_MODE) return MOCK_DEDUPLICATION_RESULT;
    try {
      const res = await client().post<TestDeduplicationResult>('/valider-non-redondance', { codeTest });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_DEDUPLICATION_RESULT, 'validerNonRedondance');
    }
  },

  // 7. Couverture nyc / Istanbul
  mesurerCouvertureProjet: async (idProjet?: string): Promise<CoverageResult> => {
    if (FORCE_MOCK_MODE) return MOCK_COVERAGE_RESULT;
    try {
      const target = idProjet ? `/api/coverage/${idProjet}` : '/api/coverage/projet-reference';
      const res = await axios.get<CoverageResult>(`${BASE_URL}${target}`, { timeout: 120000 });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_COVERAGE_RESULT, 'mesurerCouvertureProjet');
    }
  },

  // 8. Mutation testing Stryker
  obtenirMutationProjetReference: async (idProjet?: string): Promise<MutationResult> => {
    if (FORCE_MOCK_MODE) return MOCK_MUTATION_RESULT;
    try {
      const target = idProjet ? `/api/mutation/${idProjet}` : '/api/mutation/projet-reference';
      const res = await axios.get<MutationResult>(`${BASE_URL}${target}`, { timeout: 310000 });
      return res.data;
    } catch (error) {
      return handleApiError(error, MOCK_MUTATION_RESULT, 'obtenirMutationProjetReference');
    }
  }
};
