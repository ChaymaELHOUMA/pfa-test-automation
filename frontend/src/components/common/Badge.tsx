import React from 'react';

interface BadgeProps {
  variant?: 'success' | 'error' | 'warning' | 'info' | 'http';
  codeHttp?: number | null;
  children: React.ReactNode;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ variant = 'info', codeHttp, children, className = '' }) => {
  if (codeHttp !== undefined && codeHttp !== null) {
    let colorClass = 'bg-slate-100 text-slate-700 border-slate-300';
    if (codeHttp >= 200 && codeHttp < 300) colorClass = 'bg-[#00A896]/10 text-[#00A896] border-[#00A896]/30';
    if (codeHttp >= 400 && codeHttp < 500) colorClass = 'bg-[#F0A93C]/10 text-[#F0A93C] border-[#F0A93C]/30';
    if (codeHttp >= 500) colorClass = 'bg-[#E45858]/10 text-[#E45858] border-[#E45858]/30';

    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${colorClass} ${className}`}>
        {children}
      </span>
    );
  }

  const variants = {
    success: 'bg-[#00A896]/10 text-[#00A896] border-[#00A896]/30',
    error: 'bg-[#E45858]/10 text-[#E45858] border-[#E45858]/30',
    warning: 'bg-[#F0A93C]/10 text-[#F0A93C] border-[#F0A93C]/30',
    info: 'bg-[#028090]/10 text-[#028090] border-[#028090]/30',
    http: 'bg-[#1E2761]/10 text-[#1E2761] border-[#1E2761]/30'
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${variants[variant]} ${className}`}>
      {children}
    </span>
  );
};
