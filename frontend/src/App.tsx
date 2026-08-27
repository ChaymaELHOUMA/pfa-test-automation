import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Header } from './components/layout/Header';
import { Sidebar } from './components/layout/Sidebar';
import { DashboardView } from './views/DashboardView';
import { AstAnalysisView } from './views/AstAnalysisView';
import { GenerationView } from './views/GenerationView';
import { TestSuiteView } from './views/TestSuiteView';
import { CoverageMutationView } from './views/CoverageMutationView';
import { ReportView } from './views/ReportView';

const MainContent: React.FC = () => {
  const { activeTab } = useApp();

  return (
    <main className="flex-1 overflow-y-auto p-6 bg-[#F4F6FB] min-h-[calc(100vh-73px)]">
      <div className="max-w-7xl mx-auto space-y-6">
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'ast' && <AstAnalysisView />}
        {activeTab === 'generation' && <GenerationView />}
        {activeTab === 'suite' && <TestSuiteView />}
        {activeTab === 'coverage' && <CoverageMutationView />}
        {activeTab === 'report' && <ReportView />}
      </div>
    </main>
  );
};

export function App() {
  return (
    <AppProvider>
      <div className="min-h-screen flex flex-col bg-[#F4F6FB] font-sans antialiased text-slate-800">
        <Header />
        <div className="flex-1 flex overflow-hidden">
          <Sidebar />
          <MainContent />
        </div>
      </div>
    </AppProvider>
  );
}

export default App;
