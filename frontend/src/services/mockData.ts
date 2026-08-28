import {
  AstAnalysisOutcome,
  CoverageResult,
  MultiTypeTestResult,
  MutationResult,
  PipelineResult,
  PromptComparisonResult,
  TestDeduplicationResult
} from '../types/api';

export const MOCK_AST_OUTCOME: AstAnalysisOutcome = {
  erreurTechnique: false,
  messageErreur: null,
  resultat: {
    fichier: 'projet-reference/routes-utilisateurs.js',
    unites: [
      {
        type: 'route',
        nom: 'GET /users',
        methode: 'GET',
        chemin: '/users',
        parametres: ['limite', 'role', 'tri'],
        ligneDebut: 47,
        ligneFin: 78,
        branches: [
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 48 },
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 53 },
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 60 },
          { type: 'if', aUnElse: true, nombreCas: null, ligne: 67 }
        ],
        casErreur: [
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 51 },
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 54 },
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 62 }
        ]
      },
      {
        type: 'route',
        nom: 'GET /users/:id',
        methode: 'GET',
        chemin: '/users/:id',
        parametres: ['id'],
        ligneDebut: 88,
        ligneFin: 99,
        branches: [
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 90 },
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 95 }
        ],
        casErreur: [
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 91 },
          { type: 'reponse-erreur-http', codeHttp: 404, aUnFinally: null, ligne: 96 }
        ]
      },
      {
        type: 'route',
        nom: 'POST /users',
        methode: 'POST',
        chemin: '/users',
        parametres: ['nom', 'email', 'role', 'statut', 'age'],
        ligneDebut: 102,
        ligneFin: 128,
        branches: [
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 103 },
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 107 },
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 112 }
        ],
        casErreur: [
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 104 },
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 108 },
          { type: 'reponse-erreur-http', codeHttp: 409, aUnFinally: null, ligne: 113 },
          { type: 'try-catch', codeHttp: 500, aUnFinally: false, ligne: 116 }
        ]
      },
      {
        type: 'route',
        nom: 'PUT /users/:id',
        methode: 'PUT',
        chemin: '/users/:id',
        parametres: ['id', 'statut'],
        ligneDebut: 131,
        ligneFin: 151,
        branches: [
          { type: 'switch', aUnElse: null, nombreCas: 4, ligne: 135 }
        ],
        casErreur: [
          { type: 'reponse-erreur-http', codeHttp: 400, aUnFinally: null, ligne: 149 }
        ]
      },
      {
        type: 'fonction',
        nom: 'validerEmail',
        methode: null,
        chemin: null,
        parametres: ['email'],
        ligneDebut: 6,
        ligneFin: 9,
        branches: [
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 7 }
        ],
        casErreur: []
      },
      {
        type: 'fonction',
        nom: 'calculerStatistiques',
        methode: null,
        chemin: null,
        parametres: ['utilisateurs'],
        ligneDebut: 30,
        ligneFin: 44,
        branches: [
          { type: 'if', aUnElse: false, nombreCas: null, ligne: 31 }
        ],
        casErreur: []
      }
    ]
  }
};

export const MOCK_PIPELINE_RESULT: PipelineResult = {
  succes: true,
  etapeEnEchec: null,
  messageErreur: null,
  cheminFichierTest: 'node-runner/generated-tests/POST_users-structure.test.js',
  codeTest: '// Mock test code\ntest(\'cas nominal - POST /users\', async () => { expect(true).toBe(true); });',
  totalTests: 9,
  testsReussis: 9,
  testsEchoues: 0
};

export const MOCK_PROMPT_COMPARISON: PromptComparisonResult = {
  nomUnite: 'POST /users',
  cheminsAttendus: ['cas nominal', 'email invalide', 'role invalide', 'email existant 409', 'catch 500'],
  nombreTestsGeneresBrut: 4,
  testsReussisBrut: 4,
  occurrencesCheminsMentionnesBrut: 2,
  nombreTestsGeneresStructure: 8,
  testsReussisStructure: 8,
  occurrencesCheminsMentionnesStructure: 5
};

export const MOCK_MULTI_TYPE_TEST_RESULT: MultiTypeTestResult = {
  totalTests: 21,
  testsReussis: 19,
  testsEchoues: 2, // Directive 5: Au moins un test en échec pour réalisme !
  tauxSucces: 90.48,
  resultatsParType: {
    UNITAIRE: { total: 8, reussis: 7, echoues: 1, tauxSucces: 87.5 },
    INTEGRATION: { total: 7, reussis: 7, echoues: 0, tauxSucces: 100.0 },
    API: { total: 6, reussis: 5, echoues: 1, tauxSucces: 83.33 }
  },
  detailsFichiers: [
    {
      nomFichier: 'generated-tests/validerEmail.test.js',
      categorie: 'UNITAIRE',
      totalTests: 4,
      testsReussis: 3,
      testsEchoues: 1,
      succes: false,
      messageErreur: '● validerEmail › cas limite - email avec préfixe invalide : expect(received).toBe(expected)'
    },
    {
      nomFichier: 'generated-tests/POST_users-structure.test.js',
      categorie: 'API',
      totalTests: 6,
      testsReussis: 5,
      testsEchoues: 1,
      succes: false,
      messageErreur: '● POST /users › cas limite - age a 0 : Expected 0, Received 25'
    },
    {
      nomFichier: 'generated-tests/GET_users_id-brut.test.js',
      categorie: 'INTEGRATION',
      totalTests: 5,
      testsReussis: 5,
      testsEchoues: 0,
      succes: true,
      messageErreur: null
    },
    {
      nomFichier: 'examples/PUT_users_id-supertest.test.js',
      categorie: 'API',
      totalTests: 6,
      testsReussis: 6,
      testsEchoues: 0,
      succes: true,
      messageErreur: null
    }
  ]
};

