import React, { useState } from 'react';
import { Copy, Check } from 'lucide-react';

interface CodeViewerProps {
  code: string;
  language?: string;
  maxHeight?: string;
}

export const CodeViewer: React.FC<CodeViewerProps> = ({ code, language = 'javascript', maxHeight = 'max-h-96' }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const lines = code.split('\n');

  return (
    <div className="relative bg-[#141A43] rounded-xl overflow-hidden border border-slate-700/60 shadow-md">
      <div className="bg-[#1E2761] px-4 py-2 flex items-center justify-between border-b border-slate-700/60">
        <span className="text-xs font-mono text-slate-300 uppercase tracking-wider">{language}</span>
        <button
          onClick={handleCopy}
          className="flex items-center gap-1 text-xs text-slate-300 hover:text-white bg-slate-800/60 hover:bg-slate-700 px-2.5 py-1 rounded transition-all"
        >
          {copied ? (
            <>
              <Check className="w-3.5 h-3.5 text-[#00A896]" />
              <span className="text-[#00A896]">Copié</span>
            </>
          ) : (
            <>
              <Copy className="w-3.5 h-3.5" />
              <span>Copier</span>
            </>
          )}
        </button>
      </div>

      <div className={`p-4 font-mono text-xs overflow-auto ${maxHeight} leading-relaxed`}>
        <table className="w-full border-collapse">
          <tbody>
            {lines.map((line, idx) => (
              <tr key={idx} className="hover:bg-slate-800/30">
                <td className="text-slate-500 text-right pr-4 select-none w-8 font-mono opacity-60">
                  {idx + 1}
                </td>
                <td className="text-slate-100 whitespace-pre">
                  {line}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
