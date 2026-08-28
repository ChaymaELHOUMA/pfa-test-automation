import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { getBaseUrl, setBaseUrl } from '../../services/api';
import { Activity, FolderCheck, Server, Settings } from 'lucide-react';

export const Header: React.FC = () => {
  const { isBackendLive, isDemoMode, setDemoMode, activeProject, setActiveTab } = useApp();
  const [showSettings, setShowSettings] = useState(false);
  const [urlInput, setUrlInput] = useState(getBaseUrl());

  const handleSaveUrl = () => {
    setBaseUrl(urlInput);
    setShowSettings(false);
  };

  return (
    <header className="bg-[#1E2761] text-white px-6 py-3 flex items-center justify-between shadow-lg sticky top-0 z-30 border-b border-[#00A896]/20">
      {/* Left side: Active Project Indicator */}
      <div className="flex items-center gap-4">
        <button
          onClick={() => setActiveTab('upload')}
          className="flex items-center gap-2.5 bg-[#141A43] hover:bg-[#1A2255] px-3.5 py-1.5 rounded-xl border border-[#00A896]/40 text-xs transition-colors cursor-pointer"
          title="Cliquez pour changer ou importer un projet"
        >
          <FolderCheck className="w-4 h-4 text-[#00A896]" />
          <div className="text-left">
            <div className="text-[10px] text-slate-400 leading-none">Projet Actif</div>
            <div className="font-bold text-slate-100 text-xs truncate max-w-[200px]">{activeProject.nomProjet}</div>
          </div>
        </button>
      </div>

      {/* Right side: Status and settings */}
      <div className="flex items-center gap-4">
        {/* Connection Status Badge */}
        <div className="flex items-center gap-2 bg-[#141A43] px-3.5 py-1.5 rounded-full border border-slate-700/60 text-xs">
          <span className="relative flex h-2.5 w-2.5">
            <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${isBackendLive && !isDemoMode ? 'bg-[#00A896]' : 'bg-[#F0A93C]'}`}></span>
            <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${isBackendLive && !isDemoMode ? 'bg-[#00A896]' : 'bg-[#F0A93C]'}`}></span>
          </span>
          <span className="font-medium text-slate-200">
            {isDemoMode ? 'Mode Démo (Mock)' : isBackendLive ? 'Backend Connecté (Live)' : 'Backend Hors Ligne (Auto-Mock)'}
          </span>
        </div>

        {/* Demo Mode Toggle */}
        <button
          onClick={() => setDemoMode(!isDemoMode)}
          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 border ${
            isDemoMode
              ? 'bg-[#00A896] text-white border-[#00A896] shadow-md'
              : 'bg-slate-800/80 text-slate-300 border-slate-700 hover:text-white'
          }`}
        >
          <Activity className="w-3.5 h-3.5" />
          <span>{isDemoMode ? 'Mode Démo Actif' : 'Activer Démo'}</span>
        </button>

        {/* Settings Toggle */}
        <button
          onClick={() => setShowSettings(!showSettings)}
          className="p-2 rounded-lg bg-slate-800/60 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
          title="Paramètres API"
        >
          <Settings className="w-4 h-4" />
        </button>

        {/* Settings Modal Popup */}
        {showSettings && (
          <div className="absolute right-6 top-16 w-80 bg-[#1E2761] text-white p-4 rounded-xl shadow-2xl border border-slate-700 z-50 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center gap-2 mb-3 pb-2 border-b border-slate-700">
              <Server className="w-4 h-4 text-[#00A896]" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">Configuration Backend REST</h3>
            </div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Base URL Spring Boot</label>
            <input
              type="text"
              value={urlInput}
              onChange={(e) => setUrlInput(e.target.value)}
              className="w-full px-3 py-2 bg-[#141A43] border border-slate-600 rounded-lg text-xs text-white focus:outline-none focus:border-[#00A896]"
              placeholder="http://localhost:8080"
            />
            <div className="mt-4 flex justify-end gap-2">
              <button
                onClick={() => setShowSettings(false)}
                className="px-3 py-1.5 text-xs text-slate-400 hover:text-white"
              >
                Annuler
              </button>
              <button
                onClick={handleSaveUrl}
                className="px-3 py-1.5 text-xs font-semibold bg-[#00A896] hover:bg-[#009081] text-white rounded-lg transition-colors"
              >
                Enregistrer
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
};
