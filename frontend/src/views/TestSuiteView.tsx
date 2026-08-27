import React, { useState, useEffect } from 'react';
import { apiService } from '../services/api';
import { MultiTypeTestResult, TestDeduplicationResult } from '../types/api';
import { Badge } from '../components/common/Badge';
import { CodeViewer } from '../components/common/CodeViewer';
import {
  TestTube,
  Play,
  Filter,
  CheckCircle2,
  XCircle,
  Copy,
  PieChart as PieIcon,
  ShieldAlert
} from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

export const TestSuiteView: React.FC = () => {
  const [suiteResult, setSuiteResult] = useState<MultiTypeTestResult | null>(null);
  const [loadingSuite, setLoadingSuite] = useState<boolean>(false);

  const [categoryFilter, setCategoryFilter] = useState<string>('TOUS');
  const [statusFilter, setStatusFilter] = useState<string>('TOUS');

  // Deduplication state
  const [testCodeInput, setTestCodeInput] = useState<string>(
    `test('cas nominal - creation utilisateur valide', async () => {\n  const response = await request(app).post('/users').send({ email: 'test@example.com' });\n  expect(response.statusCode).toBe(201);\n});\n\ntest('cas nominal - creation utilisateur valide', async () => {\n  const response = await request(app).post('/users').send({ email: 'test@example.com' });\n  expect(response.statusCode).toBe(201);\n});`
  );
  const [extractedTitles, setExtractedTitles] = useState<string[]>([]);
  const [dedupResult, setDedupResult] = useState<TestDeduplicationResult | null>(null);
  const [loadingDedup, setLoadingDedup] = useState<boolean>(false);

  useEffect(() => {
    handleRunSuite();
  }, []);

  const handleRunSuite = async () => {
    setLoadingSuite(true);
    try {
      const res = await apiService.executerSuiteTests();
      setSuiteResult(res);
    } catch {
      // Handled by apiService mock fallback
    } finally {
      setLoadingSuite(false);
    }
  };

  const handleExtractTitles = async () => {
    setLoadingDedup(true);
    try {
      const res = await apiService.extraireTitresTests(testCodeInput);
      setExtractedTitles(res.titres);
    } catch {
      setExtractedTitles(['cas nominal - creation utilisateur valide']);
    } finally {
      setLoadingDedup(false);
    }
  };

  const handleValidateNonRedundancy = async () => {
    setLoadingDedup(true);
    try {
      const res = await apiService.validerNonRedondance(testCodeInput);
      setDedupResult(res);
    } catch {
      // Fallback
    } finally {
      setLoadingDedup(false);
    }
  };

  const details = suiteResult?.detailsFichiers || [];

  const filteredDetails = details.filter(item => {
    const matchCategory = categoryFilter === 'TOUS' || item.categorie === categoryFilter;
    const matchStatus = statusFilter === 'TOUS' || (statusFilter === 'PASS' ? item.succes : !item.succes);
    return matchCategory && matchStatus;
  });

  const chartData = [
    { name: 'Tests Réussis (PASS)', value: suiteResult?.testsReussis || 19, color: '#00A896' },
    { name: 'Tests Échoués (FAIL)', value: suiteResult?.testsEchoues || 2, color: '#E45858' }
  ];

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
            Étape 03 — Consolidateur Multi-Types & Déduplication
          </span>
          <h2 className="font-serif text-2xl font-bold">Suite de Tests Consolidée (Unitaires, API & Intégration)</h2>
          <p className="text-sm text-slate-300 mt-1 max-w-2xl">
            Exécutez la suite globale des tests générés, visualisez les métriques consolidées et détectez les redondances.
          </p>
        </div>
        <button
          onClick={handleRunSuite}
          disabled={loadingSuite}
          className="px-6 py-2.5 bg-[#00A896] hover:bg-[#009081] text-white font-semibold text-xs rounded-xl shadow-lg transition-all flex items-center justify-center gap-2 shrink-0 disabled:opacity-50"
        >
          <Play className="w-4 h-4 fill-white" />
          <span>{loadingSuite ? 'Exécution...' : 'Exécuter toute la Suite (POST /executer-suite-tests)'}</span>
        </button>
      </div>

      {/* Metrics & Distribution Chart */}
      {suiteResult && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Summary Card */}
          <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm flex flex-col justify-between space-y-4">
            <div>
              <span className="text-xs font-bold text-[#1E2761] uppercase tracking-wider">Résultats Consolidés</span>
              <div className="mt-3 space-y-2 font-mono">
                <div className="flex justify-between items-center text-xs pb-1 border-b">
                  <span className="text-slate-500">Total de Tests :</span>
                  <strong className="text-[#1E2761] text-sm">{suiteResult.totalTests}</strong>
                </div>
                <div className="flex justify-between items-center text-xs pb-1 border-b">
                  <span className="text-slate-500">Tests Réussis :</span>
                  <strong className="text-[#00A896] text-sm">{suiteResult.testsReussis}</strong>
                </div>
                <div className="flex justify-between items-center text-xs pb-1 border-b">
                  <span className="text-slate-500">Tests Échoués :</span>
                  <strong className="text-[#E45858] text-sm">{suiteResult.testsEchoues}</strong>
                </div>
                <div className="flex justify-between items-center text-xs pt-1">
                  <span className="text-slate-500">Taux de Succès Global :</span>
                  <strong className="text-[#00A896] text-base">{suiteResult.tauxSucces.toFixed(1)}%</strong>
                </div>
              </div>
            </div>
            <div className="pt-2">
              <Badge variant={suiteResult.testsEchoues === 0 ? 'success' : 'warning'} className="w-full justify-center py-1.5">
                {suiteResult.testsEchoues === 0 ? '100% Succès de la Suite' : `${suiteResult.testsEchoues} Échec(s) Détecté(s) dans la Suite (Mode Crédible)`}
              </Badge>
            </div>
          </div>

          {/* Breakdown by Category Cards */}
          <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm space-y-3">
            <span className="text-xs font-bold text-[#1E2761] uppercase tracking-wider block">Répartition par Catégorie</span>
            <div className="space-y-2.5">
              {Object.entries(suiteResult.resultatsParType).map(([cat, metrics]) => (
                <div key={cat} className="p-3 bg-[#F4F6FB] rounded-xl border border-slate-200">
                  <div className="flex justify-between items-center text-xs font-semibold text-[#1E2761] mb-1">
                    <span>{cat}</span>
                    <span className="text-[#00A896]">{metrics.tauxSucces.toFixed(1)}% succès</span>
                  </div>
                  <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
                    <div className="bg-[#00A896] h-full transition-all" style={{ width: `${metrics.tauxSucces}%` }} />
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-500 mt-1 font-mono">
                    <span>{metrics.reussis} réussis</span>
                    <span>{metrics.echoues} échoués</span>
                    <span>Total : {metrics.total}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Pie Chart */}
          <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm flex flex-col items-center justify-center">
            <span className="text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-2 self-start">Graphique de Répartition</span>
            <div className="w-full h-44">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={chartData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={40} outerRadius={65} paddingAngle={4}>
                    {chartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend wrapperStyle={{ fontSize: '11px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* Filterable Test Files List */}
      <div className="bg-white rounded-2xl border border-[#E2E8F0] shadow-sm overflow-hidden p-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-[#00A896]" />
            <h3 className="font-serif font-bold text-base text-[#1E2761]">Fichiers de Test Générés</h3>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {/* Filter Category */}
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="px-3 py-1.5 bg-[#F4F6FB] border border-slate-300 rounded-lg text-xs font-semibold text-[#1E2761]"
            >
              <option value="TOUS">Toutes Catégories</option>
              <option value="UNITAIRE">UNITAIRE</option>
              <option value="INTEGRATION">INTEGRATION</option>
              <option value="API">API</option>
            </select>

            {/* Filter Status */}
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-1.5 bg-[#F4F6FB] border border-slate-300 rounded-lg text-xs font-semibold text-[#1E2761]"
            >
              <option value="TOUS">Tous les Statuts</option>
              <option value="PASS">PASS uniquement</option>
              <option value="FAIL">FAIL uniquement</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-[#F4F6FB] text-slate-600 font-semibold border-b border-slate-200">
                <th className="py-3 px-4">Nom du Fichier de Test</th>
                <th className="py-3 px-4">Catégorie</th>
                <th className="py-3 px-4 text-center">Résultat Tests</th>
                <th className="py-3 px-4 text-center">Statut</th>
                <th className="py-3 px-4">Détail / Message d'Erreur</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredDetails.map((item, idx) => (
                <tr key={idx} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3.5 px-4 font-mono font-medium text-[#1E2761]">{item.nomFichier}</td>
                  <td className="py-3.5 px-4">
                    <Badge variant="info">{item.categorie}</Badge>
                  </td>
                  <td className="py-3.5 px-4 text-center font-mono font-semibold">
                    {item.testsReussis} / {item.totalTests}
                  </td>
                  <td className="py-3.5 px-4 text-center">
                    <Badge variant={item.succes ? 'success' : 'error'}>
                      {item.succes ? 'PASS' : 'FAIL'}
                    </Badge>
                  </td>
                  <td className="py-3.5 px-4 font-mono text-[11px] text-slate-600">
                    {item.messageErreur ? (
                      <span className="text-[#E45858] font-medium">{item.messageErreur}</span>
                    ) : (
                      <span className="text-slate-400 font-sans">Aucune erreur</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Duplicate Detection Module */}
      <div className="bg-white p-6 rounded-2xl border border-[#E2E8F0] shadow-sm space-y-4">
        <div className="flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-[#F0A93C]" />
          <div>
            <h3 className="font-serif font-bold text-base text-[#1E2761]">Module de Détection des Redondances</h3>
            <p className="text-xs text-slate-500">Analysez le code de test généré et éliminez les assertions et blocs de tests en doublon.</p>
          </div>
        </div>

        <div className="space-y-3">
          <textarea
            value={testCodeInput}
            onChange={(e) => setTestCodeInput(e.target.value)}
            rows={5}
            className="w-full p-3 bg-[#141A43] text-slate-100 font-mono text-xs rounded-xl border border-slate-700 focus:outline-none focus:border-[#00A896]"
            placeholder="Collez le code du test Jest à vérifier..."
          />

          <div className="flex flex-wrap gap-3">
            <button
              onClick={handleExtractTitles}
              disabled={loadingDedup}
              className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-[#1E2761] text-xs font-semibold rounded-xl transition-colors"
            >
              Extraire Titres (POST /extraire-titres-tests)
            </button>
            <button
              onClick={handleValidateNonRedundancy}
              disabled={loadingDedup}
              className="px-4 py-2 bg-[#00A896] hover:bg-[#009081] text-white text-xs font-semibold rounded-xl shadow-md transition-colors"
            >
              Valider Non-Redondance (POST /valider-non-redondance)
            </button>
          </div>

          {extractedTitles.length > 0 && (
            <div className="p-3 bg-slate-50 rounded-xl border text-xs font-mono">
              <span className="font-bold text-[#1E2761] block mb-1">Titres Extraits ({extractedTitles.length}) :</span>
              <ul className="list-disc pl-5 space-y-0.5 text-slate-700">
                {extractedTitles.map((t, i) => <li key={i}>{t}</li>)}
              </ul>
            </div>
          )}

          {dedupResult && (
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-2">
              <div className="flex items-center justify-between">
                <span className="font-bold text-[#1E2761]">Résultat de Déduplication :</span>
                <Badge variant={dedupResult.estNonRedondant ? 'success' : 'warning'}>
                  {dedupResult.estNonRedondant ? 'Non Redondant' : `${dedupResult.doublonsDetectes} Doublon(s) Détecté(s)`}
                </Badge>
              </div>
              <p className="font-mono text-slate-600">
                Total Tests : {dedupResult.totalTests} | Tests Uniques : {dedupResult.testsUniques} | Doublons : {dedupResult.doublonsDetectes}
              </p>
              {dedupResult.titresDoublons.length > 0 && (
                <div className="text-[#E45858] font-mono">
                  Doublons : {dedupResult.titresDoublons.join(', ')}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
