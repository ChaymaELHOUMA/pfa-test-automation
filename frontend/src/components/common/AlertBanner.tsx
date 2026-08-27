import React from 'react';
import { AlertTriangle, CheckCircle, Info, XCircle } from 'lucide-react';

interface AlertBannerProps {
  type?: 'error' | 'success' | 'warning' | 'info';
  title: string;
  message?: string | null;
  onClose?: () => void;
  className?: string;
}

export const AlertBanner: React.FC<AlertBannerProps> = ({ type = 'info', title, message, onClose, className = '' }) => {
  const styles = {
    error: {
      bg: 'bg-[#E45858]/10 border-[#E45858]/30 text-[#E45858]',
      icon: <XCircle className="w-5 h-5 text-[#E45858] shrink-0" />
    },
    success: {
      bg: 'bg-[#00A896]/10 border-[#00A896]/30 text-[#00A896]',
      icon: <CheckCircle className="w-5 h-5 text-[#00A896] shrink-0" />
    },
    warning: {
      bg: 'bg-[#F0A93C]/10 border-[#F0A93C]/30 text-[#F0A93C]',
      icon: <AlertTriangle className="w-5 h-5 text-[#F0A93C] shrink-0" />
    },
    info: {
      bg: 'bg-[#028090]/10 border-[#028090]/30 text-[#028090]',
      icon: <Info className="w-5 h-5 text-[#028090] shrink-0" />
    }
  };

  const style = styles[type];

  return (
    <div className={`p-4 rounded-xl border ${style.bg} flex items-start gap-3 transition-all ${className}`}>
      {style.icon}
      <div className="flex-1 text-sm">
        <p className="font-semibold">{title}</p>
        {message && <p className="mt-1 opacity-90 leading-relaxed">{message}</p>}
      </div>
      {onClose && (
        <button onClick={onClose} className="opacity-70 hover:opacity-100 transition-opacity">
          &times;
        </button>
      )}
    </div>
  );
};
