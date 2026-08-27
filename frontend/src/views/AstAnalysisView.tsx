import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Badge } from '../components/common/Badge';
import { AlertBanner } from '../components/common/AlertBanner';
import {
  Search,
  FileCode,
  GitBranch,
  AlertCircle,
  ChevronDown,
  ChevronUp,
  ArrowRight,
  Code2,
  ListFilter
} from 'lucide-react';

export const AstAnalysisView: React.FC = () => {
  const { currentFilePath, runAstAnalysis, astOutcome, loadingAst, setActiveTab } = useApp();
  const [filePathInput, setFilePathInput] = useState<string>(currentFilePath);
  const [expandedCards, setExpandedCards] = useState<Record<string, boolean>>({});

  const handleAnalyze = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    await runAstAnalysis(filePathInput);
  };

  const toggleCard = (nom: string) => {
    setExpandedCards(prev => ({ ...prev, [nom]: !prev[nom] }));
  };

  const presets = [
    { label: 'routes-utilisateurs.js (Routeur Express)', path: 'projet-reference/routes-utilisateurs.js' },
    { label: 'app.js (Serveur Principal)', path: 'projet-reference/app.js' },
    { label: 'utilisateurRepository.js (Repository)', path: 'projet-reference/utilisateurRepository.js' }
  ];

  const unites = astOutcome?.resultat?.unites || [];

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50">
        <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
          Étape 01 — Analyse Syntaxique AST
        </span>
        <h2 className="font-serif text-2xl font-bold">Analyse du Code Source JavaScript</h2>
        <p className="text-sm text-slate-300 mt-1 max-w-3xl">
          Analysez le fichier JavaScript via Acorn / Acorn-Walk. L'analyse extrait les fonctions, les routes Express, les branches d'exécution et les codes HTTP d'erreur.
        </p>
      </div>

      {/* Input Form & Presets */}
      <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm">
        <form onSubmit={handleAnalyze} className="space-y-4">
          <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider">
            Chemin du Fichier Source JavaScript
          </label>
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <FileCode className="w-5 h-5 text-slate-400 absolute left-3.5 top-3" />
              <input
                type="text"
                value={filePathInput}
                onChange={(e) => setFilePathInput(e.target.value)}
                className="w-full pl-11 pr-4 py-2.5 bg-[#F4F6FB] border border-slate-300 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-[#00A896] font-mono"
                placeholder="ex. projet-reference/routes-utilisateurs.js"
                required
              />
            </div>
            <button
              type="submit"
              disabled={loadingAst}
              className="px-6 py-2.5 bg-[#00A896] hover:bg-[#009081] text-white text-xs font-semibold rounded-xl shadow-md transition-colors flex items-center justify-center gap-2 disabled:opacity-50 shrink-0"
            >
              <Search className="w-4 h-4" />
              <span>{loadingAst ? 'Analyse en cours...' : 'Analyser AST (GET /analyser-ast)'}</span>
            </button>
          </div>

          {/* Presets buttons */}
          <div className="flex flex-wrap items-center gap-2 pt-2">
            <span className="text-xs text-slate-500 font-medium">Préréglages :</span>
            {presets.map((p) => (
              <button
                key={p.path}
                type="button"
                onClick={() => {
                  setFilePathInput(p.path);
                  runAstAnalysis(p.path);
                }}
                className={`text-xs px-3 py-1 rounded-lg border transition-colors ${
                  filePathInput === p.path
                    ? 'bg-[#1E2761] text-white border-[#1E2761]'
                    : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                }`}
              >
                {p.label}
              </button>
            ))}
          </div>
        </form>
      </div>

      {/* Error Banner */}
      {astOutcome?.erreurTechnique && (
        <AlertBanner
          type="error"
          title="Erreur d'Analyse AST"
          message={astOutcome.messageErreur || 'Impossible d\'analyser le fichier spécifié.'}
        />
      )}

      {/* Results Collapsible Cards */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <ListFilter className="w-4 h-4 text-[#00A896]" />
            <h3 className="font-serif text-base font-bold text-[#1E2761]">
              Unités Testables Détectées ({unites.length})
            </h3>
          </div>
          <span className="text-xs text-slate-500 font-mono">
            {astOutcome?.resultat?.fichier || currentFilePath}
          </span>
        </div>

        {unites.length === 0 ? (
          <div className="bg-white p-8 text-center rounded-2xl border border-slate-200 text-slate-500 text-xs">
            Aucune unité testable détectée. Lancez l'analyse AST pour afficher les fonctions et routes.
          </div>
        ) : (
          <div className="space-y-4">
            {unites.map((unite, idx) => {
              const isExpanded = expandedCards[unite.nom] ?? (idx === 0);
              const isRoute = unite.type === 'route';

              return (
                <div
                  key={unite.nom + idx}
                  className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden transition-all hover:border-[#00A896]/50"
                >
                  {/* Card Header */}
                  <div
                    onClick={() => toggleCard(unite.nom)}
                    className="p-5 flex items-center justify-between cursor-pointer hover:bg-slate-50/80 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className={`p-2.5 rounded-xl ${isRoute ? 'bg-indigo-50 text-[#1E2761]' : 'bg-teal-50 text-[#00A896]'}`}>
                        <Code2 className="w-5 h-5" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <h4 className="font-bold text-sm text-[#1E2761]">{unite.nom}</h4>
                          <Badge variant={isRoute ? 'http' : 'info'}>
                            {isRoute ? `ROUTE HTTP ${unite.methode || ''}` : 'FONCTION UTILITAIRE'}
                          </Badge>
                        </div>
                        <p className="text-xs text-slate-500 mt-1 font-mono">
                          Lignes {unite.ligneDebut} à {unite.ligneFin} • Paramètres : {unite.parametres?.join(', ') || 'aucun'}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-4">
                      <div className="flex items-center gap-3 text-xs text-slate-600 font-medium">
                        <span className="flex items-center gap-1">
                          <GitBranch className="w-3.5 h-3.5 text-[#028090]" />
                          {unite.branches?.length || 0} branches
                        </span>
                        <span className="flex items-center gap-1">
                          <AlertCircle className="w-3.5 h-3.5 text-[#E45858]" />
                          {unite.casErreur?.length || 0} cas erreur
                        </span>
                      </div>
                      <button className="p-1 rounded-lg hover:bg-slate-200/60 text-slate-500">
                        {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                      </button>
                    </div>
                  </div>

                  {/* Card Body Details */}
                  {isExpanded && (
                    <div className="px-5 pb-5 pt-2 border-t border-slate-100 bg-[#F4F6FB]/50 space-y-4 text-xs">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {/* Branches Section */}
                        <div className="bg-white p-4 rounded-xl border border-slate-200">
                          <div className="flex items-center gap-2 mb-2 font-semibold text-[#1E2761]">
                            <GitBranch className="w-4 h-4 text-[#00A896]" />
                            <span>Branches Conditionnelles ({unite.branches?.length || 0})</span>
                          </div>
                          {!unite.branches || unite.branches.length === 0 ? (
                            <p className="text-slate-400 italic">Aucune branche conditionnelle détectée.</p>
                          ) : (
                            <ul className="space-y-1.5 font-mono">
                              {unite.branches.map((b, i) => (
                                <li key={i} className="flex items-center justify-between text-slate-700 bg-slate-50 px-2.5 py-1.5 rounded-lg border border-slate-100">
                                  <span>Structure <strong className="text-[#1E2761]">{b.type}</strong> (Ligne {b.ligne})</span>
                                  {b.aUnElse !== null && (
                                    <span className="text-[10px] text-slate-500">{b.aUnElse ? 'Avec else' : 'Sans else'}</span>
                                  )}
                                  {b.nombreCas !== null && (
                                    <span className="text-[10px] text-[#00A896] font-semibold">{b.nombreCas} cas switch</span>
                                  )}
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>

                        {/* Error Cases Section */}
                        <div className="bg-white p-4 rounded-xl border border-slate-200">
                          <div className="flex items-center gap-2 mb-2 font-semibold text-[#1E2761]">
                            <AlertCircle className="w-4 h-4 text-[#E45858]" />
                            <span>Cas d'Erreur & Exceptions ({unite.casErreur?.length || 0})</span>
                          </div>
                          {!unite.casErreur || unite.casErreur.length === 0 ? (
                            <p className="text-slate-400 italic">Aucun cas d'erreur explicite détecté.</p>
                          ) : (
                            <ul className="space-y-1.5 font-mono">
                              {unite.casErreur.map((c, i) => (
                                <li key={i} className="flex items-center justify-between text-slate-700 bg-slate-50 px-2.5 py-1.5 rounded-lg border border-slate-100">
                                  <span>Type: <strong className="text-[#1E2761]">{c.type}</strong> (Ligne {c.ligne})</span>
                                  {c.codeHttp && (
                                    <Badge codeHttp={c.codeHttp}>
                                      HTTP {c.codeHttp}
                                    </Badge>
                                  )}
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                      </div>

                      {/* Action Button to Step 2 */}
                      <div className="flex justify-end pt-2">
                        <button
                          onClick={() => setActiveTab('generation')}
                          className="px-4 py-2 bg-[#00A896] hover:bg-[#009081] text-white font-semibold rounded-xl text-xs transition-colors flex items-center gap-2 shadow-md"
                        >
                          <span>Générer un Test pour cette Unité</span>
                          <ArrowRight className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
