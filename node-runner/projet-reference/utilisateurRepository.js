// Repository d'utilisateurs en memoire pour le projet Express de reference

let donneesInitiales = [
  { id: 1, nom: 'Alice Dupont', email: 'alice@example.com', role: 'user', statut: 'actif', age: 28 },
  { id: 2, nom: 'Bob Martin', email: 'bob@example.com', role: 'admin', statut: 'inactif', age: 35 },
  { id: 3, nom: 'Charlie Durand', email: 'charlie@example.com', role: 'user', statut: 'suspendu', age: 42 }
];

let utilisateurs = JSON.parse(JSON.stringify(donneesInitiales));
let dernierId = 3;

function tous() {
  return utilisateurs;
}

function trouverParId(id) {
  const me = utilisateurs.find(u => u.id === Number(id));
  return me ? { ...me } : null;
}

function trouverParEmail(email) {
  const me = utilisateurs.find(u => u.email === email);
  return me ? { ...me } : null;
}

function ajouter(donnees) {
  dernierId += 1;
  const nouvelUtilisateur = {
    id: dernierId,
    nom: donnees.nom,
    email: donnees.email,
    role: donnees.role || 'user',
    statut: donnees.statut || 'actif',
    age: donnees.age || 25
  };
  utilisateurs.push(nouvelUtilisateur);
  return { ...nouvelUtilisateur };
}

function mettreAJour(id, donnees) {
  const index = utilisateurs.findIndex(u => u.id === Number(id));
  if (index === -1) return null;
  utilisateurs[index] = { ...utilisateurs[index], ...donnees, id: Number(id) };
  return { ...utilisateurs[index] };
}

function supprimer(id) {
  const index = utilisateurs.findIndex(u => u.id === Number(id));
  if (index === -1) return false;
  utilisateurs.splice(index, 1);
  return true;
}

function reinitialiserEtat() {
  utilisateurs = JSON.parse(JSON.stringify(donneesInitiales));
  dernierId = 3;
}

module.exports = {
  tous,
  trouverParId,
  trouverParEmail,
  ajouter,
  mettreAJour,
  supprimer,
  reinitialiserEtat
};