export const MOCK_DEDUPLICATION_RESULT: TestDeduplicationResult = {
  totalTests: 6,
  testsUniques: 5,
  doublonsDetectes: 1,
  titresDoublons: ['cas nominal - creation utilisateur valide'],
  estNonRedondant: false,
  codeDeduplique: `const request = require('supertest');\nconst app = require('../projet-reference/app');\n\ntest('cas nominal - creation utilisateur valide', async () => {\n  const response = await request(app).post('/users').send({ email: 'nouveau@test.com' });\n  expect(response.statusCode).toBe(201);\n});`
};

export const MOCK_COVERAGE_RESULT: CoverageResult = {
  succes: true,
  messageErreur: null,
  lines: { total: 147, covered: 60, skipped: 0, pct: 40.81 },
  statements: { total: 164, covered: 64, skipped: 0, pct: 39.02 },
  functions: { total: 31, covered: 12, skipped: 0, pct: 38.70 },
  branches: { total: 117, covered: 33, skipped: 0, pct: 28.20 },
  detailsParFichier: {
    'projet-reference/app.js': {
      lines: { total: 10, covered: 8, skipped: 0, pct: 80.0 },
      statements: { total: 10, covered: 8, skipped: 0, pct: 80.0 },
      functions: { total: 2, covered: 0, skipped: 0, pct: 0.0 },
      branches: { total: 2, covered: 0, skipped: 0, pct: 0.0 }
    },
    'projet-reference/routes-utilisateurs.js': {
      lines: { total: 114, covered: 34, skipped: 0, pct: 29.82 },
      statements: { total: 125, covered: 35, skipped: 0, pct: 28.0 },
      functions: { total: 18, covered: 4, skipped: 0, pct: 22.22 },
      branches: { total: 101, covered: 26, skipped: 0, pct: 25.74 }
    },
    'projet-reference/utilisateurRepository.js': {
      lines: { total: 23, covered: 18, skipped: 0, pct: 78.26 },
      statements: { total: 29, covered: 21, skipped: 0, pct: 72.41 },
      functions: { total: 11, covered: 8, skipped: 0, pct: 72.72 },
      branches: { total: 14, covered: 7, skipped: 0, pct: 50.0 }
    }
  }
};

export const MOCK_MUTATION_RESULT: MutationResult = {
  succes: true,
  messageErreur: null,
  metrics: {
    killed: 78,
    survived: 59, // Directive 5: Au moins un mutant survivant !
    timedOut: 4,
    noCoverage: 236,
    compileError: 0,
    total: 377,
    scoreMutationPct: 21.75
  },
  metricsParFichier: {
    'projet-reference/routes-utilisateurs.js': {
      killed: 61,
      survived: 22,
      timedOut: 4,
      noCoverage: 213,
      compileError: 0,
      total: 300,
      scoreMutationPct: 21.67
    },
    'projet-reference/utilisateurRepository.js': {
      killed: 17,
      survived: 37,
      timedOut: 0,
      noCoverage: 14,
      compileError: 0,
      total: 68,
      scoreMutationPct: 25.0
    },
    'projet-reference/app.js': {
      killed: 0,
      survived: 0,
      timedOut: 0,
      noCoverage: 9,
      compileError: 0,
      total: 9,
      scoreMutationPct: 0.0
    }
  }
};

export interface SurvivedMutantDetail {
  id: string;
  fichier: string;
  ligne: number;
  mutateur: string;
  codeOriginal: string;
  codeMute: string;
  explicationSurvie: string;
}

export const MOCK_SURVIVED_MUTANTS_LIST: SurvivedMutantDetail[] = [
  {
    id: '18',
    fichier: 'projet-reference/routes-utilisateurs.js',
    ligne: 8,
    mutateur: 'Regex',
    codeOriginal: 'return /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(email);',
    codeMute: '/[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/ (Suppression de l\'ancre ^)',
    explicationSurvie: 'Les tests d\'email ne testent pas de chaînes avec des caractères parasites au début (ex: "prefix_test@domain.com").'
  },
  {
    id: '339',
    fichier: 'projet-reference/utilisateurRepository.js',
    ligne: 27,
    mutateur: 'AssignmentOperator',
    codeOriginal: 'dernierId += 1;',
    codeMute: 'dernierId -= 1;',
    explicationSurvie: 'Le test de création vérifie le statut HTTP 201 mais n\'assère pas la valeur exacte du nouvel ID (expect(body.id).toBe(4)).'
  },
  {
    id: '170',
    fichier: 'projet-reference/routes-utilisateurs.js',
    ligne: 120,
    mutateur: 'StringLiteral',
    codeOriginal: 'role: req.body.role || \'user\',',
    codeMute: 'role: req.body.role || ""',
    explicationSurvie: 'Le test omet d\'assérer que response.body.role === \'user\' lorsque le rôle est omis dans le payload.'
  },
  {
    id: '185',
    fichier: 'projet-reference/routes-utilisateurs.js',
    ligne: 137,
    mutateur: 'ConditionalExpression',
    codeOriginal: 'if (utilisateur) repo.mettreAJour(idNum, { statut: \'actif\' });',
    codeMute: 'if (true) repo.mettreAJour(idNum, { statut: \'actif\' });',
    explicationSurvie: 'La route PUT renvoie directement { statut: \'actif\' } sans ré-interroger la base. Pas de GET subséquent pour vérifier la persistance.'
  }
];
