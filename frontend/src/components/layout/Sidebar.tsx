import React from 'react';
import { useApp } from '../../context/AppContext';
import {
  LayoutDashboard,
  Search,
  Zap,
  TestTube,
  ShieldCheck,
  FileText,
  ChevronRight
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { activeTab, setActiveTab } = useApp();

  const menuItems = [
    { id: 'dashboard', label: 'Tableau de Bord', icon: LayoutDashboard, stepNum: null },
    { id: 'ast', label: '1. Analyse AST', icon: Search, stepNum: '01' },
    { id: 'generation', label: '2. Génération LLM', icon: Zap, stepNum: '02' },
    { id: 'suite', label: '3. Suite de Tests', icon: TestTube, stepNum: '03' },
    { id: 'coverage', label: '4. Couverture & Mutation', icon: ShieldCheck, stepNum: '04' },
    { id: 'report', label: '5. Rapport & Export', icon: FileText, stepNum: '05' },
  ];

  return (
    <aside className="w-64 bg-[#1E2761] text-white flex flex-col justify-between shrink-0 shadow-xl border-r border-[#00A896]/20">
      <div className="py-6 px-3">
        <div className="px-3 mb-6">
          <span className="text-[10px] font-bold uppercase tracking-wider text-[#00A896]">
            Pipeline Automatisé
          </span>
          <h2 className="font-serif text-sm font-semibold text-slate-200 mt-1">
            Flux d'Exécution IA
          </h2>
        </div>

        <nav className="space-y-1.5">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-medium transition-all group relative ${
                  isActive
                    ? 'bg-[#00A896] text-white font-semibold shadow-lg shadow-[#00A896]/30'
                    : 'text-slate-300 hover:bg-[#141A43] hover:text-white'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 transition-transform group-hover:scale-110 ${isActive ? 'text-white' : 'text-[#00A896]'}`} />
                  <span>{item.label}</span>
                </div>
                {item.stepNum && (
                  <span className={`text-[10px] px-1.5 py-0.5 rounded font-mono ${isActive ? 'bg-white/20 text-white' : 'bg-slate-800/80 text-slate-400'}`}>
                    {item.stepNum}
                  </span>
                )}
                {isActive && (
                  <div className="absolute left-0 top-2 bottom-2 w-1 bg-white rounded-r-full" />
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer Info Box */}
      <div className="p-4 m-3 rounded-xl bg-[#141A43] border border-slate-700/60 text-xs text-slate-300">
        <div className="flex items-center justify-between text-[11px] font-semibold text-[#00A896] mb-1">
          <span>Sprint Final PFA</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </div>
        <p className="text-[11px] text-slate-400 leading-snug">
          Génération LLM ciblée & Mesures instrumentées (nyc & Stryker).
        </p>
      </div>
    </aside>
  );
};
