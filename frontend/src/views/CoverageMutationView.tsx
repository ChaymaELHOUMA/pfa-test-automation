import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { apiService } from '../services/api';
import { CoverageResult, MutationResult } from '../types/api';
import { MOCK_SURVIVED_MUTANTS_LIST } from '../services/mockData';
import { Badge } from '../components/common/Badge';
import {
  Target,
  BarChart2,
  RefreshCw,
  AlertTriangle,
  ArrowRightLeft
} from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export const CoverageMutationView: React.FC = () => {
  const { activeProject } = useApp();
  const [activeSubTab, setActiveSubTab] = useState<'coverage' | 'mutation' | 'cross'>('coverage');

  const [coverageResult, setCoverageResult] = useState<CoverageResult | null>(null);
  const [loadingCoverage, setLoadingCoverage] = useState<boolean>(false);

  const [mutationResult, setMutationResult] = useState<MutationResult | null>(null);
  const [loadingMutation, setLoadingMutation] = useState<boolean>(false);

  useEffect(() => {
    loadData();
  }, [activeProject]);

  const loadData = async () => {
    setLoadingCoverage(true);
    setLoadingMutation(true);
    try {
      const [cov, mut] = await Promise.all([
        apiService.mesurerCouvertureProjet(activeProject.idProjet),
        apiService.obtenirMutationProjetReference(activeProject.idProjet)
      ]);
      setCoverageResult(cov);
      setMutationResult(mut);
    } catch {
      // Mock fallback
    } finally {
      setLoadingCoverage(false);
      setLoadingMutation(false);
    }
  };

  const linesPct = coverageResult?.lines?.pct || 40.81;
  const branchPct = coverageResult?.branches?.pct || 28.20;
  const mutScorePct = mutationResult?.metrics?.scoreMutationPct || 21.75;

  const crossData = [
    {
      name: activeProject.pointEntree,
      CouvertureLignes: linesPct,
      CouvertureBranches: branchPct,
      ScoreMutation: mutScorePct
    }
  ];

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
            Étape 04 — Assurance Qualité & Mutation Testing
          </span>
          <h2 className="font-serif text-2xl font-bold">Couverture & Mutation ({activeProject.nomProjet})</h2>
          <p className="text-sm text-slate-300 mt-1 max-w-3xl">
            Mesurez l'instrumentation avec Istanbul/Jest et la résistance aux mutations avec Stryker sur le workspace <strong>{activeProject.idProjet}</strong>.
          </p>
        </div>
        <button
          onClick={loadData}
          disabled={loadingCoverage || loadingMutation}
          className="px-4 py-2.5 bg-[#00A896] hover:bg-[#009081] text-white font-semibold text-xs rounded-xl shadow-lg transition-all flex items-center gap-2 shrink-0 disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${(loadingCoverage || loadingMutation) ? 'animate-spin' : ''}`} />
          <span>Actualiser Métriques</span>
        </button>
      </div>

      {/* Sub-Tabs Navigation */}
      <div className="flex border-b border-slate-200 gap-2">
        <button
          onClick={() => setActiveSubTab('coverage')}
          className={`pb-3 px-4 font-serif text-sm font-bold border-b-2 transition-all flex items-center gap-2 ${
            activeSubTab === 'coverage'
              ? 'border-[#00A896] text-[#00A896]'
              : 'border-transparent text-slate-500 hover:text-[#1E2761]'
          }`}
        >
          <BarChart2 className="w-4 h-4" />
          <span>Couverture (nyc / Istanbul)</span>
        </button>

        <button
          onClick={() => setActiveSubTab('mutation')}
          className={`pb-3 px-4 font-serif text-sm font-bold border-b-2 transition-all flex items-center gap-2 ${
            activeSubTab === 'mutation'
              ? 'border-[#00A896] text-[#00A896]'
              : 'border-transparent text-slate-500 hover:text-[#1E2761]'
          }`}
        >
          <Target className="w-4 h-4" />
          <span>Mutation Testing (Stryker)</span>
        </button>

        <button
          onClick={() => setActiveSubTab('cross')}
          className={`pb-3 px-4 font-serif text-sm font-bold border-b-2 transition-all flex items-center gap-2 ${
            activeSubTab === 'cross'
              ? 'border-[#00A896] text-[#00A896]'
              : 'border-transparent text-slate-500 hover:text-[#1E2761]'
          }`}
        >
          <ArrowRightLeft className="w-4 h-4" />
          <span>Vue Croisée (Couverture vs Mutation)</span>
        </button>
      </div>

      {/* TAB 1: COVERAGE NYC */}
      {activeSubTab === 'coverage' && (
        <div className="space-y-6">
          {/* Top 4 Metrics Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Lignes Couvertes (% Lines)</span>
              <p className="text-2xl font-bold text-[#00A896] mt-2">{linesPct.toFixed(1)}%</p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">
                {coverageResult?.lines?.covered || 60} / {coverageResult?.lines?.total || 147} lignes
              </p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Branches Couvertes (% Branch)</span>
              <p className="text-2xl font-bold text-[#028090] mt-2">{branchPct.toFixed(1)}%</p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">
                {coverageResult?.branches?.covered || 33} / {coverageResult?.branches?.total || 117} branches
              </p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Instructions (% Statements)</span>
              <p className="text-2xl font-bold text-[#1E2761] mt-2">
                {(coverageResult?.statements?.pct || 39.02).toFixed(1)}%
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">
                {coverageResult?.statements?.covered || 64} / {coverageResult?.statements?.total || 164} stmts
              </p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Fonctions (% Functions)</span>
              <p className="text-2xl font-bold text-slate-700 mt-2">
                {(coverageResult?.functions?.pct || 38.70).toFixed(1)}%
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">
                {coverageResult?.functions?.covered || 12} / {coverageResult?.functions?.total || 31} fonctions
              </p>
            </div>
          </div>

          {/* Files Breakdown Table */}
          <div className="bg-white rounded-2xl border border-[#E2E8F0] shadow-sm overflow-hidden p-6">
            <h3 className="font-serif font-bold text-base text-[#1E2761] mb-4">
              Couverture par Fichier ({activeProject.nomProjet})
            </h3>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="bg-[#F4F6FB] text-slate-600 font-semibold border-b border-slate-200">
                    <th className="py-3 px-4">Fichier Source</th>
                    <th className="py-3 px-4">% Lignes</th>
                    <th className="py-3 px-4">% Branches</th>
                    <th className="py-3 px-4">% Statements</th>
                    <th className="py-3 px-4">% Fonctions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-mono">
                  {Object.entries(coverageResult?.detailsParFichier || {}).map(([file, summary]) => (
                    <tr key={file} className="hover:bg-slate-50">
                      <td className="py-3.5 px-4 font-semibold text-[#1E2761]">{file}</td>
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-slate-200 h-2 rounded-full overflow-hidden">
                            <div className="bg-[#00A896] h-full" style={{ width: `${summary.lines.pct}%` }} />
                          </div>
                          <span className="text-[#00A896] font-bold">{summary.lines.pct.toFixed(1)}%</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-slate-200 h-2 rounded-full overflow-hidden">
                            <div className="bg-[#028090] h-full" style={{ width: `${summary.branches.pct}%` }} />
                          </div>
                          <span className="text-[#028090] font-bold">{summary.branches.pct.toFixed(1)}%</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">{summary.statements.pct.toFixed(1)}%</td>
                      <td className="py-3.5 px-4">{summary.functions.pct.toFixed(1)}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: MUTATION STRYKER */}
      {activeSubTab === 'mutation' && (
        <div className="space-y-6">
          {/* Top 5 Metrics Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Score Mutation</span>
              <p className="text-2xl font-bold text-[#1E2761] mt-2">{mutScorePct.toFixed(1)}%</p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">Stryker Mutator</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Mutants Tués</span>
              <p className="text-2xl font-bold text-[#00A896] mt-2">
                {mutationResult?.metrics?.killed || 78}
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">Détectés par les tests</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Timeouts</span>
              <p className="text-2xl font-bold text-amber-600 mt-2">
                {mutationResult?.metrics?.timedOut || 4}
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">Boucles interceptées</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Mutants Survivants</span>
              <p className="text-2xl font-bold text-[#E45858] mt-2">
                {mutationResult?.metrics?.survived || 59}
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">Non détectés par tests</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <span className="text-xs text-slate-500 font-medium">Non Couverts</span>
              <p className="text-2xl font-bold text-slate-400 mt-2">
                {mutationResult?.metrics?.noCoverage || 236}
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-1">Inatteints par la suite</p>
            </div>
          </div>

          {/* Survived Mutants Detail Section */}
          <div className="bg-white rounded-2xl border border-[#E2E8F0] shadow-sm overflow-hidden p-6 space-y-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-[#E45858]" />
              <div>
                <h3 className="font-serif font-bold text-base text-[#1E2761]">
                  Analyse des Mutants Survivants
                </h3>
                <p className="text-xs text-slate-500">
                  Ces mutants ont été exécutés et couverts par les tests, mais les assertions n'ont pas échoué.
                </p>
              </div>
            </div>

            <div className="space-y-3">
              {MOCK_SURVIVED_MUTANTS_LIST.map((m) => (
                <div key={m.id} className="p-4 rounded-xl bg-rose-50/40 border border-rose-200/80 space-y-2 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-bold font-mono text-[#1E2761]">
                      [Mutant #{m.id}] {m.fichier} (Ligne {m.ligne})
                    </span>
                    <Badge variant="error">Mutateur: {m.mutateur}</Badge>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-2 font-mono text-[11px]">
                    <div className="bg-white p-2 rounded border border-slate-200">
                      <span className="text-slate-400 block text-[10px]">Code Original :</span>
                      <code className="text-slate-800">{m.codeOriginal}</code>
                    </div>
                    <div className="bg-rose-100/60 p-2 rounded border border-rose-200">
                      <span className="text-rose-600 block text-[10px]">Remplacement Injecté :</span>
                      <code className="text-rose-900 font-bold">{m.codeMute}</code>
                    </div>
                  </div>
                  <p className="text-slate-600 italic leading-relaxed pt-1">
                    💡 <strong>Pourquoi a-t-il survécu ?</strong> {m.explicationSurvie}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* TAB 3: CROSS VIEW */}
      {activeSubTab === 'cross' && (
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm space-y-4">
            <h3 className="font-serif font-bold text-base text-[#1E2761]">
              Vue Croisée : Couverture Classique vs Score de Mutation Real
            </h3>
            <p className="text-xs text-slate-500">
              Mise en regard des taux de couverture de lignes/branches (nyc) et de la détection réelle des fautes (Stryker).
            </p>

            <div className="w-full h-72 pt-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={crossData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                  <YAxis unit="%" tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Legend wrapperStyle={{ fontSize: 11 }} />
                  <Bar dataKey="CouvertureLignes" name="% Lignes Covered (nyc)" fill="#00A896" />
                  <Bar dataKey="CouvertureBranches" name="% Branches Covered (nyc)" fill="#028090" />
                  <Bar dataKey="ScoreMutation" name="% Score Mutation (Stryker)" fill="#1E2761" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
