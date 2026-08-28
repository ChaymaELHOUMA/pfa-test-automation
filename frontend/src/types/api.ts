export interface ProjectMetadata {
  idProjet: string;
  nomProjet: string;
  pointEntree: string;
  repositoryFile?: string | null;
  serviceFile?: string | null;
  fichiers: string[];
}

export interface BrancheInfo {
  type: string;
  aUnElse: boolean | null;
  nombreCas: number | null;
  ligne: number;
}

export interface CasErreurInfo {
  type: string;
  codeHttp: number | null;
  aUnFinally: boolean | null;
  ligne: number;
}

export interface UniteTestable {
  type: 'fonction' | 'route' | string;
  nom: string;
  methode?: string | null;
  chemin?: string | null;
  parametres?: string[] | null;
  ligneDebut: number;
  ligneFin: number;
  branches: BrancheInfo[] | null;
  casErreur: CasErreurInfo[] | null;
}

export interface AstAnalysisResult {
  fichier: string;
  unites: UniteTestable[];
}

export interface AstAnalysisOutcome {
  erreurTechnique: boolean;
  messageErreur: string | null;
  resultat: AstAnalysisResult | null;
}

export interface PipelineRequest {
  chemin: string;
  nomUnite: string;
  mode?: 'structure' | 'brut' | 'integration';
  idProjet?: string;
}

export interface PipelineResult {
  succes: boolean;
  etapeEnEchec: string | null;
  messageErreur: string | null;
  cheminFichierTest: string | null;
  codeTest: string | null;
  totalTests: number | null;
  testsReussis: number | null;
  testsEchoues: number | null;
}

export interface PromptComparisonResult {
  nomUnite: string;
  cheminsAttendus: string[];
  nombreTestsGeneresBrut: number;
  testsReussisBrut: number;
  occurrencesCheminsMentionnesBrut: number;
  nombreTestsGeneresStructure: number;
  testsReussisStructure: number;
  occurrencesCheminsMentionnesStructure: number;
}

export interface TestDeduplicationResult {
  totalTests: number;
  testsUniques: number;
  doublonsDetectes: number;
  titresDoublons: string[];
  estNonRedondant: boolean;
  codeDeduplique: string;
}

export interface CategorieMetrics {
  total: number;
  reussis: number;
  echoues: number;
  tauxSucces: number;
}

export interface FichierDetail {
  nomFichier: string;
  categorie: string;
  totalTests: number;
  testsReussis: number;
  testsEchoues: number;
  succes: boolean;
  messageErreur: string | null;
}

export interface MultiTypeTestResult {
  totalTests: number;
  testsReussis: number;
  testsEchoues: number;
  tauxSucces: number;
  resultatsParType: Record<string, CategorieMetrics>;
  detailsFichiers: FichierDetail[];
}

export interface CoverageMetrics {
  total: number;
  covered: number;
  skipped: number;
  pct: number;
}

export interface FileCoverageSummary {
  lines: CoverageMetrics;
  branches: CoverageMetrics;
  statements: CoverageMetrics;
  functions: CoverageMetrics;
}

export interface CoverageResult {
  succes: boolean;
  messageErreur: string | null;
  lines: CoverageMetrics;
  branches: CoverageMetrics;
  statements: CoverageMetrics;
  functions: CoverageMetrics;
  detailsParFichier: Record<string, FileCoverageSummary>;
}

export interface MutantMetrics {
  killed: number;
  survived: number;
  timedOut: number;
  noCoverage: number;
  compileError: number;
  total: number;
  scoreMutationPct: number;
}

export interface MutationResult {
  succes: boolean;
  messageErreur: string | null;
  metrics: MutantMetrics;
  metricsParFichier: Record<string, MutantMetrics>;
}
