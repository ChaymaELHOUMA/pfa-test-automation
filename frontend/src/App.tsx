import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Sidebar } from './components/layout/Sidebar';
import { DashboardView } from './views/DashboardView';
import { UploadProjectView } from './views/UploadProjectView';
import { AstAnalysisView } from './views/AstAnalysisView';
import { GenerationView } from './views/GenerationView';
import { TestSuiteView } from './views/TestSuiteView';
import { CoverageMutationView } from './views/CoverageMutationView';
import { ReportView } from './views/ReportView';

const MainContent: React.FC = () => {
  const { activeTab } = useApp();

  return (
    <main className="flex-1 overflow-y-auto p-6 bg-[#F4F6FB] min-h-screen">
      <div className="max-w-7xl mx-auto space-y-6">
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'upload' && <UploadProjectView />}
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
      <div className="min-h-screen flex bg-[#F4F6FB] font-sans antialiased text-[#1E2761]">
        <Sidebar />
        <MainContent />
      </div>
    </AppProvider>
  );
}

export default App;
