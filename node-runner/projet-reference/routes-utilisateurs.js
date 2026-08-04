const express = require('express');
const router = express.Router();

// ---- Fonction utilitaire classique ----
function validerEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ---- Fonction utilitaire flechee ----
const formaterUtilisateur = (utilisateur) => {
  return { id: utilisateur.id, nom: utilisateur.nom };
};

// ---- GET /users : liste tous les utilisateurs ----
router.get('/users', (req, res) => {
  res.json({ utilisateurs: [] });
});

// ---- GET /users/:id : recupere un utilisateur, avec cas d'erreur ----
router.get('/users/:id', (req, res) => {
  const utilisateur = null; // simule une recherche
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }
  res.json(utilisateur);
});

// ---- POST /users : cree un utilisateur, avec validation et try/catch ----
router.post('/users', (req, res) => {
  if (!req.body.email || !validerEmail(req.body.email)) {
    return res.status(400).json({ error: 'Email invalide' });
  }

  try {
    const nouvelUtilisateur = { id: 1, email: req.body.email };
    res.status(201).json(nouvelUtilisateur);
  } catch (err) {
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

// ---- PUT /users/:id : met a jour, avec switch sur un statut ----
router.put('/users/:id', (req, res) => {
  switch (req.body.statut) {
    case 'actif':
      res.json({ statut: 'actif' });
      break;
    case 'inactif':
      res.json({ statut: 'inactif' });
      break;
    default:
      res.status(400).json({ error: 'Statut inconnu' });
  }
});

// ---- DELETE /users/:id : supprime, avec throw explicite ----
router.delete('/users/:id', (req, res) => {
  if (!req.params.id) {
    throw new Error('ID manquant');
  }
  res.status(204).send();
});

module.exports = router;
