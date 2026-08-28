import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { AlertBanner } from '../components/common/AlertBanner';
import { Badge } from '../components/common/Badge';
import {
  Upload,
  FileCode,
  CheckCircle2,
  Archive,
  Layers,
  ArrowRight,
  Sparkles,
  FileText,
  AlertCircle
} from 'lucide-react';

export const UploadProjectView: React.FC = () => {
  const { activeProject, setActiveProject, availableProjects, uploadProject, setActiveTab } = useApp();

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [pointEntree, setPointEntree] = useState<string>('app.js');
  const [repositoryFile, setRepositoryFile] = useState<string>('');
  const [serviceFile, setServiceFile] = useState<string>('');
  const [nomProjet, setNomProjet] = useState<string>('');
  const [uploading, setUploading] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setErrorMsg(null);
    }
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.name.endsWith('.zip')) {
        setSelectedFile(file);
        setErrorMsg(null);
      } else {
        setErrorMsg('Veuillez sélectionner un fichier au format .zip');
      }
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setErrorMsg('Veuillez sélectionner un fichier .zip à importer.');
      return;
    }
    if (!pointEntree.trim()) {
      setErrorMsg('Le point d\'entrée (ex: app.js) est obligatoire.');
      return;
    }

    setUploading(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    try {
      await uploadProject(selectedFile, pointEntree, repositoryFile, serviceFile, nomProjet);
      setSuccessMsg('Projet importé et analysé avec succès !');
      setTimeout(() => {
        setActiveTab('dashboard');
      }, 1500);
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.erreur || 'Une erreur est survenue lors de l\'upload du projet.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1E2761] text-white p-6 rounded-2xl shadow-xl border border-slate-700/50">
        <span className="inline-block px-3 py-1 bg-[#00A896]/20 text-[#00A896] text-xs font-semibold rounded-full mb-2">
          Configuration du Projet — Workspace Dynamique
        </span>
        <h2 className="font-serif text-2xl font-bold">Importer un Projet Express (.zip)</h2>
        <p className="text-sm text-slate-300 mt-1 max-w-3xl">
          Uploadez une archive <strong>.zip</strong> de votre application Node.js / Express. Spécifiez le point d'entrée principal (ex: <code>app.js</code>) pour adapter dynamiquement tout le pipeline de test.
        </p>
      </div>

      {/* Upload Form & Workspace Selection */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Col: Upload Form */}
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Drag & Drop Area */}
            <div>
              <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-2">
                Fichier d'Archive Projet (.zip)
              </label>
              <div
                onDrop={handleDrop}
                onDragOver={handleDragOver}
                className={`border-2 border-dashed rounded-2xl p-8 text-center transition-colors cursor-pointer ${
                  selectedFile
                    ? 'border-[#00A896] bg-teal-50/30'
                    : 'border-slate-300 hover:border-[#00A896] bg-slate-50'
                }`}
              >
                <input
                  type="file"
                  accept=".zip"
                  onChange={handleFileChange}
                  className="hidden"
                  id="zip-upload-input"
                />
                <label htmlFor="zip-upload-input" className="cursor-pointer flex flex-col items-center gap-2">
                  {selectedFile ? (
                    <>
                      <div className="p-3 bg-[#00A896]/10 rounded-full text-[#00A896]">
                        <Archive className="w-10 h-10" />
                      </div>
                      <span className="font-bold text-sm text-[#1E2761]">{selectedFile.name}</span>
                      <span className="text-xs text-slate-500 font-mono">
                        {(selectedFile.size / 1024).toFixed(1)} KB
                      </span>
                    </>
                  ) : (
                    <>
                      <div className="p-3 bg-slate-200/60 rounded-full text-slate-500">
                        <Upload className="w-8 h-8" />
                      </div>
                      <span className="font-semibold text-sm text-slate-700">
                        Glissez-déposez votre archive .zip ici, ou <span className="text-[#00A896] underline">parcourez</span>
                      </span>
                      <span className="text-xs text-slate-400">
                        Supporte les projets Node.js / Express avec scripts .js
                      </span>
                    </>
                  )}
                </label>
              </div>
            </div>

            {/* Point d'Entrée */}
            <div>
              <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-1.5">
                Point d'Entrée Principal <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <FileCode className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="text"
                  value={pointEntree}
                  onChange={(e) => setPointEntree(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 bg-[#F4F6FB] border border-slate-300 rounded-xl text-xs text-slate-800 font-mono focus:outline-none focus:border-[#00A896]"
                  placeholder="ex. app.js ou src/app.js"
                  required
                />
              </div>
              <span className="text-[10px] text-slate-500 mt-1 block">
                Fichier qui exporte l'application Express (<code>app</code>).
              </span>
            </div>

            {/* Nom du projet */}
            <div>
              <label className="block text-xs font-bold text-[#1E2761] uppercase tracking-wider mb-1.5">
                Nom d'Affichage du Projet (Optionnel)
              </label>
              <input
                type="text"
                value={nomProjet}
                onChange={(e) => setNomProjet(e.target.value)}
                className="w-full px-3 py-2 bg-[#F4F6FB] border border-slate-300 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-[#00A896]"
                placeholder="ex. Mon API E-Commerce"
              />
            </div>

            {/* Alerts */}
            {errorMsg && (
              <AlertBanner type="error" title="Erreur lors de l'importation" message={errorMsg} />
            )}
            {successMsg && (
              <AlertBanner type="success" title="Import réussi" message={successMsg} />
            )}

            {/* Submit Button */}
            <div className="flex justify-end pt-2">
              <button
                type="submit"
                disabled={uploading || !selectedFile}
                className="px-6 py-3 bg-[#00A896] hover:bg-[#009081] text-white text-xs font-semibold rounded-xl shadow-md transition-colors flex items-center gap-2 disabled:opacity-50"
              >
                <Sparkles className="w-4 h-4" />
                <span>{uploading ? 'Extraction & Configuration...' : 'Valider & Activer le Projet'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* Right Col: Active Project & Available Workspaces */}
        <div className="space-y-6">
          {/* Active Project Card */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-serif text-sm font-bold text-[#1E2761] flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#00A896]" />
                <span>Projet Actuel en Mémoire</span>
              </h3>
              <Badge variant="info">ACTIF</Badge>
            </div>

            <div className="p-4 bg-[#F4F6FB] rounded-xl border border-slate-200 space-y-2">
              <div className="font-bold text-sm text-[#1E2761]">{activeProject.nomProjet}</div>
              <div className="text-xs text-slate-500 font-mono">ID: {activeProject.idProjet}</div>
              <div className="text-xs text-slate-600">
                Point d'entrée : <strong className="font-mono text-[#00A896]">{activeProject.pointEntree}</strong>
              </div>
              {activeProject.repositoryFile ? (
                <div className="text-xs text-slate-600">
                  Repository : <strong className="font-mono text-indigo-600">{activeProject.repositoryFile}</strong>
                </div>
              ) : (
                <div className="text-xs text-slate-400 italic">Aucun repository configuré</div>
              )}
            </div>

            <div>
              <div className="text-xs font-bold text-[#1E2761] mb-2">
                Fichiers JavaScript Détectés ({activeProject.fichiers?.length || 0})
              </div>
              <div className="max-h-48 overflow-y-auto space-y-1.5 pr-1">
                {activeProject.fichiers?.map((f, i) => (
                  <div key={i} className="text-xs font-mono text-slate-700 bg-slate-50 px-2.5 py-1.5 rounded-lg border border-slate-100 flex items-center gap-2">
                    <FileCode className="w-3.5 h-3.5 text-[#00A896] shrink-0" />
                    <span className="truncate">{f}</span>
                  </div>
                ))}
              </div>
            </div>

            <button
              onClick={() => setActiveTab('ast')}
              className="w-full py-2.5 bg-[#1E2761] hover:bg-[#151B42] text-white text-xs font-semibold rounded-xl transition-colors flex items-center justify-center gap-2"
            >
              <span>Lancer l'Analyse AST sur ce projet</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>

          {/* Switch Available Projects */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
            <h3 className="font-serif text-sm font-bold text-[#1E2761] flex items-center gap-2">
              <Layers className="w-4 h-4 text-[#028090]" />
              <span>Changer de Projet ({availableProjects.length})</span>
            </h3>
            <div className="space-y-2 max-h-40 overflow-y-auto">
              {availableProjects.map((p) => (
                <div
                  key={p.idProjet}
                  onClick={() => setActiveProject(p)}
                  className={`p-3 rounded-xl border cursor-pointer transition-colors flex items-center justify-between text-xs ${
                    activeProject.idProjet === p.idProjet
                      ? 'bg-teal-50 border-[#00A896] text-[#00A896] font-bold'
                      : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <span className="truncate">{p.nomProjet}</span>
                  {activeProject.idProjet === p.idProjet && <CheckCircle2 className="w-4 h-4 shrink-0 text-[#00A896]" />}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
