import React, { useEffect, useRef } from 'react';
import { Terminal, CheckCircle2, Loader2 } from 'lucide-react';

interface LiveLogStreamProps {
  logs: string[];
  isRunning: boolean;
  title?: string;
}

export const LiveLogStream: React.FC<LiveLogStreamProps> = ({ logs, isRunning, title = 'Terminal d\'Exécution en Direct' }) => {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  return (
    <div className="bg-[#141A43] text-emerald-400 rounded-xl overflow-hidden shadow-lg border border-slate-700/50">
      <div className="bg-[#1E2761] px-4 py-2.5 flex items-center justify-between border-b border-slate-700/60">
        <div className="flex items-center gap-2">
          <Terminal className="w-4 h-4 text-[#00A896]" />
          <span className="text-xs font-semibold text-white uppercase tracking-wider">{title}</span>
        </div>
        <div className="flex items-center gap-2 text-xs">
          {isRunning ? (
            <>
              <Loader2 className="w-3.5 h-3.5 text-[#00A896] animate-spin" />
              <span className="text-[#00A896] font-medium">Traitement en cours...</span>
            </>
          ) : (
            <>
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
              <span className="text-slate-300">Terminé</span>
            </>
          )}
        </div>
      </div>
      <div className="p-4 font-mono text-xs max-h-56 overflow-y-auto space-y-1.5 leading-relaxed bg-[#141A43]">
        {logs.length === 0 ? (
          <p className="text-slate-500 italic">En attente de démarrage de l'opération...</p>
        ) : (
          logs.map((log, i) => (
            <div key={i} className="flex items-start gap-2">
              <span className="text-slate-500 select-none">{String(i + 1).padStart(2, '0')}</span>
              <span className={log.includes('ERROR') || log.includes('FAIL') ? 'text-[#E45858]' : log.includes('SUCCESS') || log.includes('PASS') ? 'text-[#00A896]' : 'text-slate-200'}>
                {log}
              </span>
            </div>
          ))
        )}
        <div ref={bottomRef} />
      </div>
    </div>
  );
};
