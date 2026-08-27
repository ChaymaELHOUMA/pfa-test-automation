import React from 'react';
import { useApp } from '../context/AppContext';
import { Badge } from '../components/common/Badge';
import {
  FileText,
  Download,
  Printer,
  CheckCircle2,
  TrendingUp,
  ShieldCheck,
  Zap,
  BarChart3,
  Calendar
} from 'lucide-react';

export const ReportView: React.FC = () => {
  const { executionHistory, astOutcome } = useApp();

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadJson = () => {
    const reportData = {
      date: new Date().toISOString(),
      projet: 'Projet de Référence PFA',
      unitesAnalysees: astOutcome?.resultat?.unites?.length || 6,
      historique: executionHistory,
      couverture: { lines: 40.81, branches: 28.20 },
      mutation: { scorePct: 21.75, killed: 78, survived: 59, total: 377 }
    };
    const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rapport-samba-pfa-${Date.now()}.json`;
    a.click();
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
            Étape 05 — Synthèse Global & Export
          </span>
          <h2 className="font-serif text-2xl font-bold">Rapport d'Orchestration des Tests</h2>
          <p className="text-sm text-slate-300 mt-1 max-w-2xl">
            Document de synthèse récapitulant les résultats d'analyse, de génération LLM, d'exécution et d'assurance qualité.
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <button
            onClick={handleDownloadJson}
            className="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs rounded-xl transition-all flex items-center gap-2 border border-slate-600"
          >
            <Download className="w-4 h-4" />
            <span>Exporter JSON</span>
          </button>
          <button
            onClick={handlePrint}
            className="px-4 py-2.5 bg-[#00A896] hover:bg-[#009081] text-white font-semibold text-xs rounded-xl shadow-lg transition-all flex items-center gap-2"
          >
            <Printer className="w-4 h-4" />
            <span>Imprimer / PDF</span>
          </button>
        </div>
      </div>

      {/* Printable Report Document */}
      <div className="bg-white rounded-2xl border border-[#E2E8F0] shadow-sm p-8 space-y-8 print:shadow-none print:border-none">
        {/* Document Header */}
        <div className="flex items-start justify-between border-b pb-6">
          <div>
            <h1 className="font-serif text-2xl font-bold text-[#1E2761]">
              Rapport d'Orchestration et d'Assurance Qualité
            </h1>
            <p className="text-xs text-slate-500 mt-1">
              Génération automatique de tests JS guidée par AST, nyc/Istanbul et Stryker.
            </p>
          </div>
          <div className="text-right text-xs font-mono text-slate-500">
            <p>Date : {new Date().toLocaleDateString('fr-FR')}</p>
            <p>Version : 1.0.0 (Spring Boot & React)</p>
          </div>
        </div>

        {/* Executive Summary Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-[#F4F6FB] border border-slate-200">
            <span className="text-xs text-slate-500 font-semibold uppercase">Unités AST</span>
            <p className="text-2xl font-bold text-[#1E2761] mt-1">
              {astOutcome?.resultat?.unites?.length || 6}
            </p>
            <span className="text-[11px] text-slate-500">Routes & Fonctions</span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F6FB] border border-slate-200">
            <span className="text-xs text-slate-500 font-semibold uppercase">Taux Succès Tests</span>
            <p className="text-2xl font-bold text-[#00A896] mt-1">90.5%</p>
            <span className="text-[11px] text-slate-500">Suite Consolidée Jest</span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F6FB] border border-slate-200">
            <span className="text-xs text-slate-500 font-semibold uppercase">Couverture Lignes</span>
            <p className="text-2xl font-bold text-[#028090] mt-1">40.8%</p>
            <span className="text-[11px] text-slate-500">Instrumenté par nyc</span>
          </div>

          <div className="p-4 rounded-xl bg-[#F4F6FB] border border-slate-200">
            <span className="text-xs text-slate-500 font-semibold uppercase">Score Mutation</span>
            <p className="text-2xl font-bold text-[#1E2761] mt-1">21.8%</p>
            <span className="text-[11px] text-slate-500">Campagne Stryker</span>
          </div>
        </div>

        {/* Stage Summaries */}
        <div className="space-y-6">
          <h3 className="font-serif font-bold text-lg text-[#1E2761] border-b pb-2">
            Synthèse par Étape du Pipeline
          </h3>

          <div className="space-y-4 text-xs leading-relaxed text-slate-700">
            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/60">
              <h4 className="font-bold text-sm text-[#1E2761] flex items-center gap-2 mb-1">
                <CheckCircle2 className="w-4 h-4 text-[#00A896]" />
                <span>Étape 1 : Analyse AST & Extraction de Métadonnées</span>
              </h4>
              <p>
                L'analyseur Acorn/Acorn-Walk a parcouru le code source backend Express. 6 unités testables principales ont été identifiées avec leurs branches conditionnelles (`if`/`switch`) et codes HTTP de réponse d'erreur (400, 404, 409, 500).
              </p>
            </div>

            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/60">
              <h4 className="font-bold text-sm text-[#1E2761] flex items-center gap-2 mb-1">
                <Zap className="w-4 h-4 text-[#00A896]" />
                <span>Étape 2 : Construction de Prompts & Génération LLM Ciblée</span>
              </h4>
              <p>
                La stratégie de prompt "Structuré" incorpore les branches et cas d'erreur de l'AST pour guider la génération par le LLM. Les prompts générés produisent 2 fois plus de cas de tests ciblés que le mode brut.
              </p>
            </div>

            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/60">
              <h4 className="font-bold text-sm text-[#1E2761] flex items-center gap-2 mb-1">
                <BarChart3 className="w-4 h-4 text-[#00A896]" />
                <span>Étape 3 & 4 : Consolidation Suite & Assurance Qualité</span>
              </h4>
              <p>
                La suite de 21 tests de démonstration obtient 90.5% de taux de réussite. L'analyse de couverture nyc révèle 40.81% de lignes couvertes et Stryker affiche un score de mutation de 21.75% (58.16% sur le code couvert).
              </p>
            </div>
          </div>
        </div>

        {/* History Table */}
        <div>
          <h3 className="font-serif font-bold text-lg text-[#1E2761] mb-3">
            Historique Comparatif des Exécutions
          </h3>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-[#F4F6FB] text-slate-600 font-semibold border-b border-slate-200">
                  <th className="py-2.5 px-3">Date</th>
                  <th className="py-2.5 px-3">Unité Cible</th>
                  <th className="py-2.5 px-3">Statut</th>
                  <th className="py-2.5 px-3 text-center">Tests Réussis</th>
                  <th className="py-2.5 px-3 text-center">Couverture %</th>
                  <th className="py-2.5 px-3 text-center">Mutation %</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono">
                {executionHistory.map((item) => (
                  <tr key={item.id}>
                    <td className="py-2.5 px-3 text-slate-500">{item.date}</td>
                    <td className="py-2.5 px-3 font-semibold text-[#1E2761]">{item.nom}</td>
                    <td className="py-2.5 px-3">
                      <Badge variant={item.statut === 'PASS' ? 'success' : 'warning'}>{item.statut}</Badge>
                    </td>
                    <td className="py-2.5 px-3 text-center">{item.testsReussis} / {item.totalTests}</td>
                    <td className="py-2.5 px-3 text-center">{item.couverturePct}%</td>
                    <td className="py-2.5 px-3 text-center">{item.scoreMutationPct}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
