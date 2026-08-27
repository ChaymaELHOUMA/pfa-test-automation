import React, { createContext, useContext, useState, useEffect } from 'react';
import { AstAnalysisOutcome, PipelineResult } from '../types/api';
import { apiService, checkBackendHealth, setForceMockMode, isForceMockMode } from '../services/api';
import { MOCK_AST_OUTCOME } from '../services/mockData';

export interface ExecutionRecord {
  id: string;
  date: string;
  type: string;
  nom: string;
  statut: 'PASS' | 'FAIL' | 'WARN';
  totalTests: number;
  testsReussis: number;
  testsEchoues: number;
  couverturePct: number;
  scoreMutationPct: number;
}

interface AppContextType {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  astOutcome: AstAnalysisOutcome | null;
  setAstOutcome: (outcome: AstAnalysisOutcome | null) => void;
  currentFilePath: string;
  setCurrentFilePath: (path: string) => void;
  isBackendLive: boolean;
  isDemoMode: boolean;
  setDemoMode: (demo: boolean) => void;
  executionHistory: ExecutionRecord[];
  addExecutionRecord: (record: Omit<ExecutionRecord, 'id' | 'date'>) => void;
  lastGeneratedTestCode: string | null;
  setLastGeneratedTestCode: (code: string | null) => void;
  lastPipelineResult: PipelineResult | null;
  setLastPipelineResult: (result: PipelineResult | null) => void;
  loadingAst: boolean;
  runAstAnalysis: (filePath: string) => Promise<void>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [astOutcome, setAstOutcome] = useState<AstAnalysisOutcome | null>(MOCK_AST_OUTCOME);
  const [currentFilePath, setCurrentFilePath] = useState<string>('projet-reference/routes-utilisateurs.js');
  const [isBackendLive, setIsBackendLive] = useState<boolean>(false);
  const [isDemoMode, setIsDemoModeState] = useState<boolean>(false);
  const [lastGeneratedTestCode, setLastGeneratedTestCode] = useState<string | null>(null);
  const [lastPipelineResult, setLastPipelineResult] = useState<PipelineResult | null>(null);
  const [loadingAst, setLoadingAst] = useState<boolean>(false);

  const [executionHistory, setExecutionHistory] = useState<ExecutionRecord[]>([
    {
      id: 'exec-1',
      date: new Date(Date.now() - 3600000 * 24).toLocaleString('fr-FR'),
      type: 'API',
      nom: 'POST /users',
      statut: 'PASS',
      totalTests: 9,
      testsReussis: 9,
      testsEchoues: 0,
      couverturePct: 40.81,
      scoreMutationPct: 21.75
    },
    {
      id: 'exec-2',
      date: new Date(Date.now() - 3600000 * 48).toLocaleString('fr-FR'),
      type: 'UNITAIRE',
      nom: 'validerEmail',
      statut: 'WARN',
      totalTests: 4,
      testsReussis: 3,
      testsEchoues: 1,
      couverturePct: 35.2,
      scoreMutationPct: 18.5
    }
  ]);

  useEffect(() => {
    checkHealth();
    const interval = setInterval(checkHealth, 15000);
    return () => clearInterval(interval);
  }, []);

  const checkHealth = async () => {
    const live = await checkBackendHealth();
    setIsBackendLive(live);
  };

  const setDemoMode = (demo: boolean) => {
    setForceMockMode(demo);
    setIsDemoModeState(demo);
  };

  const runAstAnalysis = async (filePath: string) => {
    setLoadingAst(true);
    setCurrentFilePath(filePath);
    try {
      const outcome = await apiService.analyserAst(filePath);
      setAstOutcome(outcome);
    } catch {
      setAstOutcome(MOCK_AST_OUTCOME);
    } finally {
      setLoadingAst(false);
    }
  };

  const addExecutionRecord = (record: Omit<ExecutionRecord, 'id' | 'date'>) => {
    const newRecord: ExecutionRecord = {
      ...record,
      id: `exec-${Date.now()}`,
      date: new Date().toLocaleString('fr-FR')
    };
    setExecutionHistory(prev => [newRecord, ...prev]);
  };

  return (
    <AppContext.Provider value={{
      activeTab,
      setActiveTab,
      astOutcome,
      setAstOutcome,
      currentFilePath,
      setCurrentFilePath,
      isBackendLive,
      isDemoMode,
      setDemoMode,
      executionHistory,
      addExecutionRecord,
      lastGeneratedTestCode,
      setLastGeneratedTestCode,
      lastPipelineResult,
      setLastPipelineResult,
      loadingAst,
      runAstAnalysis
    }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
