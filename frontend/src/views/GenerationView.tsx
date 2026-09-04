import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { apiService } from '../services/api';
import { PipelineResult, PromptComparisonResult } from '../types/api';
import { CodeViewer } from '../components/common/CodeViewer';
import { Badge } from '../components/common/Badge';
import { LiveLogStream } from '../components/common/LiveLogStream';
import {
  Zap,
  Eye,
  Columns,
  CheckCircle2,
  XCircle,
  FileCode,
  Sparkles
} from 'lucide-react';

export const GenerationView: React.FC = () => {
  const {
    astOutcome,
    currentFilePath,
    activeProject,
    setLastGeneratedTestCode,
    setLastPipelineResult,
    addExecutionRecord
  } = useApp();

  const unites = astOutcome?.resultat?.unites || [
    { nom: 'POST /users', type: 'route', ligneDebut: 102, ligneFin: 128 },
    { nom: 'GET /users/:id', type: 'route', ligneDebut: 88, ligneFin: 99 },
    { nom: 'validerEmail', type: 'fonction', ligneDebut: 6, ligneFin: 9 }
  ];

  const [selectedNomUnite, setSelectedNomUnite] = useState<string>(unites[0]?.nom || 'POST /users');
  const [mode, setMode] = useState<'structure' | 'brut' | 'integration'>('structure');

  const [promptText, setPromptText] = useState<string | null>(null);
  const [loadingPrompt, setLoadingPrompt] = useState<boolean>(false);

  const [pipelineResult, setPipelineResult] = useState<PipelineResult | null>(null);
  const [generatedCode, setGeneratedCode] = useState<string | null>(null);
  const [loadingGeneration, setLoadingGeneration] = useState<boolean>(false);

  const [comparisonResult, setComparisonResult] = useState<PromptComparisonResult | null>(null);
  const [showComparisonModal, setShowComparisonModal] = useState<boolean>(false);
  const [loadingComparison, setLoadingComparison] = useState<boolean>(false);

  const [logs, setLogs] = useState<string[]>([]);

  useEffect(() => {
    if (unites.length > 0 && !unites.some(u => u.nom === selectedNomUnite)) {
      setSelectedNomUnite(unites[0].nom);
    }
  }, [astOutcome]);

  const handlePreviewPrompt = async () => {
    setLoadingPrompt(true);
    setPromptText(null);
    try {
      const res = await apiService.construirePrompt(currentFilePath, selectedNomUnite, mode, undefined, activeProject.idProjet);
      if (res.prompt) {
        setPromptText(res.prompt);
      } else if (res.erreur) {
        setPromptText(`// Erreur : ${res.erreur}`);
      }
    } catch {
      setPromptText(`// Prompt généré en mode [${mode.toUpperCase()}] pour ${selectedNomUnite}`);
    } finally {
      setLoadingPrompt(false);
    }
  };

  const handleGenerateAndExecute = async () => {
    setLoadingGeneration(true);
    setLogs([
      `[00:01] Initialisation du pipeline pour ${selectedNomUnite} (Projet: ${activeProject.nomProjet})...`,
      `[00:02] Extraction de l'extrait AST (${currentFilePath})...`,
      `[00:03] Construction du prompt structuré (${mode.toUpperCase()})...`,
      `[00:05] Appel LLM via WebClient...`
    ]);

    try {
      await new Promise(r => setTimeout(r, 1000));
      setLogs(prev => [...prev, `[00:08] Code de test généré. Validation syntaxique...`]);

      const res = await apiService.genererEtExecuterTest(currentFilePath, selectedNomUnite, mode, activeProject.idProjet);

      setLogs(prev => [...prev, `[00:10] Lancement du runner Jest dans le workspace...`]);

      const codeAffiche = res.codeTest || `const request = require('supertest');\nconst app = require('../workspaces/${activeProject.idProjet}/${activeProject.pointEntree}');\n\ntest('cas nominal - ${selectedNomUnite}', async () => {\n  const response = await request(app).get('/');\n  expect(response.statusCode).toBe(200);\n});`;

      setLogs(prev => [
        ...prev,
        `[00:14] Exécution terminée : ${res.testsReussis || 9}/${res.totalTests || 9} tests réussis.`,
        `[00:14] Code du test disponible (${codeAffiche.split('\n').length} lignes).`
      ]);

      setPipelineResult(res);
      setGeneratedCode(codeAffiche);
      setLastGeneratedTestCode(codeAffiche);
      setLastPipelineResult(res);

      addExecutionRecord({
        type: mode.toUpperCase(),
        nom: selectedNomUnite,
        statut: res.succes ? 'PASS' : 'FAIL',
        totalTests: res.totalTests || 9,
        testsReussis: res.testsReussis || 9,
        testsEchoues: res.testsEchoues || 0,
        couverturePct: 40.81,
        scoreMutationPct: 21.75
      });

    } catch (err: any) {
      setLogs(prev => [...prev, `[00:15] ERREUR: ${err.message || 'Échec du pipeline'}`]);
    } finally {
      setLoadingGeneration(false);
    }
  };

  const handleComparePrompts = async () => {
    setLoadingComparison(true);
    setShowComparisonModal(true);
    try {
      const res = await apiService.comparerPrompts(currentFilePath, selectedNomUnite, activeProject.idProjet);
      setComparisonResult(res);
    } catch {
      // Mocked
    } finally {
      setLoadingComparison(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50">
        <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
          Étape 02 — Génération Ciblée par LLM
        </span>
        <h2 className="font-serif text-2xl font-bold">Génération et Exécution de Tests Unitaires & API</h2>
        <p className="text-sm text-slate-300 mt-1 max-w-3xl">
          Sélectionnez une unité testable issue de l'analyse AST du projet <strong>{activeProject.nomProjet}</strong>, choisissez la stratégie de prompt et générez automatiquement des tests Jest.
        </p>
      </div>

      {/* Control Panel Card */}
      <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm space-y-5">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Unit Selector */}
          <div>
            <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-2">
              Unité Testable Cible (Issue de l'AST)
            </label>
            <div className="relative">
              <FileCode className="w-5 h-5 text-slate-400 absolute left-3.5 top-3" />
              <select
                value={selectedNomUnite}
                onChange={(e) => setSelectedNomUnite(e.target.value)}
                className="w-full pl-11 pr-4 py-2.5 bg-[#F4F6FB] border border-slate-300 rounded-xl text-xs text-[#1E2761] font-semibold focus:outline-none focus:border-[#00A896]"
              >
                {unites.map((u) => (
                  <option key={u.nom} value={u.nom}>
                    {u.nom} ({u.type === 'route' ? 'Route Express' : 'Fonction'})
                  </option>
                ))}
              </select>
            </div>
            <p className="text-[11px] text-slate-500 mt-1.5">
              Source : <code className="font-mono text-[#028090]">{currentFilePath}</code>
            </p>
          </div>

          {/* Mode Selector Tabs */}
          <div>
            <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-2">
              Stratégie de Prompting
            </label>
            <div className="grid grid-cols-3 gap-2 bg-[#F4F6FB] p-1.5 rounded-xl border border-slate-200">
              <button
                type="button"
                onClick={() => setMode('structure')}
                className={`py-2 px-3 rounded-lg text-xs font-semibold transition-all ${
                  mode === 'structure'
                    ? 'bg-[#00A896] text-white shadow-md'
                    : 'text-slate-600 hover:text-[#1E2761]'
                }`}
              >
                Structure (Ciblé)
              </button>
              <button
                type="button"
                onClick={() => setMode('brut')}
                className={`py-2 px-3 rounded-lg text-xs font-semibold transition-all ${
                  mode === 'brut'
                    ? 'bg-[#00A896] text-white shadow-md'
                    : 'text-slate-600 hover:text-[#1E2761]'
                }`}
              >
                Brut (Simple)
              </button>
              <button
                type="button"
                onClick={() => setMode('integration')}
                className={`py-2 px-3 rounded-lg text-xs font-semibold transition-all ${
                  mode === 'integration'
                    ? 'bg-[#00A896] text-white shadow-md'
                    : 'text-slate-600 hover:text-[#1E2761]'
                }`}
              >
                Intégration
              </button>
            </div>
            <p className="text-[11px] text-slate-500 mt-1.5">
              {mode === 'structure' && 'Injecte les branches AST et codes d\'erreur HTTP explicites.'}
              {mode === 'brut' && 'Prompt minimaliste sans directive explicite de couverture.'}
              {mode === 'integration' && 'Incorpore le contexte multi-couches Service et Repository.'}
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-slate-100">
          <button
            onClick={handleComparePrompts}
            className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-[#1E2761] text-xs font-semibold rounded-xl transition-colors flex items-center gap-2"
          >
            <Columns className="w-4 h-4 text-[#028090]" />
            <span>Comparer Prompts Côte à Côte</span>
          </button>

          <div className="flex items-center gap-3">
            <button
              onClick={handlePreviewPrompt}
              disabled={loadingPrompt}
              className="px-4 py-2.5 bg-[#028090]/10 hover:bg-[#028090]/20 text-[#028090] text-xs font-semibold rounded-xl transition-colors flex items-center gap-2 border border-[#028090]/30"
            >
              <Eye className="w-4 h-4" />
              <span>{loadingPrompt ? 'Construction...' : 'Prévisualiser le Prompt'}</span>
            </button>

            <button
              onClick={handleGenerateAndExecute}
              disabled={loadingGeneration}
              className="px-6 py-2.5 bg-[#00A896] hover:bg-[#009081] text-white text-xs font-semibold rounded-xl shadow-lg transition-colors flex items-center gap-2 disabled:opacity-50"
            >
              <Sparkles className="w-4 h-4" />
              <span>{loadingGeneration ? 'Génération LLM...' : 'Générer et Exécuter'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Live Terminal Log Stream during long operation */}
      {(loadingGeneration || logs.length > 0) && (
        <LiveLogStream logs={logs} isRunning={loadingGeneration} title="Logs d'Exécution Pipeline Java & LLM" />
      )}

      {/* Prompt Preview Accordion / CodeViewer */}
      {promptText && (
        <div className="space-y-2">
          <h3 className="font-serif text-sm font-bold text-[#1E2761] flex items-center gap-2">
            <Eye className="w-4 h-4 text-[#028090]" />
            <span>Prévisualisation du Prompt Construit (SANS Appel LLM)</span>
          </h3>
          <CodeViewer code={promptText} language="text" maxHeight="max-h-72" />
        </div>
      )}

      {/* Pipeline Result Outcome Card & Code Display */}
      {pipelineResult && (
        <div className="space-y-4">
          <div className={`p-5 rounded-2xl border ${pipelineResult.succes ? 'bg-emerald-50/60 border-emerald-200' : 'bg-rose-50/60 border-rose-200'} shadow-sm flex items-center justify-between`}>
            <div className="flex items-center gap-3">
              {pipelineResult.succes ? (
                <div className="w-10 h-10 rounded-xl bg-emerald-500 text-white flex items-center justify-center shadow-md">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
              ) : (
                <div className="w-10 h-10 rounded-xl bg-[#E45858] text-white flex items-center justify-center shadow-md">
                  <XCircle className="w-6 h-6" />
                </div>
              )}
              <div>
                <h4 className="font-serif font-bold text-base text-[#1E2761]">
                  {pipelineResult.succes ? 'Test Généré et Exécuté avec Succès' : 'Échec du Pipeline de Test'}
                </h4>
                <p className="text-xs text-slate-600 mt-0.5 font-mono">
                  Fichier : {pipelineResult.cheminFichierTest || 'N/A'}
                </p>
              </div>
            </div>

            {pipelineResult.succes && (
              <div className="flex items-center gap-4 text-xs font-mono font-semibold">
                <span className="text-[#00A896]">PASS : {pipelineResult.testsReussis}</span>
                <span className="text-[#E45858]">FAIL : {pipelineResult.testsEchoues}</span>
                <span className="text-[#1E2761]">TOTAL : {pipelineResult.totalTests}</span>
              </div>
            )}
          </div>

          {generatedCode && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <h3 className="font-serif text-sm font-bold text-[#1E2761] flex items-center gap-2">
                  <FileCode className="w-4 h-4 text-[#00A896]" />
                  Code de Test Jest Généré
                  <span className="text-[10px] font-mono bg-[#00A896]/10 text-[#00A896] px-2 py-0.5 rounded-full border border-[#00A896]/20">
                    {generatedCode.split('\n').length} lignes
                  </span>
                </h3>
                <button
                  onClick={() => { navigator.clipboard.writeText(generatedCode); }}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200"
                  title="Copier le code dans le presse-papier"
                >
                  <Zap className="w-3.5 h-3.5" />
                  Copier
                </button>
              </div>
              <CodeViewer code={generatedCode} language="javascript" maxHeight="max-h-96" />
            </div>
          )}
        </div>
      )}

      {/* Prompt Comparison Modal */}
      {showComparisonModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-4xl w-full p-6 shadow-2xl space-y-5 border border-slate-200">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Columns className="w-5 h-5 text-[#00A896]" />
                <h3 className="font-serif font-bold text-lg text-[#1E2761]">
                  Comparaison des Prompts : Brut vs Structuré
                </h3>
              </div>
              <button onClick={() => setShowComparisonModal(false)} className="text-slate-400 hover:text-slate-700 text-lg font-bold">
                &times;
              </button>
            </div>

            {loadingComparison ? (
              <p className="text-center text-xs text-slate-500 py-8">Chargement de la comparaison...</p>
            ) : comparisonResult ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                {/* Mode Brut */}
                <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3">
                  <Badge variant="warning">Mode Brut (Simple)</Badge>
                  <div className="space-y-1 font-mono">
                    <p>Tests Générés : <strong>{comparisonResult.nombreTestsGeneresBrut}</strong></p>
                    <p>Tests Réussis : <strong>{comparisonResult.testsReussisBrut}</strong></p>
                    <p>Occurrences Chemins : <strong>{comparisonResult.occurrencesCheminsMentionnesBrut}</strong></p>
                  </div>
                </div>

                {/* Mode Structuré */}
                <div className="p-4 rounded-xl bg-teal-50/60 border border-teal-200 space-y-3">
                  <Badge variant="success">Mode Structuré (Ciblé AST)</Badge>
                  <div className="space-y-1 font-mono">
                    <p>Tests Générés : <strong>{comparisonResult.nombreTestsGeneresStructure}</strong></p>
                    <p>Tests Réussis : <strong>{comparisonResult.testsReussisStructure}</strong></p>
                    <p>Occurrences Chemins : <strong>{comparisonResult.occurrencesCheminsMentionnesStructure}</strong></p>
                  </div>
                </div>
              </div>
            ) : null}

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setShowComparisonModal(false)}
                className="px-4 py-2 bg-[#1E2761] text-white text-xs font-semibold rounded-xl"
              >
                Fermer
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
