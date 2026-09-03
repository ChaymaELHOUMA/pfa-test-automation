import React from 'react';
import { useApp } from '../../context/AppContext';
import {
  LayoutDashboard,
  Upload,
  Search,
  Zap,
  TestTube,
  ShieldCheck,
  FileText,
  FolderCheck
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { activeTab, setActiveTab, activeProject } = useApp();

  const menuItems = [
    { id: 'dashboard', label: 'Tableau de Bord', icon: LayoutDashboard, stepNum: null },
    { id: 'upload', label: 'Importer un Projet', icon: Upload, stepNum: 'NEW' },
    { id: 'ast', label: '1. Analyse AST', icon: Search, stepNum: '01' },
    { id: 'generation', label: '2. Génération LLM', icon: Zap, stepNum: '02' },
    { id: 'suite', label: '3. Suite de Tests', icon: TestTube, stepNum: '03' },
    { id: 'coverage', label: '4. Couverture & Mutation', icon: ShieldCheck, stepNum: '04' },
    { id: 'report', label: '5. Rapport & Export', icon: FileText, stepNum: '05' },
  ];

  return (
    <aside className="w-64 bg-[#1E2761] text-white flex flex-col justify-between shrink-0 shadow-xl border-r border-[#00A896]/20">
      <div className="py-6 px-3">
        {/* Active Project Widget in Sidebar */}
        <div className="px-3 mb-6">
          <button
            onClick={() => setActiveTab('upload')}
            className="w-full text-left bg-[#141A43] hover:bg-[#1A2255] p-3 rounded-xl border border-[#00A896]/40 transition-colors cursor-pointer block"
            title="Changer de projet"
          >
            <div className="flex items-center gap-2 mb-1">
              <FolderCheck className="w-4 h-4 text-[#00A896]" />
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#00A896]">Projet Actif</span>
            </div>
            <div className="font-bold text-slate-100 text-xs truncate">{activeProject.nomProjet}</div>
            <div className="text-[10px] text-slate-400 font-mono mt-0.5 truncate">{activeProject.pointEntree}</div>
          </button>
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
    </aside>
  );
};
