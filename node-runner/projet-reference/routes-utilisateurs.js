const express = require('express');
const router = express.Router();
const repo = require('./utilisateurRepository');

// ---- Fonction utilitaire classique ----
function validerEmail(email) {
  if (!email || typeof email !== 'string') return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ---- Fonction utilitaire de validation de role ----
const ROLES_VALIDES = ['admin', 'user', 'guest'];
function validerRole(role) {
  return ROLES_VALIDES.includes(role);
}

// ---- Fonction utilitaire flechee ----
const formaterUtilisateur = (utilisateur) => {
  if (!utilisateur) return null;
  return {
    id: utilisateur.id,
    nom: utilisateur.nom,
    email: utilisateur.email,
    role: utilisateur.role,
    statut: utilisateur.statut
  };
};

// ---- Fonction utilitaire de calcul de statistiques ----
function calculerStatistiques(utilisateurs) {
  if (!utilisateurs || utilisateurs.length === 0) {
    return { total: 0, actifs: 0, inactifs: 0, ageMoyen: 0 };
  }
  const total = utilisateurs.length;
  const actifs = utilisateurs.filter(u => u.statut === 'actif').length;
  const inactifs = utilisateurs.filter(u => u.statut === 'inactif').length;
  const sommeAge = utilisateurs.reduce((acc, u) => acc + (u.age || 0), 0);
  return {
    total,
    actifs,
    inactifs,
    ageMoyen: Math.round((sommeAge / total) * 10) / 10
  };
}

// ---- GET /users : liste tous les utilisateurs avec filtres et pagination ----
router.get('/users', (req, res) => {
  if (req.query.limite) {
    const limite = Number(req.query.limite);
    if (isNaN(limite)) {
      return res.status(400).json({ error: 'Limite invalide' });
    }
    if (limite > 50) {
      return res.status(400).json({ error: 'Limite trop elevee (max 50)' });
    }
  }

  let liste = repo.tous();

  if (req.query.role) {
    if (!validerRole(req.query.role)) {
      return res.status(400).json({ error: 'Role invalide' });
    }
    liste = liste.filter(u => u.role === req.query.role);
  }

  if (req.query.tri === 'asc') {
    liste = [...liste].sort((a, b) => a.nom.localeCompare(b.nom));
  } else if (req.query.tri === 'desc') {
    liste = [...liste].sort((a, b) => b.nom.localeCompare(a.nom));
  }

  if (req.query.limite) {
    liste = liste.slice(0, Number(req.query.limite));
  }

  res.json({ utilisateurs: liste });
});

// ---- GET /users/statistiques/summary : resumee des statistiques ----
router.get('/users/statistiques/summary', (req, res) => {
  const liste = repo.tous();
  const stats = calculerStatistiques(liste);
  res.json(stats);
});

// ---- GET /users/:id : recupere un utilisateur, avec cas d'erreur ----
router.get('/users/:id', (req, res) => {
  const idNum = Number(req.params.id);
  if (isNaN(idNum)) {
    return res.status(400).json({ error: 'ID invalide' });
  }

  const utilisateur = repo.trouverParId(idNum);
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }
  res.json(formaterUtilisateur(utilisateur));
});

// ---- POST /users : cree un utilisateur, avec validation et try/catch ----
router.post('/users', (req, res) => {
  if (!req.body || !req.body.email || !validerEmail(req.body.email)) {
    return res.status(400).json({ error: 'Email invalide' });
  }

  if (req.body.role && !validerRole(req.body.role)) {
    return res.status(400).json({ error: 'Role invalide' });
  }

  const existant = repo.trouverParEmail(req.body.email);
  if (existant) {
    return res.status(409).json({ error: 'Cet email existe deja' });
  }

  try {
    const nouvelUtilisateur = repo.ajouter({
      nom: req.body.nom || 'Utilisateur Anonyme',
      email: req.body.email,
      role: req.body.role || 'user',
      statut: req.body.statut || 'actif',
      age: req.body.age || 25
    });
    res.status(201).json(nouvelUtilisateur);
  } catch (err) {
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

// ---- PUT /users/:id : met a jour, avec switch sur un statut ----
router.put('/users/:id', (req, res) => {
  const idNum = Number(req.params.id);
  const utilisateur = repo.trouverParId(idNum);

  switch (req.body ? req.body.statut : null) {
    case 'actif':
      if (utilisateur) repo.mettreAJour(idNum, { statut: 'actif' });
      res.json({ statut: 'actif' });
      break;
    case 'inactif':
      if (utilisateur) repo.mettreAJour(idNum, { statut: 'inactif' });
      res.json({ statut: 'inactif' });
      break;
    case 'suspendu':
      if (utilisateur) repo.mettreAJour(idNum, { statut: 'suspendu' });
      res.json({ statut: 'suspendu' });
      break;
    default:
      res.status(400).json({ error: 'Statut inconnu' });
  }
});

// ---- PATCH /users/:id : mise a jour partielle securisee par header ----
router.patch('/users/:id', (req, res) => {
  const adminRoleHeader = req.headers['x-admin-role'];
  if (!adminRoleHeader || adminRoleHeader !== 'admin') {
    return res.status(403).json({ error: 'Acces non autorise' });
  }

  const idNum = Number(req.params.id);
  if (isNaN(idNum)) {
    return res.status(400).json({ error: 'ID invalide' });
  }

  const utilisateur = repo.trouverParId(idNum);
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }

  if (!req.body || Object.keys(req.body).length === 0) {
    return res.status(422).json({ error: 'Donnees de mise a jour absentes' });
  }

  if (req.body.email && !validerEmail(req.body.email)) {
    return res.status(400).json({ error: 'Email invalide' });
  }

  const maj = repo.mettreAJour(idNum, req.body);
  res.json(formaterUtilisateur(maj));
});

// ---- DELETE /users/:id : supprime, avec throw explicite ----
router.delete('/users/:id', (req, res) => {
  if (!req.params.id || req.params.id === 'undefined') {
    throw new Error('ID manquant');
  }

  const idNum = Number(req.params.id);
  if (isNaN(idNum)) {
    return res.status(400).json({ error: 'ID invalide' });
  }

  const supprime = repo.supprimer(idNum);
  if (!supprime) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }

  res.status(204).send();
});

// ---- POST /users/:id/reset-password : reinitialisation de mot de passe ----
router.post('/users/:id/reset-password', (req, res) => {
  const idNum = Number(req.params.id);
  if (isNaN(idNum)) {
    return res.status(400).json({ error: 'ID invalide' });
  }

  const utilisateur = repo.trouverParId(idNum);
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }

  if (utilisateur.statut === 'inactif' || utilisateur.statut === 'suspendu') {
    return res.status(403).json({ error: 'Compte inactif ou suspendu' });
  }

  const nouveauMotDePasse = req.body ? req.body.nouveauMotDePasse : null;
  if (!nouveauMotDePasse || nouveauMotDePasse.length < 8) {
    return res.status(400).json({ error: 'Le mot de passe doit contenir au moins 8 caracteres' });
  }

  res.json({ message: 'Mot de passe reinitialise avec succes', id: idNum });
});

module.exports = router;
